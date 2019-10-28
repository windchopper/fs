package com.windchopper.fs.sftp;

import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SftpFileSystemEnvironment implements SftpConstants {

    private static final Logger logger = Logger.getLogger(SftpFileSystemEnvironment.class.getCanonicalName());

    SftpPeerIdentity peerIdentity;
    String password;

    SftpFileSystemEnvironment(URI uri, Map<String, ?> environment) {
        String host = null;

        if (environment.containsKey(HOST)) {
            host = (String) environment.get(HOST);
        }

        if (host == null && uri.getHost() != null) {
            host = uri.getHost();
        }

        if (host == null) {
            host = "localhost";
        }

        int port = -1;

        if (environment.containsKey(PORT)) {
            Object portFromEnvironment = environment.get(PORT);

            if (portFromEnvironment instanceof Number) {
                port = ((Number) portFromEnvironment).intValue();
            }

            if (port < 0 && portFromEnvironment instanceof String) {
                try {
                    port = Integer.parseInt((String) portFromEnvironment);
                } catch (NumberFormatException thrown) {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.log(Level.FINE, thrown.getMessage());
                    }
                }
            }

            if (port < 0 && portFromEnvironment != null) {
                throw new IllegalArgumentException(
                    String.format("Couldn't accept \"%s\" value: %s", PORT, portFromEnvironment));
            }
        }

        if (port < 0) {
            port = uri.getPort();
        }

        if (port < 0) {
            port = STANDARD_PORT;
        }

        String username = (String) environment.get(USERNAME);
        password = (String) environment.get(PASSWORD);

        if (username == null || password == null) {
            String userInfo = uri.getUserInfo();

            if (StringUtils.isNotBlank(userInfo)) {
                String[] userInfoParts = userInfo.split("[:]");

                if (userInfoParts.length > 0 && username == null) {
                    username = userInfoParts[0];
                }

                if (userInfoParts.length > 1 && password == null) {
                    password = userInfoParts[1];
                }
            }
        }

        peerIdentity = new SftpPeerIdentity(host, port, username);
    }

}
