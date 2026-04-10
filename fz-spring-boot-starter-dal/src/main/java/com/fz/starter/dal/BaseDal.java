package com.fz.starter.dal;

import cn.hutool.db.Page;
import cn.hutool.db.PageResult;
import com.fz.starter.pojo.entity.BaseTableEntity;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/2/8 10:04
 */

public interface BaseDal<ENTITY extends BaseTableEntity<ID>, ID extends Serializable>
{

    ENTITY create(ENTITY entity);

    int create(Iterable<ENTITY> entities);

    void delete(ID id);

    void delete(Set<ID> ids);

    int update(ENTITY entity);

    int update(Iterable<ENTITY> entities);

    Optional<ENTITY> byId(ID id);

    List<ENTITY> byIds(Set<ID> ids);

    Optional<ENTITY> one(ENTITY entity);

    List<ENTITY> list(ENTITY entity);

    List<ENTITY> limit(ENTITY entity, int limit);

    PageResult<ENTITY> page(Page page, ENTITY entity);

    boolean exists(ENTITY entity);

    boolean exists(ID id);

    void selectForUpdate(List<ID> ids);

    void selectForUpdate(ENTITY entity);

    void increment(String fieldName, int delta, List<ID> ids);

    void decrement(String fieldName, int delta, List<ID> ids);

    void doBatchConsume(ENTITY entity, int batchSize, Consumer<List<ENTITY>> recordsConsumer);

    default @NonNull PageResult<ENTITY> toPageResult(int pageNumber, int pageSize, long total, List<ENTITY> content)
    {
        PageResult<ENTITY> pageResult =
                new PageResult<>(pageNumber, pageSize, (int) total);
        pageResult.addAll(content);
        return pageResult;
    }

}
