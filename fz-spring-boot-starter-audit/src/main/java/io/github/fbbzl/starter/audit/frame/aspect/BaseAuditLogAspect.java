package io.github.fbbzl.starter.audit.frame.aspect;


import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.github.fbbzl.starter.audit.BaseAuditLog;
import io.github.fbbzl.starter.audit.frame.annotation.AuditMethod;
import io.github.fbbzl.starter.audit.frame.annotation.AuditModule;
import io.github.fbbzl.starter.core.util.Futures;
import io.github.fbbzl.starter.dal.BaseDal;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.fz.erwin.exception.Throws;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.io.Serializable;

/**
 *
 * @author fz
 * @version 1.0
 * @since 2025/9/4
 */
@Slf4j
@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class BaseAuditLogAspect<ID extends Serializable, AUDIT_LOG extends BaseAuditLog<ID>>
{
    private static final int MAX_AUDIT_TEXT_LENGTH = 10000;

    @Autowired(required = false)
    BaseDal<AUDIT_LOG, ID> auditDal;

    protected abstract AUDIT_LOG buildAuditLog(AuditMethod auditLog);

    @AfterReturning(pointcut = "@annotation(audit)", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, AuditMethod audit, Object result)
    {
        doLog(joinPoint, audit, null, result);
    }

    @AfterThrowing(value = "@annotation(audit)", throwing = "err")
    public void doAfterThrowing(JoinPoint joinPoint, AuditMethod audit, Throwable err)
    {
        doLog(joinPoint, audit, err, null);
    }

    protected void doLog(
            JoinPoint joinPoint,
            @NotNull AuditMethod audit,
            @Nullable Throwable err,
            @Nullable Object jsonResult)
    {
        try {
            AUDIT_LOG auditLog = this.buildAuditLog(audit);
            auditLog.setModule(this.getModule(joinPoint));
            auditLog.setMethod(joinPoint.getSignature().getName());

            if (audit.saveParam() && ArrayUtil.isNotEmpty(joinPoint.getArgs())) {
                auditLog.setParam(StrUtil.subPre(JSONUtil.toJsonStr(joinPoint.getArgs()), MAX_AUDIT_TEXT_LENGTH));
            }

            if (err != null) {
                auditLog.setStatus(BaseAuditLog.STATUS_FAIL);
                auditLog.setErrorMsg(ExceptionUtil.getRootCauseMessage(err));
            }

            if (audit.saveResult() && jsonResult != null) {
                auditLog.setResult(StrUtil.subPre(JSONUtil.toJsonStr(jsonResult), MAX_AUDIT_TEXT_LENGTH));
            }

            save(auditLog);
        }
        catch (Exception exp) {
            log.error("error occur", exp);
        }
    }

    protected String getModule(JoinPoint joinPoint)
    {
        Class<?> targetClass = AopUtils.getTargetClass(joinPoint.getTarget());

        AuditModule auditModule = AnnotationUtil.getAnnotation(targetClass, AuditModule.class);
        Throws.ifNull(auditModule, "missing @AuditModule on class [{}]", targetClass.getName());
        Throws.ifBlank(auditModule.value(), "@AuditModule value can not be blank on class [{}]", targetClass.getName());

        return auditModule.value();
    }

    public void save(AUDIT_LOG audit)
    {
        if (auditDal == null) {
            log.warn("there is no AuditLog-Repository found, will not save audit log");
            return;
        }

        Futures.runAsync(() -> auditDal.create(audit))
                .exceptionally(ex -> {
                    log.error("audit log save failed", ex);
                    return null;
                });
    }

}
