package io.github.fbbzl.starter.web.annotation.exchange;

import io.github.fbbzl.starter.web.registrar.HttpExchangeClientRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/5/13 22:00
 */
@Target(ElementType.TYPE)
@Retention(RUNTIME)
@Documented
@Import(HttpExchangeClientRegistrar.class)
public @interface EnableHttpExchangeClientScan
{
    String[] scanBasePackages() default {};

    Class<?>[] scanBasePackageClasses() default {};
}
