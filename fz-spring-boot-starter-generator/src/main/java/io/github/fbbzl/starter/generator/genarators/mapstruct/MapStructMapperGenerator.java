package io.github.fbbzl.starter.generator.genarators.mapstruct;

import freemarker.template.Template;
import io.github.fbbzl.starter.generator.frame.BaseGenerator;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.Map;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/2/11 22:29
 */

@Slf4j
public class MapStructMapperGenerator extends BaseGenerator
{
    @Override
    public Path getFilePath(Map<String, Object> ftlContext) throws Exception
    {
        String structMapperPackage  = ftlContext.get("moduleName") + ".struct";
        String structMapperFileName = ftlContext.get("className") + "StructMapper.java";

        return javaFilePath(structMapperPackage, structMapperFileName);
    }

    @Override
    public Template getTemplate(Map<String, Object> ftlContext) throws Exception
    {
        return configuration.getTemplate("mapper_struct.ftl");
    }

}
