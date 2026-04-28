package io.github.fbbzl.starter.generator.config.properties;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;
import java.nio.file.Paths;

import static io.github.fbbzl.starter.generator.config.properties.GeneratorConfigProperties.DalPlatform.MYBATIS_PLUS;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/2/12 13:38
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@ConfigurationProperties(prefix = "code.generator")
public class GeneratorConfigProperties
{

    Path        outputPath     = Paths.get(System.getProperty("user.dir"), "generated-sources");
    String      primaryKeyType = "Long";
    DalPlatform platformType   = MYBATIS_PLUS;
    String      tables;
    String      modulePackage;
    String      tablePrefix;
    String      author;

    public enum DalPlatform
    {
        MYBATIS_PLUS, JPA,
        ;
    }

}
