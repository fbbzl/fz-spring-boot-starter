package io.github.fbbzl.starter.web.advice;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.lang.annotation.Annotation;

/**
 * Shared support logic for response advices that should only apply to application controllers.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026/4/29
 */
@SuppressWarnings("all")
@FieldDefaults(level = AccessLevel.PROTECTED)
abstract sealed class ApplicationResponseAdvice permits WebResponseOperateAdvice, WebResponseWrapAdvice
{
    @Autowired
    ApplicationContext applicationContext;

    protected boolean supportsResponse(
            MethodParameter returnType,
            Class<? extends HttpMessageConverter<?>> converterType,
            Class<? extends Annotation> ignoreAnnotationType)
    {
        Class<?> containingClass = returnType.getContainingClass();
        return isApplicationController(containingClass)
               && !returnType.hasMethodAnnotation(ignoreAnnotationType)
               && !AnnotatedElementUtils.hasAnnotation(containingClass, ignoreAnnotationType)
               && supportsConverter(converterType);
    }

    protected boolean supportsConverter(Class<? extends HttpMessageConverter<?>> converterType)
    {
        return MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType)
               || StringHttpMessageConverter.class.isAssignableFrom(converterType);
    }

    protected boolean isApplicationController(Class<?> controllerClass)
    {
        if (!AutoConfigurationPackages.has(applicationContext)) return false;
        String className = controllerClass.getName();
        return AutoConfigurationPackages.get(applicationContext).stream()
                                        .anyMatch(basePackage -> className.startsWith(basePackage + "."));
    }
}
