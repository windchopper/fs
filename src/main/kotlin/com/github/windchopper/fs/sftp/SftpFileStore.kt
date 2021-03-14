package com.github.windchopper.fs.sftp

import java.nio.file.FileStore
import java.nio.file.attribute.FileAttributeView
import java.nio.file.attribute.FileStoreAttributeView

class SftpFileStore(private val name: String, private val fileSystem: SftpFileSystem): FileStore() {

    override fun name() = name

    override fun type() = SftpFileSystem.SCHEME

    override fun isReadOnly() = false

    override fun getTotalSpace() = Long.MAX_VALUE

    override fun getUsableSpace() = Long.MAX_VALUE

    override fun getUnallocatedSpace() = Long.MAX_VALUE

    override fun <V: FileStoreAttributeView> getFileStoreAttributeView(type: Class<V>) = null

    override fun getAttribute(attribute: String) = null

    override fun supportsFileAttributeView(type: Class<out FileAttributeView>?): Boolean {
        TODO("Not yet implemented")
    }

    override fun supportsFileAttributeView(name: String?): Boolean {
        TODO("Not yet implemented")
    }

}