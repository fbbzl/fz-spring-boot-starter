package com.fz.starter.mybatisplus;

import cn.hutool.core.collection.IterUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.db.Page;
import cn.hutool.db.PageResult;
import cn.hutool.db.sql.Direction;
import cn.hutool.db.sql.Order;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.support.ColumnCache;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.fz.starter.dal.BaseDal;
import com.fz.starter.dal.Range;
import com.fz.starter.pojo.entity.BaseTableEntity;
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
import static cn.hutool.core.text.CharSequenceUtil.isBlank;
import static cn.hutool.core.text.CharSequenceUtil.isNotBlank;
import static com.baomidou.mybatisplus.extension.repository.IRepository.DEFAULT_BATCH_SIZE;
import static com.fz.starter.dal.Sqls.FOR_UPDATE;
import static com.fz.starter.dal.Sqls.sqlLimit;
import static java.util.Collections.emptyList;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/2/8 16:43
 */
public interface BaseMybatisPlusMapper<ENTITY extends BaseTableEntity<ID>, ID extends Serializable>
        extends BaseMapper<ENTITY>,
                BaseDal<ENTITY, ID>
{

    @Override
    default ENTITY create(@Nullable ENTITY entity)
    {
        this.insert(entity);
        return entity;
    }

    @Transactional
    @Override
    default void create(@Nullable Iterable<ENTITY> entities)
    {
        this.insert(IterUtil.toList(entities), DEFAULT_BATCH_SIZE);
    }

    @Override
    default void delete(@Nullable ID id)
    {
        this.deleteById(id);
    }

    @Transactional
    @Override
    default void delete(@Nullable Set<ID> ids)
    {
        if (isEmpty(ids)) return;

        this.deleteByIds(IterUtil.toList(ids));
    }

    @Override
    default int update(@Nullable ENTITY entity)
    {
        return this.updateById(entity);
    }

    @Transactional
    @Override
    default void update(@Nullable Iterable<ENTITY> entities)
    {
        this.updateById(IterUtil.toList(entities), DEFAULT_BATCH_SIZE);
    }

    @Nullable
    @Override
    default ENTITY byId(@Nullable ID id)
    {
        return this.selectById(id);
    }

    @Override
    default List<ENTITY> byIds(@Nullable Set<ID> ids)
    {
        return this.selectByIds(ids);
    }

    @Override
    default Optional<ENTITY> one(@Nullable ENTITY entity)
    {
        return Optional.ofNullable(this.selectOne(autoQuery(entity)));
    }

    @Override
    default List<ENTITY> list(@Nullable ENTITY entity, @Nullable Integer limit, @Nullable Order[] orders, @Nullable Range... ranges)
    {
        Throws.ifNull(limit, () -> "limit can not be null when doing list query");
        if (entity == null) return emptyList();

        QueryWrapper<ENTITY> wrapper = range(autoQuery(entity), ranges).last(sqlLimit(limit));
        return this.selectList(order(wrapper, orders));
    }

    @Override
    default PageResult<ENTITY> page(@Nullable Page page, @Nullable ENTITY entity)
    {
        Throws.ifNull(page, () -> "page can not be null");

        IPage<ENTITY> result =
                this.selectPage(PageDTO.<ENTITY>of(toMybatisPlusPageNumber(page), page.getPageSize()).addOrder(this.toOrderItem(page.getOrders())),
                                autoQuery(entity));
        return this.toPageResult(page.getPageNumber(), page.getPageSize(), result.getTotal(), result.getRecords());
    }

    @Override
    default boolean exists(@Nullable ENTITY entity)
    {
        return this.selectCount(autoQuery(entity, false)) > 0;
    }

    @Override
    default boolean exists(@Nullable ID id)
    {
        return this.selectCount(new QueryWrapper<ENTITY>().eq(BaseMybatisPlusEntity.Fields.id, id)) > 0;
    }

    @Transactional
    @Override
    default void selectForUpdate(@Nullable List<ID> ids)
    {
        if (ArrayUtil.isEmpty(ids)) return;

        this.selectList(new UpdateWrapper<ENTITY>().in(BaseMybatisPlusEntity.Fields.id, ids).last(FOR_UPDATE));
    }

    @Transactional
    @Override
    default void selectForUpdate(@Nullable ENTITY entity)
    {
        if (entity == null) return;

        this.selectList(autoQuery(entity).last(FOR_UPDATE));
    }

    @Override
    default void increment(String columnName, int delta, @Nullable List<ID> ids)
    {
        if (ArrayUtil.isEmpty(ids)) return;

        this.update(new UpdateWrapper<ENTITY>()
                            .setSql(columnName + " = " + columnName + " + " + delta)
                            .in(BaseMybatisPlusEntity.Fields.id, ids));
    }

    @Override
    default void decrement(String columnName, int delta, @Nullable List<ID> ids)
    {
        if (ArrayUtil.isEmpty(ids)) return;

        this.update(new UpdateWrapper<ENTITY>()
                            .setSql(columnName + " = " + columnName + " - " + delta)
                            .in(BaseMybatisPlusEntity.Fields.id, ids));
    }

    @Override
    default void doBatchConsume(@Nullable ENTITY entity, int batchSize, Consumer<List<ENTITY>> recordsConsumer)
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

    default QueryWrapper<ENTITY> range(QueryWrapper<ENTITY> wrapper, Range... ranges)
    {
        if (ArrayUtil.isEmpty(ranges)) return wrapper;

        Map<String, ColumnCache> columnMap = LambdaUtils.getColumnMap(wrapper.getEntityClass());
        if (columnMap == null) return wrapper;

        // add ranges
        for (Range range : ranges) {
            if (range == null || isBlank(range.getField())) continue;

            ColumnCache cache = columnMap.get(LambdaUtils.formatKey(range.getField()));
            if (cache == null) continue;

            String  column  = cache.getColumn();
            Object  start   = range.getStart();
            Object  end     = range.getEnd();
            boolean isClose = Boolean.TRUE.equals(range.getClose());

            if (isClose)
                wrapper.ge(start != null, column, start).le(end != null, column, end);
            else
                wrapper.gt(start != null, column, start).lt(end != null, column, end);
        }
        return wrapper;
    }

    default QueryWrapper<ENTITY> order(QueryWrapper<ENTITY> wrapper, Order... orders)
    {
        if (ArrayUtil.isEmpty(orders)) return wrapper;

        Map<String, ColumnCache> columnMap = LambdaUtils.getColumnMap(wrapper.getEntityClass());
        if (columnMap == null) return wrapper;

        // add orders
        for (Order order : orders) {
            if (order == null || isBlank(order.getField())) continue;

            ColumnCache cache = columnMap.get(LambdaUtils.formatKey(order.getField()));
            if (cache != null) {
                if (order.getDirection() == Direction.ASC)
                    wrapper.orderByAsc(cache.getColumn());
                else
                    wrapper.orderByDesc(cache.getColumn());
            }
        }
        return wrapper;
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
                    case Enum<?>       enumVal                                      -> wrapper.eq(column,   enumVal.ordinal());
                    case Number        number                                       -> wrapper.eq(column,   number);
                    case LocalDateTime localDateTime                                -> wrapper.eq(column,   localDateTime);
                    case Date          date                                         -> wrapper.eq(column,   date);
                    case Collection<?> col    when isNotEmpty(col)                  -> wrapper.in(column,   newHashSet(col));
                    case String        string when stringLike && isNotBlank(string) -> wrapper.like(column, string);
                    case String        string when isNotBlank(string)               -> wrapper.eq(column,   string);
                    default                                                         -> wrapper.eq(column,   value);
                }
            }
        }
        return wrapper;
    }

}
