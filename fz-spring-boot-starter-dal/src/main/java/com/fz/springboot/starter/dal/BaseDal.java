package com.fz.springboot.starter.dal;

import cn.hutool.db.Page;
import cn.hutool.db.PageResult;
import com.fz.springboot.starter.pojo.entity.BaseTableEntity;
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

    ENTITY baseCreate(ENTITY entity);

    int baseCreate(Iterable<ENTITY> entities);

    void baseDelete(Long id);

    void baseDelete(Set<Long> ids);

    int baseUpdate(ENTITY entity);

    int baseUpdate(Iterable<ENTITY> entities);

    Optional<ENTITY> baseById(Long id);

    List<ENTITY> baseByIds(Set<Long> ids);

    Optional<ENTITY> baseOne(ENTITY entity);

    Optional<ENTITY> baseOne(Map<String, Object> propertyAndValue);

    List<ENTITY> baseList(ENTITY entity);

    List<ENTITY> baseList(Map<String, Object> propertyAndValue);

    PageResult<ENTITY> basePage(ENTITY entity, Page page);

    PageResult<ENTITY> basePage(Map<String, Object> propertyAndValue, Page page);

    boolean baseExists(ENTITY entity);

    boolean baseExists(Map<String, Object> propertyAndValue);

    boolean baseExists(Long id);

    void selectForUpdate(Long... ids);

    void selectForUpdate(ENTITY entity);

    void increment(String fieldName, int delta, Long... ids);

    void decrement(String fieldName, int delta, Long... ids);

    void doBatch(ENTITY entity, int batchSize, Consumer<List<ENTITY>> recordsConsumer);

    default @NonNull PageResult<ENTITY> toPageResult(int pageNumber, int pageSize, long total, List<ENTITY> content) {
        PageResult<ENTITY> pageResult =
                new PageResult<>(pageNumber, pageSize, (int) total);
        pageResult.addAll(content);
        return pageResult;
    }

}
