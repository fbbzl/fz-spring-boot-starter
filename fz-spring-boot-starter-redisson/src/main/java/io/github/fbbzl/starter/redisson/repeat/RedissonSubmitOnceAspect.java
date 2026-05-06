package io.github.fbbzl.starter.redisson.repeat;

import cn.hutool.core.util.ReflectUtil;
import io.github.fbbzl.starter.core.exception.BizException;
import io.github.fbbzl.starter.core.exception.ExceptionVerb;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.fz.erwin.exception.Throws;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;

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

    @Autowired
    HttpServletRequest request;
    final RedissonClient redissonClient;

    @Around("@annotation(submitOnce)")
    public Object around(ProceedingJoinPoint point, SubmitOnce submitOnce) throws Throwable
    {
        Throws.ifTrue(submitOnce.windowMillis() <= 0, "submit once window millis must be positive");

        String expression = submitOnce.expression();
        String identity =
                toStr(defaultIfNull(evaluateExpression(point, expression),
                                    () -> findIdentity(point.getArgs(), expression)), EMPTY);
        Throws.ifBlank(identity, "identity can not be blank, expression: {}", expression);

        String          key      = buildSubmitOnceKey(submitOnce, identity);
        RBucket<String> bucket   = redissonClient.getBucket(key);
        boolean         acquired = bucket.setIfAbsent("1", Duration.ofMillis(submitOnce.windowMillis()));

        if (acquired) {
            return point.proceed();
        }

        log.warn("submit once blocked, key: {}", key);
        throw new BizException(ExceptionVerb.DATA_CONFLICT, submitOnce.message());
    }

    private Object evaluateExpression(ProceedingJoinPoint point, String expression)
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
        catch (EvaluationException | ParseException ignored) {
            return null;
        }
    }

    private String buildSubmitOnceKey(SubmitOnce submitOnce, String identity)
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
