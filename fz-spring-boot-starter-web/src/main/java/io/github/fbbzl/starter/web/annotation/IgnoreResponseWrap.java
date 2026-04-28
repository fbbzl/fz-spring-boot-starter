package io.github.fbbzl.starter.web.annotation;

import java.lang.annotation.*;

/**
 * Skip global response wrapping for special endpoints such as file download or third-party callbacks.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026/4/29
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreResponseWrap {}
