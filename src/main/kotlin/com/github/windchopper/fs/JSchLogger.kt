package com.github.windchopper.fs

import com.jcraft.jsch.Logger.*
import org.slf4j.Logger

class JSchLogger(private val logger: Logger): com.jcraft.jsch.Logger {

    override fun isEnabled(level: Int): Boolean {
        return when (level) {
            DEBUG -> logger.isDebugEnabled
            INFO -> logger.isInfoEnabled
            WARN -> logger.isWarnEnabled
            ERROR, FATAL -> logger.isErrorEnabled
            else -> false
        }
    }

    override fun log(level: Int, message: String) {
        when (level) {
            DEBUG -> logger.debug(message)
            INFO -> logger.info(message)
            WARN -> logger.warn(message)
            ERROR, FATAL -> logger.error(message)
        }
    }

}