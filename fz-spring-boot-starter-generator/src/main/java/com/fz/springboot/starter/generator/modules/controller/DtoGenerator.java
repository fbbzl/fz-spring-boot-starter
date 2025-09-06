package com.fz.springboot.starter.generator.modules.controller;

import com.fz.springboot.starter.generator.frame.Generator;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/9/2 11:08
 */

@Slf4j
@Component("dto")
public class DtoGenerator extends Generator {
    @Override
    public JavaFilePath getJavaFilePath(Map<String, Object> ftlContext) throws Exception {
        String entityPackage  = ftlContext.get("moduleName") + ".controller.dto";
        String entityFileName = ftlContext.get("className") + "Dto.java";

        return JavaFilePath.of(entityPackage, entityFileName);
    }

    @Override
    public Template getTemplate() throws Exception {
        return configuration.getTemplate("dto.ftl");
    }
}
