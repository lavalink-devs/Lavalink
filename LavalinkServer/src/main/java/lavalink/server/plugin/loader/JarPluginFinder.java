package lavalink.server.plugin.loader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

class JarPluginFinder extends PluginFinder {
    private final JarFile file;
    private final String urlBase;

    JarPluginFinder(ClassLoader loader, File file) throws IOException {
        super(loader);
        this.file = new JarFile(file);
        this.urlBase = "jar:file://" + file.toURI().getPath() + "!";
    }

    @Override
    URL createURL(String path) {
        try {
            return new URL(urlBase + (path.startsWith("/") ? path : "/" + path));
        } catch(MalformedURLException e) {
            return null;
        }
    }

    @Override
    Iterator<String> files() {
        return new JarEntryPathIterator(file.entries().asIterator());
    }

    @Override
    InputStream open(String path) throws IOException {
        return file.getInputStream(file.getEntry(path));
    }

    @Override
    public void close() throws IOException {
        file.close();
    }

    private static class JarEntryPathIterator implements Iterator<String> {
        private final Iterator<JarEntry> iterator;

        private JarEntryPathIterator(Iterator<JarEntry> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public String next() {
            return iterator.next().getName();
        }
    }
}
