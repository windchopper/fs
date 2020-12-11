module windchopper.fs {

    exports com.github.windchopper.fs.sftp;

    opens com.github.windchopper.fs.sftp;

    requires kotlin.stdlib;
    requires kotlin.reflect;
    requires kotlinx.coroutines.core;
    requires java.logging;
    requires jsch;
    requires org.apache.commons.collections4;

    provides java.nio.file.spi.FileSystemProvider
        with com.github.windchopper.fs.sftp.SftpFileSystemProvider;

}