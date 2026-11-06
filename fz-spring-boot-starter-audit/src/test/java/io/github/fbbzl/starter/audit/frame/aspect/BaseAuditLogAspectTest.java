package io.github.fbbzl.starter.audit.frame.aspect;

import io.github.fbbzl.starter.audit.BaseAuditLog;
import io.github.fbbzl.starter.audit.frame.annotation.AuditMethod;
import io.github.fbbzl.starter.audit.frame.annotation.AuditModule;
import cn.hutool.json.JSONUtil;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.InputStreamResource;

import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;

class BaseAuditLogAspectTest
{

    @Test
    void shouldAuditErrorThrowable()
    {
        AtomicBoolean logged = new AtomicBoolean(false);

        TestAuditLogAspect aspect = new TestAuditLogAspect(logged);
        JoinPoint   joinPoint = mock(JoinPoint.class);
        AuditMethod audit     = mock(AuditMethod.class);

        when(joinPoint.getTarget()).thenReturn(new AnnotatedService());
        when(joinPoint.getSignature()).thenReturn(new TestSignature());

        aspect.doAfterThrowing(joinPoint, audit, new OutOfMemoryError("oom"));

        assertThat(logged).isTrue();
    }

    @Test
    void shouldSkipDirectInfrastructureWithoutReadingOrChangingBusinessFields()
    {
        InputStream stream = mock(InputStream.class);
        Map<String, String> business = Map.of("password", "user-managed", "name", "ordinary");
        Object[] args = {stream, new InputStreamResource(stream), Path.of("example.txt"), business};
        TestAuditLogAspect aspect = new TestAuditLogAspect(new AtomicBoolean());

        aspect.doAfterReturning(joinPoint(args), options(true, true), stream);

        assertThat(aspect.saved.getParam()).isEqualTo(JSONUtil.toJsonStr(new Object[]{business}));
        assertThat(aspect.saved.getResult()).isNull();
        assertThat(args[0]).isSameAs(stream);
        assertThat(business.get("password")).isEqualTo("user-managed");
        verifyNoInteractions(stream);
    }

    @Test
    void shouldKeepSaveOptionsAndTextLimits()
    {
        TestAuditLogAspect aspect = new TestAuditLogAspect(new AtomicBoolean());
        String text = "x".repeat(11000);
        aspect.doAfterReturning(joinPoint(new Object[]{text}), options(true, true), text);
        assertThat(aspect.saved.getParam()).hasSize(10000);
        assertThat(aspect.saved.getResult()).hasSize(10000);

        aspect.doAfterReturning(joinPoint(new Object[]{text}), options(false, false), text);
        assertThat(aspect.saved.getParam()).isNull();
        assertThat(aspect.saved.getResult()).isNull();
    }

    private JoinPoint joinPoint(Object[] args)
    {
        JoinPoint joinPoint = mock(JoinPoint.class);
        when(joinPoint.getTarget()).thenReturn(new AnnotatedService());
        when(joinPoint.getSignature()).thenReturn(new TestSignature());
        when(joinPoint.getArgs()).thenReturn(args);
        return joinPoint;
    }

    private AuditMethod options(boolean params, boolean result)
    {
        AuditMethod audit = mock(AuditMethod.class);
        when(audit.saveParam()).thenReturn(params);
        when(audit.saveResult()).thenReturn(result);
        return audit;
    }

    @AuditModule("test-module")
    static class AnnotatedService
    {
    }

    static class TestSignature implements org.aspectj.lang.Signature
    {
        @Override
        public String toShortString() { return "test"; }

        @Override
        public String toLongString() { return "test"; }

        @Override
        public String getName() { return "testMethod"; }

        @Override
        public int getModifiers() { return 0; }

        @Override
        public Class getDeclaringType() { return AnnotatedService.class; }

        @Override
        public String getDeclaringTypeName() { return AnnotatedService.class.getName(); }
    }

    static class TestAuditLog extends BaseAuditLog<Long> implements Serializable
    {
        private Long          id;
        private Long          createdBy;
        private Long          updatedBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime deletedAt;

        @Override
        public Long getId() { return id; }

        @Override
        public void setId(Long id) { this.id = id; }

        @Override
        public Long getCreatedBy() { return createdBy; }

        @Override
        public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

        @Override
        public Long getUpdatedBy() { return updatedBy; }

        @Override
        public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }

        @Override
        public LocalDateTime getCreatedAt() { return createdAt; }

        @Override
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        @Override
        public LocalDateTime getUpdatedAt() { return updatedAt; }

        @Override
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

        @Override
        public LocalDateTime getDeletedAt() { return deletedAt; }

        @Override
        public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
    }

    static class TestAuditLogAspect extends BaseAuditLogAspect<Long, TestAuditLog>
    {
        private final AtomicBoolean logged;
        private TestAuditLog saved;

        TestAuditLogAspect(AtomicBoolean logged) { this.logged = logged; }

        @Override
        protected TestAuditLog buildAuditLog(AuditMethod auditLog)
        {
            return new TestAuditLog();
        }

        @Override
        public void save(TestAuditLog audit)
        {
            saved = audit;
            logged.set(true);
        }
    }
}
