package com.fz.starter.web;

import cn.hutool.core.util.PageUtil;
import cn.hutool.db.Page;
import cn.hutool.db.PageResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.FieldNameConstants;
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
@FieldNameConstants
@SuppressWarnings("unchecked")
@NoArgsConstructor(access = PRIVATE)
@AllArgsConstructor(access = PRIVATE)
@Schema(description = "generic response")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class R<DATA>
{

    private static final String
            DEFAULT_SUCCESS_MESSAGE = "ok",
            DEFAULT_FAIL_MESSAGE    = "fail";

    private static final R<?>
            DEFAULT_R_SUCCESS = ok(DEFAULT_SUCCESS_MESSAGE),
            DEFAULT_R_FAIL    = fail(DEFAULT_FAIL_MESSAGE);

    /**
     * response code
     */
    @Schema(description = "responce code")
    String code;

    /**
     * success tag
     */
    @Schema(description = "if is success, true:success, false:fail")
    Boolean success;

    /**
     * response message
     */
    @Schema(description = "response message")
    String message;

    /**
     * the response data
     */
    @Setter(PROTECTED)
    @Schema(description = "response data")
    DATA data;

    public static <DATA> R<DATA> ok()
    {
        return (R<DATA>) DEFAULT_R_SUCCESS;
    }

    public static <DATA> R<DATA> ok(DATA data)
    {
        return ok(DEFAULT_SUCCESS_MESSAGE, data);
    }

    public static <DATA> R<DATA> ok(String message)
    {
        return ok(message, null);
    }

    public static <DATA> R<DATA> ok(String message, DATA data)
    {
        return ok(String.valueOf(HttpStatus.OK.value()), message, data);
    }

    public static <DATA> R<DATA> ok(String code, String message, DATA data)
    {
        return new R<>(code, true, message, data);
    }

    public static <DATA> R<DATA> fail()
    {
        return (R<DATA>) DEFAULT_R_FAIL;
    }

    public static <DATA> R<DATA> fail(String message)
    {
        return fail(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), message);
    }

    public static <DATA> R<DATA> fail(String code, String message)
    {
        return new R<>(code, false, message, null);
    }

    public static <DATA> R<DATA> fail(String code, String message, DATA data)
    {
        return new R<>(code, false, message, data);
    }

    @Data
    @FieldNameConstants
    @FieldDefaults(level = PRIVATE)
    @EqualsAndHashCode(callSuper = true)
    @Schema(description = "generic page request")
    public static class PR<DATA> extends R<PageResult<DATA>>
    {

        public static final int DEFAULT_PAGE_SIZE = Page.DEFAULT_PAGE_SIZE;

        @Schema(description = "pageNumber, start with 0")
        int page;

        @Schema(description = "pageSize")
        int pageSize;

        @Schema(description = "total page count")
        int totalPage;

        @Schema(description = "total record count")
        int total;

        public PR()
        {
            this(0, DEFAULT_PAGE_SIZE);
        }

        public PR(int page, int pageSize)
        {
            this.page     = Math.max(page, 0);
            this.pageSize = pageSize <= 0 ? DEFAULT_PAGE_SIZE : pageSize;
        }

        public PR(int page, int pageSize, int total)
        {
            this(page, pageSize);

            this.total     = total;
            this.totalPage = PageUtil.totalPage(total, pageSize);
        }

        public boolean isFirst()
        {
            return this.page == PageUtil.getFirstPageNo();
        }

        public boolean isLast()
        {
            return this.page >= (this.totalPage - 1);
        }

        public static <DATA> PR<DATA> ok(PageResult<DATA> pageResult)
        {
            PR<DATA> pr = new PR<>(pageResult.getPage(), pageResult.getPageSize(), pageResult.getTotal());
            pr.setData(pageResult);
            return pr;
        }

    }
}
