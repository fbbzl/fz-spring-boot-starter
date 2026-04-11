package com.fz.starter.jpa.repository;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.db.Page;
import cn.hutool.db.PageResult;
import cn.hutool.db.sql.Direction;
import cn.hutool.db.sql.Order;
import com.fz.starter.jpa.BaseJpaEntity;
import com.fz.starter.jpa.Specifications;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 *
 * @author fbb
 * @version 1.0
 * @since 2020/1/3/001 13:30
 */
public class BaseRepositoryImpl<ENTITY extends BaseJpaEntity<ID>, ID extends Serializable>
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
    public ENTITY create(ENTITY entity)
    {
        return super.saveAndFlush(entity);
    }

    @Transactional
    @Override
    public int create(Iterable<ENTITY> entities)
    {
        return super.saveAllAndFlush(entities).size();
    }

    @Transactional
    @Override
    public void delete(ID id)
    {
        super.deleteById(id);
    }

    @Transactional
    @Override
    public void delete(Set<ID> ids)
    {
        super.deleteAllById(ids);
    }

    @Transactional
    @Override
    public int update(ENTITY entity)
    {
        return this.findById(entity.getId())
                   .map(byId -> {
                       BeanUtil.copyProperties(entity, byId, CopyOptions.create().setIgnoreNullValue(true).setIgnoreError(true));
                       this.saveAndFlush(byId);
                       return 1;
                   }).orElse(0);
    }

    @Transactional
    @Override
    public int update(Iterable<ENTITY> entities)
    {
        return super.saveAllAndFlush(entities).size();
    }

    @Nullable
    @Override
    public ENTITY byId(ID id)
    {
        return this.findById(id).get();
    }

    @Override
    public List<ENTITY> byIds(Set<ID> ids)
    {
        return this.findAllById(ids);
    }

    @Override
    public Optional<ENTITY> one(ENTITY entity)
    {
        return super.findOne(Specifications.byAuto(entityManager, entity));
    }

    @Override
    public List<ENTITY> list(ENTITY entity)
    {
        return findAll(Specifications.byAuto(entityManager, entity));
    }

    @Override
    public List<ENTITY> limit(ENTITY entity, int limit)
    {
        PageRequest pageRequest = PageRequest.of(0, limit);
        return findAll(Specifications.byAuto(entityManager, entity), pageRequest).getContent();
    }

    @Override
    public PageResult<ENTITY> page(Page page, ENTITY entity)
    {
        PageRequest pageRequest = PageRequest.of(page.getPageNumber(), page.getPageSize(), toSort(page.getOrders()));

        org.springframework.data.domain.Page<ENTITY> pageImpl = findAll(Specifications.byAuto(entityManager, entity), pageRequest);

        return this.toPageResult(pageImpl.getNumber(), pageImpl.getSize(), pageImpl.getTotalElements(), pageImpl.getContent());
    }

    @Override
    public boolean exists(ENTITY entity)
    {
        return this.exists(Example.of(entity, ExampleMatcher.matching().withIgnoreNullValues()));
    }

    @Override
    public boolean exists(ID id)
    {
        return super.existsById(id);
    }

    @Transactional
    @Override
    public void selectForUpdate(List<ID> ids)
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
    public void selectForUpdate(ENTITY entity)
    {
        if (entity == null) return;

        Example<ENTITY> example = Example.of(entity, ExampleMatcher.matching().withIgnoreNullValues());

        this.findAll(example, Sort.unsorted())
            .forEach(e -> entityManager.lock(e, LockModeType.PESSIMISTIC_WRITE));
    }

    @Override
    public void increment(String fieldName, int delta, List<ID> ids)
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

    @Override
    public void decrement(String fieldName, int delta, List<ID> ids)
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
    public void doBatchConsume(ENTITY entity, int batchSize, Consumer<List<ENTITY>> recordsConsumer)
    {
        int                                          pageNumber  = 0;
        PageRequest                                  pageRequest = PageRequest.of(pageNumber, batchSize);
        org.springframework.data.domain.Page<ENTITY> pageResult;

        do {
            pageResult = this.findAll(Example.of(entity, ExampleMatcher.matching().withIgnoreNullValues()), pageRequest);

            recordsConsumer.accept(pageResult.getContent());

            pageNumber++;
            pageRequest = PageRequest.of(pageNumber, batchSize);
        } while (pageResult.hasNext());
    }

    //************************************************ protected start ***********************************************//

    protected Sort toSort(Order... orders)
    {
        if (ArrayUtil.isEmpty(orders)) return Sort.unsorted();

        return Sort.by(Stream.of(orders)
                             .map(order ->
                                          new Sort.Order(order.getDirection() == Direction.ASC
                                                         ? Sort.Direction.ASC
                                                         : Sort.Direction.DESC, order.getField())).toList());
    }
}
