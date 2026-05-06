package io.github.fbbzl.starter.web.config;

import io.github.fbbzl.starter.web.customizer.ROperationCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
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
    public ROperationCustomizer roperationCustomizer()
    {
        return new ROperationCustomizer();
    }
}
