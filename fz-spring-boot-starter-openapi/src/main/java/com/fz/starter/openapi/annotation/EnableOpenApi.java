package com.fz.starter.openapi.annotation;

import com.fz.starter.openapi.config.OpenApiConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;

import java.lang.annotation.*;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/28 11:55
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ImportAutoConfiguration({OpenApiConfiguration.class})
@Documented
public @interface EnableOpenApi {}
