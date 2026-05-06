package io.github.fbbzl.starter.redisson.repeat;

import java.lang.annotation.*;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/4/22 10:20
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SubmitOnce
{
    /**
     * submit once window.
     */
    long windowMillis() default 3_000;

    /**
     * key prefix.
     */
    String keyPrefix() default "submit_once:";

    /**
     * request identity field name or SpEL expression.
     */
    String expression() default "requestId";

    /**
     * repeated submit message.
     */
    String message() default "please do not submit repeatedly";
}
