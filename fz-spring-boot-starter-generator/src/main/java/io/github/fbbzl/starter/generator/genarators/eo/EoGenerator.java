package io.github.fbbzl.starter.generator.genarators.eo;

import freemarker.template.Template;
import io.github.fbbzl.starter.generator.frame.BaseGenerator;

import java.nio.file.Path;
import java.util.Map;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/2/11 22:27
 */

public class EoGenerator extends BaseGenerator
{

    @Override
    public Path getFilePath(Map<String, Object> ftlContext) throws Exception
    {
        if (Boolean.TRUE != (ftlContext.get("excel"))) return null;

        String eoPackage  = ftlContext.get("moduleName") + ".service.eo";
        String eoFileName = ftlContext.get("className") + "Eo.java";

        return javaFilePath(eoPackage, eoFileName);
    }

    @Override
    public Template getTemplate(Map<String, Object> ftlContext) throws Exception
    {
        return configuration.getTemplate("eo.ftl");
    }

}
