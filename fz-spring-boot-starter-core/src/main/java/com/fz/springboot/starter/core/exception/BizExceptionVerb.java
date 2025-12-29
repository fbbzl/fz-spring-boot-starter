package com.fz.springboot.starter.core.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.fz.erwin.exception.Throws;

import java.util.function.Supplier;

import static cn.hutool.core.text.CharSequenceUtil.format;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/22 20:24
 */

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum BizExceptionVerb {

    NOT_AUTHENTICATED("000000", "Not authenticated"),
    ACCESS_DENIED("000001", "Access denied"),

    INVALID_INPUT("000002", "Invalid input"),
    MISSING_REQUIRED_INPUT("000003", "Missing required input"),
    ILLEGAL_STATE("000004", "Illegal state"),
    UNSUPPORTED_OPERATION("000005", "Unsupported operation"),
    RESOURCE_NOT_FOUND("000006", "Resource not found"),
    DATA_CONFLICT("000007", "Data conflict"),
    DUPLICATE_ENTRY("000008", "Duplicate entry"),
    OPERATION_TIMEOUT("000009", "Operation timeout"),

    INTERNAL_ERROR("000010", "Internal error"),
    SERVICE_UNAVAILABLE("000011", "Service unavailable"),

    UNKNOWN("999999", "Unknown error");
    ;

    /**
     * exception code
     */
    String code;

    /**
     * exception message
     */
    String words;

    public <T> Supplier<? extends BizException> on(T subject) {
        Throws.ifNull(subject, () -> "subject can not be null");
        return on(subject.getClass());
    }

    public <T> Supplier<? extends BizException> on(T subject, Throwable cause) {
        Throws.ifNull(subject, () -> "subject can not be null");
        return on(subject.getClass(), cause);
    }

    public Supplier<? extends BizException> on(Class<?> subjectType) {
        Throws.ifNull(subjectType, () -> "subjectType can not be null");
        return () -> new BizException(format("[{}] {}", subjectType.getSimpleName(), this.words));
    }

    public Supplier<? extends BizException> on(Class<?> subjectType, Throwable cause) {
        Throws.ifNull(subjectType, () -> "subjectType can not be null");
        return () -> new BizException(format("[{}] {}", subjectType.getSimpleName(), this.words), cause);
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
