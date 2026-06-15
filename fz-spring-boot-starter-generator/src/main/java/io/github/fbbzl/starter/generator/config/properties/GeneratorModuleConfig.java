package io.github.fbbzl.starter.generator.config.properties;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.nio.file.Path;
import java.nio.file.Paths;

import static io.github.fbbzl.starter.generator.config.properties.GeneratorModuleConfig.DalPlatform.MYBATIS_PLUS;

@Data
@FieldDefaults(level = AccessLevel.PROTECTED)
public class GeneratorModuleConfig {

    Path        outputPath     = Paths.get(System.getProperty("user.dir"), "generated-sources");
    String      primaryKeyType = "Long";
    DalPlatform platformType   = MYBATIS_PLUS;
    String      tables;
    String      modulePackage;
    String      tablePrefix;
    String      author;
    boolean     excel;
    String      schema;

    public enum DalPlatform
    {
        MYBATIS_PLUS, JPA,
        ;
    }

}
