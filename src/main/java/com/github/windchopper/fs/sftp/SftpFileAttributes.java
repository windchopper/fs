package com.github.windchopper.fs.sftp;

import com.jcraft.jsch.SftpATTRS;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

public class SftpFileAttributes implements BasicFileAttributes {

    final SftpATTRS attributes;

    SftpFileAttributes(SftpATTRS attributes) {
        this.attributes = attributes;
    }

    @Override public FileTime lastModifiedTime() {
        return null;
    }

    @Override public FileTime lastAccessTime() {
        return null;
    }

    @Override public FileTime creationTime() {
        return null;
    }

    @Override public boolean isRegularFile() {
        return attributes.isChr() || attributes.isBlk();
    }

    @Override public boolean isDirectory() {
        return attributes.isDir();
    }

    @Override public boolean isSymbolicLink() {
        return attributes.isLink();
    }

    @Override public boolean isOther() {
        return false;
    }

    @Override public long size() {
        return attributes.getSize();
    }

    @Override public Object fileKey() {
        return attributes.getUId();
    }

}
