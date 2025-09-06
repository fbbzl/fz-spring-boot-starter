package com.fz.springboot.starter.generator.modules.entity;

import com.fz.springboot.starter.generator.frame.Generator;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/30 14:24
 */
@Slf4j
@Component("entity")
public class EntityGenerator extends Generator {

    public JavaFilePath getJavaFilePath(Map<String, Object> ftlContext) {
        String entityPackage  = ftlContext.get("moduleName") + ".repository.entity";
        String entityFileName = ftlContext.get("className") + ".java";

        return JavaFilePath.of(entityPackage, entityFileName);
    }

    @Override
    public Template getTemplate() throws Exception {
        return configuration.getTemplate("entity.ftl");
    }
}
