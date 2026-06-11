package io.github.fbbzl.starter.mybatisplus.frame;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.fz.erwin.lang.Generics;
import io.github.fbbzl.starter.mybatisplus.BaseMybatisPlusEntity;
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

    Class<ID> idType = Generics.getGenericSuperType(this.getClass(), BaseMetaObjectHandler.class, 0);

    protected ID getCurrentUserIdentifier()
    {
        throw new UnsupportedOperationException("getCurrentUserIdentifier must be implemented");
    }

    @Override
    public void insertFill(MetaObject metaObject)
    {
        strictInsertFill(metaObject, BaseMybatisPlusEntity.Fields.createTime, LocalDateTime.class, LocalDateTime.now());
        strictInsertFill(metaObject, BaseMybatisPlusEntity.Fields.createBy, idType, this.getCurrentUserIdentifier());

        this.updateFill(metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject)
    {
        strictUpdateFill(metaObject, BaseMybatisPlusEntity.Fields.updateTime, LocalDateTime.class, LocalDateTime.now());
        strictUpdateFill(metaObject, BaseMybatisPlusEntity.Fields.updateBy, idType, this.getCurrentUserIdentifier());
    }
}