package io.github.fbbzl.starter.jpa;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.db.sql.Condition.LikeType;
import cn.hutool.db.sql.Direction;
import cn.hutool.db.sql.Order;
import io.github.fbbzl.starter.dal.Range;
import io.github.fbbzl.starter.pojo.entity.BaseTableEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import org.fz.erwin.exception.Throws;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static cn.hutool.core.text.CharSequenceUtil.isBlank;
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
        return byAuto(entityManager, sqlQueryEntity, (Range[]) null, orders);
    }

    public static <ENTITY extends BaseTableEntity> Specification<ENTITY> byAuto(
            final EntityManager entityManager,
            final ENTITY sqlQueryEntity,
            final Range[] ranges,
            Order... orders)
    {
        Throws.ifNull(sqlQueryEntity, () -> "sqlQueryEntity can not be null when building specification");

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

            // range query
            range(predicates, root, cb, entityType, allAttributes, ranges);

            // order query
            order(root, query, cb, orders);

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(EMPTY_PREDICATE));
        };
    }

    private static Comparable comparable(Object value)
    {
        return value instanceof Comparable<?> comparable ? comparable : null;
    }

    private static <ENTITY extends BaseTableEntity> Object getValue(ENTITY sqlQueryObject, Attribute<? super ENTITY, ?> attr)
    {
        return ReflectionUtils.getField((Field) attr.getJavaMember(), sqlQueryObject);
    }

    private static <ENTITY, E> SingularAttribute<? super ENTITY, E> attribute(EntityType<ENTITY> entityType, Attribute<?, E> attr)
    {
        return entityType.getSingularAttribute(attr.getName(), attr.getJavaType());
    }

    private static <ENTITY extends BaseTableEntity> void range(
            List<Predicate> predicates,
            Root<ENTITY> root,
            CriteriaBuilder cb,
            EntityType<ENTITY> entityType,
            Set<Attribute<? super ENTITY, ?>> allAttributes,
            Range... ranges)
    {
        if (ArrayUtil.isEmpty(ranges)) return;

        for (Range range : ranges) {
            if (range == null || isBlank(range.getField())) continue;

            Attribute<? super ENTITY, ?> field
                    = allAttributes.stream()
                                   .filter(attr -> attr.getName().equals(range.getField()))
                                   .findFirst()
                                   .orElse(null);
            if (field == null) continue;

            @SuppressWarnings("unchecked")
            Path<Comparable> fieldPath = (Path<Comparable>) root.get(attribute(entityType, field));
            Comparable       start     = comparable(range.getStart());
            Comparable       end       = comparable(range.getEnd());
            boolean          isClose   = Boolean.TRUE.equals(range.getClose());

            if (start != null) predicates.add(isClose ? cb.greaterThanOrEqualTo(fieldPath, start) : cb.greaterThan(fieldPath, start));
            if (end != null)   predicates.add(isClose ? cb.lessThanOrEqualTo(fieldPath, end) : cb.lessThan(fieldPath, end));
        }
    }

    private static <ENTITY> void order(Root<ENTITY> root, CriteriaQuery<?> query, CriteriaBuilder cb, Order... hutoolOrders)
    {
        if (ArrayUtil.isEmpty(hutoolOrders)) return;

        query.orderBy(Stream.of(hutoolOrders)
                            .map(order -> order.getDirection() == Direction.ASC
                                          ? cb.asc(root.get(order.getField()))
                                          : cb.desc(root.get(order.getField())))
                            .toList());
    }

}
