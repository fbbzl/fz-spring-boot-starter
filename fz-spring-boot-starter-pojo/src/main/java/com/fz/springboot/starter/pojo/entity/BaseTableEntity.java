package com.fz.springboot.starter.pojo.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/22 14:24
 */

public interface BaseTableEntity extends Serializable {

    Long getId();

    void setId(Long id);

    LocalDateTime getCreateTime();

    void setCreateTime(LocalDateTime createTime);

    Long getCreateBy();

    void setCreateBy(Long createBy);

    LocalDateTime getUpdateTime();

    void setUpdateTime(LocalDateTime updateTime);

    Long getUpdateBy();

    void setUpdateBy(Long updateBy);
}
