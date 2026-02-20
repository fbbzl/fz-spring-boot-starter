package com.fz.springboot.starter.generator.modules.mapstruct;

import com.fz.springboot.starter.generator.frame.BaseGenerator;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Map;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/2/11 22:29
 */

@Slf4j
@Component("struct_mapper")
public class StructMapperGenerator extends BaseGenerator {

    @Override
    public Path getFilePath(Map<String, Object> ftlContext) throws Exception {
        String entityPackage  = ftlContext.get("moduleName") + ".struct";
        String entityFileName = ftlContext.get("className") + "StructMapper.java";

        return javaFilePath(entityPackage, entityFileName);
    }

    @Override
    public Template getTemplate() throws Exception {
        return configuration.getTemplate("mapper_struct.ftl");
    }

}
