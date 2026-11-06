package io.github.fbbzl.starter.webflux;

import cn.hutool.core.util.PageUtil;
import cn.hutool.db.PageResult;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.FieldNameConstants;
import org.springframework.http.HttpStatus;

import java.util.List;

import static cn.hutool.db.PageResult.DEFAULT_PAGE_SIZE;
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
// generic response
@FieldDefaults(level = AccessLevel.PRIVATE)
public class R<DATA>
{

    private static final String
            DEFAULT_SUCCESS_MESSAGE = "ok",
            DEFAULT_FAIL_MESSAGE    = "fail";

    /**
     * response code
     */
    String code;

    /**
     * success tag
     */
    // if is success, true:success, false:fail
    Boolean success;

    /**
     * response message
     */
    String message;

    /**
     * the response data
     */
    @Setter(PROTECTED)
    // response data
    DATA data;

    public static <DATA> R<DATA> ok()
    {
        return ok(DEFAULT_SUCCESS_MESSAGE, null);
    }

    public static <DATA> R<DATA> ok(DATA data)
    {
        return ok(DEFAULT_SUCCESS_MESSAGE, data);
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
        return fail(DEFAULT_FAIL_MESSAGE);
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

    // generic page request
    public record PR<RECORD>(
            // pageNumber, start with 0
            int page,

            // pageSize
            int pageSize,

            // total page count
            long totalPage,

        // total record count
        long total,

        // page records
        List<RECORD> records)
    {

        public PR
        {
            page      = Math.max(page, 0);
            pageSize  = pageSize <= 0 ? DEFAULT_PAGE_SIZE : pageSize;
            totalPage = Math.max(totalPage, 0);
            records   = records == null ? List.of() : List.copyOf(records);
        }

        public boolean isFirst()
        {
            return page == PageUtil.getFirstPageNo();
        }

        public boolean isLast()
        {
            return totalPage > 0 && page >= (totalPage - 1);
        }

        public static <DATA> PR<DATA> of(int page, int pageSize, long total, List<DATA> records)
        {
            int normalizedPageSize = pageSize <= 0 ? DEFAULT_PAGE_SIZE : pageSize;
            return new PR<>(page, normalizedPageSize, PageUtil.totalPage(total, normalizedPageSize), total, records);
        }

        public static <DATA> PR<DATA> of(PageResult<DATA> pageResult)
        {
            return of(pageResult.getPage(), pageResult.getPageSize(), pageResult.getTotal(), pageResult);
        }
    }
}
