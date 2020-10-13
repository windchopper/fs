package com.github.windchopper.fs.sftp

import com.github.windchopper.fs.JSchHelper
import com.github.windchopper.fs.wrapExceptionTo
import com.jcraft.jsch.Channel
import java.io.IOException
import java.nio.file.Path
import java.nio.file.ProviderMismatchException

fun <T, C: Channel> JSchHelper<C>.performWithinChannel(action: (channel: C) -> T): T = wrapExceptionTo(::IOException) {
    val channel = connect()

    try {
        action(channel)
    } finally {
        disconnect()
    }
}

fun Path.toSftpPath(): SftpPath = this as? SftpPath
    ?:throw ProviderMismatchException("Path ${javaClass.canonicalName} is not belonging to ${SftpFileSystemProvider::class.simpleName}")
