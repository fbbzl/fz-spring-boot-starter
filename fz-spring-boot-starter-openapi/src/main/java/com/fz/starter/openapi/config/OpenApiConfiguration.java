package com.fz.starter.openapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/21 14:02
 */
@Configuration
@ConditionalOnWebApplication
public class OpenApiConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public Info info(Contact contact, License license) {
        return new Info()
                .title("default title")
                .description("default description")
                .version("default version")
                .contact(contact)
                .license(license);
    }

    @Bean
    @ConditionalOnMissingBean
    public OpenAPI openApi(Info info) {
        return new OpenAPI().info(info);
    }

    @Bean
    @ConditionalOnMissingBean
    public Contact contact() {
        return new Contact().name("default name").email("default email");
    }

    @Bean
    @ConditionalOnMissingBean
    public License license() {
        return new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0.html");
    }
}
