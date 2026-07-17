package io.github.fbbzl.starter.webflux.advice;

import cn.crane4j.core.executor.AsyncBeanOperationExecutor;
import cn.crane4j.core.support.Grouped;
import cn.crane4j.core.support.OperateTemplate;
import io.github.fbbzl.starter.webflux.R;
import io.github.fbbzl.starter.webflux.annotation.IgnoreResponseOperate;
import io.github.fbbzl.starter.webflux.annotation.IgnoreResponseWrap;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.reactive.HandlerResult;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;
import org.springframework.web.reactive.result.method.annotation.ResponseBodyResultHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

/**
 * WebFlux result handler that applies the same response wrapping policy as the MVC starter.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026/5/17
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WebFluxResponseBodyResultHandler extends ResponseBodyResultHandler
{
    ApplicationContext applicationContext;
    OperateTemplate    operateTemplate;
    AsyncBeanOperationExecutor asyncBeanOperationExecutor;
    MethodParameter    responseType;
    MethodParameter    monoResponseType;

    public WebFluxResponseBodyResultHandler(
            List<HttpMessageWriter<?>> writers,
            RequestedContentTypeResolver resolver,
            ReactiveAdapterRegistry registry,
            ApplicationContext applicationContext,
            OperateTemplate operateTemplate,
            AsyncBeanOperationExecutor asyncBeanOperationExecutor)
    {
        super(writers, resolver, registry);
        setOrder(AdviceOrder.WEB_RESPONSE_HANDLER);
        this.applicationContext = applicationContext;
        this.operateTemplate = operateTemplate;
        this.asyncBeanOperationExecutor = asyncBeanOperationExecutor;
        this.responseType = methodReturnType("response");
        this.monoResponseType = methodReturnType("monoResponse");
    }

    @Override
    public boolean supports(@NonNull HandlerResult result)
    {
        return super.supports(result)
               && isApplicationController(result.getReturnTypeSource().getContainingClass())
               && !isHttpEntity(result)
               && !isReactiveVoid(result);
    }

    @NonNull
    @Override
    public Mono<Void> handleResult(@NonNull ServerWebExchange exchange, @NonNull HandlerResult result)
    {
        return doHandleResult(result).flatMap(handledResult -> super.handleResult(exchange, handledResult));
    }

    private Mono<HandlerResult> doHandleResult(HandlerResult result)
    {
        MethodParameter returnType = result.getReturnTypeSource();
        Object          body       = result.getReturnValue();

        if (hasAnnotation(returnType, IgnoreResponseWrap.class)) {
            return Mono.just(result);
        }

        if (body instanceof Mono<?> mono) {
            return wrapAsync(mono, returnType).map(wrapped -> newResult(result, wrapped, monoResponseType));
        }
        if (body instanceof Flux<?>) {
            return Mono.just(result);
        }
        return Mono.fromCallable(() -> wrapValue(body, returnType))
                   .subscribeOn(Schedulers.boundedElastic())
                   .map(wrapped -> newResult(result, wrapped, responseType));
    }

    private Mono<R<?>> wrapAsync(Mono<?> mono, MethodParameter returnType)
    {
        return mono.flatMap(value -> wrapValueAsync(value, returnType))
                   .switchIfEmpty(wrapValueAsync(null, returnType));
    }

    private Mono<R<?>> wrapValueAsync(@Nullable Object body, MethodParameter returnType)
    {
        return Mono.<R<?>>fromCallable(() -> wrapValue(body, returnType))
                   .subscribeOn(Schedulers.boundedElastic());
    }

    private R<?> wrapValue(@Nullable Object body, MethodParameter returnType)
    {
        if (!hasAnnotation(returnType, IgnoreResponseOperate.class)) {
            Object operateBody = body instanceof R<?> response ? response.getData() : body;
            if (operateBody != null) {
                operate(operateBody);
            }
        }
        return body instanceof R<?> response ? response : R.ok(body);
    }

    private void operate(Object data)
    {
        Object operateData = data instanceof R.PR<?> page ? page.records() : data;
        operateTemplate.execute(operateData, asyncBeanOperationExecutor, Grouped.alwaysMatch());
    }

    private HandlerResult newResult(HandlerResult original, Object body, MethodParameter type)
    {
        return new HandlerResult(original.getHandler(), body, type, original.getBindingContext())
                .setExceptionHandler(original.getExceptionHandler());
    }

    private boolean hasAnnotation(MethodParameter returnType, Class<? extends Annotation> annotationType)
    {
        Class<?> containingClass = returnType.getContainingClass();
        return returnType.hasMethodAnnotation(annotationType)
               || AnnotatedElementUtils.hasAnnotation(containingClass, annotationType);
    }

    private boolean isHttpEntity(HandlerResult result)
    {
        ResolvableType returnType = result.getReturnType();
        if (HttpEntity.class.isAssignableFrom(returnType.toClass())) {
            return true;
        }
        if (!Mono.class.isAssignableFrom(returnType.toClass())) {
            return false;
        }

        return HttpEntity.class.isAssignableFrom(returnType.getGeneric(0).toClass());
    }

    private boolean isReactiveVoid(HandlerResult result)
    {
        ResolvableType returnType = result.getReturnType();
        Class<?>       rawType    = returnType.toClass();
        if (Void.TYPE.equals(rawType) || Void.class.equals(rawType)) {
            return true;
        }
        if (!Mono.class.isAssignableFrom(rawType)) {
            return false;
        }
        Class<?> genericType = returnType.getGeneric(0).toClass();
        return Void.class.equals(genericType);
    }

    private boolean isApplicationController(Class<?> controllerClass)
    {
        if (!AutoConfigurationPackages.has(applicationContext)) return false;
        String className = controllerClass.getName();
        return AutoConfigurationPackages.get(applicationContext).stream()
                                        .anyMatch(basePackage -> className.startsWith(basePackage + "."));
    }

    private static MethodParameter methodReturnType(String methodName)
    {
        try {
            Method method = WebFluxResponseBodyResultHandler.class.getDeclaredMethod(methodName);
            return new MethodParameter(method, -1);
        }
        catch (NoSuchMethodException e) {
            throw new IllegalStateException("Failed to resolve WebFlux response wrapper return type", e);
        }
    }

    @SuppressWarnings("unused")
    private static R<?> response()
    {
        return null;
    }

    @SuppressWarnings("unused")
    private static Mono<R<?>> monoResponse()
    {
        return null;
    }
}
