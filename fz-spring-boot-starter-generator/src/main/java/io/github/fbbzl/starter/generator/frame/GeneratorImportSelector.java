package io.github.fbbzl.starter.generator.frame;


import cn.hutool.core.util.ClassUtil;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;

import java.util.Set;

/**
 * Auto register all BaseGenerator implementations as beans
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026/4/9
 */
public class GeneratorImportSelector implements ImportSelector
{

    @NonNull
    @Override
    public String[] selectImports(@NonNull AnnotationMetadata importingClassMetadata)
    {
        Set<Class<?>> generatorClasses = ClassUtil.scanPackageBySuper(
                "io.github.fbbzl.starter.generator.genarators",
                BaseGenerator.class
                                                                     );

        return generatorClasses.stream()
                               .map(Class::getName)
                               .toArray(String[]::new);
    }
}
