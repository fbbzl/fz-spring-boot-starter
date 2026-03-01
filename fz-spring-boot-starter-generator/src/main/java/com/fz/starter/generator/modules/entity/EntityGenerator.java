package com.fz.starter.generator.modules.entity;

import com.fz.starter.generator.config.properties.GeneratorConfigProperties;
import com.fz.starter.generator.config.properties.GeneratorConfigProperties.DalPlatform;
import com.fz.starter.generator.frame.BaseGenerator;
import freemarker.template.Template;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Map;

import static lombok.AccessLevel.PRIVATE;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/30 14:24
 */
@Slf4j
@Component("entity")
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class EntityGenerator extends BaseGenerator {

    GeneratorConfigProperties genCfg;

    public Path getFilePath(Map<String, Object> ftlContext) {
        String entityFileName = ftlContext.get("className") + ".java";
        String entityPackage  = ftlContext.get("moduleName") + ".dal.entity";

        return javaFilePath(entityPackage, entityFileName);
    }

    @Override
    public Template getTemplate() throws Exception {
        DalPlatform platformType = genCfg.getPlatformType();

        if (platformType == DalPlatform.JPA)
            return configuration.getTemplate("jpa_entity.ftl");
        else
            return configuration.getTemplate("mybatisplus_enity.ftl");
    }
}
