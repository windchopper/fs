package com.windchopper.fs.sftp;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;

public class SftpFileSystem extends FileSystem {

    private final SftpFileSystemProvider provider;
    private final SftpPeerIdentity peerIdentity;

    private final Session session;
    private final List<ChannelSftp.LsEntry> rootEntries;

    @SuppressWarnings("unchecked")
    SftpFileSystem(SftpFileSystemProvider provider, SftpPeerIdentity peerIdentity, String password) throws IOException {
        this.provider = provider;
        this.peerIdentity = peerIdentity;

        try {
            session = provider.jsch.getSession(peerIdentity.username, peerIdentity.host, peerIdentity.port);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(password);
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

    @Override public void close() {
        session.disconnect();
        provider.fileSystems.remove(peerIdentity);
    }

    @Override public boolean isOpen() {
        return true;
    }

    @Override public boolean isReadOnly() {
        return false;
    }

    @Override public String getSeparator() {
        return SftpPath.SEPARATOR;
    }

    @Override public Iterable<Path> getRootDirectories() {
        return rootEntries.stream()
            .filter(entry -> entry.getAttrs().isDir() && !StringUtils.equalsAny(entry.getFilename(), ".", ".."))
            .map(entry -> new SftpPath(this, entry.getFilename()))
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
        return null;
    }

    @Override public UserPrincipalLookupService getUserPrincipalLookupService() {
        throw new UnsupportedOperationException();
    }

    @Override public WatchService newWatchService() throws IOException {
        throw new UnsupportedOperationException();
    }

}
