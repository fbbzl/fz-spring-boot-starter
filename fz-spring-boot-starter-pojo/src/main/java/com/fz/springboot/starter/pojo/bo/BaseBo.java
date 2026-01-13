package com.fz.springboot.starter.pojo.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fz.springboot.starter.pojo.Generics;
import com.fz.springboot.starter.pojo.entity.BaseTableEntity;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

import static cn.hutool.core.date.DatePattern.NORM_DATETIME_PATTERN;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/2/13 21:28
 */

@Data
@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class BaseBo<ENTITY extends BaseTableEntity> {

    @Getter(AccessLevel.NONE)
    transient Class<ENTITY> entityClass = Generics.getGenericType(this.getClass(), BaseBo.class, 0);

    Long id;

    @JsonFormat(pattern = NORM_DATETIME_PATTERN, timezone = "GMT+8")
    LocalDateTime createTime;

    Long createBy;

    @JsonFormat(pattern = NORM_DATETIME_PATTERN, timezone = "GMT+8")
    LocalDateTime updateTime;

    Long updateBy;

}
