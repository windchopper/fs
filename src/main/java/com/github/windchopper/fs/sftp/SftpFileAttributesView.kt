package com.github.windchopper.fs.sftp

import java.nio.file.attribute.BasicFileAttributeView
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime

class SftpFileAttributesView internal constructor(val file: SftpFile): BasicFileAttributeView {

    override fun name(): String {
        return javaClass.simpleName
    }

    override fun readAttributes(): BasicFileAttributes {
        return SftpFileAttributes(file.attributes)
    }

    override fun setTimes(lastModifiedTime: FileTime, lastAccessTime: FileTime, createTime: FileTime) {
        TODO("not implemented")
    }

}