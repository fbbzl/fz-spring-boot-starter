package com.fz.springboot.starter;


import com.fz.springboot.starter.jpa.BaseEntity.Fields;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.multipart.MultipartFile;

import static lombok.AccessLevel.*;

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
     * traceId
     */
    @Schema(description = "日志追踪id")
    @Length(max = 1024)
    String traceId;
    /**
     * request timestamp
     */
    @Schema(description = "请求时间戳")
    Long   timestamp;

    public static <T> Q<T> of(T data, String traceId, Long timestamp) {
        Q<T> q = new Q<>();
        q.setData(data);
        q.setTraceId(traceId);
        q.setTimestamp(timestamp);
        return q;
    }

    public static <T> Q<T> of(T data, String traceId) {
        return of(data, traceId, System.currentTimeMillis());
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

        @Valid
        @Getter(NONE)
        @NotNull(message = "{PQ.pagination}")
        @Schema(description = "分页参数, 页码从0开始")
        private Pagination pagination;

        public static <T> PQ<T> of(T data, Pagination pagination, String traceId, Long timestamp) {
            PQ<T> pq = new PQ<>();
            pq.setData(data);
            pq.setPagination(pagination);
            pq.setTraceId(traceId);
            pq.setTimestamp(timestamp);
            return pq;
        }

        public static <T> PQ<T> of(T data, Pagination pagination, String traceId) {
            return of(data, pagination, traceId, System.currentTimeMillis());
        }

        public Pageable toPageable() {
            return PageRequest.of(pagination.getPageNumber(), pagination.getPageSize(), pagination.getSort());
        }

        @Getter
        @Setter
        @Schema(description = "分页参数, 页码从0开始")
        @FieldDefaults(level = PRIVATE)
        public static class Pagination {

            private static final Integer DEFAULT_PAGE_NUMBER = 0;
            private static final Integer DEFAULT_PAGE_SIZE   = 10;

            @Schema(description = "页码, 从0开始")
            Integer pageNumber = DEFAULT_PAGE_NUMBER;

            @Schema(description = "页宽")
            Integer pageSize = DEFAULT_PAGE_SIZE;

            @Valid
            @Schema(description = "排序规则")
            Sort sort = Sort.by(Direction.ASC, Fields.id);
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
