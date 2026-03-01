package com.fz.starter.dal;

import cn.hutool.db.Page;
import cn.hutool.db.PageResult;
import com.fz.starter.pojo.entity.BaseTableEntity;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/2/8 10:04
 */

public interface BaseDal<ENTITY extends BaseTableEntity> {

    ENTITY create(ENTITY entity);

    int create(Iterable<ENTITY> entities);

    void delete(Long id);

    void delete(Set<Long> ids);

    int update(ENTITY entity);

    int update(Iterable<ENTITY> entities);

    Optional<ENTITY> byId(Long id);

    List<ENTITY> byIds(Set<Long> ids);

    Optional<ENTITY> one(ENTITY entity);

    Optional<ENTITY> one(Map<String, Object> propertyAndValue);

    List<ENTITY> list(ENTITY entity);

    List<ENTITY> list(Map<String, Object> propertyAndValue);

    PageResult<ENTITY> page(ENTITY entity, Page page);

    PageResult<ENTITY> page(Map<String, Object> propertyAndValue, Page page);

    boolean exists(ENTITY entity);

    boolean exists(Map<String, Object> propertyAndValue);

    boolean exists(Long id);

    void selectForUpdate(Long... ids);

    void selectForUpdate(ENTITY entity);

    void increment(String fieldName, int delta, Long... ids);

    void decrement(String fieldName, int delta, Long... ids);

    void doBatchConsume(ENTITY entity, int batchSize, Consumer<List<ENTITY>> recordsConsumer);

    default @NonNull PageResult<ENTITY> toPageResult(int pageNumber, int pageSize, long total, List<ENTITY> content) {
        PageResult<ENTITY> pageResult =
                new PageResult<>(pageNumber, pageSize, (int) total);
        pageResult.addAll(content);
        return pageResult;
    }

}
