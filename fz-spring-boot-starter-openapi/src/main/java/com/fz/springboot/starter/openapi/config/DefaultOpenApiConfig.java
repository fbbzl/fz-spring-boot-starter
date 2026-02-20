package com.fz.springboot.starter.openapi.config;

import com.fz.springboot.starter.openapi.annotation.EnableOpenApi;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/21 14:02
 */

@Configuration
@ConditionalOnWebApplication
public class DefaultOpenApiConfig implements ImportAware {

    AnnotationAttributes enableOpenApi;

    @Bean
    @ConditionalOnMissingBean
    public Info linjiInfo(Contact contact, License license) {
        String title       = enableOpenApi.getString("title");
        String description = enableOpenApi.getString("description");
        String version     = enableOpenApi.getString("version");
        return new Info()
                .title(title)
                .description(description)
                .version(version)
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

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        this.enableOpenApi = AnnotationAttributes.fromMap(importMetadata.getAnnotationAttributes(EnableOpenApi.class.getName(), false));
    }
}
