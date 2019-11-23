package com.github.windchopper.fs.sftp

import com.github.windchopper.fs.JSchLoggerBridge
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
import java.util.logging.Logger

class SftpFileSystemProvider: FileSystemProvider(), SftpRoutines {

    val secureChannel = JSch()
    val connectedFileSystems: MutableMap<SftpConfiguration.SessionIdentity, SftpFileSystem> = ConcurrentHashMap()

    init {
        JSch.setLogger(JSchLoggerBridge(Logger.getLogger(this::class.qualifiedName)))

        System.getProperty("user.home")
            ?.let { Paths.get(it).resolve(".ssh") }
            ?.let {
                wrapNotIOException {
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
        return SftpConstants.SCHEME
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

    fun retire(connectionIdentity: SftpConfiguration.SessionIdentity, fileSystem: SftpFileSystem?) {
        connectedFileSystems.remove(connectionIdentity, fileSystem)
    }

    override fun getPath(uri: URI): Path {
        return getFileSystem(uri).getPath(uri.path)
    }

    @Throws(IOException::class) override fun newByteChannel(path: Path, openOptionSet: Set<OpenOption?>, vararg fileAttributes: FileAttribute<*>?): SeekableByteChannel {
        return checkPathAndApply<SeekableByteChannel>(path) { TODO("not implemented") }
    }

    @Throws(IOException::class) override fun newDirectoryStream(path: Path, filter: DirectoryStream.Filter<in Path?>): DirectoryStream<Path> {
        return checkPathAndApply(path) { it.fileSystem.newDirectoryStream(it, filter) }
    }

    @Throws(IOException::class) override fun createDirectory(path: Path, vararg fileAttributes: FileAttribute<*>?) {
        checkPathAndAccept(path) { TODO("not implemented") }
    }

    @Throws(IOException::class) override fun delete(path: Path) {
        checkPathAndAccept(path) { TODO("not implemented") }
    }

    @Throws(IOException::class) fun delete(path: SftpPath?) {
        TODO("not implemented")
    }

    @Throws(IOException::class) override fun copy(sourcePath: Path, targetPath: Path, vararg copyOptions: CopyOption) {
        checkPathAndAccept(targetPath) { TODO("not implemented") }
    }

    @Throws(IOException::class) override fun move(sourcePath: Path, targetPath: Path, vararg copyOptions: CopyOption) {
        checkPathAndAccept(targetPath) { TODO("not implemented") }
    }

    @Throws(IOException::class) @Suppress("NAME_SHADOWING") override fun isSameFile(path1st: Path, path2nd: Path): Boolean {
        return checkPathAndApply(path1st, path2nd) { path1st, path2nd -> TODO("not implemented") }
    }

    @Throws(IOException::class) override fun isHidden(path: Path): Boolean {
        return checkPathAndApply<Boolean>(path) { TODO("not implemented") }
    }

    @Throws(IOException::class) override fun getFileStore(path: Path): FileStore {
        return checkPathAndApply<FileStore>(path) { TODO("not implemented") }
    }

    @Throws(IOException::class) override fun checkAccess(path: Path, vararg accessModes: AccessMode) {
        checkPathAndAccept(path) { TODO("not implemented") }
    }

    override fun <V: FileAttributeView?> getFileAttributeView(path: Path, viewType: Class<V>, vararg linkOptions: LinkOption): V {
        return checkPathAndApply(path) { TODO("not implemented") }
    }

    @Throws(IOException::class) @Suppress("UNCHECKED_CAST") override fun <A: BasicFileAttributes> readAttributes(path: Path, attributesType: Class<A>, vararg linkOptions: LinkOption): A {
        return checkPathAndApply(path) {
            return@checkPathAndApply if (attributesType === BasicFileAttributes::javaClass || attributesType === SftpFileAttributes::javaClass) {
                it.fileSystem.view(path.toString()).toFileAttributes() as A
            } else {
                throw attributesNotSupported(attributesType)
            }
        }
    }

    override fun readAttributes(path: Path, attributes: String, vararg linkOptions: LinkOption): Map<String, Any> {
        return checkPathAndApply(path) { TODO("not implemented") }
    }

    override fun setAttribute(path: Path, s: String, value: Any, vararg linkOptions: LinkOption) {
        checkPathAndAccept(path) { TODO("not implemented") }
    }

}