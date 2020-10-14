package com.github.windchopper.fs.sftp

import com.github.windchopper.fs.JSchHelper
import com.github.windchopper.fs.performConnected
import com.github.windchopper.fs.wrapExceptionTo
import com.jcraft.jsch.ChannelSftp.LsEntry
import org.apache.commons.collections4.map.LRUMap
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.UserPrincipalLookupService
import java.util.*

class SftpFileSystem(private val provider: SftpFileSystemProvider, private val configuration: SftpConfiguration): FileSystem() {

    val viewBuffer: MutableMap<String?, SftpFile> = LRUMap(configuration.bufferSize)
    val listBuffer: MutableMap<String?, List<SftpFile>> = LRUMap(configuration.bufferSize)
    val helper = JSchHelper(JSchHelper.Type.SFTP, configuration.channelInactivityDuration, wrapExceptionTo(::IOException) {
            with (configuration) {
                with (sessionIdentity) {
                    provider.secureChannel.getSession(username, host, port)
                        .let {
                            it.setConfig("StrictHostKeyChecking", "no")
                            it.setPassword(password)
                            it.connect()
                            it
                        }
                }
            }
        })

    fun view(path: String): SftpFile {
        return viewBuffer[path]
            ?:helper.performConnected { channel ->
                fillInBuffers(path, channel.ls(path))
                viewBuffer[path]
            }
            ?:throw FileNotFoundException(path)
    }

    @Suppress("UNCHECKED_CAST") fun fillInBuffers(path: String, entries: Vector<*>): List<SftpFile> {
        val files: MutableList<SftpFile> = ArrayList(entries.size)

        (entries as Vector<LsEntry>)
            .filter {
                it.filename != ".."
            }
            .map {
                if (it.filename == ".") {
                    SftpFile(path.substringBeforeLast(SftpConstants.PATH_SEPARATOR), path.substringAfterLast(SftpConstants.PATH_SEPARATOR), it.attrs)
                } else {
                    SftpFile(path, it.filename, it.attrs)
                        .let {
                            files.add(it)
                            it
                        }
                }
            }
            .forEach {
                viewBuffer[it.toAbsolutePath()] = it
            }

        listBuffer[path] = files
        return files
    }

    @Throws(IOException::class) fun list(path: String): List<SftpFile> {
        return (if (path == SftpConstants.PATH_SEPARATOR) path else path.removeSuffix(SftpConstants.PATH_SEPARATOR))
            .let {
                listBuffer[it]
                    ?:helper.performConnected { channel ->
                        fillInBuffers(path, channel.ls(path))
                    }
            }
    }

    override fun provider(): SftpFileSystemProvider {
        return provider
    }

    @Throws(IOException::class) override fun close() {
        helper.session.disconnect()
        provider.retire(configuration.sessionIdentity, this)
        for (buffer in listOf(viewBuffer, listBuffer)) {
            buffer.clear()
        }
    }

    fun newDirectoryStream(path: SftpPath, filter: DirectoryStream.Filter<in Path?>): DirectoryStream<Path> {
        return SftpDirectoryStream(filter, list(path.toString())
            .map { it.toPath(this, configuration.sessionIdentity) })
    }

    override fun isOpen(): Boolean {
        return helper.session.isConnected
    }

    override fun isReadOnly(): Boolean {
        return false
    }

    override fun getSeparator(): String {
        return SftpConstants.PATH_SEPARATOR
    }

    @Throws(IOException::class) fun realPath(path: String?): String {
        return helper.performConnected {
            it.realpath(path)
        }
    }

    override fun getRootDirectories(): Iterable<Path> {
        return listOf(SftpPath(this, configuration.sessionIdentity, SftpConstants.PATH_SEPARATOR))
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
        throw UnsupportedOperationException("Couldn't watch remote file system")
    }

}