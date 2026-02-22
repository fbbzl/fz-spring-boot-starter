package com.fz.starter.generator.modules.dto;

import com.fz.starter.generator.frame.BaseGenerator;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Map;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/9/2 11:08
 */

@Slf4j
@Component("dto")
public class DtoGenerator extends BaseGenerator {
    @Override
    public Path getFilePath(Map<String, Object> ftlContext) throws Exception {
        String entityPackage  = ftlContext.get("moduleName") + ".controller.dto";
        String entityFileName = ftlContext.get("className") + "Dto.java";

        return javaFilePath(entityPackage, entityFileName);
    }

    @Override
    public Template getTemplate() throws Exception {
        return configuration.getTemplate("dto.ftl");
    }
}
