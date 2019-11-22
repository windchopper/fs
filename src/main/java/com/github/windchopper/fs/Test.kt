package com.github.windchopper.fs

import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path

object Test {

    @JvmStatic
    fun main(args: Array<String>) {
        val uri = URI.create("sftp://oracle:Oracle33@vs-c06-szp-test-app.otr.ru")
        FileSystems.newFileSystem(uri, emptyMap<String, Any>()).use { fileSystem ->
            val homePath = fileSystem.getPath("/home/oracle")
            System.out.printf("home: %s%n", homePath)
            val parentPath = homePath.parent
            System.out.printf("parent: %s%n", parentPath)
            val homeRootPath = homePath.root
            System.out.printf("root: %s%n", homeRootPath)
            val nonAbsoluteHomePath = fileSystem.getPath("oracle")
            System.out.printf("non-absolute home: %s%n", nonAbsoluteHomePath)
            System.out.printf("home starts with parent: %s%n", homePath.startsWith(parentPath))
            System.out.printf("home starts with root: %s%n", homePath.startsWith(homeRootPath))
            System.out.printf("parent starts with home: %s%n", parentPath.startsWith(homePath))
            System.out.printf("home ends with non-absolute home: %s%n", homePath.endsWith(nonAbsoluteHomePath))
            System.out.printf("non-absolute home ends with home: %s%n", nonAbsoluteHomePath.endsWith(homePath))
            System.out.printf("%n")
            for (rootPath in fileSystem.rootDirectories) {
                list(rootPath)
            }
        }
    }

    fun list(path: Path?) {
        val i = Files.list(path).iterator()
        while (i.hasNext()) {
            val childPath = i.next()
            System.out.printf("%s%n", childPath)
            if (Files.isDirectory(childPath)) {
                list(childPath)
            }
        }
    }

}