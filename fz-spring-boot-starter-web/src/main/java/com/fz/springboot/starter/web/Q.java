package com.fz.springboot.starter.web;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

/**
 *
 * @param <T> request data type
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/22 15:00
 */
@Getter
@Setter
@ToString
@NoArgsConstructor(access = PROTECTED)
@Schema(description = "统一请求对象")
@FieldDefaults(level = PRIVATE)
public class Q<T> {

    /**
     * request data
     */
    @Schema(description = "业务数据")
    @Valid
    @NotNull
    T data;

    /**
     * request timestamp
     */
    @Schema(description = "请求时间戳")
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
    @Schema(description = "统一分页请求对象")
    @FieldDefaults(level = PRIVATE)
    public static class PQ<T> extends Q<T> {

        PageRequest pagination;

        public static <T> PQ<T> of(T data, PageRequest pagination, Long timestamp) {
            PQ<T> pq = new PQ<>();
            pq.setData(data);
            pq.setPagination(pagination);
            pq.setTimestamp(timestamp);
            return pq;
        }

        public static <T> PQ<T> of(T data, PageRequest pagination) {
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
    @Schema(description = "统一文件上传请求对象")
    @EqualsAndHashCode(callSuper = false)
    public static class FQ<T> extends Q<T> {

        @Size(max = 8, message = "{FQ.files}")
        @Schema(description = "上传的文件", minLength = 1, maxLength = 8)
        MultipartFile[] files;

    }
}
