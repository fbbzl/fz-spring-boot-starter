package io.github.redisson.annotation;

import org.redisson.api.RateType;

import java.lang.annotation.*;

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

    long permits() default 10;

    long timeWindowMillis() default 1000;

    String keyPrefix() default "rate_limit:";

    boolean byIp() default true;

    RateType type() default RateType.PER_CLIENT;

    String message() default "visits are too frequent please try again later";

    long keepAliveMillis() default 60_000;

}


