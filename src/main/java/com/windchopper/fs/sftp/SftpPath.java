package com.windchopper.fs.sftp;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.Arrays;

import static java.util.Arrays.stream;

public class SftpPath implements Path {

    public static final String SEPARATOR = "/";

    /*
     *
     */

    private final SftpFileSystem fileSystem;
    private final String[] elements;

    public SftpPath(SftpFileSystem fileSystem, String... elements) {
        this.fileSystem = fileSystem;
        this.elements = stream(elements)
            .map(element -> element.split(SEPARATOR))
            .flatMap(Arrays::stream)
                .filter(element -> !element.equals(SEPARATOR))
                .toArray(String[]::new);
    }

    @Override public FileSystem getFileSystem() {
        return fileSystem;
    }

    @Override public boolean isAbsolute() {
        return false;
    }

    @Override public Path getRoot() {
        return null;
    }

    @Override public Path getFileName() {
        return new SftpPath(fileSystem, elements[elements.length - 1]);
    }

    @Override public Path getParent() {
        return new SftpPath(fileSystem, Arrays.copyOfRange(elements, 0, elements.length - 1));
    }

    @Override public int getNameCount() {
        return elements.length;
    }

    @Override public Path getName(int index) {
        return new SftpPath(fileSystem, elements[index]);
    }

    @Override public Path subpath(int fromIndex, int toIndex) {
        return new SftpPath(fileSystem, Arrays.copyOfRange(elements, fromIndex, toIndex));
    }

    @Override public boolean startsWith(Path path) {
        return false;
    }

    @Override public boolean endsWith(Path path) {
        return false;
    }

    @Override public Path normalize() {
        return null;
    }

    @Override public Path resolve(Path path) {
        return null;
    }

    @Override public Path relativize(Path path) {
        return null;
    }

    @Override public URI toUri() {
        return null;
    }

    @Override public Path toAbsolutePath() {
        return null;
    }

    @Override public Path toRealPath(LinkOption... linkOptions) throws IOException {
        return null;
    }

    @Override public WatchKey register(WatchService watchService, WatchEvent.Kind<?>[] kinds, WatchEvent.Modifier... modifiers) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override public int compareTo(Path path) {
        return 0;
    }

    @Override public String toString() {
        return SEPARATOR + String.join(SEPARATOR, elements);
    }

}
