package com.windchopper.fs.sftp;

import com.jcraft.jsch.JSch;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Collections.emptyMap;

public class SftpFileSystemProvider extends FileSystemProvider {

    public static final String HOST = SftpFileSystemProvider.class.getCanonicalName() + ".host";
    public static final String PORT = SftpFileSystemProvider.class.getCanonicalName() + ".port";
    public static final String USERNAME = SftpFileSystemProvider.class.getCanonicalName() + ".username";
    public static final String PASSWORD = SftpFileSystemProvider.class.getCanonicalName() + ".password";

    public static final String THREAD_FACTORY = SftpFileSystemProvider.class.getCanonicalName() + ".threadFactory";
    public static final String THREADING_DISABLED = SftpFileSystemProvider.class.getCanonicalName() + ".threadingDisabled";

    public static final String SCHEME = "sftp";
    public static final int STANDARD_PORT = 22;

    /*
     *
     */

    private static final Logger logger = Logger.getLogger(SftpFileSystemProvider.class.getCanonicalName());

    final Map<SftpPeerIdentity, SftpFileSystem> fileSystems = new ConcurrentHashMap<>();
    final JSch jsch = new JSch();

    @Override public String getScheme() {
        return SCHEME;
    }

    @Override public FileSystem newFileSystem(URI uri, Map<String, ?> environment) throws IOException {
        SftpFileSystemEnvironment parsedEnvironment = new SftpFileSystemEnvironment(uri, environment);

        if (fileSystems.containsKey(parsedEnvironment.peerIdentity)) {
            throw new FileSystemAlreadyExistsException(
                String.format("File system already connected to %s", parsedEnvironment.peerIdentity));
        } else {
            SftpFileSystem fileSystem = new SftpFileSystem(this, parsedEnvironment);

            fileSystems.put(parsedEnvironment.peerIdentity, fileSystem);

            return fileSystem;
        }
    }

    @Override public FileSystem getFileSystem(URI uri) {
        SftpFileSystemEnvironment parsedEnvironment = new SftpFileSystemEnvironment(uri, emptyMap());

        if (fileSystems.containsKey(parsedEnvironment.peerIdentity)) {
            return fileSystems.get(parsedEnvironment.peerIdentity);
        } else {
            throw new FileSystemNotFoundException(
                String.format("File system not connected to %s", parsedEnvironment.peerIdentity));
        }
    }

    void retire(SftpPeerIdentity peerIdentity, SftpFileSystem fileSystem) {
        fileSystems.remove(peerIdentity, fileSystem);
    }

    @Override public Path getPath(URI uri) {
        return getFileSystem(uri).getPath(uri.getPath());
    }

    @Override public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> openOptionSet, FileAttribute<?>... fileAttributes) throws IOException {
        return null;
    }

    @Override public DirectoryStream<Path> newDirectoryStream(Path path, Filter<? super Path> filter) throws IOException {
        return null;
    }

    @Override public void createDirectory(Path path, FileAttribute<?>... fileAttributes) throws IOException {

    }

    @Override public void delete(Path path) throws IOException {

    }

    @Override public void copy(Path sourcePath, Path targetPath, CopyOption... copyOptions) throws IOException {

    }

    @Override public void move(Path sourcePath, Path targetPath, CopyOption... copyOptions) throws IOException {

    }

    @Override public boolean isSameFile(Path path1st, Path path2nd) throws IOException {
        return false;
    }

    @Override public boolean isHidden(Path path) throws IOException {
        return false;
    }

    @Override public FileStore getFileStore(Path path) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override public void checkAccess(Path path, AccessMode... accessModes) throws IOException {

    }

    @Override public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> viewType, LinkOption... linkOptions) {
        return null;
    }

    @Override public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> attributesType, LinkOption... linkOptions) throws IOException {
        return null;
    }

    @Override public Map<String, Object> readAttributes(Path path, String s, LinkOption... linkOptions) throws IOException {
        return null;
    }

    @Override public void setAttribute(Path path, String s, Object o, LinkOption... linkOptions) throws IOException {

    }

}
