package com.fz.springboot.starter.pojo.eo;

import com.fz.springboot.starter.pojo.Generics;
import com.fz.springboot.starter.pojo.entity.BaseTableEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/1/21 23:53
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public abstract class BaseEo<ENTITY extends BaseTableEntity> {

    @Getter(AccessLevel.NONE)
    transient Class<ENTITY> entityClass = Generics.getGenericType(this.getClass(), BaseEo.class, 0);

}
