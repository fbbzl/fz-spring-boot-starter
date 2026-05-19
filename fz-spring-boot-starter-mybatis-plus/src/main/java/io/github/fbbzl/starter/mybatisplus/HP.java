package io.github.fbbzl.starter.mybatisplus;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.db.Page;
import cn.hutool.db.PageResult;
import cn.hutool.db.sql.Direction;
import cn.hutool.db.sql.Order;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

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

    public HP(Page page)
    {
        this.page    = page;
        this.size    = page.getPageSize();
        this.current = page.getPageNumber() + 1L;
        this.orders  = toOrderItems(page.getOrders());
    }

    public static <T> HP<T> of(Page page)
    {
        return new HP<>(page);
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

    static List<OrderItem> toOrderItems(Order... orders)
    {
        if (ArrayUtil.isEmpty(orders)) return Collections.emptyList();

        return Stream.of(orders)
                     .map(order -> order.getDirection() == Direction.ASC
                                   ? OrderItem.asc(order.getField())
                                   : OrderItem.desc(order.getField()))
                     .toList();
    }
}
