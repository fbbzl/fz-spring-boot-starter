package com.fz.starter.dal;

import lombok.experimental.UtilityClass;

import static cn.hutool.core.text.CharSequenceUtil.format;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/9/2 18:04
 */
@UtilityClass
public class SqlConstants
{

    public static final String LIMIT_1  = limit(1);
    public static final String DISTINCT = " DISTINCT ";

    public static String limit(int limitCount)
    {
        return format(" LIMIT {} ", limitCount);
    }
}
