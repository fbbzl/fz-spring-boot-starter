package com.fz.springboot.starter.generator.config;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.fz.springboot.starter.generator.config.GeneratorConfig.DalPlatform.MYBATIS_PLUS;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/2/12 13:38
 */
@Data
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@ConfigurationProperties(prefix = "code.generator")
public class GeneratorConfig {

    Path        outputPath   = Paths.get(System.getProperty("user.dir"), "generated-sources");
    DalPlatform platformType = MYBATIS_PLUS;
    String      tables;
    String      modulePackage;
    String      tablePrefix;
    String      author;

    public enum DalPlatform {
        MYBATIS_PLUS, JPA,
        ;
    }

}
