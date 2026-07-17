package io.github.fbbzl.starter.mybatisplus;

import cn.hutool.core.collection.IterUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.PrimitiveArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.db.Page;
import cn.hutool.db.PageResult;
import cn.hutool.db.sql.Direction;
import cn.hutool.db.sql.Order;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.ColumnCache;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import io.github.fbbzl.starter.dal.BaseDal;
import io.github.fbbzl.starter.dal.Range;
import io.github.fbbzl.starter.pojo.validation.group.CRUD;
import jakarta.validation.constraints.NotNull;
import org.apache.ibatis.executor.BatchResult;
import org.fz.erwin.exception.Throws;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.*;

import static cn.hutool.core.collection.CollUtil.*;
import static cn.hutool.core.text.CharSequenceUtil.isBlank;
import static cn.hutool.core.text.CharSequenceUtil.isNotBlank;
import static com.baomidou.mybatisplus.extension.repository.IRepository.DEFAULT_BATCH_SIZE;
import static io.github.fbbzl.starter.dal.Sqls.FOR_UPDATE;
import static io.github.fbbzl.starter.dal.Sqls.sqlLimit;
import static java.util.Collections.emptyList;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/3/8 16:43
 */
public interface BaseMybatisPlusMapper<ENTITY extends BaseMybatisPlusEntity<ID>, ID extends Serializable>
        extends BaseMapper<ENTITY>,
                BaseDal<ENTITY, ID>
{

    @Override
    default ENTITY create(@Nullable ENTITY entity)
    {
        if (entity == null) return null;

        this.insert(entity);
        return entity;
    }

    @Override
    default List<ENTITY> create(@Nullable Iterable<ENTITY> entities)
    {
        if (isEmpty(entities)) return emptyList();
        List<ENTITY> entityList = IterUtil.toList(entities);
        this.insert(entityList, DEFAULT_BATCH_SIZE);
        return entityList;
    }

    @Override
    default void delete(@Nullable ID id)
    {
        if (id == null) {
            return;
        }
        this.deleteById(id);
    }

    @Override
    default void delete(@Nullable Iterable<ID> ids)
    {
        if (isEmpty(ids)) {
            return;
        }
        this.deleteByIds(newHashSet(ids));
    }

    @Override
    default int update(
            @Validated(CRUD.U.class)
            @NotNull(message = "entity can not be null when doing update")
            ENTITY entity)
    {
        return this.updateById(entity);
    }

    @Override
    default int update(Iterable<ENTITY> entities)
    {
        if (isEmpty(entities)) return 0;

        return affectedRows(this.updateById(IterUtil.toList(entities), DEFAULT_BATCH_SIZE));
    }

    default int affectedRows(@Nullable List<BatchResult> batchResults)
    {
        if (isEmpty(batchResults)) return 0;

        int affectedRows = 0;
        for (BatchResult batchResult : batchResults)
        {
            int[] updateCounts = batchResult.getUpdateCounts();
            if (PrimitiveArrayUtil.isEmpty(updateCounts)) continue;

            for (int updateCount : updateCounts)
            {
                if (updateCount == Statement.SUCCESS_NO_INFO)
                {
                    affectedRows++;
                }
                else if (updateCount > 0)
                {
                    affectedRows += updateCount;
                }
            }
        }
        return affectedRows;
    }

    @Nullable
    @Override
    default ENTITY byId(@Nullable ID id)
    {
        if (id == null) return null;

        return this.selectById(id);
    }

    @Override
    default List<ENTITY> byIds(@Nullable Collection<ID> ids)
    {
        if (isEmpty(ids)) return emptyList();
        return this.selectByIds(ids);
    }

    @Override
    default List<ENTITY> list(@Nullable ENTITY entity, Integer limit, @Nullable Order[] orders, @Nullable Range[] ranges)
    {
        Throws.ifNull(limit, "limit can not be null when doing list query");
        if (entity == null) return emptyList();

        QueryWrapper<ENTITY> wrapper = range(autoQuery(entity), ranges).last(sqlLimit(limit));
        return this.selectList(order(wrapper, orders));
    }

    @Override
    default List<ID> ids(@Nullable ENTITY entity, Integer limit)
    {
        Throws.ifNull(limit, "limit can not be null when doing ids query");
        if (entity == null) return emptyList();

        QueryWrapper<ENTITY> wrapper = autoQuery(entity)
                .select(BaseMybatisPlusEntity.Fields.id)
                .last(sqlLimit(limit));
        return this.selectObjs(wrapper).stream().map(id -> (ID) id).toList();
    }

    @Override
    default PageResult<ENTITY> page(Page page, @Nullable ENTITY entity)
    {
        Throws.ifNull(page, "page can not be null");
        Class<?> entityClass = entity != null ? entity.getClass()
                : ResolvableType.forClass(BaseMybatisPlusMapper.class, this.getClass()).getGeneric(0).resolve();
        return this.selectPage(HP.of(page, entityClass), autoQuery(entity)).toPageResult();
    }

    @Override
    default long count(@Nullable ENTITY entity)
    {
        if (entity == null) return 0L;
        return this.selectCount(autoQuery(entity, false));
    }

    @Override
    default boolean exists(@Nullable ID id)
    {
        if (id == null) return false;
        return this.selectCount(new QueryWrapper<ENTITY>().eq(BaseMybatisPlusEntity.Fields.id, id)) > 0;
    }

    @Override
    default void selectForUpdate(@Nullable List<ID> ids)
    {
        if (ArrayUtil.isEmpty(ids)) return;

        this.selectList(new QueryWrapper<ENTITY>().in(BaseMybatisPlusEntity.Fields.id, ids).last(FOR_UPDATE));
    }

    @Override
    default void selectForUpdate(@Nullable ENTITY entity)
    {
        if (entity == null) return;

        this.selectList(autoQuery(entity).last(FOR_UPDATE));
    }

    @Override
    default void increment(String fieldName, int delta, @Nullable List<ID> ids)
    {
        if (ArrayUtil.isEmpty(ids)) return;

        String column = resolveColumn(fieldName);
        Throws.ifBlank(column, "can not resolve column from field [{}] on entity", fieldName);

        this.update(new UpdateWrapper<ENTITY>()
                            .setSql(column + " = " + column + " + " + delta)
                            .in(BaseMybatisPlusEntity.Fields.id, ids));
    }

    @Override
    default void decrement(String fieldName, int delta, @Nullable List<ID> ids)
    {
        if (ArrayUtil.isEmpty(ids)) return;

        String column = resolveColumn(fieldName);
        Throws.ifBlank(column, "can not resolve column from field [{}] on entity", fieldName);

        this.update(new UpdateWrapper<ENTITY>()
                            .setSql(column + " = " + column + " - " + delta)
                            .in(BaseMybatisPlusEntity.Fields.id, ids));
    }

    default String resolveColumn(String fieldName)
    {
        @SuppressWarnings("unchecked")
        Class<ENTITY>            entityClass = (Class<ENTITY>) ResolvableType.forClass(BaseMybatisPlusMapper.class, this.getClass()).getGeneric(0).resolve();
        Map<String, ColumnCache> columnMap   = LambdaUtils.getColumnMap(entityClass);
        if (isEmpty(columnMap)) return null;

        return column(columnMap, fieldName);
    }

    default QueryWrapper<ENTITY> range(QueryWrapper<ENTITY> wrapper, Range... ranges)
    {
        if (ArrayUtil.isEmpty(ranges)) return wrapper;

        Map<String, ColumnCache> columnMap = LambdaUtils.getColumnMap(wrapper.getEntityClass());
        if (columnMap == null) return wrapper;

        // add ranges
        for (Range range : ranges) {
            if (range == null || isBlank(range.getField())) continue;

            String  column  = column(columnMap, range.getField());
            if (isBlank(column)) continue;
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

            String column = column(columnMap, order.getField());
            if (isBlank(column)) continue;

            if (order.getDirection() == Direction.DESC)
                wrapper.orderByDesc(column);
            else
                wrapper.orderByAsc(column);
        }
        return wrapper;
    }

    default String column(Map<String, ColumnCache> columnMap, String field)
    {
        if (isBlank(field) || isEmpty(columnMap)) return null;

        ColumnCache cache = columnMap.get(LambdaUtils.formatKey(field));
        if (cache != null) return cache.getColumn();

        return columnMap.values()
                        .stream()
                        .map(ColumnCache::getColumn)
                        .filter(field::equalsIgnoreCase)
                        .findFirst()
                        .orElse(null);
    }

    default QueryWrapper<ENTITY> autoQuery(ENTITY query)
    {
        return autoQuery(query, true);
    }

    default QueryWrapper<ENTITY> autoQuery(ENTITY query, boolean stringLike)
    {
        if (query == null) return Wrappers.query();

        @SuppressWarnings("unchecked")
        Class<ENTITY>            queryClass     = (Class<ENTITY>) query.getClass();
        QueryWrapper<ENTITY>     wrapper        = new QueryWrapper<>(queryClass);
        String                   queryClassName = queryClass.getName();
        Map<String, ColumnCache> columnMap      = LambdaUtils.getColumnMap(queryClass);
        Throws.ifEmpty(columnMap, "entity [{}] has none TableField", queryClassName);

        Field[] fields = ReflectUtil.getFields(query.getClass());
        Throws.ifEmpty(fields, "fields of [{}] has none TableField", queryClassName);

        for (Field field : fields) {
            String fieldName = field.getName();
            Object value     = ReflectUtil.getFieldValue(query, field);
            if (value == null) continue;

            ColumnCache cache = columnMap.get(LambdaUtils.formatKey(fieldName));
            if (cache == null) continue;
            String column = cache.getColumn();

            if (isJsonField(field)) {
                jsonQuery(wrapper, column, value);
                continue;
            }
            if (value instanceof String string && isBlank(string)) continue;
            if (value instanceof Collection<?> col && isEmpty(col)) continue;

            switch (value) {
                case Enum<?>       enumVal                                       -> wrapper.eq(column,   enumVal);
                case Number        number                                        -> wrapper.eq(column,   number);
                case LocalDateTime localDateTime                                 -> wrapper.eq(column,   localDateTime);
                case Date          date                                          -> wrapper.eq(column,   date);
                case Collection<?> col    when isNotEmpty(col)                   -> wrapper.in(column,   newHashSet(col));
                case String        string when stringLike  && isNotBlank(string) -> wrapper.like(column, string);
                case String        string when !stringLike && isNotBlank(string) -> wrapper.eq(column,   string);
                default                                                          -> wrapper.eq(column,   value);
            }
        }
        return wrapper;
    }

    default boolean isJsonField(Field field)
    {
        TableField tableField = field.getAnnotation(TableField.class);
        return tableField != null
               && AbstractJsonTypeHandler.class.isAssignableFrom(tableField.typeHandler());
    }

    default QueryWrapper<ENTITY> jsonQuery(QueryWrapper<ENTITY> wrapper, String column, Object value)
    {
        return switch (value) {
            case Collection<?> col when isEmpty(col) -> wrapper;
            case Collection<?> col                   -> jsonArrayContainsAny(wrapper, column, col);
            case Map<?, ?>     map when isEmpty(map) -> wrapper;
            default                                  -> jsonContains(wrapper, column, value);
        };
    }

    default QueryWrapper<ENTITY> jsonArrayContainsAny(QueryWrapper<ENTITY> wrapper, String column, Collection<?> values)
    {
        List<Object> jsonValues = new ArrayList<>();
        for (Object value : values) {
            if (value != null) jsonValues.add(value);
        }
        if (isEmpty(jsonValues)) return wrapper;

        return wrapper.and(jsonWrapper -> {
            for (int index = 0; index < jsonValues.size(); index++) {
                Object value = jsonValues.get(index);
                if (index > 0) jsonWrapper.or();
                jsonWrapper.apply("JSON_CONTAINS(" + column + ", {0})", JSONUtil.toJsonStr(value));
            }
        });
    }

    default QueryWrapper<ENTITY> jsonContains(QueryWrapper<ENTITY> wrapper, String column, Object value)
    {
        return wrapper.apply("JSON_CONTAINS(" + column + ", {0})", JSONUtil.toJsonStr(value));
    }

}
