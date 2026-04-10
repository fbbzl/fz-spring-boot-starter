package com.fz.starter.generator.genarators.xml;

import com.fz.starter.generator.config.properties.GeneratorConfigProperties.DalPlatform;
import com.fz.starter.generator.frame.BaseGenerator;
import freemarker.template.Template;
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
        DalPlatform platformType = genCfg.getPlatformType();

        if (platformType == DalPlatform.MYBATIS_PLUS) {
            String xmlPackage = ftlContext.get("moduleName") + ".dal";
            return javaFilePath(xmlPackage, ftlContext.get("className") + "Mapper.xml");
        }

        return null;
    }

    @Override
    public Template getTemplate() throws Exception
    {
        return configuration.getTemplate("mybatisplus_xml.ftl");
    }
}
