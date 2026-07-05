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
        private Long          createBy;
        private Long          updateBy;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;

        @Override
        public Long getId() { return id; }

        @Override
        public void setId(Long id) { this.id = id; }

        @Override
        public Long getCreateBy() { return createBy; }

        @Override
        public void setCreateBy(Long createBy) { this.createBy = createBy; }

        @Override
        public Long getUpdateBy() { return updateBy; }

        @Override
        public void setUpdateBy(Long updateBy) { this.updateBy = updateBy; }

        @Override
        public LocalDateTime getCreateTime() { return createTime; }

        @Override
        public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

        @Override
        public LocalDateTime getUpdateTime() { return updateTime; }

        @Override
        public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
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
