package com.windchopper.fs;

import com.jcraft.jsch.SftpATTRS;

public class SftpFile implements SftpConstants {

    final String path;
    final String name;
    final SftpATTRS attributes;

    SftpFile(String path, String name, SftpATTRS attributes) {
        this.path = path;
        this.name = name;
        this.attributes = attributes;
    }

    String toAbsolutePath() {
        return (path.equals(SEPARATOR) ? "" : path) + SEPARATOR + name;
    }

    SftpPath toPath(SftpFileSystem fileSystem, SftpSessionIdentity sessionIdentity) {
        return new SftpPath(fileSystem, sessionIdentity, path, name);
    }

    SftpFileAttributes toFileAttributes() {
        return new SftpFileAttributes(attributes);
    }

}
