package io.github.fbbzl.starter.pojo.tree;

import java.io.Serializable;

import static cn.hutool.core.text.CharSequenceUtil.splitToArray;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/4/4 11:31
 */

public interface Treeable<ID extends Serializable>
{

    String DEFAULT_ANCESTOR_SPLITTER = ",";

    ID getNodeId();

    ID getNodeParentId();

    String getAncestors();

    default String getAncestorSplitter()
    {
        return DEFAULT_ANCESTOR_SPLITTER;
    }

    default String[] getAncestorArray()
    {
        return splitToArray(getAncestors(), getAncestorSplitter());
    }
}
