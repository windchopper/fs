package com.github.windchopper.fs

import com.github.windchopper.fs.sftp.SftpFileSystemProvider
import com.jcraft.jsch.JSchException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.IOException
import java.net.ConnectException
import java.net.URI
import java.nio.file.FileSystems

class IntegrationTest {

    @Test fun testSftpFileSystemPresent() {
        val thrown = assertThrows(IOException::class.java) {
            FileSystems.newFileSystem(URI.create("sftp://localhost"), emptyMap<String, Any>())
        }

        assertTrue(thrown.cause?.javaClass == JSchException::class.java)
        assertTrue(thrown.cause?.cause?.javaClass == ConnectException::class.java)

        assertNotNull(thrown.stackTrace.find { stackTraceElement ->
            stackTraceElement.className.contains("${SftpFileSystemProvider::class.qualifiedName}")
        })
    }

}