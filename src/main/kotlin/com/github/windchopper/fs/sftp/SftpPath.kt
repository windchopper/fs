package com.github.windchopper.fs.sftp

import com.github.windchopper.fs.internal.ParsedPath
import com.github.windchopper.fs.sftp.SftpConfiguration.SessionIdentity
import java.io.IOException
import java.net.URI
import java.nio.file.*

internal fun Path.toParsedPath(): ParsedPath = (this as? SftpPath)?.parsedPath?:ParsedPath(this.toString())

class SftpPath internal constructor(
    internal val fileSystem: SftpFileSystem,
    internal val sessionIdentity: SessionIdentity,
    internal val parsedPath: ParsedPath)
        : Path {

    constructor(fileSystem: SftpFileSystem, sessionIdentity: SessionIdentity, vararg elements: String): this(
        fileSystem, sessionIdentity, ParsedPath(*elements))

    override fun getFileSystem(): SftpFileSystem {
        return fileSystem
    }

    override fun isAbsolute(): Boolean {
        return parsedPath.absolute
    }

    override fun getRoot(): Path? {
        return parsedPath.root()?.let {
            SftpPath(fileSystem, sessionIdentity, it)
        }
    }

    override fun getFileName(): Path? {
        return if (parsedPath.elements.isNotEmpty()) {
            SftpPath(fileSystem, sessionIdentity, ParsedPath(false, listOf(parsedPath.elements.last())))
        } else {
            null
        }
    }

    override fun getParent(): Path? {
        return if (parsedPath.elements.size > 1) {
            SftpPath(fileSystem, sessionIdentity, ParsedPath(parsedPath.absolute, parsedPath.elements.subList(0, parsedPath.elements.size - 1)))
        } else if (parsedPath.elements.isNotEmpty() && parsedPath.absolute) {
            SftpPath(fileSystem, sessionIdentity, ParsedPath(true, emptyList()))
        } else {
            null
        }
    }

    override fun getNameCount(): Int {
        return parsedPath.elements.size
    }

    override fun getName(index: Int): Path {
        if (index in parsedPath.elements.indices) {
            return SftpPath(fileSystem, sessionIdentity, ParsedPath(false, listOf(parsedPath.elements[index])))
        }

        throw IndexOutOfBoundsException(index)
    }

    override fun subpath(fromIndex: Int, toIndex: Int): Path {
        if (fromIndex in parsedPath.elements.indices) {
            if (toIndex - 1 in parsedPath.elements.indices) {
                return SftpPath(fileSystem, sessionIdentity, ParsedPath(false, parsedPath.elements.subList(fromIndex, toIndex)))
            }

            throw IndexOutOfBoundsException(toIndex)
        }

        throw IndexOutOfBoundsException(fromIndex)
    }

    override fun startsWith(path: Path): Boolean {
        return parsedPath.startsWith(path.toParsedPath())
    }

    override fun endsWith(path: Path): Boolean {
        return parsedPath.endsWith(path.toParsedPath())
    }

    override fun normalize(): Path {
        return SftpPath(fileSystem, sessionIdentity, parsedPath.normalize())
    }

    override fun resolve(path: Path): Path {
        return SftpPath(fileSystem, sessionIdentity, ParsedPath(parsedPath.absolute, parsedPath.elements.plus(path.toParsedPath().elements)))
    }

    override fun relativize(path: Path): Path {
        return SftpPath(fileSystem, sessionIdentity, parsedPath.relativize(path.toParsedPath()))
    }

    override fun toUri(): URI {
        return URI(SftpFileSystem.SCHEME, sessionIdentity.username, sessionIdentity.host, sessionIdentity.port, parsedPath.elements.joinToString(SftpFileSystem.PATH_SEPARATOR), null, null)
    }

    override fun toAbsolutePath(): Path {
        return SftpPath(fileSystem, sessionIdentity, ParsedPath(true, parsedPath.elements))
    }

    @Throws(IOException::class) override fun toRealPath(vararg linkOptions: LinkOption): Path {
        return SftpPath(fileSystem, sessionIdentity, fileSystem.realPath(toString()))
    }

    @Throws(IOException::class) override fun register(watchService: WatchService, kinds: Array<WatchEvent.Kind<*>?>?, vararg modifiers: WatchEvent.Modifier): WatchKey {
        throw UnsupportedOperationException("Couldn't watch remote file system")
    }

    override fun compareTo(other: Path): Int {
        return toString().compareTo(other.toString())
    }

    override fun toString(): String {
        return parsedPath.toString()
    }

}

