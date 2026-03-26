package com.fz.starter.generator.modules.bo;

import com.fz.starter.generator.frame.BaseGenerator;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Map;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/2/13 21:36
 */


@Slf4j
@Component("bo")
public class BoGenerator extends BaseGenerator
{

    @Override
    public Path getFilePath(Map<String, Object> ftlContext) throws Exception
    {
        String boPackage  = ftlContext.get("moduleName") + ".service.bo";
        String boFileName = ftlContext.get("className") + "Bo.java";

        return javaFilePath(boPackage, boFileName);
    }

    @Override
    public Template getTemplate() throws Exception
    {
        return configuration.getTemplate("bo.ftl");
    }

}