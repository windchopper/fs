package com.github.windchopper.fs.sftp

import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.Session
import java.io.IOException
import java.nio.file.Path
import java.nio.file.ProviderMismatchException

interface SftpRoutines {

    fun checkPathAndAccept(path: Path, consumer: (SftpPath) -> Unit) {
        if (path is SftpPath) consumer(path)
        else throw foreignPath(path)
    }

    fun <T> checkPathAndApply(path: Path, function: (SftpPath) -> T): T {
        if (path is SftpPath) return function(path)
        else throw foreignPath(path)
    }

    fun <T> checkPathAndApply(path1st: Path, path2nd: Path, function: (SftpPath, SftpPath) -> T): T {
        if (path1st is SftpPath && path2nd is SftpPath) return function(path1st, path2nd)
        else throw foreignPath(path1st, path2nd)
    }

    fun foreignPath(vararg paths: Path): ProviderMismatchException {
        return ProviderMismatchException(paths
            .map { it.javaClass }
            .filter { it != SftpPath::class.java }
            .map { it.canonicalName }
            .joinToString(", ", "Path of type(s) ", " is not belonging to used provider"))
    }

    fun watchNotSupported(): UnsupportedOperationException {
        throw UnsupportedOperationException("Couldn't watch remote file system")
    }

    fun attributesNotSupported(type: Class<*>): UnsupportedOperationException {
        throw UnsupportedOperationException("Attributes of type ${type.canonicalName} not supported")
    }

    @Throws(IOException::class) fun <T> wrapNotIOException(expression: () -> T): T {
        return try {
            expression.invoke()
        } catch (thrown: Exception) {
            when (thrown) {
                is IOException -> throw thrown
                else -> throw IOException(thrown)
            }
        }
    }

    @Throws(IOException::class) fun <T> doWithChannel(session: Session, action: (ChannelSftp) -> T): T {
        return wrapNotIOException {
            val channel = session.openChannel("sftp") as ChannelSftp

            channel.connect()

            try {
                action(channel)
            } finally {
                channel.disconnect()
            }
        }
    }

}