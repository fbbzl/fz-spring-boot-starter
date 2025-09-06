package com.fz.springboot.starter.jpa.repository;

import com.fz.springboot.starter.jpa.BaseEntity;
import com.fz.springboot.starter.jpa.Specifications;
import com.fz.springboot.starter.jpa.SqlLike;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static cn.hutool.core.collection.CollUtil.isEmpty;

/**
 *
 * @author fbb
 * @version 1.0
 * @since 2020/1/3/001 13:30
 */
public class BaseRepositoryImpl<T extends BaseEntity> extends SimpleJpaRepository<T, Long> implements BaseRepository<T> {

    private final Class<T>      entityClass;
    private final EntityManager entityManager;

    @Autowired(required = false)
    public BaseRepositoryImpl(Class<T> entityClass, EntityManager entityManager) {
        super(entityClass, entityManager);
        this.entityManager = entityManager;
        this.entityClass   = entityClass;
    }

    @Override
    public List<T> find(T entity) {
        return findAll(Specifications.byAuto(entityManager, entity));
    }

    @Override
    public Page<T> find(T entity, Pageable pageable) {
        return findAll(Specifications.byAuto(entityManager, entity), pageable);
    }

    @Override
    public List<T> find(String property, Object value) {
        CriteriaBuilder  cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(entityClass);

        Root<T> root = cq.from(entityClass);

        cq.where(cb.equal(root.get(property), value));
        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public List<T> find(Map<String, Object> valueMap) {
        CriteriaBuilder  cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(entityClass);

        Predicate[] predicates = this.mapToPredicates(valueMap, cb, cq.from(entityClass));

        cq.where(predicates);

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public Page<T> find(Map<String, Object> valueMap, Pageable pageable) {
        CriteriaBuilder  cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(entityClass);

        Predicate[] predicates = this.mapToPredicates(valueMap, cb, cq.from(entityClass));

        cq.where(predicates);

        TypedQuery<T> pageQuery = entityManager.createQuery(cq)
                                               .setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
                                               .setMaxResults(pageable.getPageSize());

        return new PageImpl<T>(pageQuery.getResultList(), pageable, total(predicates));
    }

    @Override
    public boolean exists(Map<String, Object> valueMap) {
        if (isEmpty(valueMap)) return false;

        CriteriaBuilder     cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);

        Root<T> root = cq.from(entityClass);

        cq.where(this.mapToPredicates(valueMap, cb, root)).select(cb.count(root));

        return entityManager.createQuery(cq).getSingleResult() > 0;
    }

    @Transactional
    @Override
    public void logicalDelete(Long id) {
        String jpql = "UPDATE " + entityClass.getSimpleName() + " e SET e.isDeleted = true WHERE e.id = :id";
        entityManager.createQuery(jpql)
                     .setParameter("id", id)
                     .executeUpdate();
    }

    @Transactional
    @Override
    public void logicalDeleteAll(Collection<Long> ids) {
        if (isEmpty(ids)) return;

        String jpql = "UPDATE " + entityClass.getSimpleName() + " e SET e.isDeleted = true WHERE e.id IN :ids";
        entityManager.createQuery(jpql)
                     .setParameter("ids", ids)
                     .executeUpdate();
    }

    //************************************************ private start *************************************************//

    private Predicate[] mapToPredicates(Map<String, Object> map, CriteriaBuilder cb, Root<T> root) {
        List<Predicate> predicates = new ArrayList<>();
        map.forEach((k, v) -> {
            boolean notNull = Objects.nonNull(v), stringType = notNull && v.getClass() == String.class, enumType =
                    notNull && v.getClass().isEnum(), otherType = notNull && !stringType && !enumType;

            // string 使用like 匹配方式, 并添加到条件列表中
            if (stringType) {
                predicates.add(cb.like(root.get(k), SqlLike.contain(v)));
            }
            else
            // 枚举类型 使用ordinal
            if (enumType) {
                predicates.add(cb.equal(root.get(k), ((Enum) v).ordinal()));
            }
            else
            // 其他类型使用 等于的匹配方式, 并添加到条件列表中
            if (otherType) {
                predicates.add(cb.equal(root.get(k), v));
            }

        });

        return predicates.toArray(new Predicate[] {});
    }

    private Long total(Predicate... predicates) {
        CriteriaBuilder     cb   = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq   = cb.createQuery(Long.class);
        Root<T>             root = cq.from(entityClass);

        cq.where(predicates);

        cq.select(cb.countDistinct(root));
        return entityManager.createQuery(cq).getSingleResult();
    }
}
