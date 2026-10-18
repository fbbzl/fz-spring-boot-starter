package io.github.fbbzl.starter.webflux.customizer;

import io.github.fbbzl.starter.openapi.AbstractQuestOperationCustomizer;
import io.github.fbbzl.starter.webflux.BaseCrudController;
import io.github.fbbzl.starter.webflux.Q;
import org.springframework.core.MethodParameter;
import org.springframework.web.method.HandlerMethod;

/**
 * Adapts shared OpenAPI customization to this web stack.
 */
public class QuestOperationCustomizer extends AbstractQuestOperationCustomizer
{
    @Override
    protected boolean supportsController(HandlerMethod handlerMethod)
    {
        return BaseCrudController.class.isAssignableFrom(handlerMethod.getBeanType());
    }

    @Override
    protected boolean supportsFallbackRequestBodyParameter(Class<?> parameterType)
    {
        return Q.class.isAssignableFrom(parameterType) || Q.FQ.class.isAssignableFrom(parameterType);
    }

    @Override
    protected boolean isMultipartFormDataParameter(MethodParameter methodParameter)
    {
        return Q.FQ.class.isAssignableFrom(methodParameter.getParameterType());
    }
}
