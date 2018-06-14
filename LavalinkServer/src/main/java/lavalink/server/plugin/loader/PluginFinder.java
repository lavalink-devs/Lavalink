package lavalink.server.plugin.loader;

import lavalink.server.plugin.LavalinkPlugin;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

abstract class PluginFinder extends ClassLoader implements Closeable {
    //there won't be any concurrent access so we can cache this
    private final byte[] buffer = new byte[4096];
    private final Map<String, ClassInfo> cachedData = new HashMap<>();

    PluginFinder(ClassLoader loader) {
        super(loader);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            return load(name.replace('.', '/') + ".class");
        } catch(IOException e) {
            throw new ClassNotFoundException(name, e);
        }
    }

    abstract Iterator<String> files() throws IOException;
    abstract InputStream open(String path) throws IOException;

    @SuppressWarnings("unchecked")
    List<Class<? extends LavalinkPlugin>> find() throws IOException {
        Iterator<String> it = files();
        List<Class<? extends LavalinkPlugin>> list = new ArrayList<>();
        while(it.hasNext()) {
            String s = it.next();
            if(s.endsWith(".class")) {
                ClassInfo info = openAndAnalyze(s);
                if(info.isPlugin()) {
                    list.add((Class<? extends LavalinkPlugin>)load(s));
                }
            }
        }
        return list;
    }

    private Class<?> load(String path) throws IOException {
        try(InputStream in = open(path)) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int r;
            while((r = in.read(buffer)) != -1) {
                baos.write(buffer, 0, r);
            }
            byte[] bytes = baos.toByteArray();
            return defineClass(null, bytes, 0, bytes.length);
        }
    }

    private ClassInfo openAndAnalyze(String path) throws IOException {
        ClassInfo info = cachedData.get(path);
        if(info != null) {
            return info;
        }
        try(InputStream in = open(path)) {
            info = analyze(in);
        }
        cachedData.put(path, info);
        return info;
    }

    private ClassInfo analyze(String name) throws IOException {
        try {
            Class<?> c = Class.forName(name, false, getParent());
            Class<?> superClass = c.getSuperclass();
            boolean found = false;
            for(Class<?> itf : c.getInterfaces()) {
                if(itf == LavalinkPlugin.class) {
                    found = true;
                    break;
                }
            }
            if(found) {
                return new ClassInfo(null, true, c.getAnnotation(LavalinkPlugin.AutoRegister.class) != null);
            }
            boolean annotation = false;
            if(c.getAnnotation(LavalinkPlugin.AutoRegister.class) != null) {
                PluginLoader.logBadPlugin(c.getName());
                annotation = true;
            }
            return new ClassInfo(superClass == null ? null : analyze(superClass.getName()), false, annotation);
        } catch(ClassNotFoundException ignored) {}
        return openAndAnalyze(name.replace('.', '/') + ".class");
    }

    private ClassInfo analyze(InputStream in) throws IOException {
        ClassReader cr = new ClassReader(in);
        FinderVisitor visitor = new FinderVisitor(Opcodes.ASM6);
        cr.accept(visitor, 0);
        return visitor.result(this);
    }

    private static class FinderVisitor extends ClassVisitor {
        private static final String PLUGIN_INTERFACE_DESC = LavalinkPlugin.class.getName().replace('.', '/');
        private static final String PLUGIN_ANNOTATION_DESC = "L" + LavalinkPlugin.AutoRegister.class.getName().replace('.', '/') + ";";
        private static final String OBJECT_DESC = "java/lang/Object";

        private String name;
        private String superName;
        private boolean hasInterface;
        private boolean hasAnnotation;

        FinderVisitor(int api) {
            super(api);
        }

        ClassInfo result(PluginFinder finder) throws IOException {
            ClassInfo parent;
            if(OBJECT_DESC.equals(superName) || hasInterface) {
                parent = null;
            } else {
                parent = finder.analyze(superName.replace('/', '.'));
            }
            if(hasAnnotation && !hasInterface) {
                PluginLoader.logBadPlugin(name.replace('/', '.'));
            }
            return new ClassInfo(parent, hasInterface, hasAnnotation);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            this.name = name;
            this.superName = superName;
            for(String s : interfaces) {
                if(PLUGIN_INTERFACE_DESC.equals(s)) {
                    hasInterface = true;
                    break;
                }
            }
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            if(PLUGIN_ANNOTATION_DESC.equals(descriptor)) {
                hasAnnotation = true;
            }
            return super.visitAnnotation(descriptor, visible);
        }
    }
}
