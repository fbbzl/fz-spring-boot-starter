package io.github.fbbzl.starter.generator.genarators.eo;

import freemarker.template.Template;
import io.github.fbbzl.starter.generator.frame.BaseGenerator;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.Map;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/2/11 22:27
 */

@Slf4j
public class EoGenerator extends BaseGenerator
{

    @Override
    public Path getFilePath(Map<String, Object> ftlContext) throws Exception
    {
        String eoPackage  = ftlContext.get("moduleName") + ".service.eo";
        String eoFileName = ftlContext.get("className") + "Eo.java";

        return javaFilePath(eoPackage, eoFileName);
    }

    @Override
    public Template getTemplate() throws Exception
    {
        return configuration.getTemplate("eo.ftl");
    }

}
