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
     * source micro server
     */
    @Schema(description = "服务调用方", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Length(max = 1024)
    String source;

    /**
     * request timestamp
     */
    @Schema(description = "请求时间戳")
    Long timestamp;

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

        public Pageable toPageable() {
            Sorts sort = pagination.getSort();
            return PageRequest.of(pagination.getPageNumber(), pagination.getPageSize(), Sort.by(sort.direction, sort.properties));
        }

        @Getter
        @Setter
        @Schema(description = "分页参数, 页码从0开始")
        @FieldDefaults(level = PRIVATE)
        public static class Pagination {

            private static final Integer DEFAULT_PAGE_NUMBER = 0;
            private static final Integer DEFAULT_PAGE_SIZE   = 10;
            static               Sorts   DEFAULT_SORT        = new Sorts(new String[] {Fields.id}, Direction.ASC);

            @Schema(description = "页码, 从0开始")
            Integer pageNumber = DEFAULT_PAGE_NUMBER;

            @Schema(description = "页宽")
            Integer pageSize   = DEFAULT_PAGE_SIZE;

            @Valid
            @Schema(description = "排序规则")
            Sorts   sort       = DEFAULT_SORT;
        }

        @Schema(description = "分页参数, 页码从0开始")
        public record Sorts(
                @Size(min = 1, max = 8, message = "{Sorts.properties}")
                @Schema(description = "排序字段")
                String[] properties,
                @Schema(description = "排序顺序, ASC:正序,DESC:倒序")
                Direction direction
        ) {}
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
