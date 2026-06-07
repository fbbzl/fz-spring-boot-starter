package io.github.fbbzl.starter.web.advice;

import org.springframework.core.Ordered;

/**
 * Shared order constants for web advices.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026/4/29
 */
public interface AdviceOrder
{

    /** {@link WebRequestAdvice} */
    int WEB_REQUEST          = Ordered.LOWEST_PRECEDENCE;

    /** {@link WebResponseWrapAdvice} */
    int WEB_RESPONSE_WRAP    = Ordered.HIGHEST_PRECEDENCE + 100;

    /** {@link WebResponseOperateAdvice} */
    int WEB_RESPONSE_OPERATE = Ordered.HIGHEST_PRECEDENCE + 200;

    /** {@link WebExceptionAdvice} */
    int WEB_EXCEPTION        = Ordered.LOWEST_PRECEDENCE;
}
