package com.github.windchopper.fs.internal

import com.github.windchopper.fs.sftp.SftpFileSystem
import java.nio.file.InvalidPathException
import java.util.*
import kotlin.collections.ArrayList

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

    fun root(): ParsedPath? {
        return if (absolute) {
            return if (elements.isNotEmpty()) {
                ParsedPath(true, emptyList())
            } else {
                this
            }
        } else {
            null
        }
    }

    fun startsWith(other: ParsedPath): Boolean {
        var otherElementIndex = 0
        var elementIndex = 0

        while (elementIndex < elements.size && otherElementIndex < other.elements.size) {
            if (elements[elementIndex] != other.elements[otherElementIndex]) {
                return false
            }

            elementIndex++
            otherElementIndex++
        }

        return otherElementIndex == other.elements.size
    }

    fun endsWith(other: ParsedPath): Boolean {
        var otherElementIndex = other.elements.size
        var elementIndex = elements.size

        while (--elementIndex >= 0 && --otherElementIndex >= 0) {
            if (elements[elementIndex] != other.elements[otherElementIndex]) {
                return false
            }
        }

        return otherElementIndex == -1
    }

    fun relativize(other: ParsedPath): ParsedPath {
        if (root() != other.root()) {
            throw IllegalArgumentException("Paths have different roots: ${this}, ${other}")
        }

        if (other === this) {
            return ParsedPath(absolute, emptyList())
        } else if (root() === null && other.elements.isEmpty()) {
            return other
        }

        // common subsequence
        var sharedSubsequenceLength = 0

        for (i in 0 until elements.size.coerceAtMost(other.elements.size)) {
            if (elements[i] == other.elements[i]) {
                sharedSubsequenceLength++
            } else {
                break
            }
        }

        val extraElements = 0.coerceAtLeast(elements.size - sharedSubsequenceLength)
        val otherExtraElements = if (other.elements.size <= sharedSubsequenceLength) emptyList() else other.elements.subList(sharedSubsequenceLength, other.elements.size)
        val parts: MutableList<String> = ArrayList(extraElements + otherExtraElements.size)

        // add .. for each extra element in this path
        parts.addAll(Collections.nCopies(extraElements, ".."))

        // add each extra element in the other path
        parts.addAll(otherExtraElements)

        return ParsedPath(false, parts)
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

    override fun hashCode(): Int {
        return 31 * absolute.hashCode() + elements.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return this === other || other is ParsedPath && absolute == other.absolute && elements == other.elements
    }

}