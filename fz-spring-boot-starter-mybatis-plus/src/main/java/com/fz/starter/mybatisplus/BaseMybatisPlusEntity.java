package com.fz.starter.mybatisplus;

import com.baomidou.mybatisplus.annotation.*;
import com.fz.starter.mybatisplus.frame.BaseMetaObjectHandler;
import com.fz.starter.pojo.entity.BaseTableEntity;
import com.fz.starter.pojo.validation.group.CRUD;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import lombok.experimental.FieldNameConstants;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author fengbinbin
 * @version 1.0
 * @see BaseMetaObjectHandler
 * @since 2026/2/8 15:43
 */

@Data
@FieldNameConstants
@EqualsAndHashCode(callSuper = false)
@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class BaseMybatisPlusEntity<ID extends Serializable> implements BaseTableEntity<ID>
{

    @Serial
    private static final long serialVersionUID = -4346857202985735177L;

    @NotNull(groups = CRUD.U.class)
    @TableId(type = IdType.AUTO)
    ID id;

    @TableField(fill = FieldFill.INSERT)
    LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT)
    ID createBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    ID updateBy;

    @TableLogic
    @TableField(select = false)
    Boolean deleted;
}
