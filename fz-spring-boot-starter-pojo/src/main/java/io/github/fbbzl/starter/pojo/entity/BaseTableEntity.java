package io.github.fbbzl.starter.pojo.entity;

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

    LocalDateTime getCreatedAt();

    void setCreatedAt(LocalDateTime createdAt);

    ID getCreatedBy();

    void setCreatedBy(ID createdBy);

    LocalDateTime getUpdatedAt();

    void setUpdatedAt(LocalDateTime updatedAt);

    ID getUpdatedBy();

    void setUpdatedBy(ID updatedBy);

    LocalDateTime getDeletedAt();

    void setDeletedAt(LocalDateTime deletedAt);
}
