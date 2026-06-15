package io.github.fbbzl.starter.webflux.advice;

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

    /** {@link WebFluxResponseBodyResultHandler} */
    int WEB_RESPONSE_HANDLER = 90;

    /** {@link WebExceptionAdvice} */
    int WEB_EXCEPTION        = Ordered.LOWEST_PRECEDENCE;
}
