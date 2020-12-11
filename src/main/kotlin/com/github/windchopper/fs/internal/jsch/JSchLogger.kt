package com.github.windchopper.fs.internal.jsch

import com.jcraft.jsch.Logger.*
import java.util.logging.Level
import java.util.logging.Logger

class JSchLogger(private val logger: Logger): com.jcraft.jsch.Logger {

    override fun isEnabled(level: Int): Boolean {
        return when (level) {
            DEBUG -> logger.isLoggable(Level.FINE)
            INFO -> logger.isLoggable(Level.INFO)
            WARN -> logger.isLoggable(Level.WARNING)
            ERROR, FATAL -> logger.isLoggable(Level.SEVERE)
            else -> false
        }
    }

    override fun log(level: Int, message: String) {
        when (level) {
            DEBUG -> logger.fine(message)
            INFO -> logger.info(message)
            WARN -> logger.warning(message)
            ERROR, FATAL -> logger.severe(message)
        }
    }

}