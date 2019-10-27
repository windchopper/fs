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
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Collections.emptyMap;

public class SftpFileSystemProvider extends FileSystemProvider {

    public static final String HOST = SftpFileSystemProvider.class.getCanonicalName() + ".host";
    public static final String PORT = SftpFileSystemProvider.class.getCanonicalName() + ".port";
    public static final String USERNAME = SftpFileSystemProvider.class.getCanonicalName() + ".username";
    public static final String PASSWORD = SftpFileSystemProvider.class.getCanonicalName() + ".password";

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

    private Pair<SftpPeerIdentity, String> determineConnectionParameters(URI uri, Map<String, ?> environment) {
        String host = null;

        if (environment.containsKey(HOST)) {
            host = (String) environment.get(HOST);
        }

        if (host == null && uri.getHost() != null) {
            host = uri.getHost();
        }

        if (host == null) {
            host = "localhost";
        }

        int port = -1;

        if (environment.containsKey(PORT)) {
            Object portFromEnvironment = environment.get(PORT);

            if (portFromEnvironment instanceof Number) {
                port = ((Number) portFromEnvironment).intValue();
            }

            if (port < 0 && portFromEnvironment instanceof String) {
                try {
                    port = Integer.parseInt((String) portFromEnvironment);
                } catch (NumberFormatException thrown) {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.log(Level.FINE, thrown.getMessage());
                    }
                }
            }

            if (port < 0 && portFromEnvironment != null) {
                throw new IllegalArgumentException(
                    String.format("Couldn't accept \"%s\" value: %s", PORT, portFromEnvironment));
            }
        }

        if (port < 0) {
            port = uri.getPort();
        }

        if (port < 0) {
            port = STANDARD_PORT;
        }

        String username = (String) environment.get(USERNAME);
        String password = (String) environment.get(PASSWORD);

        if (username == null || password == null) {
            String userInfo = uri.getUserInfo();

            if (StringUtils.isNotBlank(userInfo)) {
                String[] userInfoParts = userInfo.split("[:]");

                if (userInfoParts.length > 0 && username == null) {
                    username = userInfoParts[0];
                }

                if (userInfoParts.length > 1 && password == null) {
                    password = userInfoParts[1];
                }
            }
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, String.format("Connecting to %s:%d as %s", host, port, username));
        }

        return Pair.of(
            new SftpPeerIdentity(host, port, username),
            password);
    }

    @Override public FileSystem newFileSystem(URI uri, Map<String, ?> environment) throws IOException {
        Pair<SftpPeerIdentity, String> connectionParameters = determineConnectionParameters(uri, environment);

        if (fileSystems.containsKey(connectionParameters.getKey())) {
            throw new FileSystemAlreadyExistsException(
                String.format("File system already connected to %s", connectionParameters.getKey()));
        } else {
            SftpFileSystem fileSystem = new SftpFileSystem(this, connectionParameters.getKey(), connectionParameters.getValue());

            fileSystems.put(connectionParameters.getKey(), fileSystem);

            return fileSystem;
        }
    }

    @Override public FileSystem getFileSystem(URI uri) {
        Pair<SftpPeerIdentity, String> connectionParameters = determineConnectionParameters(uri, emptyMap());

        if (fileSystems.containsKey(connectionParameters.getKey())) {
            return fileSystems.get(connectionParameters.getKey());
        } else {
            throw new FileSystemNotFoundException(
                String.format("File system not connected to %s", connectionParameters.getKey()));
        }
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
