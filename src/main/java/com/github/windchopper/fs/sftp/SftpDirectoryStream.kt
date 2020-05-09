package com.github.windchopper.fs.sftp

import java.nio.file.DirectoryStream
import java.nio.file.Path

class SftpDirectoryStream(filter: DirectoryStream.Filter<in Path?>, paths: List<Path>): DirectoryStream<Path> {

    private val filteredPaths: MutableList<Path> = paths
        .filter { path -> filter.accept(path) }
        .toMutableList()

    override fun iterator(): MutableIterator<Path> {
        return filteredPaths.iterator()
    }

    override fun close() {
        // nothing to do
    }

}