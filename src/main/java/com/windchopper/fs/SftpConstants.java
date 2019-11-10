package com.windchopper.fs;

import java.time.Duration;

public interface SftpConstants {

    String HOST = "Sftp.host";
    String PORT = "Sftp.port";
    String USERNAME = "Sftp.username";
    String PASSWORD = "Sftp.password";
    String BUFFER_SIZE = "Sftp.bufferSize";
    String BUFFER_LIFETIME = "Sftp.bufferLifetime";

    int DEFAULT_PORT = 22;
    int DEFAULT_BUFFER_SIZE = 1000;
    Duration DEFAULT_BUFFER_LIFETIME = Duration.ofMinutes(10);

    String SCHEME = "sftp";
    String SEPARATOR = "/";

}
