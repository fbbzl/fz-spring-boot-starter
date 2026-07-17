package io.github.fbbzl.starter.jpa.repository;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.db.Page;
import cn.hutool.db.PageResult;
import cn.hutool.db.sql.Direction;
import cn.hutool.db.sql.Order;
import io.github.fbbzl.starter.dal.Range;
import io.github.fbbzl.starter.dal.annotation.ReadOnly;
import io.github.fbbzl.starter.jpa.BaseJpaEntity;
import io.github.fbbzl.starter.jpa.Specifications;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.criteria.*;
import lombok.AccessLevel;
import lombok.experimental.Delegate;
import lombok.experimental.FieldDefaults;
import org.fz.erwin.exception.Throws;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static cn.hutool.core.collection.CollUtil.isEmpty;
import static cn.hutool.core.collection.CollUtil.isNotEmpty;
import static java.util.Collections.emptyList;

/**
 *
 * @author fbb
 * @version 1.0
 * @since 2020/1/3/001 13:30
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BaseRepositoryImpl<ENTITY extends BaseJpaEntity<ID>, ID extends Serializable>
        implements BaseRepository<ENTITY, ID>, JpaRepositoryImplementation<ENTITY, ID>
{

    Class<ENTITY> entityClass;
    EntityManager entityManager;
    @Delegate
    SimpleJpaRepository<ENTITY, ID> delegateRepository;

    @Autowired(required = false)
    public BaseRepositoryImpl(Class<ENTITY> entityClass, EntityManager entityManager)
    {
        this.entityManager      = entityManager;
        this.entityClass        = entityClass;
        this.delegateRepository = new SimpleJpaRepository<>(entityClass, entityManager);
    }

    @Transactional
    @Override
    public ENTITY create(@Nullable ENTITY entity)
    {
        if (entity == null) return null;
        return delegateRepository.saveAndFlush(entity);
    }

    @Transactional
    @Override
    public List<ENTITY> create(@Nullable Iterable<ENTITY> entities)
    {
        if (isEmpty(entities)) return emptyList();
        return delegateRepository.saveAllAndFlush(entities);
    }

    @Transactional
    @Override
    public void delete(@Nullable ID id)
    {
        if (id == null) return;
        deleteByIds(List.of(id));
    }

    @Transactional
    @Override
    public void delete(@Nullable Iterable<ID> ids)
    {
        deleteByIds(toList(ids));
    }

    @Override
    public <S extends ENTITY> S save(@NonNull S entity)
    {
        return delegateRepository.save(entity);
    }

    @Transactional
    @Override
    public void deleteById(@NonNull ID id)
    {
        delete(id);
    }

    @Transactional
    @Override
    @SuppressWarnings("unchecked")
    public void deleteAllById(@NonNull Iterable<? extends ID> ids)
    {
        delete((Iterable<ID>) ids);
    }

    @Transactional
    @Override
    public void delete(@Nullable ENTITY entity)
    {
        if (entity == null) return;
        deleteByIds(List.of(entity.getId()));
    }

    @Transactional
    @Override
    public void deleteAll(@NonNull Iterable<? extends ENTITY> entities)
    {
        deleteByIds(extractIds(entities));
    }

    @Override
    public <S extends ENTITY> S saveAndFlush(@NonNull S entity)
    {
        return delegateRepository.saveAndFlush(entity);
    }

    @Transactional
    @Override
    public void deleteInBatch(@NonNull Iterable<ENTITY> entities)
    {
        deleteByIds(extractIds(entities));
    }

    @Transactional
    @Override
    public void deleteAllInBatch(@NonNull Iterable<ENTITY> entities)
    {
        deleteByIds(extractIds(entities));
    }

    @Transactional
    @Override
    public void deleteAllByIdInBatch(@NonNull Iterable<ID> ids)
    {
        deleteByIds(toList(ids));
    }

    @Transactional
    @Override
    public void deleteAll()
    {
        CriteriaBuilder        cb     = entityManager.getCriteriaBuilder();
        CriteriaUpdate<ENTITY> update = cb.createCriteriaUpdate(entityClass);
        Root<ENTITY>           root   = update.from(entityClass);

        update.set(root.get(BaseJpaEntity.Fields.deletedAt), LocalDateTime.now());
        update.where(cb.isNull(root.get(BaseJpaEntity.Fields.deletedAt)));

        entityManager.createQuery(update).executeUpdate();
    }

    @Transactional
    @Override
    public void deleteAllInBatch()
    {
        deleteAll();
    }

    private void deleteByIds(Collection<ID> ids)
    {
        if (isEmpty(ids)) return;

        CriteriaBuilder        cb     = entityManager.getCriteriaBuilder();
        CriteriaUpdate<ENTITY> update = cb.createCriteriaUpdate(entityClass);
        Root<ENTITY>           root   = update.from(entityClass);

        update.set(root.get(BaseJpaEntity.Fields.deletedAt), LocalDateTime.now());
        update.where(
                root.get(BaseJpaEntity.Fields.id).in(ids),
                cb.isNull(root.get(BaseJpaEntity.Fields.deletedAt))
        );

        entityManager.createQuery(update).executeUpdate();
    }

    private List<ID> toList(Iterable<ID> ids)
    {
        return ids == null ? emptyList() : StreamSupport.stream(ids.spliterator(), false).toList();
    }

    private List<ID> extractIds(Iterable<? extends ENTITY> entities)
    {
        if (entities == null) return emptyList();
        return StreamSupport.stream(entities.spliterator(), false)
                            .filter(Objects::nonNull)
                            .map(BaseJpaEntity::getId)
                            .toList();
    }

    @Transactional
    @Override
    public int update(ENTITY entity)
    {
        Throws.ifNull(entity, "entity can not be null when doing update");
        Throws.ifNull(entity.getId(), "id can not be null when doing update");

        return delegateRepository.findById(entity.getId())
                                 .map(byId -> {
                                     BeanUtil.copyProperties(entity, byId, CopyOptions.create().setIgnoreNullValue(true).setIgnoreError(false));
                                     delegateRepository.saveAndFlush(byId);
                                     return 1;
                                 })
                                 .orElse(0);
    }

    @Transactional
    @Override
    public int update(Iterable<ENTITY> entities)
    {
        if (entities == null) return 0;

        List<ENTITY> entityList = StreamSupport.stream(entities.spliterator(), false).toList();
        if (isEmpty(entityList)) return 0;

        Map<ID, ENTITY> updateEntityMap = new LinkedHashMap<>();
        for (ENTITY entity : entityList) {
            Throws.ifNull(entity, "entity can not be null when doing update");
            Throws.ifNull(entity.getId(), "id can not be null when doing update");
            updateEntityMap.put(entity.getId(), entity);
        }

        List<ENTITY> existingEntities = delegateRepository.findAllById(updateEntityMap.keySet());
        if (isEmpty(existingEntities)) return 0;

        CopyOptions copyOptions = CopyOptions.create().setIgnoreNullValue(true).setIgnoreError(false);
        for (ENTITY existingEntity : existingEntities) {
            BeanUtil.copyProperties(updateEntityMap.get(existingEntity.getId()), existingEntity, copyOptions);
        }
        delegateRepository.saveAllAndFlush(existingEntities);
        return existingEntities.size();
    }

    @Nullable
    @Override
    public ENTITY byId(@Nullable ID id)
    {
        if (id != null) return delegateRepository.findById(id).orElse(null);
        else return null;
    }

    @Override
    public List<ENTITY> byIds(@Nullable Collection<ID> ids)
    {
        if (isEmpty(ids)) return emptyList();
        else return delegateRepository.findAllById(ids);
    }

    @Override
    public List<ENTITY> list(@Nullable ENTITY entity, Integer limit, @Nullable Order[] orders, @Nullable Range[] ranges)
    {
        Throws.ifNull(limit, "limit can not be null when doing list-query");
        if (entity == null) return emptyList();

        PageRequest pageRequest = PageRequest.of(0, limit, toSort(orders));
        return findAll(Specifications.byAuto(entityManager, entity, ranges), pageRequest).getContent();
    }

    @ReadOnly
    @Override
    @SuppressWarnings({"unchecked"})
    public List<ID> ids(@Nullable ENTITY entity, Integer limit)
    {
        Throws.ifNull(limit, "limit can not be null when doing ids-query");
        if (entity == null) return emptyList();

        CriteriaBuilder       cb     = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object> cq     = cb.createQuery();
        Root<ENTITY>          root   = cq.from(entityClass);
        Path<ID>              idPath = root.get(BaseJpaEntity.Fields.id);
        cq.select(idPath);

        Predicate predicate = Specifications.byAuto(entityManager, entity).toPredicate(root, cq, cb);
        Predicate deletedAt = cb.isNull(root.get(BaseJpaEntity.Fields.deletedAt));
        if (predicate != null) {
            cq.where(predicate, deletedAt);
        }
        else {
            cq.where(deletedAt);
        }

        return entityManager.createQuery(cq).setMaxResults(limit).getResultList().stream().map(id -> (ID) id).toList();
    }

    @Override
    public PageResult<ENTITY> page(Page page, @Nullable ENTITY entity)
    {
        Throws.ifNull(page, "page can not be null when doing page-query");

        PageRequest pageRequest = PageRequest.of(page.getPageNumber(), page.getPageSize(), toSort(page.getOrders()));

        org.springframework.data.domain.Page<ENTITY> pageImpl = entity == null
                                                                  ? findAll(pageRequest)
                                                                  : findAll(Specifications.byAuto(entityManager, entity), pageRequest);

        return this.toPageResult(pageImpl.getNumber(), pageImpl.getSize(), pageImpl.getTotalElements(), pageImpl.getContent());
    }

    @Override
    public long count(@Nullable ENTITY entity)
    {
        if (entity == null) return 0L;
        return delegateRepository.count(Specifications.byAuto(entityManager, entity, false));
    }

    @Override
    public boolean exists(@Nullable ID id)
    {
        if (id == null) return false;

        return delegateRepository.existsById(id);
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
        cq.where(predicate, cb.isNull(root.get(BaseJpaEntity.Fields.deletedAt)));

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
        Predicate deletedAt = cb.isNull(root.get(BaseJpaEntity.Fields.deletedAt));
        if (predicate != null) {
            cq.where(predicate, deletedAt);
        }
        else {
            cq.where(deletedAt);
        }

        entityManager.createQuery(cq)
                     .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                     .getResultList();
    }

    @Transactional
    @Override
    public void increment(String fieldName, int delta, @Nullable List<ID> ids)
    {
        if (ArrayUtil.isEmpty(ids)) return;

        CriteriaBuilder        cb     = entityManager.getCriteriaBuilder();
        CriteriaUpdate<ENTITY> update = cb.createCriteriaUpdate(entityClass);
        Root<ENTITY>           root   = update.from(entityClass);

        Path<Number> fieldPath = root.get(fieldName);

        Expression<Number> incrementExpression = cb.sum(fieldPath, cb.literal(delta));

        update.set(fieldPath, incrementExpression);

        update.where(root.get(BaseJpaEntity.Fields.id).in(ids), cb.isNull(root.get(BaseJpaEntity.Fields.deletedAt)));

        entityManager.createQuery(update).executeUpdate();
    }

    @Transactional
    @Override
    public void decrement(String fieldName, int delta, @Nullable List<ID> ids)
    {
        if (ArrayUtil.isEmpty(ids)) return;

        CriteriaBuilder        cb     = entityManager.getCriteriaBuilder();
        CriteriaUpdate<ENTITY> update = cb.createCriteriaUpdate(entityClass);
        Root<ENTITY>           root   = update.from(entityClass);

        Path<Number> fieldPath = root.get(fieldName);

        Expression<Number> decrementExpression = cb.diff(fieldPath, cb.literal(delta));

        update.set(fieldPath, decrementExpression);

        update.where(root.get(BaseJpaEntity.Fields.id).in(ids), cb.isNull(root.get(BaseJpaEntity.Fields.deletedAt)));

        entityManager.createQuery(update).executeUpdate();
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

        predicates.add(cb.isNull(root.get(BaseJpaEntity.Fields.deletedAt)));

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

        return Sort.by(Stream.of(orders).map(order -> new Sort.Order(order.getDirection() == Direction.DESC
                                                                     ? Sort.Direction.DESC
                                                                     : Sort.Direction.ASC, order.getField())).toList());
    }
}
