package com.github.windchopper.fs.sftp

import java.nio.file.DirectoryStream
import java.nio.file.Path

class SftpDirectoryStream(private val paths: MutableList<Path>): DirectoryStream<Path> {

    override fun iterator(): MutableIterator<Path> {
        return paths.iterator()
    }

    override fun close() { // nothing to do
    }

}