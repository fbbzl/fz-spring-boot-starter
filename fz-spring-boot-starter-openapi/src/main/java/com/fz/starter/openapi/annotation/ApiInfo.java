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
public @interface ApiInfo
{
    String title() default "default title";

    String description() default "default description";

    String version() default "0.0.1";

    String contactName() default "default name";

    String contactEmail() default "default email";

    String contactUrl() default "";

    String licenseName() default "Apache 2.0";

    String licenseUrl() default "https://www.apache.org/licenses/LICENSE-2.0.html";
}
