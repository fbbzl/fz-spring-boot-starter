package com.fz.springboot.starter.openapi.config;

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
public class DefaultOpenApiConfig {

    @Bean
    @ConditionalOnMissingBean
    public Info linjiInfo(Contact contact, License license) {
        return new Info()
                .title("通用管理系统 API")
                .description("通用管理系统 API文档(fz-springboot-starter-openapi)")
                .version("1.0")
                .contact(contact)
                .license(license);
    }

    @Bean
    @ConditionalOnMissingBean
    public OpenAPI openAPI(Info info) {
        return new OpenAPI().info(info);
    }

    @Bean
    @ConditionalOnMissingBean
    public Contact linjiContact() {
        return new Contact()
                .name("fz development team")
                .email("developer@fz.com");
    }

    @Bean
    @ConditionalOnMissingBean
    public License linjiLicense() {
        return new License()
                .name("Apache 2.0")
                .url("https://www.apache.org/licenses/LICENSE-2.0.html");
    }

}
