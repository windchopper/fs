@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package com.github.windchopper.fs.sftp

import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory
import org.apache.sshd.server.SshServer
import org.apache.sshd.server.auth.password.PasswordAuthenticator
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.io.TempDir
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path

@TestInstance(Lifecycle.PER_CLASS)
class SftpTest {

    var server: SshServer = SshServer.setUpDefaultServer()

    @BeforeAll fun initialize(@TempDir tempDirPath: Path) {
        listOf("dir_1", "dir_2", "dir_3")
            .map(tempDirPath::resolve)
            .forEach(Files::createDirectory)

        with (server) {
            keyPairProvider = SimpleGeneratorHostKeyProvider()
            subsystemFactories = listOf(SftpSubsystemFactory())
            fileSystemFactory = VirtualFileSystemFactory(tempDirPath)
            passwordAuthenticator = PasswordAuthenticator { username, password, session ->
                username == "user"
            }

            start()
        }
    }

    @AfterAll fun shutdown() {
        server.stop()
    }

    @Test fun testPaths() {
        FileSystems.newFileSystem(URI.create("sftp://user@localhost"), mapOf(SftpConfiguration.PropertyNames.PORT to server.port)).use { fileSystem ->
            val path = fileSystem.getPath("/home/user")

            assertEquals("/home/user", path.toString())
            assertEquals("/home", path.parent.toString())
            assertEquals("/", path.root.toString())

            val relativePath = fileSystem.getPath("user")

            assertEquals("user", relativePath.toString())
            assertTrue(path.startsWith(path.parent))
            assertTrue(path.startsWith(path.root))
            assertFalse(path.parent.startsWith(path))
            assertFalse(path.startsWith(relativePath))
            assertTrue(path.endsWith(relativePath))
            assertFalse(relativePath.startsWith(path))
        }
    }

    @Test fun testListFiles() {
        val files = HashSet<String>()

        fun list(path: Path) {
            files.add(path.toString())
            for (childPath in Files.list(path)) {
                if (Files.isDirectory(childPath)) {
                    list(childPath)
                }
            }
        }

        FileSystems.newFileSystem(URI.create("sftp://user@localhost"), mapOf(SftpConfiguration.PropertyNames.PORT to server.port)).use { fileSystem ->
            for (rootPath in fileSystem.rootDirectories) {
                list(rootPath)
            }
        }

        assertEquals(setOf("/", "/dir_1", "/dir_2", "/dir_3"), files)
    }

}