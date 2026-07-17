package io.github.fbbzl.starter.redisson.limiter.aspect;

import io.github.fbbzl.starter.core.exception.BizException;
import io.github.fbbzl.starter.core.exception.ExceptionVerb;
import io.github.fbbzl.starter.redisson.annotation.RateLimit;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RedissonRateLimitAspectTest
{

    @BeforeEach
    void setUp()
    {
        HttpServletRequest request = new MockHttpServletRequest("GET", "/limited");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown()
    {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void shouldThrowRequestLimitedBizExceptionWhenAcquireFails() throws Throwable
    {
        RedissonClient redissonClient = mock(RedissonClient.class);
        RRateLimiter   rateLimiter    = mock(RRateLimiter.class);
        when(redissonClient.getRateLimiter(anyString())).thenReturn(rateLimiter);
        when(rateLimiter.tryAcquire()).thenReturn(false);

        ProceedingJoinPoint point     = mock(ProceedingJoinPoint.class);
        MethodSignature     signature = mock(MethodSignature.class);
        when(point.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(TestController.class.getMethod("limited"));

        RateLimit rateLimit = mock(RateLimit.class);
        when(rateLimit.keyPrefix()).thenReturn("rate_limit:");
        when(rateLimit.byIp()).thenReturn(false);
        when(rateLimit.type()).thenReturn(RateType.PER_CLIENT);
        when(rateLimit.permits()).thenReturn(1L);
        when(rateLimit.timeWindowMillis()).thenReturn(1000L);
        when(rateLimit.keepAliveMillis()).thenReturn(60_000L);
        when(rateLimit.message()).thenReturn("too many requests");

        RedissonRateLimitAspect aspect = new RedissonRateLimitAspect(redissonClient);

        assertThatThrownBy(() -> aspect.around(point, rateLimit))
                .isInstanceOf(BizException.class)
                .hasMessage("too many requests")
                .satisfies(error -> {
                    BizException bizException = (BizException) error;
                    org.assertj.core.api.Assertions.assertThat(bizException.getVerb())
                                                  .isEqualTo(ExceptionVerb.REQUEST_LIMITED);
                });
    }

    public static class TestController
    {
        public void limited() {}
    }
}
