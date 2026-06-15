package io.github.fbbzl.starter.pojo.crane4j;

import cn.crane4j.annotation.Mapping;
import cn.crane4j.core.container.Container;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Assemble fields from a Spring bean that implements Crane4j {@link Container}.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026/5/21
 */
@Documented
@Repeatable(AssembleBean.List.class)
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface AssembleBean
{

    Class<? extends Container<?>> bean();

    String id() default "";

    int sort() default Integer.MAX_VALUE;

    String key() default "";

    Class<?> keyType() default Object.class;

    Class<?> keyResolver() default Object.class;

    String keyDesc() default "";

    String handler() default "";

    Class<?> handlerType() default Object.class;

    Mapping[] props() default {};

    String[] prop() default {};

    Class<?>[] propTemplates() default {};

    String[] groups() default {};

    String propertyMappingStrategy() default "";

    @Documented
    @Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    @interface List
    {
        AssembleBean[] value();
    }
}
