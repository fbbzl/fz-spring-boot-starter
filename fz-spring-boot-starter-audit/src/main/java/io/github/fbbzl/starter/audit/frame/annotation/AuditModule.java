package io.github.fbbzl.starter.audit.frame.annotation;

import java.lang.annotation.*;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/4/25 19:25
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditModule
{
    /**
     * module name
     */
    String value();

}
