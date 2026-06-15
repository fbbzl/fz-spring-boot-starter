package io.github.fbbzl.starter.generator.genarators.xml;

import freemarker.template.Template;
import io.github.fbbzl.starter.generator.config.properties.GeneratorModuleConfig.DalPlatform;
import io.github.fbbzl.starter.generator.frame.BaseGenerator;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.Map;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/3/31 20:00
 */

@Slf4j
public class MybatisXmlGenerator extends BaseGenerator
{
    @Override
    public Path getFilePath(Map<String, Object> ftlContext) throws Exception
    {
        DalPlatform platformType = (DalPlatform) ftlContext.get("platformType");

        if (platformType == DalPlatform.MYBATIS_PLUS) {
            String xmlPackage = ftlContext.get("moduleName") + ".dal";
            return javaFilePath(xmlPackage, ftlContext.get("className") + "Mapper.xml");
        }

        return null;
    }

    @Override
    public Template getTemplate(Map<String, Object> ftlContext) throws Exception
    {
        return configuration.getTemplate("mybatisplus_xml.ftl");
    }
}
