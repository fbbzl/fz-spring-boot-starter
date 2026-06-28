package io.github.fbbzl.starter.generator.genarators.entity;

import freemarker.template.Template;
import io.github.fbbzl.starter.generator.config.properties.GeneratorModuleConfig.DalPlatform;
import io.github.fbbzl.starter.generator.frame.BaseGenerator;

import java.nio.file.Path;
import java.util.Map;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/30 14:24
 */
public class EntityGenerator extends BaseGenerator
{

    public Path getFilePath(Map<String, Object> ftlContext)
    {
        String entityFileName = ftlContext.get("className") + ".java";
        String entityPackage  = ftlContext.get("moduleName") + ".dal.entity";

        return javaFilePath(entityPackage, entityFileName);
    }

    @Override
    public Template getTemplate(Map<String, Object> ftlContext) throws Exception
    {
        DalPlatform platformType = (DalPlatform) ftlContext.get("platformType");

        if (platformType == DalPlatform.JPA)
            return configuration.getTemplate("jpa_entity.ftl");
        else
            return configuration.getTemplate("mybatisplus_enity.ftl");
    }
}
