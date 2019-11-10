package com.windchopper.fs.sftp;

import com.github.windchopper.common.util.Pipeliner;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;

public class SftpFileSystem extends FileSystem implements SftpConstants {

    private final SftpFileSystemProvider provider;
    private final SftpConfiguration configuration;

    private final Map<String, SftpFile> viewBuffer;
    private final Map<String, List<SftpFile>> listBuffer;

    private final Session session;

    SftpFileSystem(SftpFileSystemProvider provider, SftpConfiguration configuration) throws IOException {
        this.provider = provider;
        this.configuration = configuration;

        try {
            viewBuffer = new LRUMap<>(configuration.bufferSize);
            listBuffer = new LRUMap<>(configuration.bufferSize);

            session = Pipeliner.of(provider.jsch)
                .mapFailable(configuration.sessionIdentity::createJschSession)
                .accept(session -> session.setConfig("StrictHostKeyChecking", "no"))
                .set(session -> session::setPassword, configuration.password)
                .acceptFailable(Session::connect)
                .get();
        } catch (JSchException thrown) {
            throw new IOException(thrown);
        }
    }

    SftpFile view(String path) throws IOException {
        SftpFile file = viewBuffer.get(path);

        if (file == null) {
            try {
                ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
                channel.connect();

                try {
                    fillInBuffers(path, channel.ls(path));
                    file = viewBuffer.get(path);
                } finally {
                    channel.disconnect();
                }
            } catch (JSchException | SftpException thrown) {
                throw new IOException(thrown);
            }
        }

        if (file == null) {
            throw new FileNotFoundException(path);
        }

        return file;
    }

    List<SftpFile> fillInBuffers(String path, Vector<?> entries) {
        List<SftpFile> files = new ArrayList<>(entries.size());

        for (Object entry : entries) {
            LsEntry typedEntry = (LsEntry) entry;
            SftpFile file;

            if (typedEntry.getFilename().equals("..")) {
                continue;
            }

            if (typedEntry.getFilename().equals(".")) {
                file = new SftpFile(StringUtils.substringBeforeLast(path, SEPARATOR), StringUtils.substringAfterLast(path, SEPARATOR), typedEntry.getAttrs());
            } else {
                files.add(file = new SftpFile(path, typedEntry.getFilename(), typedEntry.getAttrs()));
            }

            viewBuffer.put(file.toAbsolutePath(), file);
        }

        listBuffer.put(path, files);
        return files;
    }

    List<SftpFile> list(String path) throws IOException {
        List<SftpFile> files = listBuffer.get(path = path.equals(SEPARATOR) ? path : StringUtils.removeEnd(path, SEPARATOR));

        if (files == null) {
            try {
                ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
                channel.connect();

                try {
                    files = fillInBuffers(path, channel.ls(path));
                } finally {
                    channel.disconnect();
                }
            } catch (JSchException | SftpException thrown) {
                throw new IOException(thrown);
            }
        }

        return files;
    }

    @Override public SftpFileSystemProvider provider() {
        return provider;
    }

    @Override public void close() {
        session.disconnect();
        provider.retire(configuration.sessionIdentity, this);
        List.of(viewBuffer, listBuffer)
            .forEach(Map::clear);
    }

    DirectoryStream<Path> newDirectoryStream(SftpPath path, DirectoryStream.Filter<? super Path> filter) throws IOException {
        return new SftpDirectoryStream(list(path.toString()).stream()
            .map(file -> file.toPath(this, configuration.sessionIdentity))
            .collect(toList()));
    }

    @Override public boolean isOpen() {
        return session.isConnected();
    }

    @Override public boolean isReadOnly() {
        return false;
    }

    @Override public String getSeparator() {
        return SftpConfiguration.SEPARATOR;
    }

    String realPath(String path) throws IOException {
        try {
            ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            try {
                return channelSftp.realpath(path);
            } finally {
                channelSftp.disconnect();
            }
        } catch (JSchException | SftpException thrown) {
            throw new IOException(
                String.format("Couldn't resolve path %s", path),
                thrown);
        }
    }

    @Override public Iterable<Path> getRootDirectories() {
        return List.of(new SftpPath(this, configuration.sessionIdentity, SftpConfiguration.SEPARATOR));
    }

    @Override public Iterable<FileStore> getFileStores() {
        throw new UnsupportedOperationException();
    }

    @Override public Set<String> supportedFileAttributeViews() {
        return emptySet();
    }

    @Override public Path getPath(String firstPathElement, String... restPathElements) {
        return new SftpPath(this, configuration.sessionIdentity, Stream.concat(
            Stream.of(firstPathElement),
            stream(restPathElements))
                .toArray(String[]::new));
    }

    @Override public PathMatcher getPathMatcher(String path) {
        throw new UnsupportedOperationException();
    }

    @Override public UserPrincipalLookupService getUserPrincipalLookupService() {
        throw new UnsupportedOperationException();
    }

    @Override public WatchService newWatchService() {
        throw new UnsupportedOperationException("Couldn't watch remote file system");
    }

}
