package com.github.windchopper.fs.sftp

import com.jcraft.jsch.SftpATTRS

class SftpFile(val path: String, val name: String, val attributes: SftpATTRS) {

    fun toAbsolutePath(): String {
        return (if (path == SftpFileSystem.PATH_SEPARATOR) "" else path) + SftpFileSystem.PATH_SEPARATOR + name
    }

    fun toPath(fileSystem: SftpFileSystem, sessionIdentity: SftpConfiguration.SessionIdentity): SftpPath {
        return SftpPath(fileSystem, sessionIdentity, path, name)
    }

    fun toFileAttributes(): SftpFileAttributes {
        return SftpFileAttributes(attributes)
    }

}