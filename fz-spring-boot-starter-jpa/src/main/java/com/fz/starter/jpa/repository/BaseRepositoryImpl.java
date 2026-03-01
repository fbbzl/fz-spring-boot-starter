package com.fz.starter.jpa.repository;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.db.Page;
import cn.hutool.db.PageResult;
import cn.hutool.db.sql.Condition.LikeType;
import cn.hutool.db.sql.Direction;
import cn.hutool.db.sql.Order;
import com.fz.starter.jpa.BaseJpaEntity;
import com.fz.starter.jpa.Specifications;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static cn.hutool.core.collection.CollUtil.isEmpty;
import static cn.hutool.db.sql.SqlUtil.buildLikeValue;
import static java.util.Collections.emptyList;

/**
 *
 * @author fbb
 * @version 1.0
 * @since 2020/1/3/001 13:30
 */
public class BaseRepositoryImpl<ENTITY extends BaseJpaEntity> extends SimpleJpaRepository<ENTITY, Long> implements BaseRepository<ENTITY> {

    private final Class<ENTITY> entityClass;
    private final EntityManager entityManager;

    @Autowired(required = false)
    public BaseRepositoryImpl(Class<ENTITY> entityClass, EntityManager entityManager) {
        super(entityClass, entityManager);
        this.entityManager = entityManager;
        this.entityClass   = entityClass;
    }

    @Transactional
    @Override
    public ENTITY create(ENTITY entity) {
        return this.saveAndFlush(entity);
    }

    @Transactional
    @Override
    public int create(Iterable<ENTITY> entities) {
        return this.saveAllAndFlush(entities).size();
    }

    @Transactional
    @Override
    public void delete(Long id) {
        this.deleteById(id);
    }

    @Transactional
    @Override
    public void delete(Set<Long> ids) {
        this.deleteAllById(ids);
    }

    @Transactional
    @Override
    public int update(ENTITY entity) {
        return this.findById(entity.getId())
                   .map(byId -> {
                       BeanUtil.copyProperties(entity, byId, CopyOptions.create().setIgnoreNullValue(true).setIgnoreError(true));
                       this.saveAndFlush(byId);
                       return 1;
                   }).orElse(0);
    }

    @Transactional
    @Override
    public int update(Iterable<ENTITY> entities) {
        return this.saveAllAndFlush(entities).size();
    }

    @Override
    public Optional<ENTITY> byId(Long id) {
        return this.findById(id);
    }

    @Override
    public List<ENTITY> byIds(Set<Long> ids) {
        return this.findAllById(ids);
    }

    @Override
    public Optional<ENTITY> one(ENTITY entity) {
        return super.findOne(Specifications.byAuto(entityManager, entity));
    }

    @Override
    public Optional<ENTITY> one(Map<String, Object> propertyAndValue) {
        if (isEmpty(propertyAndValue)) return Optional.empty();

        CriteriaBuilder       cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ENTITY> cq = cb.createQuery(entityClass);

        cq.where(this.mapToPredicates(propertyAndValue, cb, cq.from(entityClass)));

        return Optional.ofNullable(entityManager.createQuery(cq).getSingleResult());
    }

    @Override
    public List<ENTITY> list(ENTITY entity) {
        return findAll(Specifications.byAuto(entityManager, entity));
    }

    @Override
    public PageResult<ENTITY> page(ENTITY entity, Page page) {
        PageRequest pageRequest = PageRequest.of(page.getPageNumber(), page.getPageSize(), toSort(page.getOrders()));

        org.springframework.data.domain.Page<ENTITY> pageImpl = findAll(Specifications.byAuto(entityManager, entity), pageRequest);

        return this.toPageResult(pageImpl.getNumber(), pageImpl.getSize(), pageImpl.getTotalElements(), pageImpl.getContent());
    }

    @Override
    public List<ENTITY> list(Map<String, Object> propertyAndValue) {
        if (isEmpty(propertyAndValue)) return emptyList();

        CriteriaBuilder       cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ENTITY> cq = cb.createQuery(entityClass);

        cq.where(this.mapToPredicates(propertyAndValue, cb, cq.from(entityClass)));

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public PageResult<ENTITY> page(Map<String, Object> propertyAndValue, Page page) {
        CriteriaBuilder       cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ENTITY> cq = cb.createQuery(entityClass);

        Predicate[] predicates = this.mapToPredicates(propertyAndValue, cb, cq.from(entityClass));

        cq.where(predicates);

        TypedQuery<ENTITY> pageQuery =
                entityManager.createQuery(cq)
                             .setFirstResult(page.getPageNumber() * page.getPageSize())
                             .setMaxResults(page.getPageSize());

        PageRequest      pageRequest = PageRequest.of(page.getPageNumber(), page.getPageSize(), this.toSort(page.getOrders()));
        PageImpl<ENTITY> pageImpl    = new PageImpl<>(pageQuery.getResultList(), pageRequest, this.total(predicates));
        return this.toPageResult(pageImpl.getNumber(), pageImpl.getSize(), pageImpl.getTotalElements(), pageImpl.getContent());
    }

    @Override
    public boolean exists(ENTITY entity) {
        return this.exists(Example.of(entity, ExampleMatcher.matching().withIgnoreNullValues()));
    }

    @Override
    public boolean exists(Map<String, Object> propertyAndValue) {
        if (isEmpty(propertyAndValue)) return false;

        CriteriaBuilder     cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);

        Root<ENTITY> root = cq.from(entityClass);

        cq.where(this.mapToPredicates(propertyAndValue, cb, root)).select(cb.count(root));

        return entityManager.createQuery(cq).getSingleResult() > 0;
    }

