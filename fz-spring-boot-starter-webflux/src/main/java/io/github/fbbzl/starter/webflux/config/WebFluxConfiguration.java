package io.github.fbbzl.starter.webflux.config;

import cn.crane4j.core.executor.AsyncBeanOperationExecutor;
import cn.crane4j.core.parser.handler.strategy.PropertyMappingStrategyManager;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.OperateTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.fbbzl.starter.pojo.crane4j.AssembleBeanAnnotationHandler;
import io.github.fbbzl.starter.webflux.advice.WebExceptionAdvice;
import io.github.fbbzl.starter.webflux.advice.WebFluxResponseBodyResultHandler;
import io.github.fbbzl.starter.webflux.codec.Crane4jJsonDecoder;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/5/17
 */
@Import(WebExceptionAdvice.class)
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class WebFluxConfiguration implements WebFluxConfigurer
{

    private final ObjectMapper               objectMapper;
    private final OperateTemplate            operateTemplate;
    private final AsyncBeanOperationExecutor asyncBeanOperationExecutor;

    public WebFluxConfiguration(
            ObjectMapper objectMapper,
            OperateTemplate operateTemplate,
            AsyncBeanOperationExecutor asyncBeanOperationExecutor)
    {
        this.objectMapper = objectMapper;
        this.operateTemplate = operateTemplate;
        this.asyncBeanOperationExecutor = asyncBeanOperationExecutor;
    }

    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer)
    {
        configurer.defaultCodecs().jackson2JsonDecoder(
                new Crane4jJsonDecoder(objectMapper, operateTemplate, asyncBeanOperationExecutor));
    }

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
    public WebFluxResponseBodyResultHandler webFluxResponseBodyResultHandler(
            ServerCodecConfigurer serverCodecConfigurer,
            RequestedContentTypeResolver contentTypeResolver,
            ReactiveAdapterRegistry reactiveAdapterRegistry,
            ApplicationContext applicationContext,
            OperateTemplate operateTemplate,
            AsyncBeanOperationExecutor asyncBeanOperationExecutor)
    {
        return new WebFluxResponseBodyResultHandler(serverCodecConfigurer.getWriters(),
                                                    contentTypeResolver,
                                                    reactiveAdapterRegistry,
                                                    applicationContext,
                                                    operateTemplate,
                                                    asyncBeanOperationExecutor);
    }

}
