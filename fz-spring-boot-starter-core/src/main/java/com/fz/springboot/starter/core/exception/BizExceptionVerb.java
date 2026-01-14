package com.fz.springboot.starter.core.exception;

import org.fz.erwin.exception.Throws;

import java.util.Objects;
import java.util.function.Supplier;

import static cn.hutool.core.text.CharSequenceUtil.format;
import static cn.hutool.core.text.CharSequenceUtil.toCamelCase;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/22 20:24
 */


public enum BizExceptionVerb {

    NOT_AUTHENTICATED,
    ACCESS_DENIED,
    INVALID_INPUT,
    MISSING_REQUIRED_INPUT,
    ILLEGAL_STATE,
    UNSUPPORTED_OPERATION,
    RESOURCE_NOT_FOUND,
    DATA_CONFLICT,
    DUPLICATE_ENTRY,
    OPERATION_TIMEOUT,
    INTERNAL_ERROR,
    SERVICE_UNAVAILABLE,
    UNKNOWN,
    ;;

    public <T> Supplier<? extends BizException> on(T subject) {
        Throws.ifNull(subject, () -> "subject can not be null");
        return on(subject.getClass());
    }

    public <T> Supplier<? extends BizException> on(T subject, Throwable cause) {
        Throws.ifNull(subject, () -> "subject can not be null");
        return on(subject.getClass(), cause);
    }

    public Supplier<? extends BizException> on(Class<?> subjectType) {
        return on(subjectType, null);
    }

    public Supplier<? extends BizException> on(Class<?> subjectType, Throwable cause) {
        Objects.requireNonNull(subjectType, "subjectType can not be null");
        return () -> new BizException(format("[{}] {}", subjectType.getSimpleName(), toCamelCase(this.name())), cause);
    }

    /**
     * @author fengbinbin
     * @version 1.0
     * @since 2025/8/22 20:24
     */
    public static class BizException extends RuntimeException {

        public BizException(Object message) {
            super(message.toString());
        }

        public BizException(Object message, Throwable cause) {
            super(message.toString(), cause);
        }

    }
}