    @Override
    public boolean exists(Long id) {
        return super.existsById(id);
    }

    @Transactional
    @Override
    public void selectForUpdate(Long... ids) {
        if (ArrayUtil.isEmpty(ids)) return;

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ENTITY> cq = cb.createQuery(entityClass);
        Root<ENTITY> root = cq.from(entityClass);

        Predicate predicate = root.get(BaseJpaEntity.Fields.id).in((Object[]) ids);
        cq.where(predicate);

        entityManager.createQuery(cq).setLockMode(LockModeType.PESSIMISTIC_WRITE).getResultList();
    }

    @Transactional
    @Override
    public void selectForUpdate(ENTITY entity) {
        if (entity == null) return;

        Example<ENTITY> example = Example.of(entity, ExampleMatcher.matching().withIgnoreNullValues());

        this.findAll(example, Sort.unsorted())
            .forEach(e -> entityManager.lock(e, LockModeType.PESSIMISTIC_WRITE));
    }

    @Override
    public void increment(String fieldName, int delta, Long... ids) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<ENTITY> update = cb.createCriteriaUpdate(entityClass);
        Root<ENTITY> root = update.from(entityClass);

        Path<Number> fieldPath = root.get(fieldName);

        Expression<Number> incrementExpression = cb.sum(fieldPath, cb.literal(delta));

        update.set(fieldPath, incrementExpression);

        update.where(root.get(BaseJpaEntity.Fields.id).in((Object[]) ids));

        entityManager.createQuery(update).executeUpdate();
    }

    @Override
    public void decrement(String fieldName, int delta, Long... ids) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<ENTITY> update = cb.createCriteriaUpdate(entityClass);
        Root<ENTITY> root = update.from(entityClass);

        Path<Number> fieldPath = root.get(fieldName);

        Expression<Number> decrementExpression = cb.diff(fieldPath, cb.literal(delta));

        update.set(fieldPath, decrementExpression);

        update.where(root.get(BaseJpaEntity.Fields.id).in((Object[]) ids));

        entityManager.createQuery(update).executeUpdate();
    }

    @Override
    public void doBatchConsume(ENTITY entity, int batchSize, Consumer<List<ENTITY>> recordsConsumer) {
        int pageNumber = 0;
        PageRequest pageRequest = PageRequest.of(pageNumber, batchSize);
        org.springframework.data.domain.Page<ENTITY> pageResult;

        do {
            pageResult = this.findAll(Example.of(entity, ExampleMatcher.matching().withIgnoreNullValues()), pageRequest);

            recordsConsumer.accept(pageResult.getContent());

            pageNumber++;
            pageRequest = PageRequest.of(pageNumber, batchSize);
        } while (pageResult.hasNext());
    }

    //************************************************ protected start ***********************************************//

    protected Sort toSort(Order... orders) {
        if (ArrayUtil.isEmpty(orders)) return Sort.unsorted();

        return Sort.by(Stream.of(orders)
                             .map(order ->
                                          new Sort.Order(order.getDirection() == Direction.ASC
                                                         ? Sort.Direction.ASC
                                                         : Sort.Direction.DESC, order.getField())).toList());
    }

    protected Predicate[] mapToPredicates(Map<String, Object> map, CriteriaBuilder cb, Root<ENTITY> root) {
        List<Predicate> predicates = new ArrayList<>();
        map.forEach((k, v) -> {
            boolean notNull    = v != null;
            boolean stringType = notNull && v.getClass() == String.class;
            boolean enumType   = notNull && v.getClass().isEnum();
            boolean otherType  = notNull && !stringType && !enumType;

            if (stringType) predicates.add(cb.like(root.get(k), buildLikeValue(v.toString(), LikeType.Contains, false)));
            else
            if (enumType)   predicates.add(cb.equal(root.get(k), ((Enum<?>) v).ordinal()));
            else
            if (otherType)  predicates.add(cb.equal(root.get(k), v));
        });

        return predicates.toArray(new Predicate[]{});
    }

    protected Long total(Predicate... predicates) {
        CriteriaBuilder     cb   = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq   = cb.createQuery(Long.class);
        Root<ENTITY>        root = cq.from(entityClass);

        cq.where(predicates);

        cq.select(cb.countDistinct(root));
        return entityManager.createQuery(cq).getSingleResult();
    }
}
