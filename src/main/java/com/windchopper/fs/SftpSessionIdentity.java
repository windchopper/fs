package com.windchopper.fs;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.net.URI;
import java.net.URISyntaxException;

public class SftpSessionIdentity {

    final String host;
    final int port;
    final String username;

    public SftpSessionIdentity(String host, int port, String username) {
        this.host = host;
        this.port = port;
        this.username = username;
    }

    public URI composeUri(String path) {
        try {
            return new URI(SftpConfiguration.SCHEME, username, host, port, path, null, null);
        } catch (URISyntaxException thrown) {
            throw new IllegalArgumentException(
                String.format("Invalid path: %s", path));
        }
    }

    public Session createJschSession(JSch jsch) throws JSchException {
        return jsch.getSession(username, host, port);
    }

    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public SftpSessionIdentity clone() {
        return new SftpSessionIdentity(host, port, username);
    }

    @Override public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(host)
            .append(port)
            .append(username)
            .toHashCode();
    }

    @Override public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        SftpSessionIdentity that = (SftpSessionIdentity) object;

        return new EqualsBuilder()
            .append(port, that.port)
            .append(host, that.host)
            .append(username, that.username)
            .isEquals();
    }

    @Override public String toString() {
        return new ToStringBuilder(this)
            .append("host", host)
            .append("port", port)
            .append("username", username)
            .toString();
    }

}
