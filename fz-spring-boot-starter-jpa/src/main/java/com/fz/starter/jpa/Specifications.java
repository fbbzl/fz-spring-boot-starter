package com.fz.starter.jpa;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.db.sql.Condition.LikeType;
import cn.hutool.db.sql.Direction;
import cn.hutool.db.sql.Order;
import com.fz.starter.pojo.entity.BaseTableEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
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
     * @param <ENTITY>            the type parameter
     * @param entityManager  the entity manager
     * @param sqlQueryEntity the sql query entity
     * @return the specification
     */
    public static <ENTITY extends BaseTableEntity> Specification<ENTITY> byAuto(
            final EntityManager entityManager,
            final ENTITY sqlQueryEntity,
            Order... orders)
    {
        final Class<ENTITY> type = (Class<ENTITY>) sqlQueryEntity.getClass();

        return (Root<ENTITY> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate>                   predicates    = new ArrayList<>(10);
            EntityType<ENTITY>                entityType    = entityManager.getMetamodel().entity(type);
            Set<Attribute<? super ENTITY, ?>> allAttributes = entityType.getAttributes();

            // for each field
            allAttributes.forEach(field -> {
                Object queryValue = getValue(sqlQueryEntity, field);
                if (queryValue == null) return;

                switch (queryValue) {
                    case List<?>       list          -> predicates.add(cb.in(root.get(attribute(entityType, field))).in(list));
                    case String        string        -> predicates.add(cb.like((Path<String>) root.get(attribute(entityType, field)), buildLikeValue(string, LikeType.Contains, false)));
                    case Enum<?>       enumVal       -> predicates.add(cb.equal(root.get(attribute(entityType, field)), enumVal.ordinal()));
                    case Number        number        -> predicates.add(cb.equal(root.get(attribute(entityType, field)), number));
                    case LocalDateTime localDateTime -> predicates.add(cb.equal(root.get(attribute(entityType, field)), localDateTime));
                    case Date          date          -> predicates.add(cb.equal(root.get(attribute(entityType, field)), date));
                    default                          -> predicates.add(cb.equal(root.get(attribute(entityType, field)), queryValue));
                }
            });

            // order
            order(root, cb, orders);

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(EMPTY_PREDICATE));
        };
    }

    private static <ENTITY extends BaseTableEntity> Object getValue(ENTITY sqlQueryObject, Attribute<? super ENTITY, ?> attr)
    {
        return ReflectionUtils.getField((Field) attr.getJavaMember(), sqlQueryObject);
    }

    private static <ENTITY, E> SingularAttribute<? super ENTITY, E> attribute(EntityType<ENTITY> entityType, Attribute<?, E> attr)
    {
        return entityType.getSingularAttribute(attr.getName(), attr.getJavaType());
    }

    private static <ENTITY> void order(Root<ENTITY> root, CriteriaBuilder cb, Order... hutoolOrders)
    {
        if (ArrayUtil.isEmpty(hutoolOrders)) return;

        for (Order hutoolOrder : hutoolOrders) {
            if (hutoolOrder.getDirection() == Direction.ASC)
                cb.asc(root.get(hutoolOrder.getField()));
            else
                cb.desc(root.get(hutoolOrder.getField()));
        }
    }

}
