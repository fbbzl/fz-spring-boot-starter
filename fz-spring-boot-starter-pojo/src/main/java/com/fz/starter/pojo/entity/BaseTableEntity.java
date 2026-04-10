package com.fz.starter.pojo.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/22 14:24
 */

public interface BaseTableEntity<ID extends Serializable> extends Serializable
{

    ID getId();

    void setId(ID id);

    LocalDateTime getCreateTime();

    void setCreateTime(LocalDateTime createTime);

    ID getCreateBy();

    void setCreateBy(ID createBy);

    LocalDateTime getUpdateTime();

    void setUpdateTime(LocalDateTime updateTime);

    ID getUpdateBy();

    void setUpdateBy(ID updateBy);
}
