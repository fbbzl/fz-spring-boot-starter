package io.github.fbbzl.starter.dal;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 4/28/2026 3:42 下午
 */

public interface Range
{
    String getField();

    Object getStart();

    Object getEnd();

    Boolean getClose();
}
