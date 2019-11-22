module windchopper.fs {

    exports com.github.windchopper.fs;
    exports com.github.windchopper.fs.sftp;

    opens com.github.windchopper.fs;
    opens com.github.windchopper.fs.sftp;

    requires java.logging;
    requires kotlin.stdlib.jdk8;
    requires kotlin.stdlib;
    requires kotlin.reflect;
    requires jsch;
    requires org.apache.commons.lang3;
    requires org.apache.commons.collections4;

    provides java.nio.file.spi.FileSystemProvider
        with com.github.windchopper.fs.sftp.SftpFileSystemProvider;

}