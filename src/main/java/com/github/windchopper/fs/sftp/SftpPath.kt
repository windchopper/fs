package com.github.windchopper.fs.sftp

import java.io.IOException
import java.net.URI
import java.nio.file.*
import java.util.*

class SftpPath(val fileSystem: SftpFileSystem, val sessionIdentity: SftpConfiguration.SessionIdentity, val absolute: Boolean, vararg pathElements: String): Path, SftpRoutines {

    private val pathElements: Array<String> = arrayOf(*pathElements)

    constructor(fileSystem: SftpFileSystem, connectionIdentity: SftpConfiguration.SessionIdentity, vararg pathElements: String): this(
        fileSystem, connectionIdentity, pathElements.joinToString(SftpConstants.SEPARATOR))

    constructor(fileSystem: SftpFileSystem, connectionIdentity: SftpConfiguration.SessionIdentity, pathString: String): this(
        fileSystem, connectionIdentity, pathString.startsWith(SftpConstants.SEPARATOR), *pathString.split(SftpConstants.SEPARATOR)
            .filter { it.isNotBlank() }
            .toTypedArray())

    override fun getFileSystem(): FileSystem {
        return fileSystem
    }

    override fun isAbsolute(): Boolean {
        return absolute
    }

    override fun getRoot(): Path? {
        return if (absolute && pathElements.size > 0) SftpPath(fileSystem, sessionIdentity, SftpConstants.SEPARATOR) else null
    }

    override fun getFileName(): Path? {
        return if (pathElements.size > 0) SftpPath(fileSystem, sessionIdentity, pathElements[pathElements.size - 1]) else null
    }

    override fun getParent(): Path? {
        if (pathElements.size > 1) {
            return SftpPath(fileSystem, sessionIdentity, absolute, *Arrays.copyOfRange(pathElements, 0, pathElements.size - 1))
        }
        return if (pathElements.size > 0 && absolute) {
            SftpPath(fileSystem, sessionIdentity, SftpConstants.SEPARATOR)
        } else null
    }

    override fun getNameCount(): Int {
        return pathElements.size
    }

    override fun getName(index: Int): Path {
        if (index < pathElements.size) {
            return SftpPath(fileSystem, sessionIdentity, pathElements[index])
        }
        throw IllegalArgumentException(String.format("Index %d out of bounds (%d)", index, pathElements.size))
    }

    override fun subpath(fromIndex: Int, toIndex: Int): Path {
        if (fromIndex >= 0) {
            if (toIndex <= pathElements.size) {
                return SftpPath(fileSystem, sessionIdentity, *Arrays.copyOfRange(pathElements, fromIndex, toIndex))
            }
            throw IllegalArgumentException(String.format("End index %d out of bounds (%d)", toIndex, pathElements.size))
        }
        throw IllegalArgumentException(String.format("Begin index %d out of bounds", fromIndex))
    }

    override fun startsWith(path: Path): Boolean {
        return checkPathAndApply(path) {
            var j = 0
            val jcount = it.pathElements.size
            var i = 0
            val icount = pathElements.size
            while (i < icount && j < jcount) {
                if (pathElements[i] != it.pathElements[j]) {
                    return@checkPathAndApply false
                }
                i++
                j++
            }
            j == jcount
        }
    }

    override fun endsWith(path: Path): Boolean {
        return checkPathAndApply(path) {
            var j = it.pathElements.size
            var i = pathElements.size
            while (--i >= 0 && --j >= 0) {
                if (pathElements[i] != it.pathElements[j]) {
                    return@checkPathAndApply false
                }
            }
            j == -1
        }
    }

    override fun normalize(): Path {
        return SftpPath(fileSystem, sessionIdentity, toString())
    }

    override fun resolve(path: Path): Path {
        return checkPathAndApply(path) {
            SftpPath(
                fileSystem,
                sessionIdentity,
                *pathElements.plus(it.pathElements))
        }
    }

    override fun relativize(path: Path): Path {
        return checkPathAndApply<Path>(path) { domesticPath: SftpPath? -> throw UnsupportedOperationException() }
    }

    override fun toUri(): URI {
        return URI(SftpConstants.SCHEME, sessionIdentity.username, sessionIdentity.host, sessionIdentity.port, pathElements.joinToString(SftpConstants.SEPARATOR), null, null)
    }

    override fun toAbsolutePath(): Path {
        return if (absolute) this else SftpPath(fileSystem, sessionIdentity, toString(SftpConstants.SEPARATOR))
    }

    @Throws(IOException::class)
    override fun toRealPath(vararg linkOptions: LinkOption): Path {
        return SftpPath(fileSystem, sessionIdentity, fileSystem.realPath(toString()))
    }

    @Throws(IOException::class)
    override fun register(watchService: WatchService, kinds: Array<WatchEvent.Kind<*>?>?, vararg modifiers: WatchEvent.Modifier): WatchKey {
        throw UnsupportedOperationException()
    }

    override fun compareTo(path: Path): Int {
        return checkPathAndApply(path) { domesticPath: SftpPath -> Arrays.compare(pathElements, domesticPath.pathElements) }
    }

    fun toString(prefix: String): String {
        return pathElements.joinToString(SftpConstants.SEPARATOR, prefix)
    }

    override fun toString(): String {
        return toString(if (absolute) SftpConstants.SEPARATOR else "")
    }

}