package com.fz.springboot.starter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;


/**
 * generic http result
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2020/12/16 16:36
 */

@Getter
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
    Boolean status;

    /**
     * response message
     */
    @Schema(description = "响应信息")
    String message;

    /**
     * the response data
     */
    @Schema(description = "响应业务数据")
    T data;

    public static R<?> ok() {
        return DEFAULT_R_SUCCESS;
    }

    public static <T> R<T> ok(T data) {
        return ok(DEFAULT_SUCCESS_MESSAGE, data);
    }

    public static <T> R<T> ok(String msg) {
        return ok(msg, null);
    }

    public static <T> R<T> ok(String message, T data) {
        return ok(String.valueOf(HttpStatus.OK.value()), message, data);
    }

    public static <T> R<T> ok(String code, String message, T data) {
        return new R<>(code, true, message, data);
    }

    public static R<?> fail() {
        return DEFAULT_R_FAIL;
    }

    public static <T> R<T> fail(T data) {
        return fail(DEFAULT_FAIL_MESSAGE, data);
    }

    public static <T> R<T> fail(String msg) {
        return fail(msg, null);
    }

    public static <T> R<T> fail(String message, T data) {
        return fail(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), message, data);
    }

    public static <T> R<T> fail(String code, String message, T data) {
        return new R<>(code, false, message, data);
    }

    public static <T> R<T> bad(String message) {
        return fail(String.valueOf(HttpStatus.BAD_REQUEST.value()), message, null);
    }

    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "统一分页响应对象")
    public static class PR<T> extends R<List<T>> {

        @JsonIgnoreProperties("content")
        Page<T> pagination;

        public PR(String code, Boolean success, String message, Page<T> page) {
            super(code, success, message, page.getContent());
            this.pagination = new PageImpl<>(page.getContent(), page.getPageable(), page.getTotalElements());
        }

        public static <T> PR<T> ok(Page<T> page) {
            return ok(DEFAULT_SUCCESS_MESSAGE, page);
        }

        public static <T> PR<T> ok(String message, Page<T> page) {
            return ok(null, message, page);
        }

        public static <T> PR<T> ok(String code, String message, Page<T> page) {
            return new PR<>(code, true, message, page);
        }

    }

}
