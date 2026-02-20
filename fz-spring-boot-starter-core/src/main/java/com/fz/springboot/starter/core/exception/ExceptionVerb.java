package com.fz.springboot.starter.core.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ExceptionVerb {

    ACCESS_DENIED("403"),
    INVALID_INPUT("400"),
    ILLEGAL_STATE("422"),
    UNSUPPORTED_OPERATION("405"),
    RESOURCE_NOT_FOUND("404"),
    DATA_CONFLICT("409"),
    DUPLICATE_ENTRY("409"),
    OPERATION_TIMEOUT("408"),
    INTERNAL_ERROR("500"),
    SERVICE_UNAVAILABLE("503"),
    UNKNOWN("500");

    String bisCode;

    public <T> Supplier<? extends BizException> on(T subject, Object context) {
        Throws.ifNull(subject, () -> "subject can not be null");
        return on(subject.getClass(), context);
    }

    public <T> Supplier<? extends BizException> on(T subject, Object context, Throwable cause) {
        Throws.ifNull(subject, () -> "subject can not be null");
        return on(subject.getClass(), context, cause);
    }

    public Supplier<? extends BizException> on(Class<?> subjectType, Object context) {
        return on(subjectType, context, null);
    }

    public Supplier<? extends BizException> on(Class<?> subjectType, Object context, Throwable cause) {
        Objects.requireNonNull(subjectType, "subjectType can not be null");
        return () -> new BizException(this, format("[{}] {}, context: [{}]", subjectType.getSimpleName(), toCamelCase(this.name()), context), cause);
    }

    /**
     * @author fengbinbin
     * @version 1.0
     * @since 2025/8/22 20:24
     */
    @Getter
    public static class BizException extends RuntimeException {

        ExceptionVerb verb;

        public BizException(ExceptionVerb verb, Object message) {
            super(message.toString());
            this.verb = verb;
        }

        public BizException(ExceptionVerb verb, Object message, Throwable cause) {
            super(message.toString(), cause);
            this.verb = verb;
        }

    }
}
