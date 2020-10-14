package com.github.windchopper.fs

import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.util.logging.Logger

object Test {

    @JvmStatic
    fun main(args: Array<String>) {
        val uri = URI.create("sftp://oracle:Oracle33@vs-c06-szp-test-app.otr.ru")

        FileSystems.newFileSystem(uri, emptyMap<String, Any>()).use { fileSystem ->
            val homePath = fileSystem.getPath("/home/oracle")
            println("home: ${homePath}")
            val parentPath = homePath.parent
            println("parent: ${parentPath}")
            val homeRootPath = homePath.root
            println("root: ${homeRootPath}")
            val nonAbsoluteHomePath = fileSystem.getPath("oracle")
            println("non-absolute home: ${nonAbsoluteHomePath}")
            println("home starts with parent: ${homePath.startsWith(parentPath)}")
            println("home starts with root: ${homePath.startsWith(homeRootPath)}")
            println("parent starts with home: ${parentPath.startsWith(homePath)}")
            println("home ends with non-absolute home: ${homePath.endsWith(nonAbsoluteHomePath)}")
            println("non-absolute home ends with home: ${nonAbsoluteHomePath.endsWith(homePath)}")
            println()
            for (rootPath in fileSystem.rootDirectories) {
                list(rootPath)
            }

            Logger.getLogger(Test::class.qualifiedName).info("end")
        }

    }

    fun list(path: Path) {
        for (childPath in Files.list(path)) {
            println("${childPath}")
            if (Files.isDirectory(childPath)) {
                list(childPath)
            }
        }
    }

}