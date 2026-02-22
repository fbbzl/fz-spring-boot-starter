package com.fz.starter.mybatisplus;

import static cn.hutool.core.collection.CollUtil.isEmpty;
import static cn.hutool.core.text.CharSequenceUtil.isNotBlank;
import static com.baomidou.mybatisplus.extension.repository.IRepository.DEFAULT_BATCH_SIZE;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.IterUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.db.Page;
import cn.hutool.db.PageResult;
import cn.hutool.db.sql.Direction;
import cn.hutool.db.sql.Order;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.fz.starter.dal.BaseDal;
import com.fz.starter.pojo.entity.BaseTableEntity;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.apache.ibatis.executor.BatchResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/2/8 16:43
 */
public interface BaseMybatisPlusMapper<ENTITY extends BaseMybatisPlusEntity>
        extends BaseMapper<ENTITY>,
                BaseDal<ENTITY> {

    @Override
    default ENTITY baseCreate(ENTITY entity) {
        this.insert(entity);
        return entity;
    }

    @Transactional
    @Override
    default int baseCreate(Iterable<ENTITY> entities) {
        List<BatchResult> inserts = this.insert(IterUtil.toList(entities), DEFAULT_BATCH_SIZE);
        return effectRows(inserts);
    }

    @Override
    default void baseDelete(Long id) {
        this.deleteById(id);
    }

    @Transactional
    @Override
    default void baseDelete(Set<Long> ids) {
        if (isEmpty(ids)) return;

        this.deleteByIds(IterUtil.toList(ids));
    }

    @Override
    default int baseUpdate(ENTITY entity) {
        return this.updateById(entity);
    }

    @Transactional
    @Override
    default int baseUpdate(Iterable<ENTITY> entities) {
        return effectRows(this.updateById(IterUtil.toList(entities), DEFAULT_BATCH_SIZE));
    }

    @Override
    default Optional<ENTITY> baseById(Long id) {
        return Optional.ofNullable(this.selectById(id));
    }

    @Override
    default List<ENTITY> baseByIds(Set<Long> ids) {
        return this.selectByIds(IterUtil.toList(ids));
    }

    @Override
    default Optional<ENTITY> baseOne(ENTITY entity) {
        return Optional.ofNullable(this.selectOne(buildQueryWrapper(entity)));
    }

    @Override
    default Optional<ENTITY> baseOne(Map<String, Object> propertyAndValue) {
        return Optional.ofNullable(this.selectOne(buildQueryWrapper(propertyAndValue)));
    }

    @Override
    default List<ENTITY> baseList(ENTITY entity) {
        return this.selectList(buildQueryWrapper(entity));
    }

    @Override
    default List<ENTITY> baseList(Map<String, Object> propertyAndValue) {
        return this.selectList(buildQueryWrapper(propertyAndValue));
    }

    @Override
    default PageResult<ENTITY> basePage(ENTITY entity, Page page) {
        IPage<ENTITY> result =
                this.selectPage(
                        PageDTO.<ENTITY>of(toMybatisPlusPageNumber(page), page.getPageSize()).addOrder(this.toOrderItem(page.getOrders())),
                        buildQueryWrapper(entity)
                               );
        return this.toPageResult(page.getPageNumber(), page.getPageSize(), result.getTotal(), result.getRecords());
    }

    @Override
    default PageResult<ENTITY> basePage(Map<String, Object> propertyAndValue, Page page) {
        IPage<ENTITY> result =
                this.selectPage(
                        PageDTO.<ENTITY>of(toMybatisPlusPageNumber(page), page.getPageSize()).addOrder(this.toOrderItem(page.getOrders())),
                        buildQueryWrapper(propertyAndValue)
                               );
        return this.toPageResult(page.getPageNumber(), page.getPageSize(), result.getTotal(), result.getRecords());
    }

    @Override
    default boolean baseExists(ENTITY entity) {
        return this.selectCount(buildQueryWrapper(entity)) > 0;
    }

    @Override
    default boolean baseExists(Map<String, Object> propertyAndValue) {
        return this.selectCount(buildQueryWrapper(propertyAndValue)) > 0;
    }

    @Override
    default boolean baseExists(Long id) {
        return this.selectById(id) != null;
    }

    @Transactional
    @Override
    default void selectForUpdate(Long... ids) {
        if (ArrayUtil.isEmpty(ids)) return;

        this.selectList(new LambdaUpdateWrapper<ENTITY>().in(BaseTableEntity::getId, (Object[]) ids).last("FOR UPDATE"));
    }

    @Transactional
    @Override
    default void selectForUpdate(ENTITY entity) {
        if (entity == null) return;

        this.selectList(buildQueryWrapper(entity).last("FOR UPDATE"));
    }

    @Override
    default void increment(String columnName, int delta, Long... ids) {
        if (ArrayUtil.isEmpty(ids)) return;

        this.update(new LambdaUpdateWrapper<ENTITY>()
                .setSql(columnName + " = " + columnName + " + " + delta)
                .in(BaseTableEntity::getId, (Object[]) ids));
    }

    @Override
    default void decrement(String columnName, int delta, Long... ids) {
        if (ArrayUtil.isEmpty(ids)) return;

        this.update(new LambdaUpdateWrapper<ENTITY>()
                .setSql(columnName + " = " + columnName + " - " + delta)
                .in(BaseTableEntity::getId, (Object[]) ids));
    }

    @Override
    default void doBatch(ENTITY entity, int batchSize, Consumer<List<ENTITY>> recordsConsumer) {
        int pageNumber = 0;
        Page page = new Page(pageNumber, batchSize);
        PageResult<ENTITY> pageResult;

        do {
            pageResult = this.basePage(entity, page);

            recordsConsumer.accept(pageResult);

            pageNumber++;
            page.setPageNumber(pageNumber);
        } while (pageResult.size() == batchSize);
    }

    default int effectRows(List<BatchResult> batchResults) {
        return batchResults.stream()
                           .mapToInt(result -> Arrays.stream(result.getUpdateCounts()).sum())
                           .sum();
    }

    default <T> QueryWrapper<T> buildQueryWrapper(Map<String, Object> params) {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        MapUtil.removeNullValue(params);
        params.forEach((fieldName, value) -> {
            Class<?> valueType = value.getClass();
            fieldName = StringUtils.camelToUnderline(fieldName);
            if (String.class == valueType) {
                String strValue = value.toString();
                wrapper.like(isNotBlank(strValue), fieldName, strValue);
            }
            else if (Enum.class.isAssignableFrom(valueType)) {
                Enum<?> enumValue = (Enum<?>) value;
                wrapper.eq(fieldName, enumValue.ordinal());
            }
            else wrapper.eq(fieldName, value);
        });
        return wrapper;
    }

    default int toMybatisPlusPageNumber(Page page) {
        return page.getPageNumber() + 1;
    }

    default OrderItem[] toOrderItem(Order... orders) {
        if (ArrayUtil.isEmpty(orders)) return new OrderItem[]{};

        return Stream.of(orders)
                     .map(order ->
                                  order.getDirection() == Direction.ASC ?
                                  OrderItem.asc(order.getField()) : OrderItem.desc(order.getField())).toArray(OrderItem[]::new);
    }

    default <T> QueryWrapper<T> buildQueryWrapper(T entity) {
        return buildQueryWrapper(BeanUtil.beanToMap(entity));
    }

}
