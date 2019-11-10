package com.windchopper.fs;

import org.apache.commons.lang3.ObjectUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.Arrays;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.function.Function.identity;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;

public class SftpPath implements Path, SftpConstants, SftpSharedRoutines {

    private final SftpFileSystem fileSystem;
    private final SftpSessionIdentity connectionIdentity;
    private final boolean absolute;
    private final String[] pathElements;

    public SftpPath(SftpFileSystem fileSystem, SftpSessionIdentity connectionIdentity, boolean absolute, String... pathElements) {
        this.fileSystem = fileSystem;
        this.connectionIdentity = connectionIdentity;
        this.absolute = absolute;
        this.pathElements = pathElements;
    }

    public SftpPath(SftpFileSystem fileSystem, SftpSessionIdentity connectionIdentity, String... pathElements) {
        this(
            fileSystem,
            connectionIdentity,
            String.join(SEPARATOR, pathElements));
    }

    public SftpPath(SftpFileSystem fileSystem, SftpSessionIdentity connectionIdentity, String pathString) {
        this(
            fileSystem,
            connectionIdentity,
            pathString.startsWith(SEPARATOR),
            stream(pathString.split(SEPARATOR))
                .filter(not(SEPARATOR::equals))
                .filter(not(""::equals))
                .toArray(String[]::new));
    }

    @Override public SftpFileSystem getFileSystem() {
        return fileSystem;
    }

    @Override public boolean isAbsolute() {
        return absolute;
    }

    @Override public Path getRoot() {
        return absolute && pathElements.length > 0 ? new SftpPath(fileSystem, connectionIdentity, SEPARATOR) : null;
    }

    @Override public Path getFileName() {
        return pathElements.length > 0 ? new SftpPath(fileSystem, connectionIdentity, pathElements[pathElements.length - 1]) : null;
    }

    @Override public Path getParent() {
        if (pathElements.length > 1) {
            return new SftpPath(fileSystem, connectionIdentity, absolute, Arrays.copyOfRange(pathElements, 0, pathElements.length - 1));
        }

        if (pathElements.length > 0 && absolute) {
            return new SftpPath(fileSystem, connectionIdentity, SEPARATOR);
        }

        return null;
    }

    @Override public int getNameCount() {
        return pathElements.length;
    }

    @Override public Path getName(int index) {
        if (index < pathElements.length) {
            return new SftpPath(fileSystem, connectionIdentity, pathElements[index]);
        }

        throw new IllegalArgumentException(
            String.format("Index %d out of bounds (%d)", index, pathElements.length));
    }

    @Override public Path subpath(int fromIndex, int toIndex) {
        if (fromIndex >= 0) {
            if (toIndex <= pathElements.length) {
                return new SftpPath(fileSystem, connectionIdentity, Arrays.copyOfRange(pathElements, fromIndex, toIndex));
            }

            throw new IllegalArgumentException(
                String.format("End index %d out of bounds (%d)", toIndex, pathElements.length));
        }

        throw new IllegalArgumentException(
            String.format("Begin index %d out of bounds", fromIndex));
    }

    @Override public boolean startsWith(Path path) {
        return checkPathAndApply(path, domesticPath -> {
            int j = 0, jcount = domesticPath.pathElements.length;

            for (int i = 0, icount = pathElements.length; i < icount && j < jcount; i++, j++) {
                if (ObjectUtils.notEqual(pathElements[i], domesticPath.pathElements[j])) {
                    return false;
                }
            }

            return j == jcount;
        });
    }

    @Override public boolean endsWith(Path path) {
        return checkPathAndApply(path, domesticPath -> {
            int j = domesticPath.pathElements.length;

            for (int i = pathElements.length; --i >= 0 && --j >= 0; ) {
                if (ObjectUtils.notEqual(pathElements[i], domesticPath.pathElements[j])) {
                    return false;
                }
            }

            return j == -1;
        });
    }

    @Override public Path normalize() {
        return new SftpPath(fileSystem, connectionIdentity, toString());
    }

    @Override public Path resolve(Path path) {
        return checkPathAndApply(path, domesticPath -> new SftpPath(
            fileSystem,
            connectionIdentity,
            Stream.of(pathElements, domesticPath.pathElements)
                .map(Arrays::stream)
                .flatMap(identity())
                .toArray(String[]::new)));
    }

    @Override public Path relativize(Path path) {
        return checkPathAndApply(path, domesticPath -> {
            throw new UnsupportedOperationException();
        });
    }

    @Override public URI toUri() {
        return connectionIdentity.composeUri(String.join(SEPARATOR, pathElements));
    }

    @Override public Path toAbsolutePath() {
        return absolute ? this : new SftpPath(fileSystem, connectionIdentity, toString(SEPARATOR));
    }

    @Override public Path toRealPath(LinkOption... linkOptions) throws IOException {
        return new SftpPath(fileSystem, connectionIdentity, fileSystem.realPath(toString()));
    }

    @Override public WatchKey register(WatchService watchService, WatchEvent.Kind<?>[] kinds, WatchEvent.Modifier... modifiers) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override public int compareTo(Path path) {
        return checkPathAndApply(path, domesticPath -> Arrays.compare(pathElements, domesticPath.pathElements));
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
