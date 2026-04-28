package io.github.fbbzl.starter.generator.frame;

import freemarker.template.Configuration;
import freemarker.template.Template;
import io.github.fbbzl.starter.generator.config.properties.GeneratorConfigProperties;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.nio.file.Path;
import java.util.Map;

import static cn.hutool.core.text.CharSequenceUtil.contains;
import static lombok.AccessLevel.PROTECTED;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/29 14:32
 */

@Slf4j
@FieldDefaults(level = PROTECTED)
public abstract class BaseGenerator implements BeanNameAware
{

    @Autowired
    Configuration             configuration;
    @Value("${code.generator.author:fz}")
    String                    author;
    @Getter
    volatile
    String                    generatorBeanName;
    @Autowired
    GeneratorConfigProperties genCfg;

    @Override
    public void setBeanName(@NonNull String name)
    {
        generatorBeanName = name;
    }

    public abstract Path getFilePath(Map<String, Object> ftlContext) throws Exception;

    public abstract Template getTemplate() throws Exception;

    /**
     * generate the java file path
     */
    public static Path javaFilePath(String fullPackage, String className)
    {
        if (contains(fullPackage, '.')) {
            fullPackage = fullPackage.replace(".", "/");
        }

        return Path.of(fullPackage, className);
    }

}
