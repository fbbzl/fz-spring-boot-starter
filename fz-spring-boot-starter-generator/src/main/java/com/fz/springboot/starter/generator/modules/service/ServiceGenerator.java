package com.fz.springboot.starter.generator.modules.service;

import com.fz.springboot.starter.generator.frame.Generator;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/30 16:15
 */
@Slf4j
@Component("service")
public class ServiceGenerator extends Generator {

    @Override
    public JavaFilePath getJavaFilePath(Map<String, Object> ftlContext) {
        String servicePackage  = ftlContext.get("moduleName") + ".service";
        String serviceFileName = "I" + ftlContext.get("className") + "Service.java";

        return JavaFilePath.of(servicePackage, serviceFileName);
    }

    @Override
    public Template getTemplate() throws Exception {
        return configuration.getTemplate("service.ftl");
    }
}
