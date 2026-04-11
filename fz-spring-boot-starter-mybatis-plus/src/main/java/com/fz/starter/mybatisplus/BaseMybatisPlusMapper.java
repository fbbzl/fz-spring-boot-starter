package com.fz.starter.mybatisplus;

import cn.hutool.core.collection.IterUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.db.Page;
import cn.hutool.db.PageResult;
import cn.hutool.db.sql.Direction;
import cn.hutool.db.sql.Order;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.ColumnCache;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.fz.starter.dal.BaseDal;
import com.fz.starter.dal.SqlConstants;
import com.fz.starter.pojo.entity.BaseTableEntity;
import org.apache.ibatis.executor.BatchResult;
import org.fz.erwin.exception.Throws;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static cn.hutool.core.collection.CollUtil.*;
import static cn.hutool.core.text.CharSequenceUtil.isNotBlank;
import static com.baomidou.mybatisplus.extension.repository.IRepository.DEFAULT_BATCH_SIZE;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/2/8 16:43
 */
public interface BaseMybatisPlusMapper<ENTITY extends BaseMybatisPlusEntity<ID>, ID extends Serializable>
        extends BaseMapper<ENTITY>,
                BaseDal<ENTITY, ID>
{

    @Override
    default ENTITY create(ENTITY entity)
    {
        this.insert(entity);
        return entity;
    }

    @Transactional
    @Override
    default int create(Iterable<ENTITY> entities)
    {
        List<BatchResult> inserts = this.insert(IterUtil.toList(entities), DEFAULT_BATCH_SIZE);
        return effectRows(inserts);
    }

    @Override
    default void delete(ID id)
    {
        this.deleteById(id);
    }

    @Transactional
    @Override
    default void delete(Set<ID> ids)
    {
        if (isEmpty(ids)) return;

        this.deleteByIds(IterUtil.toList(ids));
    }

    @Override
    default int update(ENTITY entity)
    {
        return this.updateById(entity);
    }

    @Transactional
    @Override
    default int update(Iterable<ENTITY> entities)
    {
        return effectRows(this.updateById(IterUtil.toList(entities), DEFAULT_BATCH_SIZE));
    }

    @Nullable
    @Override
    default ENTITY byId(ID id)
    {
        return this.selectById(id);
    }

    @Override
    default List<ENTITY> byIds(Set<ID> ids)
    {
        return this.selectByIds(IterUtil.toList(ids));
    }

    @Override
    default Optional<ENTITY> one(ENTITY entity)
    {
        return Optional.ofNullable(this.selectOne(autoQuery(entity)));
    }

    @Override
    default List<ENTITY> list(ENTITY entity)
    {
        return this.selectList(autoQuery(entity));
    }

    @Override
    default List<ENTITY> limit(ENTITY entity, int limit)
    {
        return this.selectList(autoQuery(entity).last(SqlConstants.limit(limit)));
    }

    @Override
    default PageResult<ENTITY> page(Page page, ENTITY entity)
    {
        IPage<ENTITY> result =
                this.selectPage(PageDTO.<ENTITY>of(toMybatisPlusPageNumber(page), page.getPageSize()).addOrder(this.toOrderItem(page.getOrders())),
                                autoQuery(entity));
        return this.toPageResult(page.getPageNumber(), page.getPageSize(), result.getTotal(), result.getRecords());
    }

    @Override
    default boolean exists(ENTITY entity)
    {
        return this.selectCount(autoQuery(entity, false)) > 0;
    }

    @Override
    default boolean exists(ID id)
    {
        return this.selectCount(new LambdaUpdateWrapper<ENTITY>().eq(BaseTableEntity::getId, id)) > 0;
    }

    @Transactional
    @Override
    default void selectForUpdate(List<ID> ids)
    {
        if (ArrayUtil.isEmpty(ids)) return;

        this.selectList(new LambdaUpdateWrapper<ENTITY>().in(BaseTableEntity::getId, ids).last(" FOR UPDATE "));
    }

    @Transactional
    @Override
    default void selectForUpdate(ENTITY entity)
    {
        if (entity == null) return;

        this.selectList(autoQuery(entity).last(" FOR UPDATE "));
    }

    @Override
    default void increment(String columnName, int delta, List<ID> ids)
    {
        if (ArrayUtil.isEmpty(ids)) return;

        this.update(new LambdaUpdateWrapper<ENTITY>()
                            .setSql(columnName + " = " + columnName + " + " + delta)
                            .in(BaseTableEntity::getId, ids));
    }

    @Override
    default void decrement(String columnName, int delta, List<ID> ids)
    {
        if (ArrayUtil.isEmpty(ids)) return;

        this.update(new LambdaUpdateWrapper<ENTITY>()
                            .setSql(columnName + " = " + columnName + " - " + delta)
                            .in(BaseTableEntity::getId, ids));
    }

    @Override
    default void doBatchConsume(ENTITY entity, int batchSize, Consumer<List<ENTITY>> recordsConsumer)
    {
        int                pageNumber = 0;
        Page               page       = new Page(pageNumber, batchSize);
        PageResult<ENTITY> pageResult;

        do {
            pageResult = this.page(page, entity);

            recordsConsumer.accept(pageResult);

            pageNumber++;
            page.setPageNumber(pageNumber);
        } while (pageResult.size() == batchSize);
    }

    default int effectRows(List<BatchResult> batchResults)
    {
        return batchResults.stream()
                           .mapToInt(result -> Arrays.stream(result.getUpdateCounts()).sum())
                           .sum();
    }

    default int toMybatisPlusPageNumber(Page page)
    {
        return page.getPageNumber() + 1;
    }

    default OrderItem[] toOrderItem(Order... orders)
    {
        if (ArrayUtil.isEmpty(orders)) return new OrderItem[]{};

        return Stream.of(orders)
                     .map(order ->
                                  order.getDirection() == Direction.ASC ?
                                  OrderItem.asc(order.getField()) : OrderItem.desc(order.getField())).toArray(OrderItem[]::new);
    }

    default LambdaQueryWrapper<ENTITY> createTimeQuery(boolean isEq, LocalDateTime start, LocalDateTime end)
    {
        LambdaQueryWrapper<ENTITY> timeRangeQuery = Wrappers.lambdaQuery();

        if (isEq) timeRangeQuery.ge(start != null, BaseMybatisPlusEntity::getCreateTime, start).le(end != null, BaseMybatisPlusEntity::getCreateTime, end);
        else timeRangeQuery.gt(start != null, BaseMybatisPlusEntity::getCreateTime, start).lt(end != null, BaseMybatisPlusEntity::getCreateTime, end);

        return timeRangeQuery;
    }

    default LambdaQueryWrapper<ENTITY> updateTimeQuery(boolean isEq, LocalDateTime start, LocalDateTime end)
    {
        LambdaQueryWrapper<ENTITY> timeRangeQuery = Wrappers.lambdaQuery();

        if (isEq) timeRangeQuery.ge(start != null, BaseMybatisPlusEntity::getUpdateTime, start).le(end != null, BaseMybatisPlusEntity::getUpdateTime, end);
        else timeRangeQuery.gt(start != null, BaseMybatisPlusEntity::getUpdateTime, start).lt(end != null, BaseMybatisPlusEntity::getUpdateTime, end);

        return timeRangeQuery;
    }

    default QueryWrapper<ENTITY> autoQuery(ENTITY query)
    {
        return autoQuery(query, true);
    }

    default QueryWrapper<ENTITY> autoQuery(ENTITY query, boolean stringLike)
    {
        QueryWrapper<ENTITY> wrapper = new QueryWrapper<>();
        if (query != null) {
            @SuppressWarnings("unchecked")
            Class<ENTITY> queryClass = (Class<ENTITY>) query.getClass();
            String                   queryClassName = queryClass.getName();
            Map<String, ColumnCache> columnMap      = LambdaUtils.getColumnMap(queryClass);
            Throws.ifEmpty(columnMap, () -> "entity [" + queryClassName + "] has none TableField");

            Field[] fields = ReflectUtil.getFields(query.getClass());
            Throws.ifEmpty(fields, () -> "fields of [" + queryClassName + "] has none TableField");

            for (Field field : fields) {
                String fieldName = field.getName();
                Object value     = ReflectUtil.getFieldValue(query, field);
                if (value == null) continue;

                ColumnCache cache = columnMap.get(LambdaUtils.formatKey(fieldName));
                if (cache == null) continue;
                String column = cache.getColumn();

                switch (value) {
                    case Enum<?> enumVal -> wrapper.eq(column, enumVal.ordinal());
                    case Number number -> wrapper.eq(column, number);
                    case LocalDateTime localDateTime -> wrapper.eq(column, localDateTime);
                    case Date date -> wrapper.eq(column, date);
                    case Collection<?> col when isNotEmpty(col) -> wrapper.in(column, newHashSet(col));
                    case String string when stringLike && isNotBlank(string) -> wrapper.like(column, string);
                    case String string when isNotBlank(string) -> wrapper.eq(column, string);
                    default -> wrapper.eq(column, value);
                }
            }
        }
        return wrapper;
    }

}
