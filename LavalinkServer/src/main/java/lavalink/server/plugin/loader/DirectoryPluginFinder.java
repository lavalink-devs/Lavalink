package lavalink.server.plugin.loader;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

class DirectoryPluginFinder extends PluginFinder {
    private final File directory;

    DirectoryPluginFinder(ClassLoader loader, File directory) {
        super(loader);
        this.directory = directory;
    }

    @Override
    Iterator<String> files() {
        return new FileIterator(directory);
    }

    @Override
    InputStream open(String path) throws IOException {
        return new FileInputStream(new File(directory, path));
    }

    @Override
    public void close() {
        //nothing to do here
    }

    private static class FileIterator implements Iterator<String> {
        private final Iterator<File> files;
        private final int substringLength;

        private FileIterator(File root) {
            this.files = FileUtils.iterateFiles(root, new String[]{"class"}, true);
            this.substringLength = root.getAbsolutePath().length() + File.separator.length();
        }

        @Override
        public boolean hasNext() {
            return files.hasNext();
        }

        @Override
        public String next() {
            return files.next().getAbsolutePath().substring(substringLength);
        }
    }
}
