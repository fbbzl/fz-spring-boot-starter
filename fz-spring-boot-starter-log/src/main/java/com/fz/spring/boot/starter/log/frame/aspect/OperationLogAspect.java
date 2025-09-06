package com.fz.spring.boot.starter.log.frame.aspect;

import com.fz.spring.boot.starter.log.components.OperationLogRepository;
import com.fz.spring.boot.starter.log.components.entity.OperationLog;
import com.fz.spring.boot.starter.log.frame.annotation.EnableOperationLog;
import com.fz.spring.boot.starter.log.frame.annotation.Log;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;

import static java.util.concurrent.CompletableFuture.runAsync;

/**
 * @author fz
 * @version 1.0
 * @since 2025/9/4
 */
@Slf4j
@Aspect
@RequiredArgsConstructor
@ConditionalOnBean(annotation = EnableOperationLog.class)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public abstract class OperationLogAspect {

    OperationLogRepository operationLogRepo;

    @Around("@annotation(operateLog)")
    public Object recordLog(ProceedingJoinPoint joinPoint, Log operateLog) throws Throwable {
        OperationLog.OperationLogBuilder logBuilder =
                OperationLog.builder()
                            .type(operateLog.type())
                            .description(operateLog.description())
                            .operatorId(getCurrentUserId())
                            .operatorName(getCurrentUserName());

        String errorMsg = null;

        try {
            return joinPoint.proceed();
        }
        catch (Exception e) {
            errorMsg = e.getMessage();
            throw e;
        }
        finally {
            logBuilder.errorMsg(errorMsg);
            saveLogAsync(logBuilder.build());
        }
    }

    private void saveLogAsync(OperationLog operationLog) {
        runAsync(() -> operationLogRepo.save(operationLog));
    }

    protected abstract Long getCurrentUserId();

    protected abstract String getCurrentUserName();
}
