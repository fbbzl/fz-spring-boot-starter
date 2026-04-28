package io.github.fbbzl.starter.audit;


import io.github.fbbzl.starter.pojo.entity.BaseTableEntity;
import io.github.fbbzl.starter.pojo.validation.group.CRUD;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "audit log")
@FieldDefaults(level = PRIVATE)
public abstract class BaseAuditLog<ID extends Serializable> implements BaseTableEntity<ID>
{

    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_FAIL    = "fail";

    @NotNull(groups = CRUD.ALL.class, message = "{BaseAuditLog.operatorId}")
    @Schema(description = "operator user id")
    Long operatorId;

    @NotNull(groups = CRUD.ALL.class, message = "{BaseAuditLog.operatorName}")
    @Size(max = 128)
    @Schema(description = "operator user name")
    String operatorName;

    @Size(max = 64, message = "{BaseAuditLog.module}")
    @NotNull(groups = CRUD.ALL.class, message = "{BaseAuditLog.module}")
    @Schema(description = "operation module")
    String module;

    @NotNull(groups = CRUD.ALL.class, message = "{BaseAuditLog.method}")
    @Schema(description = "method name")
    String method;

    @Nullable
    @Schema(description = "method param json")
    String param;

    @Nullable
    @Schema(description = "method result json")
    String result;

    @NotNull(groups = CRUD.ALL.class, message = "{BaseAuditLog.status}")
    @Schema(description = "method status")
    String status = STATUS_SUCCESS;

    @Nullable
    @Schema(description = "error message")
    String errorMsg;

    @NotNull(groups = CRUD.ALL.class, message = "{BaseAuditLog.timeCost}")
    @Schema(description = "method time cost")
    Long timeCost;

}
