package com.fz.starter.openapi.config;

import com.fz.starter.openapi.annotation.ApiInfo;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/21 14:02
 */
@AutoConfiguration
@ConditionalOnWebApplication
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OpenApiConfiguration implements ImportAware
{

    Info info;

    @Bean
    @ConditionalOnMissingBean
    public OpenAPI openApi()
    {
        return new OpenAPI().info(info);
    }

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata)
    {
        AnnotationAttributes attributes =
                AnnotationAttributes.fromMap(importMetadata.getAnnotationAttributes(ApiInfo.class.getName(), false));

        if (attributes != null)
        {
            Contact contact = new Contact()
                    .name(attributes.getString("contactName"))
                    .email(attributes.getString("contactEmail"))
                    .url(attributes.getString("contactUrl"));

            License license = new License()
                    .name(attributes.getString("licenseName"))
                    .url(attributes.getString("licenseUrl"));

            this.info = new Info()
                    .title(attributes.getString("title"))
                    .description(attributes.getString("description"))
                    .version(attributes.getString("version"))
                    .contact(contact)
                    .license(license);
        }
    }
}
