package io.github.fbbzl.starter.mybatisplus;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.db.Page;
import cn.hutool.db.PageResult;
import cn.hutool.db.sql.Direction;
import cn.hutool.db.sql.Order;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.support.ColumnCache;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static cn.hutool.core.text.CharSequenceUtil.isBlank;

/**
 * Hutool page adapter for MyBatis-Plus pagination.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026/5/19
 */
@Getter
@Setter
@Accessors(chain = true)
public class HP<T> implements IPage<T>
{

    final Page            page;
    List<T>               records = Collections.emptyList();
    long                  total;
    long                  size;
    long                  current;
    final List<OrderItem> orders;

    public HP(Page page, Class<?> entityClass)
    {
        Objects.requireNonNull(entityClass, "entityClass must not be null");
        this.page    = page;
        this.size    = page.getPageSize();
        this.current = page.getPageNumber() + 1L;
        this.orders  = toOrderItems(entityClass, page.getOrders());
    }

    public static <T> HP<T> of(Page page, Class<?> entityClass)
    {
        return new HP<>(page, entityClass);
    }

    @Override
    public List<OrderItem> orders()
    {
        return orders;
    }

    public PageResult<T> toPageResult()
    {
        PageResult<T> pageResult = new PageResult<>((int) current - 1, (int) size, (int) total);
        pageResult.addAll(records);
        return pageResult;
    }

    static List<OrderItem> toOrderItems(Class<?> entityClass, Order... orders)
    {
        if (ArrayUtil.isEmpty(orders)) return Collections.emptyList();

        return Stream.of(orders)
                     .filter(order -> order != null && !isBlank(order.getField()))
                     .map(order -> order.getDirection() == Direction.ASC
                                   ? OrderItem.asc(orderField(entityClass, order.getField()))
                                   : OrderItem.desc(orderField(entityClass, order.getField())))
                     .toList();
    }

    static String orderField(Class<?> entityClass, String field)
    {
        if (entityClass == null || isBlank(field)) return field;

        Map<String, ColumnCache> columnMap = LambdaUtils.getColumnMap(entityClass);
        if (columnMap == null) return field;

        ColumnCache cache = columnMap.get(LambdaUtils.formatKey(field));
        if (cache != null) return cache.getColumn();

        return columnMap.values()
                        .stream()
                        .map(ColumnCache::getColumn)
                        .filter(field::equalsIgnoreCase)
                        .findFirst()
                        .orElse(field);
    }
}
