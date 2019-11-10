package com.windchopper.fs.sftp;

import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

public class SftpFileAttributesView implements BasicFileAttributeView {

    final SftpFile file;

    SftpFileAttributesView(SftpFile file) {
        this.file = file;
    }

    @Override
    public String name() {
        return getClass().getSimpleName();
    }

    @Override
    public BasicFileAttributes readAttributes() {
        return new SftpFileAttributes(file.attributes);
    }

    @Override
    public void setTimes(FileTime lastModifiedTime, FileTime lastAccessTime, FileTime createTime) {
        throw new UnsupportedOperationException();
    }

}
