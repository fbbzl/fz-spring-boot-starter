package io.github.fbbzl.starter.web.config;

import cn.crane4j.core.parser.handler.strategy.PropertyMappingStrategyManager;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import io.github.fbbzl.starter.pojo.crane4j.AssembleBeanAnnotationHandler;
import io.github.fbbzl.starter.web.advice.WebExceptionAdvice;
import io.github.fbbzl.starter.web.advice.WebRequestAdvice;
import io.github.fbbzl.starter.web.advice.WebResponseOperateAdvice;
import io.github.fbbzl.starter.web.advice.WebResponseWrapAdvice;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 4/12/2026 12:23
 */
@Import({
        WebExceptionAdvice.class,
        WebRequestAdvice.class,
        WebResponseOperateAdvice.class,
        WebResponseWrapAdvice.class
})
@AutoConfiguration
@ConditionalOnWebApplication
public class WebMvcConfiguration
{

    @Bean
    @ConditionalOnMissingBean(AssembleBeanAnnotationHandler.class)
    public AssembleBeanAnnotationHandler assembleBeanAnnotationHandler(
            AnnotationFinder annotationFinder,
            Crane4jGlobalConfiguration globalConfiguration,
            PropertyMappingStrategyManager propertyMappingStrategyManager,
            ApplicationContext applicationContext)
    {
        return new AssembleBeanAnnotationHandler(
                annotationFinder,
                globalConfiguration,
                propertyMappingStrategyManager,
                applicationContext
        );
    }

}
