package io.github.fbbzl.starter.audit;


import io.github.fbbzl.starter.pojo.entity.BaseTableEntity;
import io.github.fbbzl.starter.pojo.validation.group.CRUD;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.lang.Nullable;

import java.io.Serializable;

import static lombok.AccessLevel.PRIVATE;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/9/4 17:09
 */

@Data
// audit log
@FieldDefaults(level = PRIVATE)
public abstract class BaseAuditLog<ID extends Serializable> implements BaseTableEntity<ID>
{

    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_FAIL    = "fail";

    @NotNull(groups = CRUD.ALL.class, message = "{BaseAuditLog.operatorId}")
    // operator user id
    Long operatorId;

    @NotNull(groups = CRUD.ALL.class, message = "{BaseAuditLog.operatorName}")
    @Size(max = 128)
    // operator user name
    String operatorName;

    @Size(max = 64, message = "{BaseAuditLog.module}")
    @NotNull(groups = CRUD.ALL.class, message = "{BaseAuditLog.module}")
    // operation module
    String module;

    @NotNull(groups = CRUD.ALL.class, message = "{BaseAuditLog.method}")
    // method name
    String method;

    @Nullable
    // method param json
    String param;

    @Nullable
    // method result json
    String result;

    @NotNull(groups = CRUD.ALL.class, message = "{BaseAuditLog.status}")
    // method status
    String status = STATUS_SUCCESS;

    @Nullable
    // error message
    String errorMsg;

}
