package com.github.windchopper.fs.sftp

import com.jcraft.jsch.SftpATTRS
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime
import java.time.Instant

class SftpFileAttributes(private val attributes: SftpATTRS): BasicFileAttributes {

    private val lastModifiedTime: Instant = Instant.ofEpochSecond(attributes.mTime.toLong())
    private val lastAccessedTime: Instant = Instant.ofEpochSecond(attributes.aTime.toLong())

    override fun lastModifiedTime(): FileTime {
        return FileTime.from(lastModifiedTime)
    }

    override fun lastAccessTime(): FileTime {
        return FileTime.from(lastAccessedTime)
    }

    override fun creationTime(): FileTime {
        throw UnsupportedOperationException("No creation time available through SFTP")
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