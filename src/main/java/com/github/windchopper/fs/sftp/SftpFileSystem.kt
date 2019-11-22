package com.github.windchopper.fs.sftp

import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.ChannelSftp.LsEntry
import com.jcraft.jsch.JSchException
import com.jcraft.jsch.Session
import com.jcraft.jsch.SftpException
import org.apache.commons.collections4.map.LRUMap
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.UserPrincipalLookupService
import java.util.*
import java.util.stream.Collectors

class SftpFileSystem(private val provider: SftpFileSystemProvider, private val configuration: SftpConfiguration): FileSystem(), SftpRoutines {

    val viewBuffer: MutableMap<String?, SftpFile>
    val listBuffer: MutableMap<String?, List<SftpFile>>
    val session: Session

    init {
        try {
            viewBuffer = LRUMap(configuration.bufferSize)
            listBuffer = LRUMap(configuration.bufferSize)
            with (configuration) {
                with (sessionIdentity) {
                    session = provider.secureChannel.getSession(username, host, port)
                        .let {
                            it.setConfig("StrictHostKeyChecking", "no")
                            it.setPassword(password)
                            it.connect()
                            it
                        }
                }
            }
        } catch (thrown: JSchException) {
            throw IOException(thrown)
        }
    }

    fun view(path: String?): SftpFile {
        return viewBuffer[path]
            ?:try {
                val channel = session.openChannel("sftp") as ChannelSftp
                channel.connect()
                try {
                    fillInBuffers(path, channel.ls(path))
                    viewBuffer[path]
                } finally {
                    channel.disconnect()
                }
            } catch (thrown: Exception) {
                when (thrown) {
                    is IOException -> throw thrown
                    else -> throw IOException(thrown)
                }
            }
            ?:throw FileNotFoundException(path)
    }

    fun fillInBuffers(path: String?, entries: Vector<*>): List<SftpFile> {
        val files: MutableList<SftpFile> = ArrayList(entries.size)
        for (entry in entries) {
            val typedEntry = entry as LsEntry
            var file: SftpFile
            if (typedEntry.filename == "..") {
                continue
            }
            if (typedEntry.filename == ".") {
                file = SftpFile(path!!.substringBeforeLast(SftpConstants.SEPARATOR), path!!.substringAfterLast(SftpConstants.SEPARATOR), typedEntry.attrs)
            } else {
                files.add(SftpFile(path!!, typedEntry.filename, typedEntry.attrs).also { file = it })
            }
            viewBuffer[file.toAbsolutePath()] = file
        }
        listBuffer[path] = files
        return files
    }

    @Throws(IOException::class) fun list(path: String): List<SftpFile>? {
        var path = path
        var files = listBuffer[if (path == SftpConstants.SEPARATOR) path else path.removeSuffix(SftpConstants.SEPARATOR).also { path = it }]
        if (files == null) {
            files = try {
                val channel = session.openChannel("sftp") as ChannelSftp
                channel.connect()
                try {
                    fillInBuffers(path, channel.ls(path))
                } finally {
                    channel.disconnect()
                }
            } catch (thrown: JSchException) {
                throw IOException(thrown)
            } catch (thrown: SftpException) {
                throw IOException(thrown)
            }
        }
        return files
    }

    override fun provider(): SftpFileSystemProvider {
        return provider
    }

    @Throws(IOException::class) override fun close() {
        session.disconnect()
        provider.retire(configuration.sessionIdentity, this)
        for (buffer in listOf(viewBuffer, listBuffer)) {
            buffer.clear()
        }
    }

    fun newDirectoryStream(path: SftpPath, filter: DirectoryStream.Filter<in Path?>?): DirectoryStream<Path> {
        return SftpDirectoryStream(list(path.toString())!!.stream()
            .map { file: SftpFile -> file.toPath(this, configuration.sessionIdentity) }
            .collect(Collectors.toList()))
    }

    override fun isOpen(): Boolean {
        return session.isConnected
    }

    override fun isReadOnly(): Boolean {
        return false
    }

    override fun getSeparator(): String {
        return SftpConstants.SEPARATOR
    }

    @Throws(IOException::class) fun realPath(path: String?): String {
        return try {
            val channelSftp = session.openChannel("sftp") as ChannelSftp
            channelSftp.connect()
            try {
                channelSftp.realpath(path)
            } finally {
                channelSftp.disconnect()
            }
        } catch (thrown: JSchException) {
            throw IOException(String.format("Couldn't resolve path %s", path),
                thrown)
        } catch (thrown: SftpException) {
            throw IOException(String.format("Couldn't resolve path %s", path),
                thrown)
        }
    }

    override fun getRootDirectories(): Iterable<Path> {
        return SftpPath(this, configuration.sessionIdentity, SftpConstants.SEPARATOR).toList()
    }

    override fun getFileStores(): Iterable<FileStore> {
        TODO("not implemented")
    }

    override fun supportedFileAttributeViews(): Set<String> {
        return emptySet()
    }

    override fun getPath(firstPathElement: String, vararg restPathElements: String): Path {
        return SftpPath(this, configuration.sessionIdentity, *arrayOf(firstPathElement).plus(restPathElements))
    }

    override fun getPathMatcher(path: String): PathMatcher {
        TODO("not implemented")
    }

    override fun getUserPrincipalLookupService(): UserPrincipalLookupService {
        TODO("not implemented")
    }

    override fun newWatchService(): WatchService {
        throw watchNotSupported()
    }

}