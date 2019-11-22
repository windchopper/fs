package com.github.windchopper.fs.sftp

import com.jcraft.jsch.SftpATTRS
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime

class SftpFileAttributes(private val attributes: SftpATTRS): BasicFileAttributes {

    override fun lastModifiedTime(): FileTime {
        TODO("not implemented")
    }

    override fun lastAccessTime(): FileTime {
        TODO("not implemented")
    }

    override fun creationTime(): FileTime {
        TODO("not implemented")
    }

    override fun isRegularFile(): Boolean {
        return attributes.isChr || attributes.isBlk
    }

    override fun isDirectory(): Boolean {
        return attributes.isDir
    }

    override fun isSymbolicLink(): Boolean {
        return attributes.isLink
    }

    override fun isOther(): Boolean {
        return false
    }

    override fun size(): Long {
        return attributes.size
    }

    override fun fileKey(): Any {
        return attributes.uId
    }

}