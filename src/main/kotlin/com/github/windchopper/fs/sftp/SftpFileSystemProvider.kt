package com.github.windchopper.fs.sftp

import com.github.windchopper.fs.JSchLogger
import com.github.windchopper.fs.logger
import com.github.windchopper.fs.wrapExceptionTo
import com.jcraft.jsch.JSch
import java.io.IOException
import java.net.URI
import java.nio.channels.SeekableByteChannel
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileAttribute
import java.nio.file.attribute.FileAttributeView
import java.nio.file.spi.FileSystemProvider
import java.util.concurrent.ConcurrentHashMap

class SftpFileSystemProvider: FileSystemProvider() {

    val secureChannel = JSch()
    val connectedFileSystems: MutableMap<SftpConfiguration.SessionIdentity, SftpFileSystem> = ConcurrentHashMap()

    init {
        JSch.setLogger(JSchLogger(logger<SftpFileSystemProvider>()))

        System.getProperty("user.home")
            ?.let { Paths.get(it).resolve(".ssh") }
            ?.let {
                wrapExceptionTo(::IOException) {
                    listOf(it.resolve("id_rsa"), it.resolve("id_dsa"), it.resolve("id_ecdsa"))
                        .filter { Files.exists(it) }
                        .forEach { secureChannel.addIdentity(it.toRealPath().toString()) }

                    val knownHostsFile = it.resolve("known_hosts")

                    if (Files.exists(knownHostsFile)) {
                        secureChannel.setKnownHosts(knownHostsFile.toRealPath().toString())
                    }
                }
            }
    }

    override fun getScheme(): String {
        return SftpFileSystem.SCHEME
    }

    override fun newFileSystem(uri: URI, environment: Map<String, *>): FileSystem {
        with (SftpConfiguration(uri, environment)) {
            if (connectedFileSystems.containsKey(sessionIdentity)) {
                throw FileSystemAlreadyExistsException("File system already connected to ${sessionIdentity}")
            }

            return SftpFileSystem(this@SftpFileSystemProvider, this)
                .let {
                    connectedFileSystems[sessionIdentity] = it
                    it
                }
        }
    }

    override fun getFileSystem(uri: URI): FileSystem {
        with (SftpConfiguration(uri)) {
            if (connectedFileSystems.containsKey(sessionIdentity)) {
                return connectedFileSystems[sessionIdentity]!!
            }

            throw FileSystemNotFoundException("File system not connected to ${sessionIdentity}")
        }
    }

    fun retire(connectionIdentity: SftpConfiguration.SessionIdentity, fileSystem: SftpFileSystem) {
        connectedFileSystems.remove(connectionIdentity, fileSystem)
    }

    override fun getPath(uri: URI): Path {
        return getFileSystem(uri).getPath(uri.path)
    }

    @Throws(IOException::class) override fun newByteChannel(path: Path, openOptionSet: Set<OpenOption?>, vararg fileAttributes: FileAttribute<*>?): SeekableByteChannel {
        TODO("not implemented")
    }

    @Throws(IOException::class) override fun newDirectoryStream(path: Path, filter: DirectoryStream.Filter<in Path?>): DirectoryStream<Path> {
        return path.toSftpPath().let {
            it.fileSystem.newDirectoryStream(it, filter)
        }
    }

    @Throws(IOException::class) override fun createDirectory(path: Path, vararg fileAttributes: FileAttribute<*>?) {
        TODO("not implemented")
    }

    @Throws(IOException::class) override fun delete(path: Path) {
        TODO("not implemented")
    }

    @Throws(IOException::class) override fun copy(sourcePath: Path, targetPath: Path, vararg copyOptions: CopyOption) {
        TODO("not implemented")
    }

    @Throws(IOException::class) override fun move(sourcePath: Path, targetPath: Path, vararg copyOptions: CopyOption) {
        TODO("not implemented")
    }

    @Throws(IOException::class) @Suppress("NAME_SHADOWING") override fun isSameFile(path1st: Path, path2nd: Path): Boolean {
        TODO("not implemented")
    }

    @Throws(IOException::class) override fun isHidden(path: Path): Boolean {
        TODO("not implemented")
    }

    @Throws(IOException::class) override fun getFileStore(path: Path): FileStore {
        TODO("not implemented")
    }

    @Throws(IOException::class) override fun checkAccess(path: Path, vararg accessModes: AccessMode) {
        TODO("not implemented")
    }

    override fun <V: FileAttributeView?> getFileAttributeView(path: Path, viewType: Class<V>, vararg linkOptions: LinkOption): V {
        TODO("not implemented")
    }

    @Throws(IOException::class) @Suppress("UNCHECKED_CAST") override fun <A: BasicFileAttributes> readAttributes(path: Path, attributesType: Class<A>, vararg linkOptions: LinkOption): A {
        return path.toSftpPath().let {
            if (attributesType == BasicFileAttributes::class.java || attributesType == SftpFileAttributes::class.java) {
                it.fileSystem.view(it.toString()).toFileAttributes() as A
            } else {
                throw UnsupportedOperationException("Attributes of type ${attributesType.canonicalName} not supported")
            }
        }
    }

    override fun readAttributes(path: Path, attributes: String, vararg linkOptions: LinkOption): Map<String, Any> {
        TODO("not implemented")
    }

    override fun setAttribute(path: Path, s: String, value: Any, vararg linkOptions: LinkOption) {
        TODO("not implemented")
    }

}