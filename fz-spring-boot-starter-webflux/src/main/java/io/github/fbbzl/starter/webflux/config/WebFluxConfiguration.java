package io.github.fbbzl.starter.webflux.config;

import cn.crane4j.core.parser.handler.strategy.PropertyMappingStrategyManager;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.OperateTemplate;
import io.github.fbbzl.starter.pojo.crane4j.AssembleBeanAnnotationHandler;
import io.github.fbbzl.starter.webflux.advice.WebExceptionAdvice;
import io.github.fbbzl.starter.webflux.advice.WebFluxResponseBodyResultHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/5/17
 */
@Import(WebExceptionAdvice.class)
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class WebFluxConfiguration
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

    @Bean
    public WebFluxResponseBodyResultHandler fzWebFluxResponseBodyResultHandler(
            ServerCodecConfigurer serverCodecConfigurer,
            RequestedContentTypeResolver contentTypeResolver,
            ReactiveAdapterRegistry reactiveAdapterRegistry,
            ApplicationContext applicationContext,
            OperateTemplate operateTemplate)
    {
        return new WebFluxResponseBodyResultHandler(serverCodecConfigurer.getWriters(),
                                                    contentTypeResolver,
                                                    reactiveAdapterRegistry,
                                                    applicationContext,
                                                    operateTemplate);
    }
}
