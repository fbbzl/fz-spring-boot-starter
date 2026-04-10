package com.fz.starter.generator.genarators.service;

import com.fz.starter.generator.config.properties.GeneratorConfigProperties.DalPlatform;
import com.fz.starter.generator.frame.BaseGenerator;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.Map;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/30 17:26
 */

@Slf4j
public class ServiceGenerator extends BaseGenerator
{

    @Override
    public Path getFilePath(Map<String, Object> ftlContext)
    {
        String servicePackage  = ftlContext.get("moduleName") + ".service";
        String serviceFileName = ftlContext.get("className") + "Service.java";

        DalPlatform platformType = genCfg.getPlatformType();

        if (platformType == DalPlatform.JPA)
            ftlContext.put("dalClass", ftlContext.get("className") + "Repository");
        else
            ftlContext.put("dalClass", ftlContext.get("className") + "Mapper");

        return javaFilePath(servicePackage, serviceFileName);
    }

    @Override
    public Template getTemplate() throws Exception
    {
        return configuration.getTemplate("service.ftl");
    }
}
