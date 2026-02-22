package com.fz.starter.jpa;

import cn.hutool.db.sql.Condition.LikeType;
import com.fz.starter.pojo.entity.BaseTableEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static cn.hutool.db.sql.SqlUtil.buildLikeValue;

/**
 *
 * @author fbb
 * @version 1.0
 * @see BaseTableEntity
 * @since 2020/1/1/001 9:10
 */
@SuppressWarnings("all")
public class Specifications {

    private static final Predicate[] EMPTY_PREDICATE = new Predicate[] {};

    /**
     * By auto specification.
     *
     * @param <T>            the type parameter
     * @param entityManager  the entity manager
     * @param sqlQueryEntity the sql query entity
     * @return the specification
     */
    public static <T extends BaseTableEntity> Specification<T> byAuto(final EntityManager entityManager, final T sqlQueryEntity) {
        final Class<T> type = (Class<T>) sqlQueryEntity.getClass();

        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            // 查询条件集合
            List<Predicate> predicates = new ArrayList<>(10);
            // 从JPA EntityManager 中获取实体操作代理的元数据
            // JPA通过动态代理的方式为每一个实体操作创建代理, 并且所有代理类持有实体类的元数据
            EntityType<T> entityType = entityManager.getMetamodel().entity(type);

            // 获取所有属性, 包括父类属性
            Set<Attribute<? super T, ?>> allAttributes = entityType.getAttributes();

            // 全量遍历所申明的成员变量, 如果能够从查询对象中获取值的话, 就作为查询条件
            allAttributes.forEach(field -> {
                // 通过反射获取属性值
                Object queryValue = getValue(sqlQueryEntity, field);

                // 校验: 类型判断
                // 如果是 string 类型,特殊处理成 like 方式
                // 如果是 枚举 类型则使用 ordinal
                // 其他类型则使用等于
                boolean notNull    = queryValue != null,
                        stringType = notNull && field.getJavaType() == String.class,
                        enumType   = notNull && field.getJavaType().isEnum(),
                        otherType  = notNull && !stringType && !enumType;

                // string 使用like contain 匹配方式, 并添加到条件列表中
                if (stringType) {
                    Path queryPath = root.get(attribute(entityType, field));
                    predicates.add(cb.like(queryPath, buildLikeValue(queryValue.toString(), LikeType.Contains, false)));
                }
                else
                // 枚举类型 使用ordinal
                if (enumType) {
                    Path queryPath = root.get(attribute(entityType, field));
                    predicates.add(cb.equal(queryPath, ((Enum) queryValue).ordinal()));
                }
                else
                // 其他类型使用等于 匹配方式, 并添加到条件列表中
                if (otherType) {
                    Path queryPath = root.get(attribute(entityType, field));
                    predicates.add(cb.equal(queryPath, queryValue));
                }
            });
            // 条件列表为空时 直接返回 conjunction(条件与，没有时返回true)
            // 条件列表不为空 and 添加到查询cb中
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(EMPTY_PREDICATE));
        };
    }

    private static <T extends BaseTableEntity> Object getValue(T sqlQueryObject, Attribute<? super T, ?> attr) {
        return ReflectionUtils.getField((Field) attr.getJavaMember(), sqlQueryObject);
    }

    private static <T, E> SingularAttribute<? super T, E> attribute(EntityType<T> entityType, Attribute<?, E> attr) {
        return entityType.getSingularAttribute(attr.getName(), attr.getJavaType());
    }
}
