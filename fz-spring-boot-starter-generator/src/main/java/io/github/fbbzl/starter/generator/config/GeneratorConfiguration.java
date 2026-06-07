package io.github.fbbzl.starter.generator.config;

import io.github.fbbzl.starter.generator.config.properties.GeneratorConfigProperties;
import io.github.fbbzl.starter.generator.frame.GeneratorImportSelector;
import io.github.fbbzl.starter.generator.frame.TypeMapping;
import io.github.fbbzl.starter.generator.frame.TypeMapping.DefaultTypeMapping;
import io.github.fbbzl.starter.generator.frame.context.MysqlContextLoader;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/4/4 10:24
 */
@Import({
        MysqlContextLoader.class,
        GeneratorImportSelector.class})
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
