package com.fz.starter.audit.frame.annotation;


import java.lang.annotation.*;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/9/4 17:09
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditMethod
{

    boolean saveParam() default true;

    boolean saveResult() default true;
}
