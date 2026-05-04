package io.github.redisson.annotation;

import org.redisson.client.codec.Codec;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta annotations for all redisson object annotations
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2025/3/1 14:40
 */
@Autowired(required = false)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectRObject {

    String value();

    Class<? extends Codec> codec() default StringCodec.class;

}
