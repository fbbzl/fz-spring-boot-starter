package io.github.fbbzl.starter.generator.genarators.entity;

import freemarker.template.Template;
import io.github.fbbzl.starter.generator.config.properties.GeneratorConfigProperties.DalPlatform;
import io.github.fbbzl.starter.generator.frame.BaseGenerator;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.Map;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/30 14:24
 */
@Slf4j
public class EntityGenerator extends BaseGenerator
{

    public Path getFilePath(Map<String, Object> ftlContext)
    {
        String entityFileName = ftlContext.get("className") + ".java";
        String entityPackage  = ftlContext.get("moduleName") + ".dal.entity";

        return javaFilePath(entityPackage, entityFileName);
    }

    @Override
    public Template getTemplate() throws Exception
    {
        DalPlatform platformType = genCfg.getPlatformType();

        if (platformType == DalPlatform.JPA)
            return configuration.getTemplate("jpa_entity.ftl");
        else
            return configuration.getTemplate("mybatisplus_enity.ftl");
    }
}
