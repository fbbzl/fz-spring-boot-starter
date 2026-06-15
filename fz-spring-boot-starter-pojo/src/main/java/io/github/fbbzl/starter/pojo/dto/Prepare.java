package io.github.fbbzl.starter.pojo.dto;

/**
 * DTO lifecycle hook.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026/5/22 16:00
 */
public interface Prepare
{

    default void prepareCreate() {}

    default void prepareUpdate() {}

    default void prepareQuery() {}

    default void prepareDelete() {}
}
