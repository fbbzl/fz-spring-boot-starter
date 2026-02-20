package com.fz.springboot.starter.pojo;


import cn.hutool.db.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "unified request objects")
@FieldDefaults(level = PRIVATE)
public class Q<DATA> {

    /**
     * request data
     */
    @Schema(description = "business data")
    @Valid
    @NotNull
    DATA data;

    /**
     * request timestamp
     */
    @Schema(description = "request a timestamp")
    Long timestamp;

    public static <T> Q<T> of(T data, long timestamp) {
        Q<T> q = new Q<>();
        q.setData(data);
        q.setTimestamp(timestamp);
        return q;
    }

    public static <T> Q<T> of(T data) {
        return of(data, System.currentTimeMillis());
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
    @Schema(description = "unified paging request objects")
    @FieldDefaults(level = PRIVATE)
    public static class PQ<T> extends Q<T> {

        @Valid
        @NotNull(message = "{PQ.pagination}")
        @Schema(description = "paging parameters page numbers start from 0")
        private Page page;

        public static <T> PQ<T> of(T data, Page page, Long timestamp) {
            PQ<T> pq = new PQ<>();
            pq.setData(data);
            pq.setPage(page);
            pq.setTimestamp(timestamp);
            return pq;
        }

        public static <T> PQ<T> of(T data, Page pagination) {
            return of(data, pagination, System.currentTimeMillis());
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
    @Schema(description = "unify the file upload request objects")
    @EqualsAndHashCode(callSuper = false)
    public static class FQ<T> extends Q<T> {

        @Schema(description = "uploaded file")
        MultipartFile file;

    }
}
