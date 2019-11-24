package com.github.windchopper.fs.sftp

import java.net.URI
import java.time.Duration
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.reflect.KClass
import kotlin.reflect.full.cast

class SftpConfiguration(uri: URI, environment: Map<String, *> = emptyMap<String, Any>()) {

    data class SessionIdentity(val host: String, val port: Int, val username: String?)

    val sessionIdentity: SessionIdentity
    val password: String?
    val bufferSize: Int
    val channelInactivityDuration: Duration

    init {
        val host = valueFromEnvironment(environment, SftpConstants.HOST, String::class)?:uri.host.trimToNull()?:SftpConstants.DEFAULT_HOST
        val port = valueFromEnvironment(environment, SftpConstants.PORT, Int::class, Integer::parseUnsignedInt)?:uri.port.nullWhenNotPositive()?:SftpConstants.DEFAULT_PORT
        val userInfoParts = uri.userInfo?.trimToNull()?.split(":")
        val username = environment[SftpConstants.USERNAME]?.toString()?:userInfoParts?.getOrNull(0)
        password = environment[SftpConstants.PASSWORD]?.toString()?:userInfoParts?.getOrNull(1)
        sessionIdentity = SessionIdentity(host, port, username)
        bufferSize = valueFromEnvironment(environment, SftpConstants.BUFFER_SIZE, Int::class, Integer::parseUnsignedInt)?:SftpConstants.DEFAULT_BUFFER_SIZE
        channelInactivityDuration = valueFromEnvironment(environment, SftpConstants.CHANNEL_INACTIVITY_DURATION, Duration::class, Duration::parse)?:Duration.parse(SftpConstants.DEFAULT_CHANNEL_INACTIVITY_DURATION)
    }

    fun <T: Any> valueFromEnvironment(environment: Map<String, *>, key: String?, type: KClass<T>, converter: ((String) -> T)? = null): T? {
        var value: T? = null
        val rawValue = environment[key]

        if (type.isInstance(rawValue)) {
            value = type.cast(rawValue)
        }

        converter?.let {
            if (value == null && rawValue is String && rawValue.isNotBlank()) {
                try {
                    value = converter(rawValue)
                } catch (thrown: Exception) {
                    with (Logger.getLogger(javaClass.canonicalName)) {
                        if (isLoggable(Level.FINE)) log(Level.FINE, thrown.message)
                    }
                }
            }
        }

        if (value == null && rawValue != null && !(rawValue is String && rawValue.isBlank())) {
            throw IllegalArgumentException("Couldn't accept \"${key}\" value (${rawValue}) as ${type.qualifiedName}")
        }

        return value
    }

}