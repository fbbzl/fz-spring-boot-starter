package com.fz.springboot.starter.generator.modules.repository;

import com.fz.springboot.starter.generator.frame.Generator;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/30 15:54
 */

@Slf4j
@Component("repository")
public class RepositoryGenerator extends Generator {

    public JavaFilePath getJavaFilePath(Map<String, Object> ftlContext) {
        String repositoryPackage  = ftlContext.get("moduleName") + ".repository";
        String repositoryFileName = ftlContext.get("className") + "Repository.java";

        return JavaFilePath.of(repositoryPackage, repositoryFileName);
    }

    @Override
    public Template getTemplate() throws Exception {
        return configuration.getTemplate("repository.ftl");
    }
}
