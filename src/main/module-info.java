module windchopper.fs.sftp {

    exports com.windchopper.fs.secureFileTransferProtocol;

    opens com.windchopper.fs.secureFileTransferProtocol;

    requires jsch;
    requires windchopper.common.util;

    provides java.nio.file.spi.FileSystemProvider
        with com.windchopper.fs.secureFileTransferProtocol.SftpFileSystemProvider;

}