package com.fz.spring.boot.starter.log.frame.annotation;

import com.fz.spring.boot.starter.log.frame.aspect.OperationLogAspect;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/9/4 18:02
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)

@Import({
        OperationLogAspect.class
})
public @interface EnableOperationLog {
}
