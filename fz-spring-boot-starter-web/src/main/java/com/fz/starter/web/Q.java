package com.fz.starter.web;


import cn.hutool.core.util.ArrayUtil;
import cn.hutool.db.Page;
import cn.hutool.db.sql.Order;
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
    @NotNull
    DATA data;

    /**
     * request timestamp
     */
    @Schema(description = "request timestamp")
    long timestamp = System.currentTimeMillis();

    public static <DATA> Q<DATA> of(DATA data)
    {
        Q<DATA> q = new Q<>();
        q.setData(data);
        return q;
    }

    /**
     * ordered query request
     */
    @Getter
    @Setter
    @ToString
    @Schema(description = "generic ordered request objects")
    @FieldDefaults(level = PRIVATE)
    public static class OQ<DATA> extends Q<DATA>
    {

        @Size(max = 128, message = "{OQ.orders.size}")
        @Schema(description = "orders")
        Order[] orders;

        public static <DATA> OQ<DATA> of(DATA data, Order... orders)
        {
            OQ<DATA> oq = new OQ<>();
            oq.setData(data);
            oq.setOrders(orders);
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

        @NotNull(message = "{PQ.page}")
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
    public static class FQ<DATA> extends Q<DATA>
    {

        @Size(max = 128, message = "{FQ.size}")
        @Schema(description = "uploaded files")
        MultipartFile[] files;

        public MultipartFile getSingleFile()
        {
            return ArrayUtil.get(files, 0);
        }
    }
}
