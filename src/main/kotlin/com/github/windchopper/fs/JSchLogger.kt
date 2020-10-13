package com.github.windchopper.fs

import java.util.logging.Level
import java.util.logging.Logger

class JSchLogger(private val logger: Logger): com.jcraft.jsch.Logger {

    override fun isEnabled(level: Int): Boolean {
        return logger.isLoggable(translateLevel(level))
    }

    override fun log(level: Int, message: String) {
        logger.log(translateLevel(level), message)
    }

    companion object {
        fun translateLevel(level: Int): Level {
            return when (level) {
                com.jcraft.jsch.Logger.DEBUG -> Level.FINE
                com.jcraft.jsch.Logger.INFO -> Level.INFO
                com.jcraft.jsch.Logger.WARN -> Level.WARNING
                com.jcraft.jsch.Logger.ERROR, com.jcraft.jsch.Logger.FATAL -> Level.SEVERE
                else -> Level.OFF
            }
        }
    }

}