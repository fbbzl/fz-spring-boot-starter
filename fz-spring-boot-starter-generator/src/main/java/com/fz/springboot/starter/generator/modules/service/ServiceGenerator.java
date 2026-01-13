package com.fz.springboot.starter.generator.modules.service;

import com.fz.springboot.starter.generator.frame.BaseGenerator;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Map;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/30 17:26
 */

@Slf4j
@Component("service")
public class ServiceGenerator extends BaseGenerator {

    @Override
    public Path getFilePath(Map<String, Object> ftlContext) {
        String servicePackage  = ftlContext.get("moduleName") + ".service";
        String serviceFileName = ftlContext.get("className") + "Service.java";

        return javaFilePath(servicePackage, serviceFileName);
    }

    @Override
    public Template getTemplate() throws Exception {
        return configuration.getTemplate("service.ftl");
    }
}
