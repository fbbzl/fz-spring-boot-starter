package com.fz.springboot.starter.pojo;

import cn.hutool.core.util.PageUtil;
import cn.hutool.db.Page;
import cn.hutool.db.PageResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;


/**
 * generic http result
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2020/12/16 16:36
 */

@Getter
@SuppressWarnings("unchecked")
@Schema(description = "统一响应对象")
@NoArgsConstructor(access = PRIVATE)
@AllArgsConstructor(access = PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class R<T> {

    private static final String
            DEFAULT_SUCCESS_MESSAGE = "ok",
            DEFAULT_FAIL_MESSAGE    = "fail";

    private static final R<?>
            DEFAULT_R_SUCCESS = ok(DEFAULT_SUCCESS_MESSAGE),
            DEFAULT_R_FAIL    = fail(DEFAULT_FAIL_MESSAGE);

    /**
     * response code
     */
    @Schema(description = "响应码")
    String code;

    /**
     * success tag
     */
    @Schema(description = "是否成功, true:成功, false:异常")
    Boolean success;

    /**
     * response message
     */
    @Schema(description = "响应信息")
    String message;

    /**
     * the response data
     */
    @Setter(PROTECTED)
    @Schema(description = "响应业务数据")
    T data;

    public static <T> R<T> ok() {
        return (R<T>) DEFAULT_R_SUCCESS;
    }

    public static <T> R<T> ok(T data) {
        return ok(DEFAULT_SUCCESS_MESSAGE, data);
    }

    public static <T> R<T> ok(String message) {
        return ok(message, null);
    }

    public static <T> R<T> ok(String message, T data) {
        return ok(String.valueOf(HttpStatus.OK.value()), message, data);
    }

    public static <T> R<T> ok(String code, String message, T data) {
        return new R<>(code, true, message, data);
    }

    public static <T> R<T> fail() {
        return (R<T>) DEFAULT_R_FAIL;
    }

    public static <T> R<T> fail(String message) {
        return fail(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), message);
    }

    public static <T> R<T> fail(String code, String message) {
        return new R<>(code, false, message, null);
    }

    public static <T> R<T> fail(String code, String message, T data) {
        return new R<>(code, false, message, data);
    }


    @EqualsAndHashCode(callSuper = true)
    @Data
    @FieldDefaults(level = PRIVATE)
    @Schema(description = "统一分页响应对象")
    public static class PR<DATA> extends R<PageResult<DATA>> {

        public static final int DEFAULT_PAGE_SIZE = Page.DEFAULT_PAGE_SIZE;

        @Schema(description = "页码, 从0开始")
        int page;

        @Schema(description = "每页结果数")
        int pageSize;

        @Schema(description = "总页数")
        int totalPage;

        @Schema(description = "总数")
        int total;

        public PR() {
            this(0, DEFAULT_PAGE_SIZE);
        }

        public PR(int page, int pageSize) {
            this.page     = Math.max(page, 0);
            this.pageSize = pageSize <= 0 ? DEFAULT_PAGE_SIZE : pageSize;
        }

        public PR(int page, int pageSize, int total) {
            this(page, pageSize);

            this.total     = total;
            this.totalPage = PageUtil.totalPage(total, pageSize);
        }

        public boolean isFirst() {
            return this.page == PageUtil.getFirstPageNo();
        }

        public boolean isLast() {
            return this.page >= (this.totalPage - 1);
        }

        public static <DATA> PR<DATA> ok(PageResult<DATA> pageResult) {
            PR<DATA> pr = new PR<>(pageResult.getPage(), pageResult.getPageSize(), pageResult.getTotal());
            pr.setData(pageResult);
            return pr;
        }

    }
}
