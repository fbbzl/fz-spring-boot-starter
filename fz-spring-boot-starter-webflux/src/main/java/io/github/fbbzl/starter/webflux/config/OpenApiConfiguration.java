package io.github.fbbzl.starter.webflux.config;

import io.github.fbbzl.starter.webflux.customizer.QOperationCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/21 14:02
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class OpenApiConfiguration
{
    @Bean
    public QOperationCustomizer qOperationCustomizer()
    {
        return new QOperationCustomizer();
    }
}
