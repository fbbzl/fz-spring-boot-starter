package io.github.fbbzl.starter.generator.genarators.dal;

import freemarker.template.Template;
import io.github.fbbzl.starter.generator.config.properties.GeneratorModuleConfig.DalPlatform;
import io.github.fbbzl.starter.generator.frame.BaseGenerator;

import java.nio.file.Path;
import java.util.Map;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/30 15:54
 */

public class DalGenerator extends BaseGenerator
{
    public Path getFilePath(Map<String, Object> ftlContext)
    {
        String dalPackage  = ftlContext.get("moduleName") + ".dal";
        Object dalFileName = ftlContext.get("className");

        DalPlatform platformType = (DalPlatform) ftlContext.get("platformType");

        return platformType == DalPlatform.JPA ?
               javaFilePath(dalPackage, dalFileName + "Repository.java")
                                                :
               javaFilePath(dalPackage, dalFileName + "Mapper.java");
    }

    @Override
    public Template getTemplate(Map<String, Object> ftlContext) throws Exception
    {
        DalPlatform platformType = (DalPlatform) ftlContext.get("platformType");

        return platformType == DalPlatform.JPA ?
               configuration.getTemplate("jpa_repository.ftl")
                                                :
               configuration.getTemplate("mybatisplus_mapper.ftl");

    }
}
