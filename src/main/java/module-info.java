module windchopper.fs {

    exports com.github.windchopper.fs;
    exports com.github.windchopper.fs.sftp;

    opens com.github.windchopper.fs;
    opens com.github.windchopper.fs.sftp;

    requires java.logging;
    requires jsch;
    requires org.apache.commons.lang3;
    requires org.apache.commons.collections4;
    requires windchopper.common.util;

    provides java.nio.file.spi.FileSystemProvider
        with com.github.windchopper.fs.sftp.SftpFileSystemProvider;

}