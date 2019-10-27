module windchopper.fs.sftp {

    exports com.windchopper.fs.sftp;

    opens com.windchopper.fs.sftp;

    requires jsch;

    provides java.nio.file.spi.FileSystemProvider
        with com.windchopper.fs.sftp.SftpFileSystemProvider;

}