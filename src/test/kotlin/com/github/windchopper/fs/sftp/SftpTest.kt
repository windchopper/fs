@file:Suppress("UNUSED_ANONYMOUS_PARAMETER", "NestedLambdaShadowedImplicitParameter")

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

    lateinit var server: SshServer

    @BeforeAll fun initialize(@TempDir tempDirPath: Path) {
        listOf("dir_1", "dir_2", "dir_3")
            .map(tempDirPath::resolve)
            .forEach(Files::createDirectory)

        server = SshServer.setUpDefaultServer()

        with (server) {
            port = 22
            keyPairProvider = SimpleGeneratorHostKeyProvider()
            passwordAuthenticator = PasswordAuthenticator { username, password, session -> true }
            subsystemFactories = listOf(SftpSubsystemFactory())
            fileSystemFactory = VirtualFileSystemFactory().also { fileSystemFactory ->
                fileSystemFactory.defaultHomeDir = tempDirPath
            }

            start()
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

        FileSystems.newFileSystem(URI.create("sftp://user@localhost"), emptyMap<String, Any>()).use { fileSystem ->
            for (rootPath in fileSystem.rootDirectories) {
                list(rootPath)
            }
        }

        assertEquals(setOf("/", "/dir_1", "/dir_2", "/dir_3"), files)
    }

}