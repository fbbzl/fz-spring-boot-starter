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

    NOT_LOGIN("000000", "not login"),
    UNAUTHORIZED("000001", "unauthorized"),
    NOT_FOUND("000002", "not found"),
    UN_SUPPORT_OPERATION("000003", "un support operation"),
    DUPLICATE("000004", "duplicate data"),
    TIMEOUT("000005", "operation timeout"),
    CONFLICT("000006", "data conflict"),
    ILLEGAL_PARAMETER("000007", "illegal parameter"),
    UN_KNOWN("999999", "un known error"),
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
