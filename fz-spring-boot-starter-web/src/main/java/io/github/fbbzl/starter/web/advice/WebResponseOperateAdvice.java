package io.github.fbbzl.starter.web.advice;

import cn.crane4j.core.support.OperateTemplate;
import io.github.fbbzl.starter.web.R;
import io.github.fbbzl.starter.web.annotation.IgnoreResponseOperate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Assemble response payload data after the global response wrapper has normalized the return value.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026/4/29
 */
@Order(AdviceOrder.WEB_RESPONSE_OPERATE)
@RestControllerAdvice
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public final class WebResponseOperateAdvice extends ApplicationResponseAdvice implements ResponseBodyAdvice<Object>
{
    OperateTemplate operateTemplate;

    @Override
    public boolean supports(@NonNull MethodParameter returnType, @NonNull Class<? extends HttpMessageConverter<?>> converterType)
    {
        return supportsResponse(returnType, converterType, IgnoreResponseOperate.class);
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
        if (body instanceof R<?> res && res.getData() != null) {
            operateTemplate.execute(res.getData());
        }
        return body;
    }
}
