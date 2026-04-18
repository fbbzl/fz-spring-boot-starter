package com.fz.starter.audit.frame.aspect;


import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.json.JSONUtil;
import com.fz.starter.audit.BaseAudit;
import com.fz.starter.audit.frame.annotation.AuditLog;
import com.fz.starter.dal.BaseDal;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.NamedThreadLocal;
import org.springframework.lang.Nullable;

import java.io.Serializable;

import static java.util.concurrent.CompletableFuture.runAsync;

/**
 *
 * @author fz
 * @version 1.0
 * @since 2025/9/4
 */
@Slf4j
@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class BaseAuditAspect<ID extends Serializable, AUDIT extends BaseAudit<ID>>
{
    @Autowired(required = false)
    BaseDal<AUDIT, ID> auditDal;

    protected abstract AUDIT getAudit(AuditLog auditLog);

    protected abstract void setCurrentUser(AUDIT audit);

    static final ThreadLocal<Long> METHOD_COST_TIME = new NamedThreadLocal<>("CostTime");

    @Before(value = "@annotation(auditLog)")
    public void doBefore(JoinPoint joinPoint, AuditLog auditLog)
    {
        METHOD_COST_TIME.set(System.currentTimeMillis());
    }

    @AfterReturning(pointcut = "@annotation(auditLog)", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, AuditLog auditLog, Object result)
    {
        handleLog(joinPoint, auditLog, null, result);
    }

    @AfterThrowing(value = "@annotation(auditLog)", throwing = "err")
    public void doAfterThrowing(JoinPoint joinPoint, AuditLog auditLog, Exception err)
    {
        handleLog(joinPoint, auditLog, err, null);
    }

    protected void handleLog(
            JoinPoint joinPoint,
            @NotNull AuditLog audit,
            @Nullable Exception err,
            @Nullable Object jsonResult)
    {
        try {
            AUDIT auditLog = this.getAudit(audit);
            auditLog.setModule(audit.module());
            this.setCurrentUser(auditLog);
            auditLog.setMethod(joinPoint.getSignature().getName());
            auditLog.setTimeCost(System.currentTimeMillis() - METHOD_COST_TIME.get());

            if (audit.saveParam() && ArrayUtil.isNotEmpty(joinPoint.getArgs())) {
                auditLog.setParam(JSONUtil.toJsonStr(joinPoint.getArgs()));
            }

            if (err != null) {
                auditLog.setStatus(BaseAudit.STATUS_FAIL);
                auditLog.setErrorMsg(ExceptionUtil.getRootCauseMessage(err));
            }

            if (audit.saveResult() && jsonResult != null) {
                auditLog.setResult(JSONUtil.toJsonStr(jsonResult));
            }

            save(auditLog);
        }
        catch (Exception exp) {
            log.error("error occur:{}", exp.getMessage());
        }
        finally {
            METHOD_COST_TIME.remove();
        }
    }

    public void save(AUDIT audit)
    {
        if (auditDal != null) {
            runAsync(() -> auditDal.create(audit));
        }
    }

}
