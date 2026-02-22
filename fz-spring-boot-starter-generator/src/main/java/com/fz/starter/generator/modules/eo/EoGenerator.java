package com.fz.starter.generator.modules.eo;

import com.fz.starter.generator.frame.BaseGenerator;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Map;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/2/11 22:27
 */

@Slf4j
@Component("eo")
public class EoGenerator extends BaseGenerator {

    @Override
    public Path getFilePath(Map<String, Object> ftlContext) throws Exception {
        String entityPackage  = ftlContext.get("moduleName") + ".service.eo";
        String entityFileName = ftlContext.get("className") + "Eo.java";

        return javaFilePath(entityPackage, entityFileName);
    }

    @Override
    public Template getTemplate() throws Exception {
        return configuration.getTemplate("eo.ftl");
    }

}
