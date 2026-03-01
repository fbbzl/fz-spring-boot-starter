package com.fz.starter.pojo.bo;

import static cn.hutool.core.date.DatePattern.NORM_DATETIME_PATTERN;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fz.starter.core.util.Generics;
import com.fz.starter.pojo.entity.BaseTableEntity;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

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
