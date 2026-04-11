package com.fz.starter.generator.config;

import com.fz.starter.generator.config.properties.GeneratorConfigProperties;
import com.fz.starter.generator.frame.GeneratorImportSelector;
import com.fz.starter.generator.frame.TypeMapping;
import com.fz.starter.generator.frame.TypeMapping.DefaultTypeMapping;
import com.fz.starter.generator.frame.context.MysqlContextLoader;
import com.fz.starter.generator.frame.invoker.DefaultGeneratorInvoker;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/4/4 10:24
 */
@Import({
        MysqlContextLoader.class,
        GeneratorConfigProperties.class,
        GeneratorImportSelector.class,
        DefaultGeneratorInvoker.class})
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
