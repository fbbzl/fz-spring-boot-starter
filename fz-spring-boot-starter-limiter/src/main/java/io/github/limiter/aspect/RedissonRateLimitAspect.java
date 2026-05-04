package io.github.limiter.aspect;

import io.github.limiter.annotation.RateLimit;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/4/22 10:20
 */

@Aspect
@Component
public class RedissonRateLimitAspect
{

    RedissonClient redissonClient;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint point, RateLimit rateLimit) throws Throwable
    {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        String       key         = buildRateLimitKey(request, rateLimit);
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);

        // 设置限流规则
        // RateType.OVERALL: 全局限流
        // RateType.PER_CLIENT: 按客户端限流
        RateType rateType = rateLimit.type() == RateLimitType.OVERALL ? RateType.OVERALL : RateType.PER_CLIENT;

        // 初始化限流器：每timeWindow时间内产生permits个令牌
        if (!rateLimiter.isExists()) {
            rateLimiter.trySetRate(
                    rateType,
                    rateLimit.permits(),
                    rateLimit.timeWindow(),
                    convertToRateIntervalUnit(rateLimit.unit())
                                  );
        }

        // 尝试获取令牌，不阻塞
        boolean acquired = rateLimiter.tryAcquire();

        if (acquired) {
            return point.proceed();
        } else {
            // 获取剩余等待时间用于更友好的错误提示
            long   remainTime = rateLimiter.remainWaitTime();
            String message    = rateLimit.message() + (remainTime > 0 ? "，请" + (remainTime / 1000) + "秒后重试" : "");
            throw new RuntimeException(message);
        }
    }

    private String buildRateLimitKey(HttpServletRequest request, RateLimit rateLimit)
    {
        StringBuilder keyBuilder = new StringBuilder(rateLimit.keyPrefix());

        if (rateLimit.byIp()) {
            keyBuilder.append(getIpAddress(request)).append(":");
        }

        String methodName = request.getRequestURI();
        keyBuilder.append(methodName.replace("/", "_"));

        return keyBuilder.toString();
    }

    private RateIntervalUnit convertToRateIntervalUnit(java.util.concurrent.TimeUnit timeUnit)
    {
        switch (timeUnit) {
            case SECONDS:
                return RateIntervalUnit.SECONDS;
            case MINUTES:
                return RateIntervalUnit.MINUTES;
            case HOURS:
                return RateIntervalUnit.HOURS;
            case DAYS:
                return RateIntervalUnit.DAYS;
            default:
                return RateIntervalUnit.SECONDS;
        }
    }

    private String getIpAddress(HttpServletRequest request)
    {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}