package com.fz.starter.core.annotation;

import lombok.NonNull;
import org.fz.erwin.exception.Throws;
import org.fz.erwin.lambda.Try;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.AliasFor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.core.io.support.EncodedResource;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2021/5/25 10:04
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PropertySource("")
public @interface YamlSource {

    @AliasFor(annotation = PropertySource.class, attribute = "name")
    String name() default "";

    @AliasFor(annotation = PropertySource.class, attribute = "value")
    String[] value();

    @AliasFor(annotation = PropertySource.class, attribute = "ignoreResourceNotFound")
    boolean ignoreResourceNotFound() default false;

    @AliasFor(annotation = PropertySource.class, attribute = "encoding")
    String encoding() default "";

    @AliasFor(annotation = PropertySource.class, attribute = "factory")
    Class<? extends YamlAndPropertySourceFactory> factory() default YamlAndPropertySourceFactory.class;

    /**
     * @author fengbinbin
     * @version 1.0
     * @since 2021/5/11 11:01
     */

    class YamlAndPropertySourceFactory extends DefaultPropertySourceFactory {

        @NonNull
        @Override
        public org.springframework.core.env.PropertySource<?> createPropertySource(String name, EncodedResource encodedResource) throws IOException {
            Resource resource = encodedResource.getResource();

            if (isYml(resource)) {
                List<org.springframework.core.env.PropertySource<?>> sources = new YamlPropertySourceLoader().load(resource.getFilename(), resource);
                Throws.ifEmpty(sources, Try.get(() -> "can not find resource [" + encodedResource.getResource().getURL() + "]"));

                return sources.iterator().next();
            }

            return super.createPropertySource(name, encodedResource);
        }

        //************************************       private start      ***************************************************//

        private boolean isYml(Resource resource) {
            String filename = resource.getFilename();
            Throws.ifBlank(filename, () -> "resource filename is null or blank");

            return filename.endsWith(".yml") || filename.endsWith(".yaml");
        }

        //************************************       private start      ***************************************************//
    }
}
