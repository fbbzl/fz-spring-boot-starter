package io.github.fbbzl.starter.webflux.advice;

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
     * {@link WebFluxResponseBodyResultHandler}
     */
    public static final int WEB_RESPONSE_HANDLER = 90;

    /**
     * {@link WebExceptionAdvice}
     */
    public static final int WEB_EXCEPTION = Ordered.LOWEST_PRECEDENCE;
}
