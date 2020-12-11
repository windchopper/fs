@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package com.github.windchopper.fs.sftp

import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory
import org.apache.sshd.server.SshServer
import org.apache.sshd.server.auth.password.PasswordAuthenticator
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory
import org.junit.jupiter.api.*
import org.junit.jupiter.api.io.TempDir
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path

@TestInstance(TestInstance.Lifecycle.PER_CLASS) class SftpTest {

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

            Assertions.assertEquals("/home/user", path.toString())
            Assertions.assertEquals("/home", path.parent.toString())
            Assertions.assertEquals("/", path.root.toString())
            Assertions.assertEquals("user", relativePath.toString())
            Assertions.assertEquals("/home/user/directory", nonNormalizedPath.normalize().toString())
            Assertions.assertEquals("../directory", nonNormalizedNonAbsolutePath.toString())
            Assertions.assertEquals("/", path.root.toString())
            Assertions.assertNull(relativePath.root)
            Assertions.assertNull(nonNormalizedNonAbsolutePath.root)
            assertThrows<InvalidPathException>(nonNormalizedInvalidPath::normalize)
            Assertions.assertTrue(path.startsWith(path.parent))
            Assertions.assertTrue(path.startsWith(path.root))
            Assertions.assertFalse(path.parent.startsWith(path))
            Assertions.assertFalse(path.startsWith(relativePath))
            Assertions.assertTrue(path.endsWith(relativePath))
            Assertions.assertFalse(relativePath.startsWith(path))

            val path1st = fileSystem.getPath("/dir1/dir2")
            val path2nd = fileSystem.getPath("/dir2/dir3")

            Assertions.assertEquals("../../dir2/dir3", path1st.relativize(path2nd).toString())
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

        Assertions.assertEquals(setOf("/", "/subDir"), files)
    }

    @Test fun testReadFiles() {
        FileSystems.newFileSystem(URI.create("sftp://user@localhost"), mapOf(SftpConfiguration.PropertyNames.PORT to server.port)).use { fileSystem ->
            val textFileContent = Files.readString(fileSystem.getPath("/subDir/textFile.txt"), Charsets.UTF_8)
            Assertions.assertEquals(TEXT_FILE_CONTENT, textFileContent)
        }
    }

}