package io.github.fbbzl.starter.webflux.advice;

import cn.crane4j.core.executor.AsyncBeanOperationExecutor;
import cn.crane4j.core.support.OperateTemplate;
import io.github.fbbzl.starter.webflux.R;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.HandlerResult;
import org.springframework.web.reactive.accept.HeaderContentTypeResolver;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class WebFluxResponseBodyResultHandlerTest
{

    private WebFluxResponseBodyResultHandler handler;

    @BeforeEach
    void setUp()
    {
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        AutoConfigurationPackages.register(applicationContext, "io.github.fbbzl.starter.webflux.advice");

        handler = new WebFluxResponseBodyResultHandler(
                ServerCodecConfigurer.create().getWriters(),
                new HeaderContentTypeResolver(),
                ReactiveAdapterRegistry.getSharedInstance(),
                applicationContext,
                mock(OperateTemplate.class),
                mock(AsyncBeanOperationExecutor.class));
    }

    @Test
    void shouldWrapMonoValueIntoR() throws Exception
    {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test/mono"));
        HandlerResult result = handlerResult("mono", Mono.just("mono-value"));

        handler.handleResult(exchange, result).block();

        assertThat(exchange.getResponse().getBodyAsString().block())
                .contains("\"code\":\"200\"", "\"success\":true", "\"data\":\"mono-value\"");
    }

    @Test
    void shouldWrapSyncValueIntoR() throws Exception
    {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test/sync"));
        HandlerResult result = handlerResult("sync", "sync-value");

        handler.handleResult(exchange, result).block();

        assertThat(exchange.getResponse().getBodyAsString().block())
                .contains("\"code\":\"200\"", "\"success\":true", "\"data\":\"sync-value\"");
    }

    @Test
    void shouldPassFluxThroughWithoutWrapping() throws Exception
    {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test/flux").accept(MediaType.APPLICATION_JSON));
        Flux<String> flux = Flux.interval(Duration.ofMillis(10))
                                .take(3)
                                .map(i -> switch (i.intValue()) {
                                    case 0 -> "a";
                                    case 1 -> "b";
                                    default -> "c";
                                });
        HandlerResult result = handlerResult("flux", flux);

        handler.handleResult(exchange, result).block();

        String body = exchange.getResponse().getBodyAsString().block();
        assertThat(body)
                .contains("a", "b", "c")
                .doesNotContain("\"code\"", "\"success\"", "\"data\"");
    }

    @Test
    void shouldReturnRAsIsWhenControllerReturnsR() throws Exception
    {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test/r"));
        HandlerResult result = handlerResult("r", R.ok("r-value"));

        handler.handleResult(exchange, result).block();

        assertThat(exchange.getResponse().getBodyAsString().block())
                .contains("\"code\":\"200\"", "\"success\":true", "\"data\":\"r-value\"");
    }

    private HandlerResult handlerResult(String methodName, Object returnValue) throws Exception
    {
        WrapTestController controller = new WrapTestController();
        MethodParameter methodParameter = MethodParameter.forExecutable(
                WrapTestController.class.getDeclaredMethod(methodName), -1);
        return new HandlerResult(controller, returnValue, methodParameter, new BindingContext());
    }

    @RestController
    static class WrapTestController
    {

        @GetMapping("/test/mono")
        Mono<String> mono()
        {
            return Mono.just("mono-value");
        }

        @GetMapping("/test/sync")
        String sync()
        {
            return "sync-value";
        }

        @GetMapping("/test/flux")
        Flux<String> flux()
        {
            return Flux.interval(Duration.ofMillis(10))
                       .take(3)
                       .map(i -> switch (i.intValue()) {
                           case 0 -> "a";
                           case 1 -> "b";
                           default -> "c";
                       });
        }

        @GetMapping("/test/r")
        R<String> r()
        {
            return R.ok("r-value");
        }
    }
}
