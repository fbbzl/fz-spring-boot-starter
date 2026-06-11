package io.github.fbbzl.starter.redisson.repeat;

import cn.hutool.core.util.ReflectUtil;
import io.github.fbbzl.starter.core.exception.BizException;
import io.github.fbbzl.starter.core.exception.ExceptionVerb;
import org.fz.erwin.exception.Throws;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.Duration;

import static cn.hutool.core.convert.Convert.toStr;
import static cn.hutool.core.util.ObjectUtil.defaultIfNull;
import static cn.hutool.core.util.StrUtil.*;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/4/22 10:20
 */

@Slf4j
@Aspect
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RedissonSubmitOnceAspect
{

    static final ExpressionParser        EXPRESSION_PARSER         = new SpelExpressionParser();
    static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    final RedissonClient redissonClient;

    @Around("@annotation(submitOnce)")
    public Object around(ProceedingJoinPoint point, SubmitOnce submitOnce) throws Throwable
    {
        Throws.ifTrue(submitOnce.windowMillis() <= 0, "submit once window millis must be positive");

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes attributes = requestAttributes instanceof ServletRequestAttributes sra ? sra : null;
        HttpServletRequest       request    = attributes != null ? attributes.getRequest() : null;
        if (request == null) {
            log.warn("SubmitOnce aspect invoked without web request context, bypassing limit");
            return point.proceed();
        }

        String expression = submitOnce.expression();
        String identity =
                toStr(defaultIfNull(evaluateExpression(request, point, expression),
                                    () -> findIdentity(point.getArgs(), expression)), EMPTY);
        Throws.ifBlank(identity, "identity can not be blank, expression: {}", expression);

        String          key      = buildSubmitOnceKey(request, submitOnce, identity);
        RBucket<String> bucket   = redissonClient.getBucket(key);
        boolean         acquired = bucket.setIfAbsent("1", Duration.ofMillis(submitOnce.windowMillis()));

        if (acquired) {
            return point.proceed();
        }

        log.warn("submit once blocked, key: {}", key);
        throw new BizException(ExceptionVerb.DATA_CONFLICT, submitOnce.message());
    }

    private Object evaluateExpression(HttpServletRequest request, ProceedingJoinPoint point, String expression)
    {
        if (isBlank(expression)) return null;

        try {
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method          method    = signature.getMethod();

            MethodBasedEvaluationContext context =
                    new MethodBasedEvaluationContext(point.getTarget(), method, point.getArgs(), PARAMETER_NAME_DISCOVERER);
            context.setVariable("request", request);
            context.setVariable("method", method);
            context.setVariable("target", point.getTarget());
            context.setVariable("targetClass", point.getTarget() == null ? null : point.getTarget().getClass());

            return EXPRESSION_PARSER.parseExpression(expression).getValue(context);
        }
        catch (EvaluationException | ParseException e) {
            log.warn("SpEL expression evaluation failed, expression: {}. Error: {}", expression, e.getMessage());
            return null;
        }
    }

    private String buildSubmitOnceKey(HttpServletRequest request, SubmitOnce submitOnce, String identity)
    {
        return format("{}{}:{}:{}",
                      blankToDefault(submitOnce.keyPrefix(), "submit_once:"),
                      request.getMethod(),
                      request.getRequestURI().replace("/", "_"),
                      identity);
    }

    private String findIdentity(Object[] args, String expression)
    {
        if (args == null) return null;

        for (Object arg : args) {
            Object value    = arg == null || isBlank(expression) ? null : ReflectUtil.getFieldValue(arg, expression);
            String identity = value == null ? null : value.toString();
            if (!isBlank(identity)) return identity;
        }

        return null;
    }
}
