package io.github.fbbzl.starter.generator.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties("code")
public class GeneratorConfigProperties
{
    private List<GeneratorModuleConfig> generator;
}
