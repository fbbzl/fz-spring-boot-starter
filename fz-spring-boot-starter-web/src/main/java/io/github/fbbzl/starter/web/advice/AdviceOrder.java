package io.github.fbbzl.starter.web.advice;

import lombok.experimental.UtilityClass;
import org.springframework.core.Ordered;

/**
 * Shared order constants for web advices.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026/4/29
 */
@UtilityClass
public final class AdviceOrder
{

    /**
     * {@link WebRequestAdvice}
     */
    public static final int WEB_REQUEST = Ordered.LOWEST_PRECEDENCE;

    /**
     * {@link WebResponseWrapAdvice}
     */
    public static final int WEB_RESPONSE_WRAP = Ordered.HIGHEST_PRECEDENCE + 100;

    /**
     * {@link WebResponseOperateAdvice}
     */
    public static final int WEB_RESPONSE_OPERATE = Ordered.HIGHEST_PRECEDENCE + 200;

    /**
     * {@link WebExceptionAdvice}
     */
    public static final int WEB_EXCEPTION = Ordered.LOWEST_PRECEDENCE;
}
