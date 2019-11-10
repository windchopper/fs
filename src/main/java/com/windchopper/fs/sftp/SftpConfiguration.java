package com.windchopper.fs.sftp;

import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.function.Function.identity;

public class SftpConfiguration implements SftpConstants {

    SftpSessionIdentity sessionIdentity;
    String password;

    int bufferSize;

    SftpConfiguration(URI uri, Map<String, ?> environment) {
        String host = Optional.ofNullable(valueFromEnvironment(environment, HOST, String.class, identity()))
            .or(() -> Optional.ofNullable(uri.getHost())
                .filter(StringUtils::isNotBlank))
            .orElse("localhost");

        int port = Optional.ofNullable(valueFromEnvironment(environment, PORT, Integer.class, Integer::parseUnsignedInt))
            .or(() -> Optional.of(uri.getPort())
                .filter(value -> value > 0))
            .orElse(DEFAULT_PORT);

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

        sessionIdentity = new SftpSessionIdentity(host, port, username);

        bufferSize = Optional.ofNullable(valueFromEnvironment(environment, BUFFER_SIZE, Integer.class, Integer::parseUnsignedInt))
            .orElse(DEFAULT_BUFFER_SIZE);
    }

    <T> T valueFromEnvironment(Map<String, ?> environment, String key, Class<T> type, Function<String, T> converter) {
        T value = null;
        Object rawValue = environment.get(key);

        if (type.isInstance(rawValue)) {
            value =  type.cast(rawValue);
        }

        if (value == null && rawValue instanceof String && StringUtils.isNotBlank((String) rawValue)) {
            try {
                value = converter.apply((String) rawValue);
            } catch (Exception thrown) {
                Logger logger = Logger.getLogger(getClass().getCanonicalName());

                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, thrown.getMessage());
                }
            }
        }

        if (value == null && rawValue != null && !(rawValue instanceof String && StringUtils.isBlank((String) rawValue))) {
            throw new IllegalArgumentException(
                String.format("Couldn't accept \"%s\" value (%s) as %s", key, rawValue, type.getCanonicalName()));
        }

        return value;
    }

}
