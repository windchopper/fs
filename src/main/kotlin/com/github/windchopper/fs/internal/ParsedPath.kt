package com.github.windchopper.fs.internal

import com.github.windchopper.fs.sftp.SftpFileSystem
import java.nio.file.InvalidPathException

class ParsedPath {

    val absolute: Boolean
    val elements: List<String>

    constructor(absolute: Boolean, elements: List<String>) {
        this.absolute = absolute
        this.elements = elements
    }

    constructor(vararg fragments: String) {
        elements = fragments
            .flatMap { it.split(SftpFileSystem.PATH_SEPARATOR) }
            .filter { it.isNotBlank() }
            .toMutableList()
        absolute = true == fragments.firstOrNull()
            ?.startsWith(SftpFileSystem.PATH_SEPARATOR)
    }

    @Throws(InvalidPathException::class) fun normalize(): ParsedPath {
        val normalizedElements = ArrayList<String>()

        for (index in elements.indices) {
            val currentElement = elements[index]

            if (currentElement == ".") continue else if (currentElement == "..") {
                if (normalizedElements.isNotEmpty()) {
                    normalizedElements.removeLast()
                    continue
                }

                if (absolute) {
                    throw InvalidPathException(toString(), "No directory upper than root")
                }
            }

            normalizedElements.add(currentElement)
        }

        return ParsedPath(absolute, normalizedElements)
    }

    override fun toString(): String {
        return elements.joinToString(SftpFileSystem.PATH_SEPARATOR, if (absolute) SftpFileSystem.PATH_SEPARATOR else "")
    }

}