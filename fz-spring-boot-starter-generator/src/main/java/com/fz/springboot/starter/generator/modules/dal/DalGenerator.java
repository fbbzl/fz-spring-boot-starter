package com.fz.springboot.starter.generator.modules.dal;

import com.fz.springboot.starter.generator.config.GeneratorConfig;
import com.fz.springboot.starter.generator.config.GeneratorConfig.DalPlatform;
import com.fz.springboot.starter.generator.frame.BaseGenerator;
import freemarker.template.Template;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Map;

import static lombok.AccessLevel.PRIVATE;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/30 15:54
 */

@Slf4j
@Component("dal")
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class DalGenerator extends BaseGenerator {

    GeneratorConfig genCfg;

    public Path getFilePath(Map<String, Object> ftlContext) {
        DalPlatform platformType = genCfg.getPlatformType();

        String dalPackage  = ftlContext.get("moduleName") + ".dal";
        String dalFileName = ftlContext.get("className").toString();

        if (platformType == DalPlatform.JPA)
            dalFileName = dalFileName + "Repository.java";
        else
            dalFileName = dalFileName + "Mapper.java";

        return javaFilePath(dalPackage, dalFileName);
    }

    @Override
    public Template getTemplate() throws Exception {
        DalPlatform platformType = genCfg.getPlatformType();

        if (platformType == DalPlatform.JPA)
            return configuration.getTemplate("jpa_repository.ftl");
        else
            return configuration.getTemplate("mybatisplus_mapper.ftl");
    }
}
