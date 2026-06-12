package io.github.fbbzl.starter.dal;

import cn.hutool.db.Page;
import cn.hutool.db.PageResult;
import cn.hutool.db.sql.Order;
import io.github.fbbzl.starter.pojo.entity.BaseTableEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/2/8 10:04
 */

public interface BaseDal<ENTITY extends BaseTableEntity<ID>, ID extends Serializable>
{

    ENTITY create(@Nullable ENTITY entity);

    List<ENTITY> create(@Nullable Iterable<ENTITY> entities);

    void delete(@Nullable ID id);

    void delete(@Nullable Iterable<ID> ids);

    int update(@Nullable ENTITY entity);

    int update(@Nullable Iterable<ENTITY> entities);

    @Nullable
    ENTITY byId(@Nullable ID id);

    List<ENTITY> byIds(@Nullable Collection<ID> ids);

    Optional<ENTITY> one(@Nullable ENTITY entity);

    List<ENTITY> list(@Nullable ENTITY entity, @Nullable Integer limit, @Nullable Order[] orders, @Nullable Range[] ranges);

    default List<ID> ids(@Nullable ENTITY entity, @Nullable Integer limit)
    {
        return this.list(entity, limit, null, null).stream().map(BaseTableEntity::getId).toList();
    }

    PageResult<ENTITY> page(@Nullable Page page, @Nullable ENTITY entity);

    boolean exists(@Nullable ENTITY entity);

    boolean exists(@Nullable ID id);

    void selectForUpdate(@Nullable List<ID> ids);

    void selectForUpdate(@Nullable ENTITY entity);

    void increment(String fieldName, int delta, @Nullable List<ID> ids);

    void decrement(String fieldName, int delta, @Nullable List<ID> ids);

    void doBatchConsume(@Nullable ENTITY entity, int batchSize, Consumer<List<ENTITY>> recordsConsumer);

    long count(@Nullable ENTITY entity);

    default @NonNull PageResult<ENTITY> toPageResult(int pageNumber, int pageSize, long total, List<ENTITY> content)
    {
        PageResult<ENTITY> pageResult =
                new PageResult<>(pageNumber, pageSize, (int) total);
        pageResult.addAll(content);
        return pageResult;
    }

}
