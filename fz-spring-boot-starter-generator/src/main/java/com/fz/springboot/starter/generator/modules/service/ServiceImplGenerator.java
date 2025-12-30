package com.fz.springboot.starter.generator.modules.service;

import com.fz.springboot.starter.generator.frame.Generator;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/30 17:26
 */

@Slf4j
@Component
public class ServiceImplGenerator extends Generator {

    @Override
    public JavaFilePath getJavaFilePath(Map<String, Object> ftlContext) {
        String serviceImplPackage  = ftlContext.get("moduleName") + ".service.impl";
        String serviceImplFileName = ftlContext.get("className") + "ServiceImpl.java";

        return JavaFilePath.of(serviceImplPackage, serviceImplFileName);
    }

    @Override
    public Template getTemplate() throws Exception {
        return configuration.getTemplate("service_impl.ftl");
    }
}
