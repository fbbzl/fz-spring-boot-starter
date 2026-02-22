package com.fz.starter.core.util;

import org.slf4j.Logger;
import org.slf4j.Marker;

import java.util.function.Supplier;

/**
 * avoid log performance loss. OFF > FATAL > ERROR > WARN > INFO > DEBUG > TRACE > ALL
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021/3/2 11:05
 */
public final class Logs {

    private Logs() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static void error(Logger logger, Supplier<String> msg) {
        if (logger.isErrorEnabled()) {
            logger.error(msg.get());
        }
    }

    public static void error(Logger logger, Supplier<String> replacementMsg, Object object) {
        if (logger.isErrorEnabled()) {
            logger.error(replacementMsg.get(), object);
        }
    }

    public static void error(Logger logger, Supplier<String> replacementMsg, Object... objects) {
        if (logger.isErrorEnabled()) {
            logger.error(replacementMsg.get(), objects);
        }
    }

    public static void error(Logger logger, Supplier<String> replacementMsg, Object object1, Object object2) {
        if (logger.isErrorEnabled()) {
            logger.error(replacementMsg.get(), object1, object2);
        }
    }

    public static void error(Logger logger, Supplier<String> msg, Throwable throwable) {
        if (logger.isErrorEnabled()) {
            logger.error(msg.get(), throwable);
        }
    }

    public static void error(Logger logger, Marker marker, Supplier<String> msg) {
        if (logger.isErrorEnabled()) {
            logger.error(marker, msg.get());
        }
    }

    public static void error(Logger logger, Marker marker, Supplier<String> msg, Throwable throwable) {
        if (logger.isErrorEnabled()) {
            logger.error(marker, msg.get(), throwable);
        }
    }

    public static void error(Logger logger, Marker marker, Supplier<String> msg, Object object) {
        if (logger.isErrorEnabled()) {
            logger.error(marker, msg.get(), object);
        }
    }

    public static void error(Logger logger, Marker marker, Supplier<String> msg, Object... objects) {
        if (logger.isErrorEnabled()) {
            logger.error(marker, msg.get(), objects);
        }
    }

    public static void error(Logger logger, Marker marker, Supplier<String> msg, Object object1, Object object2) {
        if (logger.isErrorEnabled()) {
            logger.error(marker, msg.get(), object1, object2);
        }
    }

    public static void warn(Logger logger, Supplier<String> msg) {
        if (logger.isWarnEnabled()) {
            logger.warn(msg.get());
        }
    }

    public static void warn(Logger logger, Supplier<String> replacementMsg, Object object) {
        if (logger.isWarnEnabled()) {
            logger.warn(replacementMsg.get(), object);
        }
    }

    public static void warn(Logger logger, Supplier<String> replacementMsg, Object... objects) {
        if (logger.isWarnEnabled()) {
            logger.warn(replacementMsg.get(), objects);
        }
    }

    public static void warn(Logger logger, Supplier<String> replacementMsg, Object object1, Object object2) {
        if (logger.isWarnEnabled()) {
            logger.warn(replacementMsg.get(), object1, object2);
        }
    }

    public static void warn(Logger logger, Supplier<String> msg, Throwable throwable) {
        if (logger.isWarnEnabled()) {
            logger.warn(msg.get(), throwable);
        }
    }

    public static void warn(Logger logger, Marker marker, Supplier<String> msg) {
        if (logger.isWarnEnabled()) {
            logger.warn(marker, msg.get());
        }
    }

    public static void warn(Logger logger, Marker marker, Supplier<String> msg, Throwable throwable) {
        if (logger.isWarnEnabled()) {
            logger.warn(marker, msg.get(), throwable);
        }
    }

    public static void warn(Logger logger, Marker marker, Supplier<String> msg, Object object) {
        if (logger.isWarnEnabled()) {
            logger.warn(marker, msg.get(), object);
        }
    }

    public static void warn(Logger logger, Marker marker, Supplier<String> msg, Object... objects) {
        if (logger.isWarnEnabled()) {
            logger.warn(marker, msg.get(), objects);
        }
    }

    public static void warn(Logger logger, Marker marker, Supplier<String> msg, Object object1, Object object2) {
        if (logger.isWarnEnabled()) {
            logger.warn(marker, msg.get(), object1, object2);
        }
    }

    public static void info(Logger logger, Supplier<String> msg) {
        if (logger.isInfoEnabled()) {
            logger.info(msg.get());
        }
    }

    public static void info(Logger logger, Supplier<String> replacementMsg, Object object) {
        if (logger.isInfoEnabled()) {
            logger.info(replacementMsg.get(), object);
        }
    }

    public static void info(Logger logger, Supplier<String> replacementMsg, Object... objects) {
        if (logger.isInfoEnabled()) {
            logger.info(replacementMsg.get(), objects);
        }
    }

    public static void info(Logger logger, Supplier<String> replacementMsg, Object object1, Object object2) {
        if (logger.isInfoEnabled()) {
            logger.info(replacementMsg.get(), object1, object2);
        }
    }

