@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package com.github.windchopper.fs.sftp

import org.apache.sshd.server.SshServer
import org.apache.sshd.server.auth.password.PasswordAuthenticator
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestInstance.Lifecycle
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path

@TestInstance(Lifecycle.PER_CLASS)
class SftpTest {

    lateinit var server: SshServer

    @BeforeAll fun initialize() {
        server = SshServer.setUpDefaultServer().also {
            it.port = 22
            it.keyPairProvider = SimpleGeneratorHostKeyProvider()
            it.passwordAuthenticator = PasswordAuthenticator { username, password, session -> true }
            it.start()
        }
    }

    @AfterAll fun shutdown() {
        server.stop()
    }

    @Test fun testPaths() {
        FileSystems.newFileSystem(URI.create("sftp://user@localhost"), emptyMap<String, Any>()).use { fileSystem ->
            val path = fileSystem.getPath("/home/user")
            val parentPath = path.parent
            val rootPath = path.root

            assertEquals("/home/user", path.toString())
            assertEquals("/home", parentPath.toString())
            assertEquals("/", rootPath.toString())

            val relativePath = fileSystem.getPath("user")

            assertEquals("user", relativePath.toString())
            assertTrue(path.startsWith(parentPath))
            assertTrue(path.startsWith(rootPath))
            assertFalse(parentPath.startsWith(path))
            assertFalse(path.startsWith(relativePath))
            assertTrue(path.endsWith(relativePath))
            assertFalse(relativePath.startsWith(path))
        }
    }

    @Test @Disabled fun testListFiles() {
        fun list(path: Path) {
            for (childPath in Files.list(path)) {
                println("${childPath}")
                if (Files.isDirectory(childPath)) {
                    list(childPath)
                }
            }
        }

        FileSystems.newFileSystem(URI.create("sftp://user@localhost"), emptyMap<String, Any>()).use { fileSystem ->
            for (rootPath in fileSystem.rootDirectories) {
                list(rootPath)
            }
        }
    }

}