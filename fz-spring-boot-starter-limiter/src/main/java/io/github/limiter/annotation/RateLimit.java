package io.github.limiter.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/4/22 10:20
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit
{

    int permits() default 10;

    long timeWindow() default 60;

    TimeUnit unit() default TimeUnit.SECONDS;

    String keyPrefix() default "rate_limit:";

    boolean byIp() default true;

    RateLimitType type() default RateLimitType.PER_CLIENT;

    String message() default "访问过于频繁，请稍后再试";

    enum RateLimitType
    {
        OVERALL,
        PER_CLIENT
    }
}


