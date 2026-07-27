package io.github.fbbzl.starter.pojo.tree;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.lang.tree.parser.NodeParser;
import cn.hutool.core.util.ReflectUtil;
import io.github.fbbzl.starter.pojo.bo.BaseBo;

import java.io.Serializable;
import java.util.List;

import static cn.hutool.core.text.CharSequenceUtil.splitToArray;
import static org.apache.commons.lang3.ArrayUtils.EMPTY_STRING_ARRAY;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/4/4 11:31
 */
@SuppressWarnings("unchecked")
public interface Treeable<ID extends Serializable>
{

    String DEFAULT_ANCESTOR_SPLITTER    = ",";
    String DEFAULT_NODE_ID_FIELD        = "id";
    String DEFAULT_NODE_PARENT_ID_FIELD = "parentId";

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

    static boolean isTreeableType(Class<?> clazz)
    {
        return clazz != null && Treeable.class.isAssignableFrom(clazz);
    }

    static <ID extends Serializable> List<Tree<ID>> buildByTreeable(
            List<? extends BaseBo<ID>> list,
            ID rootId,
            TreeNodeConfig treeNodeConfig)
    {
        List<BaseBo<ID>> castedList = (List<BaseBo<ID>>) list;
        return build(castedList, rootId, treeNodeConfig, (bo, tree) ->
        {
            Treeable<ID> treeNodeBo = (Treeable<ID>) bo;
            fillTreeNode(tree, treeNodeBo.getNodeId(), treeNodeBo.getNodeParentId(), bo);
        });
    }

    /**
     * Check whether the class can be used as a tree node by reflection.
     * The default reflection mode requires {@link #DEFAULT_NODE_ID_FIELD} and {@link #DEFAULT_NODE_PARENT_ID_FIELD} fields.
     *
     * @param clazz the class to check
     * @return {@code true} if the class has both required fields
     */
    static boolean isReflectTreeable(Class<?> clazz)
    {
        if (clazz == null || clazz.isInterface() || clazz.isPrimitive() || clazz.isArray()) {
            return false;
        }
        return ReflectUtil.getField(clazz, DEFAULT_NODE_ID_FIELD) != null
               &&
               ReflectUtil.getField(clazz, DEFAULT_NODE_PARENT_ID_FIELD) != null;
    }

    static <ID extends Serializable> List<Tree<ID>> buildByReflectTreeable(
            List<?> list,
            ID rootId,
            TreeNodeConfig treeNodeConfig)
    {
        List<Object> objectList = (List<Object>) list;
        return build(objectList, rootId, treeNodeConfig, (bo, tree) ->
        {
            ID id       = (ID) ReflectUtil.getFieldValue(bo, DEFAULT_NODE_ID_FIELD);
            ID parentId = (ID) ReflectUtil.getFieldValue(bo, DEFAULT_NODE_PARENT_ID_FIELD);
            fillTreeNode(tree, id, parentId, bo);
        });
    }

    private static <ID extends Serializable, T> List<Tree<ID>> build(
            List<T> list,
            ID rootId,
            TreeNodeConfig treeNodeConfig,
            NodeParser<T, ID> parser)
    {
        return TreeUtil.build(list, rootId, treeNodeConfig, parser);
    }

    private static <ID extends Serializable, T> void fillTreeNode(
            Tree<ID> tree,
            ID id,
            ID parentId,
            T data)
    {
        tree.setId(id);
        tree.setParentId(parentId);
        tree.putExtra("data", data);
    }
}
