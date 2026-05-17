package io.github.fbbzl.starter.webflux.config;

import cn.crane4j.core.support.OperateTemplate;
import io.github.fbbzl.starter.webflux.advice.WebExceptionAdvice;
import io.github.fbbzl.starter.webflux.advice.WebFluxResponseBodyResultHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
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
