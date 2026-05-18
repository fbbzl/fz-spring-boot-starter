package io.github.fbbzl.starter.web.config;

import io.github.fbbzl.starter.web.customizer.QOperationCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/21 14:02
 */
@AutoConfiguration
@ConditionalOnWebApplication
public class OpenApiConfiguration
{
    @Bean
    @ConditionalOnMissingBean
    public QOperationCustomizer roperationCustomizer()
    {
        return new QOperationCustomizer();
    }
}
