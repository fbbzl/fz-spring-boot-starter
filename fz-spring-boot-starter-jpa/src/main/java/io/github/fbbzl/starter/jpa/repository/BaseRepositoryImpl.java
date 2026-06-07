package io.github.fbbzl.starter.jpa.repository;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.db.Page;
import cn.hutool.db.PageResult;
import cn.hutool.db.sql.Direction;
import cn.hutool.db.sql.Order;
import io.github.fbbzl.starter.core.util.Throws;
import io.github.fbbzl.starter.dal.Range;
import io.github.fbbzl.starter.jpa.BaseJpaEntity;
import io.github.fbbzl.starter.jpa.Specifications;
import io.github.fbbzl.starter.pojo.entity.BaseTableEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static cn.hutool.core.collection.CollUtil.isEmpty;
import static cn.hutool.core.collection.CollUtil.isNotEmpty;
import static java.util.Collections.emptyList;

/**
 *
 * @author fbb
 * @version 1.0
 * @since 2020/1/3/001 13:30
 */
public class BaseRepositoryImpl<ENTITY extends BaseTableEntity<ID>, ID extends Serializable>
        extends SimpleJpaRepository<ENTITY, ID>
        implements BaseRepository<ENTITY, ID>
{

    private final Class<ENTITY> entityClass;
    private final EntityManager entityManager;

    @Autowired(required = false)
    public BaseRepositoryImpl(Class<ENTITY> entityClass, EntityManager entityManager)
    {
        super(entityClass, entityManager);
        this.entityManager = entityManager;
        this.entityClass   = entityClass;
    }

    @Transactional
    @Override
    public ENTITY create(@Nullable ENTITY entity)
    {
        return super.saveAndFlush(entity);
    }

    @Transactional
    @Override
    public List<ENTITY> create(@Nullable Iterable<ENTITY> entities)
    {
        if (isEmpty(entities)) return emptyList();
        return super.saveAllAndFlush(entities);
    }

    @Transactional
    @Override
    public void delete(@Nullable ID id)
    {
        if (id != null) super.deleteById(id);
    }

    @Transactional
    @Override
    public void delete(@Nullable Iterable<ID> ids)
    {
        if (isEmpty(ids)) return;

        super.deleteAllById(ids);
    }

    @Transactional
    @Override
    public ENTITY update(@Nullable ENTITY entity)
    {
        Throws.ifNull(entity, "entity can not be null when doing update");
        Throws.ifNull(entity.getId(), "id can not be null when doing update");

        this.findById(entity.getId()).ifPresent(byId -> {
            BeanUtil.copyProperties(entity, byId, CopyOptions.create().setIgnoreNullValue(true).setIgnoreError(true));
            this.saveAndFlush(byId);
        });

        return entity;
    }

    @Transactional
    @Override
    public void update(@Nullable Iterable<ENTITY> entities)
    {
        if (isEmpty(entities)) return;
        super.saveAllAndFlush(entities);
    }

    @Nullable
    @Override
    public ENTITY byId(@Nullable ID id)
    {
        if (id != null) return this.findById(id).orElse(null);
        else return null;
    }

    @Override
    public List<ENTITY> byIds(@Nullable Collection<ID> ids)
    {
        if (isEmpty(ids)) return emptyList();
        else              return this.findAllById(ids);
    }

    @Override
    public Optional<ENTITY> one(@Nullable ENTITY entity)
    {
        return super.findOne(Specifications.byAuto(entityManager, entity));
    }

    @Override
    public List<ENTITY> list(@Nullable ENTITY entity, @Nullable Integer limit, @Nullable Order[] orders, @Nullable Range[] ranges)
    {
        Throws.ifNull(limit, "limit can not be null when doing list-query");
        if (entity == null) return emptyList();

        PageRequest pageRequest = PageRequest.of(0, limit, toSort(orders));
        return findAll(Specifications.byAuto(entityManager, entity, ranges), pageRequest).getContent();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ID> ids(@Nullable ENTITY entity, @Nullable Integer limit)
    {
        Throws.ifNull(limit, "limit can not be null when doing ids-query");
        if (entity == null) return emptyList();

        CriteriaBuilder      cb   = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object> cq   = cb.createQuery();
        Root<ENTITY>         root = cq.from(entityClass);
        cq.select(root.get(BaseJpaEntity.Fields.id));

        Predicate predicate = Specifications.byAuto(entityManager, entity).toPredicate(root, cq, cb);
        if (predicate != null) cq.where(predicate);

        return entityManager.createQuery(cq).setMaxResults(limit).getResultList().stream().map(id -> (ID) id).toList();
    }

    @Override
    public PageResult<ENTITY> page(@Nullable Page page, @Nullable ENTITY entity)
    {
        Throws.ifNull(page, "page can not be null when doing page-query");

        PageRequest pageRequest = PageRequest.of(page.getPageNumber(), page.getPageSize(), toSort(page.getOrders()));

        org.springframework.data.domain.Page<ENTITY> pageImpl = findAll(Specifications.byAuto(entityManager, entity), pageRequest);

        return this.toPageResult(pageImpl.getNumber(), pageImpl.getSize(), pageImpl.getTotalElements(), pageImpl.getContent());
    }

    @Override
    public long count(@Nullable ENTITY entity)
    {
        if (entity == null) return 0L;
        return this.count(Specifications.byAuto(entityManager, entity, false));
    }

    @Override
    public boolean exists(@Nullable ENTITY entity)
    {
        if (entity == null) return false;

        return this.count(Specifications.byAuto(entityManager, entity, false)) > 0;
    }

    @Override
    public boolean exists(@Nullable ID id)
    {
        return super.existsById(id);
    }

    @Transactional
    @Override
    public void selectForUpdate(@Nullable List<ID> ids)
    {
        if (ArrayUtil.isEmpty(ids)) return;

        CriteriaBuilder       cb   = entityManager.getCriteriaBuilder();
        CriteriaQuery<ENTITY> cq   = cb.createQuery(entityClass);
        Root<ENTITY>          root = cq.from(entityClass);

        Predicate predicate = root.get(BaseJpaEntity.Fields.id).in(ids);
        cq.where(predicate);

        entityManager.createQuery(cq).setLockMode(LockModeType.PESSIMISTIC_WRITE).getResultList();
    }

    @Transactional
    @Override
    public void selectForUpdate(@Nullable ENTITY entity)
    {
        if (entity == null) return;

        CriteriaBuilder       cb   = entityManager.getCriteriaBuilder();
        CriteriaQuery<ENTITY> cq   = cb.createQuery(entityClass);
        Root<ENTITY>          root = cq.from(entityClass);

        Predicate predicate = Specifications.byAuto(entityManager, entity)
                .toPredicate(root, cq, cb);
        if (predicate != null) cq.where(predicate);

        entityManager.createQuery(cq)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultList();
    }

    @Transactional
    @Override
    public void increment(String fieldName, int delta, @Nullable List<ID> ids)
    {
        CriteriaBuilder        cb     = entityManager.getCriteriaBuilder();
        CriteriaUpdate<ENTITY> update = cb.createCriteriaUpdate(entityClass);
        Root<ENTITY>           root   = update.from(entityClass);

        Path<Number> fieldPath = root.get(fieldName);

        Expression<Number> incrementExpression = cb.sum(fieldPath, cb.literal(delta));

        update.set(fieldPath, incrementExpression);

        update.where(root.get(BaseJpaEntity.Fields.id).in(ids));

        entityManager.createQuery(update).executeUpdate();
    }

    @Transactional
    @Override
    public void decrement(String fieldName, int delta, @Nullable List<ID> ids)
    {
        CriteriaBuilder        cb     = entityManager.getCriteriaBuilder();
        CriteriaUpdate<ENTITY> update = cb.createCriteriaUpdate(entityClass);
        Root<ENTITY>           root   = update.from(entityClass);

        Path<Number> fieldPath = root.get(fieldName);

        Expression<Number> decrementExpression = cb.diff(fieldPath, cb.literal(delta));

        update.set(fieldPath, decrementExpression);

        update.where(root.get(BaseJpaEntity.Fields.id).in(ids));

        entityManager.createQuery(update).executeUpdate();
    }

    @Override
    public void doBatchConsume(@Nullable ENTITY entity, int batchSize, Consumer<List<ENTITY>> recordsConsumer)
    {
        int                                          pageNumber  = 0;
        PageRequest                                  pageRequest = PageRequest.of(pageNumber, batchSize);
        org.springframework.data.domain.Page<ENTITY> pageResult;

        do {
            pageResult = entity == null
                         ? this.findAll(pageRequest)
                         : this.findAll(Specifications.byAuto(entityManager, entity), pageRequest);

            recordsConsumer.accept(pageResult.getContent());

            pageNumber++;
            pageRequest = PageRequest.of(pageNumber, batchSize);
        } while (pageResult.hasNext());
    }

    @SafeVarargs
    public final <VALUE extends Comparable<? super VALUE>> CriteriaQuery<ENTITY> rangeQuery(
            ENTITY query,
            String field,
            boolean isClose,
            VALUE... values)
    {
        VALUE start = ArrayUtil.min(values);
        VALUE end   = ArrayUtil.max(values);

        CriteriaBuilder       cb   = entityManager.getCriteriaBuilder();
        CriteriaQuery<ENTITY> cq   = cb.createQuery(entityClass);
        Root<ENTITY>          root = cq.from(entityClass);

        List<Predicate> predicates = new ArrayList<>(8);

        if (query != null) {
            Predicate autoPredicate = Specifications.byAuto(entityManager, query).toPredicate(root, cq, cb);
            if (autoPredicate != null) predicates.add(autoPredicate);
        }

        Path<VALUE> fieldPath = root.get(field);

        if (start != null) {
            predicates.add(isClose ? cb.greaterThanOrEqualTo(fieldPath, start) : cb.greaterThan(fieldPath, start));
        }

        if (end != null) {
            predicates.add(isClose ? cb.lessThanOrEqualTo(fieldPath, end) : cb.lessThan(fieldPath, end));
        }

        if (isNotEmpty(predicates)) cq.where(cb.and(predicates.toArray(new Predicate[]{})));

        return cq;
    }

    public <RESULT> CriteriaQuery<RESULT> order(Root<ENTITY> root, CriteriaQuery<RESULT> criteriaQuery, CriteriaBuilder cb, Order... orders)
    {
        if (ArrayUtil.isNotEmpty(orders)) {
            criteriaQuery.orderBy(Stream.of(orders)
                                        .map(order -> order.getDirection() == Direction.ASC
                                                      ? cb.asc(root.get(order.getField()))
                                                      : cb.desc(root.get(order.getField()))).toList());
        }

        return criteriaQuery;
    }

    //************************************************ protected start ***********************************************//

    protected Sort toSort(Order... orders)
    {
        if (ArrayUtil.isEmpty(orders)) return Sort.unsorted();

        return Sort.by(Stream.of(orders).map(order -> new Sort.Order(order.getDirection() == Direction.ASC
                                                                     ? Sort.Direction.ASC
                                                                     : Sort.Direction.DESC, order.getField())).toList());
    }
}
