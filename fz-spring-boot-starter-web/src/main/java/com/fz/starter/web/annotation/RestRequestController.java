package com.fz.starter.web.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.*;

/**
 * combine controller and request-mapping
 * @author fengbinbin
 * @version 1.0
 * @since 4/13/2022 7:37 PM
 */

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented

@RestController
@RequestMapping
public @interface RestRequestController {

    @AliasFor(annotation = RequestMapping.class, attribute = "value")
    String[] mapping() default {};
}
