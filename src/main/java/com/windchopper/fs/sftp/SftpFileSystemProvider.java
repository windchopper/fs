package com.windchopper.fs.sftp;

import com.jcraft.jsch.JSch;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.emptyMap;

public class SftpFileSystemProvider extends FileSystemProvider implements SftpConstants, SftpRoutines {

    final Map<SftpSessionIdentity, SftpFileSystem> connectedFileSystems = new ConcurrentHashMap<>();
    final JSch jsch = new JSch();

    @Override public String getScheme() {
        return SftpConfiguration.SCHEME;
    }

    @Override public FileSystem newFileSystem(URI uri, Map<String, ?> environment) throws IOException {
        SftpConfiguration configuration = new SftpConfiguration(uri, environment);

        if (connectedFileSystems.containsKey(configuration.sessionIdentity)) {
            throw new FileSystemAlreadyExistsException(
                String.format("File system already connected to %s", configuration.sessionIdentity));
        }

        SftpFileSystem fileSystem = new SftpFileSystem(this, configuration);
        connectedFileSystems.put(configuration.sessionIdentity, fileSystem);
        return fileSystem;
    }

    @Override public FileSystem getFileSystem(URI uri) {
        SftpConfiguration configuration = new SftpConfiguration(uri, emptyMap());

        if (connectedFileSystems.containsKey(configuration.sessionIdentity)) {
            return connectedFileSystems.get(configuration.sessionIdentity);
        }

        throw new FileSystemNotFoundException(
            String.format("File system not connected to %s", configuration.sessionIdentity));
    }

    void retire(SftpSessionIdentity connectionIdentity, SftpFileSystem fileSystem) {
        connectedFileSystems.remove(connectionIdentity, fileSystem);
    }

    @Override public Path getPath(URI uri) {
        return getFileSystem(uri).getPath(uri.getPath());
    }

    @Override public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> openOptionSet, FileAttribute<?>... fileAttributes) throws IOException {
        return checkPathAndApply(path, domesticPath -> {
            throw new UnsupportedOperationException();
        });
    }

    @Override public DirectoryStream<Path> newDirectoryStream(Path path, Filter<? super Path> filter) throws IOException {
        return checkPathAndApply(path, domesticPath -> domesticPath.getFileSystem().newDirectoryStream(domesticPath, filter));
    }

    @Override public void createDirectory(Path path, FileAttribute<?>... fileAttributes) throws IOException {
        checkPathAndAccept(path, domesticPath -> {
            throw new UnsupportedOperationException();
        });
    }

    @Override public void delete(Path path) throws IOException {
        checkPathAndAccept(path, domesticPath -> {
            throw new UnsupportedOperationException();
        });
    }

    void delete(SftpPath path) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override public void copy(Path sourcePath, Path targetPath, CopyOption... copyOptions) throws IOException {
        checkPathAndAccept(targetPath, domesticTargetPath -> {
            throw new UnsupportedOperationException();
        });
    }

    @Override public void move(Path sourcePath, Path targetPath, CopyOption... copyOptions) throws IOException {
        checkPathAndAccept(targetPath, domesticTargetPath -> {
            throw new UnsupportedOperationException();
        });
    }

    @Override public boolean isSameFile(Path path1st, Path path2nd) throws IOException {
        return checkPathAndApply(path1st, path2nd, (domesticPath1st, domesticPath2nd) -> {
            throw new UnsupportedOperationException();
        });
    }

    @Override public boolean isHidden(Path path) throws IOException {
        return checkPathAndApply(path, domesticPath -> {
            throw new UnsupportedOperationException();
        });
    }

    @Override public FileStore getFileStore(Path path) throws IOException {
        return checkPathAndApply(path, domesticPath -> {
            throw new UnsupportedOperationException();
        });
    }

    @Override public void checkAccess(Path path, AccessMode... accessModes) throws IOException {
        checkPathAndAccept(path, domesticPath -> {
            throw new UnsupportedOperationException();
        });
    }

    @Override public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> viewType, LinkOption... linkOptions) {
        return checkPathAndApply(path, domesticPath -> {
            throw new UnsupportedOperationException();
        });
    }

    @SuppressWarnings("unchecked") @Override public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> attributesType, LinkOption... linkOptions) throws IOException {
        return checkPathAndApply(path, domesticPath -> {
            if (attributesType == BasicFileAttributes.class || attributesType == SftpFileAttributes.class) {
                return (A) domesticPath.getFileSystem().view(path.toString()).toFileAttributes();
            }

            throw new UnsupportedOperationException(
                String.format("Attributes of type %s not supported", attributesType.getCanonicalName()));
        });
    }

    @Override public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... linkOptions) throws IOException {
        return checkPathAndApply(path, domesticPath -> {
            throw new UnsupportedOperationException();
        });
    }

    @Override public void setAttribute(Path path, String s, Object value, LinkOption... linkOptions) throws IOException {
        checkPathAndAccept(path, domesticPath -> {
            throw new UnsupportedOperationException();
        });
    }

}
