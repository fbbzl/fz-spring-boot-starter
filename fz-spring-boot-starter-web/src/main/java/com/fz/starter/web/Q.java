package com.fz.starter.web;


import cn.hutool.core.util.ArrayUtil;
import cn.hutool.db.Page;
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

    public static <T> Q<T> of(T data)
    {
        Q<T> q = new Q<>();
        q.setData(data);
        return q;
    }

    /**
     * page request
     *
     * @param <T> page request data type
     * @author fengbinbin
     * @version 1.0
     * @since 2025/8/22 15:30
     */
    @Getter
    @Setter
    @ToString
    @Schema(description = "generic paging request objects")
    @FieldDefaults(level = PRIVATE)
    public static class PQ<T> extends Q<T>
    {

        @NotNull(message = "{PQ.page}")
        @Schema(description = "paging parameters page numbers start from 0")
        Page page;

        public static <T> PQ<T> of(T data, Page page)
        {
            PQ<T> pq = new PQ<>();
            pq.setData(data);
            pq.setPage(page);
            return pq;
        }
    }

    /**
     * file upload request
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2025/9/3 10:13
     */
    @Data
    @FieldDefaults(level = PRIVATE)
    @Schema(description = "generic the file upload request objects")
    @EqualsAndHashCode(callSuper = false)
    public static class FQ<T> extends Q<T>
    {

        @Size(max = 128)
        @Schema(description = "uploaded files")
        MultipartFile[] files;

        public MultipartFile getSingleFile()
        {
            return ArrayUtil.get(files, 0);
        }
    }
}
