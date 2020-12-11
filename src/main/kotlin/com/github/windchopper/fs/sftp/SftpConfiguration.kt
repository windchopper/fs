package com.github.windchopper.fs.sftp

import com.github.windchopper.fs.internal.logger
import com.github.windchopper.fs.internal.positive
import java.net.URI
import java.time.Duration
import java.util.logging.Level
import kotlin.reflect.KClass
import kotlin.reflect.full.cast

class SftpConfiguration(uri: URI, environment: Map<String, *> = emptyMap<String, Any>()) {

    internal open class Property<T: Any>(
        val name: String,
        val type: KClass<T>,
        private val stringReader: (String) -> T? = { type.cast(it) },
        private val uriReader: ((URI) -> T?)? = null) {

        companion object {

            val logger = logger<Property<*>>()

        }

        open fun read(environment: Map<String, *>, uri: URI): T? {
            val rawValue = environment[name]

            var value = if (type.isInstance(rawValue)) {
                type.cast(rawValue)
            } else {
                null
            }

            if (value == null && rawValue != null) when (rawValue) {
                is String -> try {
                    value = stringReader.invoke(rawValue)
                } catch (notParsed: Exception) {
                    logger.log(Level.SEVERE, "Couldn't parse \"${name}\" value: ${rawValue}", notParsed)
                }
                else -> {
                    logger.log(Level.SEVERE, "Couldn't admit \"${name}\" value: ${rawValue}")
                }
            }

            if (value == null && uriReader != null) {
                value = uriReader.invoke(uri)
            }

            return value
        }

    }

    internal class DefaultedProperty<T: Any>(
        name: String,
        type: KClass<T>,
        val defaultValue: T,
        stringReader: (String) -> T? = { type.cast(it) },
        uriReader: ((URI) -> T?)? = null)
            : Property<T>(name, type, stringReader, uriReader) {

        override fun read(environment: Map<String, *>, uri: URI): T {
            return super.read(environment, uri)?:defaultValue
        }

    }

    object PropertyNames {

        private const val PREFIX = "Sftp."

        const val HOST = "${PREFIX}host"
        const val PORT = "${PREFIX}port"
        const val USERNAME = "${PREFIX}username"
        const val PASSWORD = "${PREFIX}password"
        const val BUFFER_SIZE = "${PREFIX}bufferSize"
        const val CHANNEL_INACTIVITY_DURATION = "${PREFIX}channelInactivityDuration"

    }

    internal object Properties {

        val host = DefaultedProperty(PropertyNames.HOST, String::class,"localhost",
            uriReader = URI::getHost)

        val port = DefaultedProperty(PropertyNames.PORT, Int::class, 22,
            String::toInt, { it.port.positive() })

        val username = Property(PropertyNames.USERNAME, String::class,
            uriReader = { it.userInfo?.split(":")?.getOrNull(0) })

        val password = Property(PropertyNames.PASSWORD, String::class,
            uriReader = { it.userInfo?.split(":")?.getOrNull(1) })

        val bufferSize = DefaultedProperty(PropertyNames.BUFFER_SIZE, Int::class, 10_000,
            String::toInt)

        val channelInactivityDuration = DefaultedProperty(PropertyNames.CHANNEL_INACTIVITY_DURATION, Duration::class, Duration.ofSeconds(30),
            Duration::parse)

    }

    data class SessionIdentity(val host: String, val port: Int, val username: String?)

    val password = Properties.password.read(environment, uri)
    val sessionIdentity = SessionIdentity(Properties.host.read(environment, uri), Properties.port.read(environment, uri), Properties.username.read(environment, uri))
    val bufferSize = Properties.bufferSize.read(environment, uri)
    val channelInactivityDuration = Properties.channelInactivityDuration.read(environment, uri)

}
