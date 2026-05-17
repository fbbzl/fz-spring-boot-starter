package io.github.fbbzl.starter.webflux.annotation;

import java.lang.annotation.*;

/**
 * Skip crane4j response data assembly but keep the global response wrapper.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026/4/29
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreResponseOperate {}
