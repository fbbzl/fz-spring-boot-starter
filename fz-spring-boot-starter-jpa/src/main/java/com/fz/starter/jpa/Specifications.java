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
     * @param <T>            the type parameter
     * @param entityManager  the entity manager
     * @param sqlQueryEntity the sql query entity
     * @return the specification
     */
    public static <T extends BaseTableEntity> Specification<T> byAuto(final EntityManager entityManager, final T sqlQueryEntity) {
        final Class<T> type = (Class<T>) sqlQueryEntity.getClass();

        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>(10);
            EntityType<T> entityType = entityManager.getMetamodel().entity(type);
            Set<Attribute<? super T, ?>> allAttributes = entityType.getAttributes();

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
