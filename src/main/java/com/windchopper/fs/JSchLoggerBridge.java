package com.windchopper.fs;

import java.util.logging.Level;
import java.util.logging.Logger;

public class JSchLoggerBridge implements com.jcraft.jsch.Logger {

    private final Logger logger;

    public JSchLoggerBridge(Logger logger) {
        this.logger = logger;
    }

    @Override public boolean isEnabled(int level) {
        return logger.isLoggable(translateLevel(level));
    }

    @Override public void log(int level, String message) {
        logger.log(translateLevel(level), message);
    }

    public static Level translateLevel(int level) {
        switch (level) {
            case DEBUG:
                return Level.FINE;

            case INFO:
                return Level.INFO;

            case WARN:
                return Level.WARNING;

            case ERROR:
            case FATAL:
                return Level.SEVERE;

            default:
                return Level.OFF;
        }
    }

}
