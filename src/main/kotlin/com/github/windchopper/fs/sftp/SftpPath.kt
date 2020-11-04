package com.github.windchopper.fs.sftp

import com.github.windchopper.fs.sftp.SftpConfiguration.SessionIdentity
import java.io.IOException
import java.net.URI
import java.nio.file.*

data class ParsedPath(val absolute: Boolean, val elements: List<String>)

@Throws(InvalidPathException::class) internal fun parse(vararg fragments: String): ParsedPath {
    val elements = fragments
        .flatMap { it.split(SftpFileSystem.PATH_SEPARATOR) }
        .filter { it.isNotBlank() }
        .toMutableList()

    val absolute = fragments.firstOrNull()
        ?.startsWith(SftpFileSystem.PATH_SEPARATOR) == true

    return ParsedPath(absolute, elements)
}

internal fun toString(absolute: Boolean, pathElements: List<String>): String = pathElements.joinToString(SftpFileSystem.PATH_SEPARATOR,
    if (absolute) SftpFileSystem.PATH_SEPARATOR else "")

@Throws(InvalidPathException::class) internal fun normalize(absolute: Boolean, elements: List<String>): ParsedPath {
    val normalizedElements = ArrayList<String>()

    for (index in elements.indices) {
        val currentElement = elements[index]

        if (currentElement == ".") continue else if (currentElement == "..") {
            if (normalizedElements.isNotEmpty()) {
                normalizedElements.removeLast()
                continue
            }

            if (absolute) {
                throw InvalidPathException(toString(absolute, elements),
                    "No directory upper than root")
            }
        }

        normalizedElements.add(currentElement)
    }

    return ParsedPath(absolute, normalizedElements)
}

internal fun Path.toParsedPath(): ParsedPath = (this as? SftpPath)
    ?.toParsedPath()?: parse(this.toString())

class SftpPath internal constructor(
    private val fileSystem: SftpFileSystem,
    private val sessionIdentity: SessionIdentity,
    private val absolute: Boolean,
    private val elements: List<String>)
        : Path {

    internal constructor(fileSystem: SftpFileSystem, sessionIdentity: SessionIdentity, parsedPath: ParsedPath): this(
        fileSystem, sessionIdentity, parsedPath.absolute, parsedPath.elements)

    constructor(fileSystem: SftpFileSystem, sessionIdentity: SessionIdentity, vararg elements: String): this(
        fileSystem, sessionIdentity, parse(*elements))

    override fun getFileSystem(): SftpFileSystem {
        return fileSystem
    }

    override fun isAbsolute(): Boolean {
        return absolute
    }

    override fun getRoot(): Path? {
        return if (absolute && elements.isNotEmpty()) {
            SftpPath(fileSystem, sessionIdentity, true, emptyList())
        } else {
            null
        }
    }

    override fun getFileName(): Path? {
        return if (elements.isNotEmpty()) {
            SftpPath(fileSystem, sessionIdentity, false, listOf(elements.last()))
        } else {
            null
        }
    }

    override fun getParent(): Path? {
        return if (elements.size > 1) {
            SftpPath(fileSystem, sessionIdentity, absolute, elements.subList(0, elements.size - 1))
        } else if (elements.isNotEmpty() && absolute) {
            SftpPath(fileSystem, sessionIdentity, true, listOf(SftpFileSystem.PATH_SEPARATOR))
        } else {
            null
        }
    }

    override fun getNameCount(): Int {
        return elements.size
    }

    override fun getName(index: Int): Path {
        if (index in elements.indices) {
            return SftpPath(fileSystem, sessionIdentity, false, listOf(elements[index]))
        }

        throw IndexOutOfBoundsException(index)
    }

    override fun subpath(fromIndex: Int, toIndex: Int): Path {
        if (fromIndex in elements.indices) {
            if (toIndex - 1 in elements.indices) {
                return SftpPath(fileSystem, sessionIdentity, false, elements.subList(fromIndex, toIndex))
            }

            throw IndexOutOfBoundsException(toIndex)
        }

        throw IndexOutOfBoundsException(fromIndex)
    }

    override fun startsWith(path: Path): Boolean {
        return path.toParsedPath().let { parsedPath ->
            var j = 0
            val jcount = parsedPath.elements.size
            var i = 0
            val icount = elements.size
            while (i < icount && j < jcount) {
                if (elements[i] != parsedPath.elements[j]) {
                    return false
                }
                i++
                j++
            }
            j == jcount
        }
    }

    override fun endsWith(path: Path): Boolean {
        return path.toParsedPath().let { parsedPath ->
            var j = parsedPath.elements.size
            var i = elements.size
            while (--i >= 0 && --j >= 0) {
                if (elements[i] != parsedPath.elements[j]) {
                    return false
                }
            }
            j == -1
        }
    }

    override fun normalize(): Path {
        val normalizedPath = normalize(absolute, elements)
        return SftpPath(fileSystem, sessionIdentity, normalizedPath.absolute, normalizedPath.elements)
    }

    override fun resolve(path: Path): Path {
        return SftpPath(fileSystem, sessionIdentity, absolute, elements.plus(path.toParsedPath().elements))
    }

    override fun relativize(path: Path): Path {
        TODO("not implemented")
    }

    override fun toUri(): URI {
        return URI(SftpFileSystem.SCHEME, sessionIdentity.username, sessionIdentity.host, sessionIdentity.port, elements.joinToString(SftpFileSystem.PATH_SEPARATOR), null, null)
    }

    override fun toAbsolutePath(): Path {
        return if (absolute) this else SftpPath(fileSystem, sessionIdentity, true, elements)
    }

    @Throws(IOException::class) override fun toRealPath(vararg linkOptions: LinkOption): Path {
        return SftpPath(fileSystem, sessionIdentity, fileSystem.realPath(toString()))
    }

    @Throws(IOException::class) override fun register(watchService: WatchService, kinds: Array<WatchEvent.Kind<*>?>?, vararg modifiers: WatchEvent.Modifier): WatchKey {
        throw UnsupportedOperationException("Couldn't watch remote file system")
    }

    internal fun toParsedPath(): ParsedPath {
        return ParsedPath(absolute, elements)
    }

    override fun compareTo(other: Path): Int {
        return toString().compareTo(other.toString())
    }

    override fun toString(): String {
        return toString(absolute, elements)
    }

}

