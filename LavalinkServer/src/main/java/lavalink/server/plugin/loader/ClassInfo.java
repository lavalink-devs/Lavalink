package lavalink.server.plugin.loader;

final class ClassInfo {
    /*
    null if either
        no parent (java.lang.Object)
        class implements LavalinkPlugin (no need to look up other parents)
     */
    private final ClassInfo parent;
    private final boolean implementsInterface;
    private final boolean hasAnnotation;

    ClassInfo(ClassInfo parent, boolean implementsInterface, boolean hasAnnotation) {
        this.parent = parent;
        this.implementsInterface = implementsInterface;
        this.hasAnnotation = hasAnnotation;
    }

    private boolean implementsInterface() {
        return implementsInterface || (parent != null && parent.implementsInterface());
    }

    boolean isPlugin() {
        return hasAnnotation && implementsInterface();
    }
}
