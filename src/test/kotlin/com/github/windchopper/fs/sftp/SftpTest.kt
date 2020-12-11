@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package com.github.windchopper.fs.sftp

import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory
import org.apache.sshd.server.SshServer
import org.apache.sshd.server.auth.password.PasswordAuthenticator
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.io.TempDir
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path

@TestInstance(Lifecycle.PER_CLASS)
class SftpTest {

    companion object {
        const val TEXT_FILE_CONTENT = "Some short text here."
    }

    var server: SshServer = SshServer.setUpDefaultServer()

    @BeforeAll fun initialize(@TempDir tempDirPath: Path) {
        Files.write(Files.createDirectory(tempDirPath.resolve("subDir")).resolve("textFile.txt"), TEXT_FILE_CONTENT.toByteArray(Charsets.UTF_8))

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
            val relativePath = fileSystem.getPath("user")
            val nonNormalizedPath = fileSystem.getPath("/home/../home/user/./directory")
            val nonNormalizedInvalidPath = fileSystem.getPath("/../home/user")
            val nonNormalizedNonAbsolutePath = fileSystem.getPath("../directory")

            assertEquals("/home/user", path.toString())
            assertEquals("/home", path.parent.toString())
            assertEquals("/", path.root.toString())
            assertEquals("user", relativePath.toString())
            assertEquals("/home/user/directory", nonNormalizedPath.normalize().toString())
            assertEquals("../directory", nonNormalizedNonAbsolutePath.toString())
            assertEquals("/", path.root.toString())
            assertNull(relativePath.root)
            assertNull(nonNormalizedNonAbsolutePath.root)
            assertThrows<InvalidPathException>(nonNormalizedInvalidPath::normalize)
            assertTrue(path.startsWith(path.parent))
            assertTrue(path.startsWith(path.root))
            assertFalse(path.parent.startsWith(path))
            assertFalse(path.startsWith(relativePath))
            assertTrue(path.endsWith(relativePath))
            assertFalse(relativePath.startsWith(path))

            val path1st = fileSystem.getPath("/dir1/dir2")
            val path2nd = fileSystem.getPath("/dir2/dir3")

            assertEquals("../../dir2/dir3", path1st.relativize(path2nd).toString())
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

        assertEquals(setOf("/", "/subDir"), files)
    }

    @Test fun testReadFiles() {
        FileSystems.newFileSystem(URI.create("sftp://user@localhost"), mapOf(SftpConfiguration.PropertyNames.PORT to server.port)).use { fileSystem ->
            val textFileContent = Files.readString(fileSystem.getPath("/subDir/textFile.txt"), Charsets.UTF_8)
            assertEquals(TEXT_FILE_CONTENT, textFileContent)
        }
    }

}