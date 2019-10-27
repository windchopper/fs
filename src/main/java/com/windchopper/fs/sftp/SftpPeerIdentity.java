package com.windchopper.fs.sftp;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class SftpPeerIdentity {

    final String host;
    final int port;
    final String username;

    public SftpPeerIdentity(String host, int port, String username) {
        this.host = host;
        this.port = port;
        this.username = username;
    }

    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public SftpPeerIdentity clone() {
        return new SftpPeerIdentity(host, port, username);
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

        SftpPeerIdentity that = (SftpPeerIdentity) object;

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
