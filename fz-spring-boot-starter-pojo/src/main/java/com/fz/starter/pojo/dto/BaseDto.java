package com.fz.starter.pojo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fz.starter.pojo.validation.group.CRUD;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDateTime;

import static cn.hutool.core.date.DatePattern.NORM_DATETIME_PATTERN;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/9/2 11:28
 */

@Data
@FieldNameConstants
@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class BaseDto<ID>
{

    @NotNull(groups = CRUD.U.class, message = "id can not be null when doing update")
    ID            id;
    @JsonFormat(pattern = NORM_DATETIME_PATTERN, timezone = "GMT+8")
    LocalDateTime createTime;
    ID createBy;
    @JsonFormat(pattern = NORM_DATETIME_PATTERN, timezone = "GMT+8")
    LocalDateTime updateTime;
    ID updateBy;
}