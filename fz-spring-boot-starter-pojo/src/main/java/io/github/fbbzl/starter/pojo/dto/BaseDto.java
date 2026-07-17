package io.github.fbbzl.starter.pojo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.fbbzl.starter.pojo.validation.group.CRUD;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import java.time.LocalDateTime;

import static cn.hutool.core.date.DatePattern.NORM_DATETIME_PATTERN;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/9/2 11:28
 */

@Data
@FieldNameConstants
@SuppressWarnings("all")
@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class BaseDto<ID extends Serializable> implements Prepare, Serializable
{

    @NotNull(groups = CRUD.U.class, message = "id can not be null when doing update")
    ID            id;
    @JsonFormat(pattern = NORM_DATETIME_PATTERN, timezone = "GMT+8")
    LocalDateTime createdAt;
    ID createdBy;
    @JsonFormat(pattern = NORM_DATETIME_PATTERN, timezone = "GMT+8")
    LocalDateTime updatedAt;
    ID updatedBy;
}