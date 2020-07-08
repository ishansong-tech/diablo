package com.ishansong.diablo.core.utils;

import org.slf4j.Logger;

import java.util.Objects;
import java.util.function.Supplier;

public final class LogUtils {

    private static final LogUtils LOG_UTIL = new LogUtils();

    private LogUtils() {

    }

    public static LogUtils getInstance() {
        return LOG_UTIL;
    }

    public static void debug(final Logger logger, final String format, final Supplier<Object> supplier) {
        if (logger.isDebugEnabled()) {
            logger.debug(format, supplier.get());
        }
    }

    public static void debug(final Logger logger, final Supplier<Object> supplier) {
        if (logger.isDebugEnabled()) {
            logger.debug(Objects.toString(supplier.get()));
        }
    }

    public static void info(final Logger logger, final String format, final Supplier<Object> supplier) {
        if (logger.isInfoEnabled()) {
            logger.info(format, supplier.get());
        }
    }

    public static void info(final Logger logger, final Supplier<Object> supplier) {
        if (logger.isInfoEnabled()) {
            logger.info(Objects.toString(supplier.get()));
        }
    }

    public static void info(final Logger logger, final Supplier<Object> supplier, Object... arguments) {
        if (logger.isInfoEnabled()) {
            logger.info(Objects.toString(supplier.get()), arguments);
        }
    }

    public static void error(final Logger logger, final String format, final Supplier<Object> supplier) {
        if (logger.isErrorEnabled()) {
            logger.error(format, supplier.get());
        }
    }

    public static void error(final Logger logger, final Supplier<Object> supplier, Object... arguments) {
        if (logger.isErrorEnabled()) {
            logger.error(Objects.toString(supplier.get()), arguments);
        }
    }

    public static void error(final Logger logger, final Supplier<Object> supplier) {
        if (logger.isErrorEnabled()) {
            logger.error(Objects.toString(supplier.get()));
        }
    }

    public static void warn(final Logger logger, final String format, final Supplier<Object> supplier) {
        if (logger.isWarnEnabled()) {
            logger.warn(format, supplier.get());
        }
    }

    public static void warn(final Logger logger, final Supplier<Object> supplier) {
        if (logger.isWarnEnabled()) {
            logger.warn(Objects.toString(supplier.get()));
        }
    }

    public static void warn(final Logger logger, final Supplier<Object> supplier, Object... arguments) {
        if (logger.isWarnEnabled()) {
            logger.warn(Objects.toString(supplier), arguments);
        }
    }
}
