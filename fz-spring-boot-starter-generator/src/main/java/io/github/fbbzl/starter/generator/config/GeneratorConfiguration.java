package io.github.fbbzl.starter.generator.config;

import io.github.fbbzl.starter.generator.config.properties.GeneratorConfigProperties;
import io.github.fbbzl.starter.generator.frame.TypeMapping;
import io.github.fbbzl.starter.generator.frame.TypeMapping.DefaultTypeMapping;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/4/4 10:24
 */
@EnableConfigurationProperties(GeneratorConfigProperties.class)
@AutoConfiguration
public class GeneratorConfiguration
{

    @Bean
    @ConditionalOnMissingBean
    public TypeMapping defaultTypeMapping()
    {
        return new DefaultTypeMapping();
    }
}