    public static void info(Logger logger, Supplier<String> msg, Throwable throwable) {
        if (logger.isInfoEnabled()) {
            logger.info(msg.get(), throwable);
        }
    }

    public static void info(Logger logger, Marker marker, Supplier<String> msg) {
        if (logger.isInfoEnabled()) {
            logger.info(marker, msg.get());
        }
    }

    public static void info(Logger logger, Marker marker, Supplier<String> msg, Throwable throwable) {
        if (logger.isInfoEnabled()) {
            logger.info(marker, msg.get(), throwable);
        }
    }

    public static void info(Logger logger, Marker marker, Supplier<String> msg, Object object) {
        if (logger.isInfoEnabled()) {
            logger.info(marker, msg.get(), object);
        }
    }

    public static void info(Logger logger, Marker marker, Supplier<String> msg, Object... objects) {
        if (logger.isInfoEnabled()) {
            logger.info(marker, msg.get(), objects);
        }
    }

    public static void info(Logger logger, Marker marker, Supplier<String> msg, Object object1, Object object2) {
        if (logger.isInfoEnabled()) {
            logger.info(marker, msg.get(), object1, object2);
        }
    }

    public static void debug(Logger logger, Supplier<String> msg) {
        if (logger.isDebugEnabled()) {
            logger.debug(msg.get());
        }
    }

    public static void debug(Logger logger, Supplier<String> replacementMsg, Object object) {
        if (logger.isDebugEnabled()) {
            logger.debug(replacementMsg.get(), object);
        }
    }

    public static void debug(Logger logger, Supplier<String> replacementMsg, Object... objects) {
        if (logger.isDebugEnabled()) {
            logger.debug(replacementMsg.get(), objects);
        }
    }

    public static void debug(Logger logger, Supplier<String> replacementMsg, Object object1, Object object2) {
        if (logger.isDebugEnabled()) {
            logger.debug(replacementMsg.get(), object1, object2);
        }
    }

    public static void debug(Logger logger, Supplier<String> msg, Throwable throwable) {
        if (logger.isDebugEnabled()) {
            logger.debug(msg.get(), throwable);
        }
    }

    public static void debug(Logger logger, Marker marker, Supplier<String> msg) {
        if (logger.isDebugEnabled()) {
            logger.debug(marker, msg.get());
        }
    }

    public static void debug(Logger logger, Marker marker, Supplier<String> msg, Throwable throwable) {
        if (logger.isDebugEnabled()) {
            logger.debug(marker, msg.get(), throwable);
        }
    }

    public static void debug(Logger logger, Marker marker, Supplier<String> msg, Object object) {
        if (logger.isDebugEnabled()) {
            logger.debug(marker, msg.get(), object);
        }
    }

    public static void debug(Logger logger, Marker marker, Supplier<String> msg, Object... objects) {
        if (logger.isDebugEnabled()) {
            logger.debug(marker, msg.get(), objects);
        }
    }

    public static void debug(Logger logger, Marker marker, Supplier<String> msg, Object object1, Object object2) {
        if (logger.isDebugEnabled()) {
            logger.debug(marker, msg.get(), object1, object2);
        }
    }

    public static void trace(Logger logger, Supplier<String> msg) {
        if (logger.isTraceEnabled()) {
            logger.trace(msg.get());
        }
    }

    public static void trace(Logger logger, Supplier<String> replacementMsg, Object object) {
        if (logger.isTraceEnabled()) {
            logger.trace(replacementMsg.get(), object);
        }
    }

    public static void trace(Logger logger, Supplier<String> replacementMsg, Object... objects) {
        if (logger.isTraceEnabled()) {
            logger.trace(replacementMsg.get(), objects);
        }
    }

    public static void trace(Logger logger, Supplier<String> replacementMsg, Object object1, Object object2) {
        if (logger.isTraceEnabled()) {
            logger.trace(replacementMsg.get(), object1, object2);
        }
    }

    public static void trace(Logger logger, Supplier<String> msg, Throwable throwable) {
        if (logger.isTraceEnabled()) {
            logger.trace(msg.get(), throwable);
        }
    }

    public static void trace(Logger logger, Marker marker, Supplier<String> msg) {
        if (logger.isTraceEnabled()) {
            logger.trace(marker, msg.get());
        }
    }

    public static void trace(Logger logger, Marker marker, Supplier<String> msg, Throwable throwable) {
        if (logger.isTraceEnabled()) {
            logger.trace(marker, msg.get(), throwable);
        }
    }

    public static void trace(Logger logger, Marker marker, Supplier<String> msg, Object object) {
        if (logger.isTraceEnabled()) {
            logger.trace(marker, msg.get(), object);
        }
    }

    public static void trace(Logger logger, Marker marker, Supplier<String> msg, Object... objects) {
        if (logger.isTraceEnabled()) {
            logger.trace(marker, msg.get(), objects);
        }
    }

    public static void trace(Logger logger, Marker marker, Supplier<String> msg, Object object1, Object object2) {
        if (logger.isTraceEnabled()) {
            logger.trace(marker, msg.get(), object1, object2);
        }
    }

}
