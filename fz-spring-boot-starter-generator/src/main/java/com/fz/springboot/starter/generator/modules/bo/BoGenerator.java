package com.fz.springboot.starter.generator.modules.bo;

import com.fz.springboot.starter.generator.frame.BaseGenerator;
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
public class BoGenerator extends BaseGenerator {

    @Override
    public Path getFilePath(Map<String, Object> ftlContext) throws Exception {
        String entityPackage  = ftlContext.get("moduleName") + ".service.bo";
        String entityFileName = ftlContext.get("className") + "Bo.java";

        return javaFilePath(entityPackage, entityFileName);
    }

    @Override
    public Template getTemplate() throws Exception {
        return configuration.getTemplate("bo.ftl");
    }

}