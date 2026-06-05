package io.github.fbbzl.starter.web;


import cn.hutool.core.util.ArrayUtil;
import cn.hutool.db.Page;
import cn.hutool.db.sql.Order;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.fbbzl.starter.dal.Range;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

/**
 *
 * @param <DATA> request data type
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/22 15:00
 */
@Getter
@Setter
@ToString
@NoArgsConstructor(access = PROTECTED)
@Schema(description = "generic request objects")
@FieldDefaults(level = PRIVATE)
public class Q<DATA>
{

    /**
     * request data
     */
    @Schema(description = "request data")
    @Valid
    @NotNull(message = "{Q.data}")
    DATA data;

    /**
     * request timestamp
     */
    @Schema(description = "request timestamp")
    long timestamp = System.currentTimeMillis();

    @Size(max = 64, message = "{Q.requestId.size}")
    @Schema(description = "request identifier")
    String requestId;

    public static <DATA> Q<DATA> of(DATA data)
    {
        Q<DATA> q = new Q<>();
        q.setData(data);
        return q;
    }

    /**
     * range query request
     */
    @Getter
    @Setter
    @ToString
    @Schema(description = "generic range query request objects")
    @FieldDefaults(level = PRIVATE)
    public static class RQ<DATA> extends Q<DATA>
    {

        @Valid
        @Size(max = 128, message = "{RQ.ranges.size}")
        @Schema(description = "range query conditions")
        Range[] ranges;

        @SafeVarargs
        public static <DATA, RANGE extends Range> RQ<DATA> of(DATA data, RANGE... ranges)
        {
            RQ<DATA> rq = new RQ<>();
            rq.setData(data);
            rq.setRanges(ranges);
            return rq;
        }

        /**
         * range query condition
         */
        @Getter
        @Setter
        @ToString
        @Schema(description = "range query condition")
        @FieldDefaults(level = PRIVATE)
        public static class BaseRange implements Range
        {

            @Size(max = 128, message = "{RQ.BaseRange.field.size}")
            @NotNull(message = "{RQ.BaseRange.field}")
            @Schema(description = "range query field")
            String field;

            @Schema(description = "range query start value")
            Object start;

            @Schema(description = "range query end value")
            Object end;

            @Schema(description = "whether the range query is closed interval")
            Boolean close = Boolean.TRUE;

            public static <VALUE extends Comparable<? super VALUE>> Range of(String field, VALUE start, VALUE end)
            {
                return of(field, start, end, true);
            }

            public static <VALUE extends Comparable<? super VALUE>> Range of(String field, VALUE start, VALUE end, Boolean close)
            {
                BaseRange range = new BaseRange();
                range.setField(field);
                range.setStart(start);
                range.setEnd(end);
                range.setClose(close);
                return range;
            }
        }
    }

    /**
     * ordered query request
     */
    @Getter
    @Setter
    @ToString
    @Schema(description = "generic ordered request objects")
    @FieldDefaults(level = PRIVATE)
    public static class OQ<DATA> extends RQ<DATA>
    {

        @Size(max = 128, message = "{OQ.orders.size}")
        @Schema(description = "orders")
        Order[] orders;

        public static <DATA> OQ<DATA> of(DATA data, Order... orders)
        {
            return of(data, orders, (BaseRange) null);
        }

        public static <DATA> OQ<DATA> of(DATA data, Order[] orders, BaseRange... ranges)
        {
            OQ<DATA> oq = new OQ<>();
            oq.setData(data);
            oq.setOrders(orders);
            oq.setRanges(ranges);
            return oq;
        }
    }

    /**
     * page request
     */
    @Getter
    @Setter
    @ToString
    @Schema(description = "generic paging request objects")
    @FieldDefaults(level = PRIVATE)
    public static class PQ<DATA> extends Q<DATA>
    {

        @NotNull(message = "{PQ.page.page}")
        @Schema(description = "paging parameters page numbers start from 0")
        Page page;

        public static <DATA> PQ<DATA> of(DATA data, Page page)
        {
            PQ<DATA> pq = new PQ<>();
            pq.setData(data);
            pq.setPage(page);
            return pq;
        }
    }

    /**
     * file upload request
     */
    @Data
    @FieldDefaults(level = PRIVATE)
    @Schema(description = "generic the file upload request objects")
    @EqualsAndHashCode(callSuper = false)
    public static class FQ
    {

        @Size(max = 128, message = "{FQ.files.size}")
        @Schema(description = "uploaded files")
        MultipartFile[] files;

        @JsonIgnore
        public MultipartFile getSingleFile()
        {
            return ArrayUtil.get(files, 0);
        }
    }
}
