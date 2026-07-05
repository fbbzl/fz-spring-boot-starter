package io.github.fbbzl.starter.redisson.repeat;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RedissonSubmitOnceAspectTest
{

    @BeforeEach
    void setUp()
    {
        HttpServletRequest request = new MockHttpServletRequest("POST", "/test");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown()
    {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void shouldReleaseLockWhenProceedThrowsException() throws Throwable
    {
        RedissonClient    redissonClient = mock(RedissonClient.class);
        @SuppressWarnings("unchecked")
        RBucket<Object> bucket          = mock(RBucket.class);
        when(redissonClient.getBucket(anyString())).thenReturn(bucket);
        when(bucket.setIfAbsent(anyString(), any(Duration.class))).thenReturn(true);

        ProceedingJoinPoint point = mock(ProceedingJoinPoint.class);
        MethodSignature     signature = mock(MethodSignature.class);
        when(point.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(TraceIdArg.class.getMethod("getTraceId"));
        when(point.getArgs()).thenReturn(new Object[] { new TraceIdArg() });
        when(point.proceed()).thenThrow(new RuntimeException("boom"));

        SubmitOnce submitOnce = mock(SubmitOnce.class);
        when(submitOnce.expression()).thenReturn("traceId");
        when(submitOnce.windowMillis()).thenReturn(5000L);
        when(submitOnce.message()).thenReturn("duplicate submit");
        when(submitOnce.keyPrefix()).thenReturn("submit_once:");

        RedissonSubmitOnceAspect aspect = new RedissonSubmitOnceAspect(redissonClient);

        assertThatThrownBy(() -> aspect.around(point, submitOnce))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("boom");

        verify(bucket).delete();
    }

    public static class TraceIdArg
    {
        private String traceId = "trace-123";

        public String getTraceId() { return traceId; }

        public void setTraceId(String traceId) { this.traceId = traceId; }
    }
}
