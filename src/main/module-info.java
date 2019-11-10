module windchopper.fs {

    exports com.windchopper.fs;
    exports com.windchopper.fs.sftp;

    opens com.windchopper.fs;
    opens com.windchopper.fs.sftp;

    requires jsch;
    requires windchopper.common.util;

    provides java.nio.file.spi.FileSystemProvider
        with com.windchopper.fs.sftp.SftpFileSystemProvider;

}