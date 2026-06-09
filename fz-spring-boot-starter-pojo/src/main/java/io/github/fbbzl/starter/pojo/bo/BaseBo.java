package io.github.fbbzl.starter.pojo.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
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
 * @since 2026/2/13 21:28
 */

@Data
@FieldNameConstants
@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class BaseBo<ID> implements Serializable
{

    ID id;
    @JsonFormat(pattern = NORM_DATETIME_PATTERN, timezone = "GMT+8")
    LocalDateTime createTime;
    ID createBy;
    @JsonFormat(pattern = NORM_DATETIME_PATTERN, timezone = "GMT+8")
    LocalDateTime updateTime;
    ID updateBy;

}
