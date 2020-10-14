package com.github.windchopper.fs.sftp

import java.io.IOException
import java.net.URI
import java.nio.file.*
import java.util.*

class SftpPath(private val fileSystem: SftpFileSystem, private val sessionIdentity: SftpConfiguration.SessionIdentity, private val absolute: Boolean, vararg pathElements: String): Path {

    private val pathElements: Array<String> = arrayOf(*pathElements)

    constructor(fileSystem: SftpFileSystem, connectionIdentity: SftpConfiguration.SessionIdentity, vararg pathElements: String): this(
        fileSystem, connectionIdentity, pathElements.joinToString(SftpFileSystem.PATH_SEPARATOR))

    constructor(fileSystem: SftpFileSystem, connectionIdentity: SftpConfiguration.SessionIdentity, pathString: String): this(
        fileSystem, connectionIdentity, pathString.startsWith(SftpFileSystem.PATH_SEPARATOR), *pathString.split(SftpFileSystem.PATH_SEPARATOR)
            .filter { it.isNotBlank() }
            .toTypedArray())

    override fun getFileSystem(): SftpFileSystem {
        return fileSystem
    }

    override fun isAbsolute(): Boolean {
        return absolute
    }

    override fun getRoot(): Path? {
        return if (absolute && pathElements.isNotEmpty()) SftpPath(fileSystem, sessionIdentity, SftpFileSystem.PATH_SEPARATOR) else null
    }

    override fun getFileName(): Path? {
        return if (pathElements.isNotEmpty()) SftpPath(fileSystem, sessionIdentity, pathElements[pathElements.size - 1]) else null
    }

    override fun getParent(): Path? {
        return if (pathElements.size > 1) {
            SftpPath(fileSystem, sessionIdentity, absolute, *pathElements.copyOfRange(0, pathElements.size - 1))
        } else if (pathElements.isNotEmpty() && absolute) {
            SftpPath(fileSystem, sessionIdentity, SftpFileSystem.PATH_SEPARATOR)
        } else null
    }

    override fun getNameCount(): Int {
        return pathElements.size
    }

    override fun getName(index: Int): Path {
        return if (index < pathElements.size) {
            SftpPath(fileSystem, sessionIdentity, pathElements[index])
        } else {
            throw IllegalArgumentException("Index ${index} out of bounds (${pathElements.size})")
        }
    }

    override fun subpath(fromIndex: Int, toIndex: Int): Path {
        return if (fromIndex >= 0) {
            if (toIndex <= pathElements.size) {
                SftpPath(fileSystem, sessionIdentity, *Arrays.copyOfRange(pathElements, fromIndex, toIndex))
            } else {
                throw IllegalArgumentException("End index ${toIndex} out of bounds (${pathElements.size})")
            }
        } else {
            throw IllegalArgumentException("Begin index ${fromIndex} out of bounds")
        }
    }

    override fun startsWith(path: Path): Boolean {
        return path.toSftpPath().let {
            var j = 0
            val jcount = it.pathElements.size
            var i = 0
            val icount = pathElements.size
            while (i < icount && j < jcount) {
                if (pathElements[i] != it.pathElements[j]) {
                    return false
                }
                i++
                j++
            }
            j == jcount
        }
    }

    override fun endsWith(path: Path): Boolean {
        return path.toSftpPath().let {
            var j = it.pathElements.size
            var i = pathElements.size
            while (--i >= 0 && --j >= 0) {
                if (pathElements[i] != it.pathElements[j]) {
                    return false
                }
            }
            j == -1
        }
    }

    override fun normalize(): Path {
        return SftpPath(fileSystem, sessionIdentity, toString())
    }

    override fun resolve(path: Path): Path {
        return SftpPath(fileSystem, sessionIdentity, *pathElements.plus(path.toSftpPath().pathElements))
    }

    override fun relativize(path: Path): Path {
        TODO("not implemented")
    }

    override fun toUri(): URI {
        return URI(SftpFileSystem.SCHEME, sessionIdentity.username, sessionIdentity.host, sessionIdentity.port, pathElements.joinToString(SftpFileSystem.PATH_SEPARATOR), null, null)
    }

    override fun toAbsolutePath(): Path {
        return if (absolute) this else SftpPath(fileSystem, sessionIdentity, toString(SftpFileSystem.PATH_SEPARATOR))
    }

    @Throws(IOException::class) override fun toRealPath(vararg linkOptions: LinkOption): Path {
        return SftpPath(fileSystem, sessionIdentity, fileSystem.realPath(toString()))
    }

    @Throws(IOException::class) override fun register(watchService: WatchService, kinds: Array<WatchEvent.Kind<*>?>?, vararg modifiers: WatchEvent.Modifier): WatchKey {
        throw UnsupportedOperationException("Couldn't watch remote file system")
    }

    override fun compareTo(other: Path): Int {
        return Arrays.compare(pathElements, other.toSftpPath().pathElements)
    }

    fun toString(prefix: String): String {
        return pathElements.joinToString(SftpFileSystem.PATH_SEPARATOR, prefix)
    }

    override fun toString(): String {
        return toString(if (absolute) SftpFileSystem.PATH_SEPARATOR else "")
    }

}