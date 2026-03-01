package com.fz.starter.mybatisplus.frame;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.fz.starter.mybatisplus.BaseMybatisPlusEntity;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/22 15:00
 */

public abstract class BaseMetaObjectHandler implements MetaObjectHandler {

    protected abstract Long getCurrentLoginUserId();

    @Override
    public void insertFill(MetaObject metaObject) {
        strictInsertFill(metaObject, BaseMybatisPlusEntity.Fields.createTime, LocalDateTime.class, LocalDateTime.now());
        strictInsertFill(metaObject, BaseMybatisPlusEntity.Fields.createBy, Long.class, this.getCurrentLoginUserId());

        this.updateFill(metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        strictUpdateFill(metaObject, BaseMybatisPlusEntity.Fields.updateTime, LocalDateTime.class, LocalDateTime.now());
        strictUpdateFill(metaObject, BaseMybatisPlusEntity.Fields.updateBy, Long.class, this.getCurrentLoginUserId());
    }
}