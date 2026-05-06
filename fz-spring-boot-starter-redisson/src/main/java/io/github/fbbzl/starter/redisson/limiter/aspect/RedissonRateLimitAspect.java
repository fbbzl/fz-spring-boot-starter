package io.github.fbbzl.starter.redisson.limiter.aspect;

import cn.hutool.extra.servlet.JakartaServletUtil;
import io.github.fbbzl.starter.redisson.annotation.RateLimit;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;
import java.time.Duration;

import static java.time.temporal.ChronoUnit.MILLIS;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/4/22 10:20
 */

@Slf4j
@Aspect
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RedissonRateLimitAspect
{
    @Autowired
    HttpServletRequest request;
    final RedissonClient redissonClient;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint point, RateLimit rateLimit) throws Throwable
    {
        String       key         = buildRateLimitKey(request, point, rateLimit);
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);

        if (!rateLimiter.isExists()) {
            rateLimiter.trySetRate(
                    rateLimit.type(),
                    rateLimit.permits(),
                    Duration.of(rateLimit.timeWindowMillis(), MILLIS),
                    Duration.of(rateLimit.keepAliveMillis(), MILLIS));
        }

        boolean acquired = rateLimiter.tryAcquire();

        if (acquired)
            return point.proceed();
        else
            throw new UnavailableException(rateLimit.message());
    }

    private String buildRateLimitKey(HttpServletRequest request, ProceedingJoinPoint point, RateLimit rateLimit)
    {
        StringBuilder keyBuilder = new StringBuilder(rateLimit.keyPrefix());

        // 1 add ip
        if (rateLimit.byIp()) {
            String ip = JakartaServletUtil.getClientIP(request);
            keyBuilder.append(ip).append(":");
        }

        // 2 add method
        MethodSignature signature      = (MethodSignature) point.getSignature();
        Method          method         = signature.getMethod();
        String          fullMethodName = method.getDeclaringClass().getName() + "." + method.getName();
        keyBuilder.append(fullMethodName).append(":");

        // 3 add path
        keyBuilder.append(request.getRequestURI().replace("/", "_"));

        return keyBuilder.toString();
    }
}