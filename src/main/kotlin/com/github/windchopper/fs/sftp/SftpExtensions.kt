package com.github.windchopper.fs.sftp

import java.nio.file.Path
import java.nio.file.ProviderMismatchException

fun Path.toSftpPath(): SftpPath = this as? SftpPath
    ?:throw ProviderMismatchException("Path ${javaClass.canonicalName} is not belonging to ${SftpFileSystemProvider::class.simpleName}")
