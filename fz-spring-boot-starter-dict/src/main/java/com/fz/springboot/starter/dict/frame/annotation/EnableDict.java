package com.fz.springboot.starter.dict.frame.annotation;

import com.fz.springboot.starter.dict.DictConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;

import java.lang.annotation.*;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/9/2 19:05
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented

@ImportAutoConfiguration({DictConfiguration.class})
public @interface EnableDict {
}
