package com.windchopper.fs.sftp;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;

public class SftpFileSystem extends FileSystem implements SftpConstants {

    private final SftpFileSystemProvider provider;
    private final SftpFileSystemEnvironment environment;

    private final Session session;
    private final List<ChannelSftp.LsEntry> rootEntries;

    @SuppressWarnings("unchecked")
    SftpFileSystem(SftpFileSystemProvider provider, SftpFileSystemEnvironment environment) throws IOException {
        this.provider = provider;
        this.environment = environment;

        try {
            session = provider.jsch.getSession(environment.peerIdentity.username, environment.peerIdentity.host, environment.peerIdentity.port);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(environment.password);
            session.connect();

            ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            try {
                rootEntries = channelSftp.ls(SftpPath.SEPARATOR);
            } finally {
                channelSftp.disconnect();
            }
        } catch (JSchException | SftpException thrown) {
            throw new IOException(thrown);
        }
    }

    @Override public FileSystemProvider provider() {
        return provider;
    }

    URI composeUri(String pathString) {
        try {
            return new URI(SCHEME, environment.peerIdentity.username, environment.peerIdentity.host, environment.peerIdentity.port,
                pathString, null, null);
        } catch (URISyntaxException thrown) {
            throw new IllegalArgumentException(
                String.format("Invalid path: %s", pathString));
        }
    }

    @Override public void close() {
        session.disconnect();
        provider.retire(environment.peerIdentity, this);
    }

    @Override public boolean isOpen() {
        return session.isConnected();
    }

    @Override public boolean isReadOnly() {
        return false;
    }

    @Override public String getSeparator() {
        return SEPARATOR;
    }

    String realPath(String pathString) throws IOException {
        try {
            ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            try {
                return channelSftp.realpath(pathString);
            } finally {
                channelSftp.disconnect();
            }
        } catch (JSchException | SftpException thrown) {
            throw new IOException(
                String.format("Couldn't resolve path %s", pathString),
                thrown);
        }
    }

    @Override public Iterable<Path> getRootDirectories() {
        return rootEntries.stream()
            .filter(entry -> entry.getAttrs().isDir() && !StringUtils.equalsAny(entry.getFilename(), ".", ".."))
            .map(entry -> new SftpPath(this, SEPARATOR + entry.getFilename()))
            .collect(toList());
    }

    @Override public Iterable<FileStore> getFileStores() {
        throw new UnsupportedOperationException();
    }

    @Override public Set<String> supportedFileAttributeViews() {
        return emptySet();
    }

    @Override public Path getPath(String firstPathElement, String... restPathElements) {
        return new SftpPath(this, Stream.concat(
            Stream.of(firstPathElement),
            stream(restPathElements))
                .toArray(String[]::new));
    }

    @Override public PathMatcher getPathMatcher(String pathString) {
        throw new UnsupportedOperationException();
    }

    @Override public UserPrincipalLookupService getUserPrincipalLookupService() {
        throw new UnsupportedOperationException();
    }

    @Override public WatchService newWatchService() throws IOException {
        throw new UnsupportedOperationException();
    }

}
