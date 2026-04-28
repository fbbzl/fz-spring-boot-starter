package io.github.fbbzl.starter.web.advice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.fbbzl.starter.web.R;
import io.github.fbbzl.starter.web.annotation.IgnoreResponseWrap;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Wrap controller responses into {@link R} before any downstream response advice mutates the payload.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026/4/29
 */
@Slf4j
@Order(AdviceOrder.WEB_RESPONSE_WRAP)
@RestControllerAdvice
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public final class WebResponseWrapAdvice extends ApplicationResponseAdvice implements ResponseBodyAdvice<Object>
{
    ObjectMapper objectMapper;

    @Override
    public boolean supports(@NonNull MethodParameter returnType, @NonNull Class<? extends HttpMessageConverter<?>> converterType)
    {
        return supportsResponse(returnType, converterType, IgnoreResponseWrap.class);
    }

    @Override
    public @Nullable Object beforeBodyWrite(
            @Nullable Object body,
            @NonNull MethodParameter returnType,
            @NonNull MediaType selectedContentType,
            @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
            @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response)
    {
        if (response instanceof ServletServerHttpResponse servletResponse
            && HttpStatus.valueOf(servletResponse.getServletResponse().getStatus()).isError()) return body;

        R<?> res = body instanceof R<?> result ? result : R.ok(body);
        if (StringHttpMessageConverter.class.isAssignableFrom(selectedConverterType)) {
            try {
                return objectMapper.writeValueAsString(res);
            }
            catch (JsonProcessingException e) {
                log.error("serialize wrapped string response failed", e);
                throw new IllegalStateException("serialize wrapped string response failed", e);
            }
        }
        return res;
    }
}
