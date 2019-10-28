package com.windchopper.fs.sftp;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.function.Function.identity;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;

public class SftpPath implements Path, SftpConstants {

    private final SftpFileSystem fileSystem;

    private final String[] pathElements;
    private final boolean absolute;

    SftpPath(SftpFileSystem fileSystem, String... pathElements) {
        this.fileSystem = fileSystem;
        String joinedElements = String.join(SEPARATOR, pathElements);
        absolute = joinedElements.startsWith(SEPARATOR);
        this.pathElements = stream(joinedElements.split(SEPARATOR))
            .filter(not(SEPARATOR::equals))
            .toArray(String[]::new);
    }

    @Override public FileSystem getFileSystem() {
        return fileSystem;
    }

    @Override public boolean isAbsolute() {
        return absolute;
    }

    @Override public Path getRoot() {
        return absolute && pathElements.length > 0 ? new SftpPath(fileSystem, SEPARATOR) : null;
    }

    @Override public Path getFileName() {
        return pathElements.length > 0 ? new SftpPath(fileSystem, pathElements[pathElements.length - 1]) : null;
    }

    @Override public Path getParent() {
        if (pathElements.length > 1) {
            return new SftpPath(fileSystem, Arrays.copyOfRange(pathElements, 0, pathElements.length));
        }

        if (pathElements.length > 0 && absolute) {
            return new SftpPath(fileSystem, SEPARATOR);
        }

        return null;
    }

    @Override public int getNameCount() {
        return pathElements.length;
    }

    @Override public Path getName(int index) {
        if (index < pathElements.length) {
            return new SftpPath(fileSystem, pathElements[index]);
        }

        throw new IllegalArgumentException(
            String.format("Index %d out of bounds (%d)", index, pathElements.length));
    }

    @Override public Path subpath(int fromIndex, int toIndex) {
        if (fromIndex >= 0) {
            if (toIndex <= pathElements.length) {
                return new SftpPath(fileSystem, Arrays.copyOfRange(pathElements, fromIndex, toIndex));
            }

            throw new IllegalArgumentException(
                String.format("End index %d out of bounds (%d)", toIndex, pathElements.length));
        }

        throw new IllegalArgumentException(
            String.format("Begin index %d out of bounds", fromIndex));
    }

    @Override public boolean startsWith(Path path) {
        throw new UnsupportedOperationException();
    }

    @Override public boolean endsWith(Path path) {
        throw new UnsupportedOperationException();
    }

    @Override public Path normalize() {
        return new SftpPath(fileSystem, toString());
    }

    @Override public Path resolve(Path path) {
        if (path instanceof SftpPath) {
            SftpPath sftpPath = (SftpPath) path;
            return new SftpPath(fileSystem, Stream.of(pathElements, sftpPath.pathElements)
                .map(Arrays::stream)
                .flatMap(identity())
                .toArray(String[]::new));
        }

        throw new ProviderMismatchException(
            String.format("Path of type %s is not belonging to used provider", path.getClass().getCanonicalName()));
    }

    @Override public Path relativize(Path path) {
        throw new UnsupportedOperationException();
    }

    @Override public URI toUri() {
        return fileSystem.composeUri(String.join(SEPARATOR, pathElements));
    }

    @Override public Path toAbsolutePath() {
        return absolute ? this : new SftpPath(fileSystem, toString(SEPARATOR));
    }

    @Override public Path toRealPath(LinkOption... linkOptions) throws IOException {
        return new SftpPath(fileSystem, fileSystem.realPath(toString()));
    }

    @Override public WatchKey register(WatchService watchService, WatchEvent.Kind<?>[] kinds, WatchEvent.Modifier... modifiers) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override public int compareTo(Path path) {
        if (Objects.requireNonNull(path, "path") instanceof SftpPath) {
            SftpPath sftpPath = (SftpPath) path;
            return Arrays.compare(pathElements, sftpPath.pathElements);
        }

        throw new ProviderMismatchException(
            String.format("Path of type %s is not belonging to used provider", path.getClass().getCanonicalName()));
    }

    String toString(String prefix) {
        return stream(pathElements)
            .collect(joining(SEPARATOR, prefix, ""));
    }

    @Override public String toString() {
        String prefix = "";

        if (absolute) {
            prefix = SEPARATOR;
        }

        return toString(prefix);
    }

}
