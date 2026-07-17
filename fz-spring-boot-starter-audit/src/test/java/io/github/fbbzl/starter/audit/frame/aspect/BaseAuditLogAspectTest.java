package io.github.fbbzl.starter.audit.frame.aspect;

import io.github.fbbzl.starter.audit.BaseAuditLog;
import io.github.fbbzl.starter.audit.frame.annotation.AuditMethod;
import io.github.fbbzl.starter.audit.frame.annotation.AuditModule;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

        TestAuditLogAspect(AtomicBoolean logged) { this.logged = logged; }

        @Override
        protected TestAuditLog buildAuditLog(AuditMethod auditLog)
        {
            return new TestAuditLog();
        }

        @Override
        public void save(TestAuditLog audit)
        {
            logged.set(true);
        }
    }
}
