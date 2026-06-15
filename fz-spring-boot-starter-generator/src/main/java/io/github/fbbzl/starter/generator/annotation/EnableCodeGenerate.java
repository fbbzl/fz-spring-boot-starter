package io.github.fbbzl.starter.generator.annotation;

import io.github.fbbzl.starter.generator.frame.GeneratorImportSelector;
import io.github.fbbzl.starter.generator.frame.context.MysqlContextLoader;
import io.github.fbbzl.starter.generator.frame.invoker.DefaultGeneratorInvoker;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/4/22 13:06
 */
@Import({
        MysqlContextLoader.class,
        GeneratorImportSelector.class,
        DefaultGeneratorInvoker.class})
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableCodeGenerate
{
}
