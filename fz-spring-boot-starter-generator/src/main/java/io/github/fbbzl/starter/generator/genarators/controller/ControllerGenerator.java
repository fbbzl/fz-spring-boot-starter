package io.github.fbbzl.starter.generator.genarators.controller;

import freemarker.template.Template;
import io.github.fbbzl.starter.generator.frame.BaseGenerator;

import java.nio.file.Path;
import java.util.Map;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/30 17:38
 */

public class ControllerGenerator extends BaseGenerator
{

    @Override
    public Path getFilePath(Map<String, Object> ftlContext)
    {
        this.addRequestMapping(ftlContext);
        String controllerPackage  = ftlContext.get("moduleName") + ".controller";
        String controllerFileName = ftlContext.get("className") + "Controller.java";

        return javaFilePath(controllerPackage, controllerFileName);
    }

    @Override
    public Template getTemplate(Map<String, Object> ftlContext) throws Exception
    {
        return configuration.getTemplate("controller.ftl");
    }

    //************************************************ private start *************************************************//

    private void addRequestMapping(Map<String, Object> ftlContext)
    {
        String className = ftlContext.get("className").toString();
        ftlContext.put("requestMapping", upperCamelToKebabCase(className));
    }

    private String upperCamelToKebabCase(String upperCamelCase)
    {
        if (upperCamelCase == null || upperCamelCase.isEmpty()) {
            return upperCamelCase;
        }
        // 在每个大写字母前插入 '-'，并排除首个字符前的 '-', 驼峰转烤串
        return upperCamelCase.replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase();
    }
}
