package io.github.fbbzl.starter.core.exception;

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
@SuppressWarnings("all")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ExceptionVerb
{
    INVALID_INPUT(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    RESOURCE_NOT_FOUND(404),
    UNSUPPORTED_OPERATION(405),
    OPERATION_TIMEOUT(408),
    DATA_CONFLICT(409),
    DUPLICATE_ENTRY(409),
    ILLEGAL_STATE(422),
    REQUEST_LIMITED(429),
    INTERNAL_ERROR(500),
    SERVICE_UNAVAILABLE(503),
    UNKNOWN(500);

    int httpStatusCode;

    public <SUBJECT> Supplier<BizException> on(SUBJECT subject, Object context)
    {
        Throws.ifNull(subject, "subject can not be null");
        return on(subject.getClass(), context);
    }

    public <SUBJECT> Supplier<BizException> on(SUBJECT subject, Object context, Throwable cause)
    {
        Throws.ifNull(subject, "subject can not be null");
        return on(subject.getClass(), context, cause);
    }

    public Supplier<BizException> on(Class<?> subjectType, Object context)
    {
        return on(subjectType, context, null);
    }

    public Supplier<BizException> on(Class<?> subjectType, Object context, Throwable cause)
    {
        Objects.requireNonNull(subjectType, "subjectType can not be null");
        return () -> new BizException(this, format("[{}] {}, context: [{}]", subjectType.getSimpleName(), toCamelCase(this.name()), context), cause);
    }

}
