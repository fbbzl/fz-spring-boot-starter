package io.github.fbbzl.starter.audit.frame.aspect;


import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.ArrayUtil;
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
import org.aspectj.lang.annotation.Before;
import io.github.fbbzl.starter.core.util.Throws;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.NamedThreadLocal;
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
    @Autowired(required = false)
    BaseDal<AUDIT_LOG, ID> auditDal;

    protected abstract AUDIT_LOG buildAuditLog(AuditMethod auditLog);

    static final ThreadLocal<Long> METHOD_COST_TIME = new NamedThreadLocal<>("CostTime");

    @Before(value = "@annotation(audit)")
    public void doBefore(JoinPoint joinPoint, AuditMethod audit)
    {
        METHOD_COST_TIME.set(System.currentTimeMillis());
    }

    @AfterReturning(pointcut = "@annotation(audit)", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, AuditMethod audit, Object result)
    {
        doLog(joinPoint, audit, null, result);
    }

    @AfterThrowing(value = "@annotation(audit)", throwing = "err")
    public void doAfterThrowing(JoinPoint joinPoint, AuditMethod audit, Exception err)
    {
        doLog(joinPoint, audit, err, null);
    }

    protected void doLog(
            JoinPoint joinPoint,
            @NotNull AuditMethod audit,
            @Nullable Exception err,
            @Nullable Object jsonResult)
    {
        try {
            AUDIT_LOG auditLog = this.buildAuditLog(audit);
            auditLog.setModule(this.getModule(joinPoint));
            auditLog.setMethod(joinPoint.getSignature().getName());
            Long startTime = METHOD_COST_TIME.get();
            auditLog.setTimeCost(startTime != null ? System.currentTimeMillis() - startTime : -1L);

            if (audit.saveParam() && ArrayUtil.isNotEmpty(joinPoint.getArgs())) {
                auditLog.setParam(JSONUtil.toJsonStr(joinPoint.getArgs()));
            }

            if (err != null) {
                auditLog.setStatus(BaseAuditLog.STATUS_FAIL);
                auditLog.setErrorMsg(ExceptionUtil.getRootCauseMessage(err));
            }

            if (audit.saveResult() && jsonResult != null) {
                auditLog.setResult(JSONUtil.toJsonStr(jsonResult));
            }

            save(auditLog);
        }
        catch (Exception exp) {
            log.error("error occur", exp);
        }
        finally {
            METHOD_COST_TIME.remove();
        }
    }

    protected String getModule(JoinPoint joinPoint)
    {
        Class<?> targetClass = AopUtils.getTargetClass(joinPoint.getTarget());

        AuditModule auditModule = AnnotationUtil.getAnnotation(targetClass, AuditModule.class);
        Throws.ifNull(auditModule, "missing @AuditModule on class [{}]", targetClass.getName());
        Throws.ifNull(auditModule.value(), "@AuditModule value can not be blank on class [{}]", targetClass.getName());

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
