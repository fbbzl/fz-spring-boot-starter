package com.fz.starter.pojo.dto;

import static cn.hutool.core.date.DatePattern.NORM_DATETIME_PATTERN;

import com.fz.starter.core.util.Generics;
import com.fz.starter.pojo.entity.BaseTableEntity;
import com.fz.starter.pojo.validation.CRUD;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/9/2 11:28
 */

@Data
@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class BaseDto<ENTITY extends BaseTableEntity> {

    @Getter(AccessLevel.NONE)
    transient Class<ENTITY> entityClass = Generics.getGenericType(this.getClass(), BaseDto.class, 0);

    @NotNull(groups = CRUD.U.class, message = "id can not be null when doing update")
    Long id;

    @DateTimeFormat(pattern = NORM_DATETIME_PATTERN)
    LocalDateTime createTime;

    Long createBy;

    @DateTimeFormat(pattern = NORM_DATETIME_PATTERN)
    LocalDateTime updateTime;

    Long updateBy;
}