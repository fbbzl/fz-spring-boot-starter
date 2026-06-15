package io.github.fbbzl.starter.pojo.tree;

import java.io.Serializable;

import static cn.hutool.core.text.CharSequenceUtil.splitToArray;
import static org.apache.commons.lang3.ArrayUtils.EMPTY_STRING_ARRAY;

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
        String ancestors = getAncestors();
        return ancestors != null ? splitToArray(ancestors, getAncestorSplitter()) : EMPTY_STRING_ARRAY;
    }
}
