package com.fz.starter.mybatisplus.frame;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.fz.starter.core.util.Generics;
import com.fz.starter.mybatisplus.BaseMybatisPlusEntity;
import org.apache.ibatis.reflection.MetaObject;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/22 15:00
 */

public abstract class BaseMetaObjectHandler<ID extends Serializable> implements MetaObjectHandler
{

    Class<ID> idClass = Generics.getGenericSuperType(this.getClass(), BaseMetaObjectHandler.class, 0);

    protected abstract ID getCurrentLoginUserId();

    @Override
    public void insertFill(MetaObject metaObject)
    {
        strictInsertFill(metaObject, BaseMybatisPlusEntity.Fields.createTime, LocalDateTime.class, LocalDateTime.now());
        strictInsertFill(metaObject, BaseMybatisPlusEntity.Fields.createBy, idClass, this.getCurrentLoginUserId());

        this.updateFill(metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject)
    {
        strictUpdateFill(metaObject, BaseMybatisPlusEntity.Fields.updateTime, LocalDateTime.class, LocalDateTime.now());
        strictUpdateFill(metaObject, BaseMybatisPlusEntity.Fields.updateBy, idClass, this.getCurrentLoginUserId());
    }
}