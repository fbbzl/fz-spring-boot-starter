package com.fz.springboot.starter.generator.frame;

import cn.hutool.core.text.CharSequenceUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/29 14:32
 */

@Slf4j
@FieldDefaults(level = PROTECTED)
public abstract class Generator implements BeanNameAware {

    @Autowired                          Configuration configuration;
    @Value("${code.generator.author:}") String        author;
    @Getter volatile                    String        generatorName;

    @Override
    public void setBeanName(@NonNull String name) {
        generatorName = name;
    }

    public abstract JavaFilePath getJavaFilePath(Map<String, Object> ftlContext) throws Exception;

    public abstract Template getTemplate() throws Exception;


    @Data
    @FieldDefaults(level = PRIVATE)
    public static class JavaFilePath {
        String fullPackage;
        String className;

        public static JavaFilePath of(String fullPackage, String className) {
            if (CharSequenceUtil.contains(fullPackage, '.')) {
                fullPackage = packagePathToFilePath(fullPackage);
            }

            JavaFilePath javaFilePath = new JavaFilePath();
            javaFilePath.setFullPackage(fullPackage);
            javaFilePath.setClassName(className);

            return javaFilePath;
        }

        public static String packagePathToFilePath(String classPath) {
            return classPath.replace(".", "/");
        }

        public String[] get() {
            return new String[] {fullPackage, className};
        }
    }

}
