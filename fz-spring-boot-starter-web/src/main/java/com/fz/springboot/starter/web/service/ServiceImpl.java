package com.fz.springboot.starter.web.service;


import com.fz.springboot.starter.core.exception.BizExceptionVerb;
import com.fz.springboot.starter.jpa.BaseEntity;
import com.fz.springboot.starter.jpa.repository.BaseRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.fz.erwin.exception.Throws;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @param <T> request data type
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/22 15:00
 */

@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class ServiceImpl<R extends BaseRepository<T>, T extends BaseEntity> implements IService<T> {

    @Autowired R repo;

    @Override
    public List<T> find(T entity) {
        return repo.find(entity);
    }

    @Override
    public List<T> find(Map<String, Object> params) {
        return repo.find(params);
    }

    @Override
    public Page<T> find(T entity, Pageable pageable) {
        return repo.find(entity, pageable);
    }

    @Override
    public Page<T> find(Map<String, Object> params, Pageable pageable) {
        return repo.find(params, pageable);
    }

    @Override
    public T create(T entity) {
        return repo.saveAndFlush(entity);
    }

    @Override
    public T update(T entity) {
        Throws.ifNull(entity.getId(), BizExceptionVerb.INVALID_INPUT.on(entity.getClass()));
        return repo.saveAndFlush(entity);
    }

    @Transactional
    @Override
    public List<T> updateAll(List<T> entities) {
        return repo.saveAllAndFlush(entities);
    }

    @Override
    public Optional<T> findById(Long id) {
        return repo.findById(id);
    }

    @Override
    public List<T> findByIds(List<Long> ids) {
        return repo.findAllById(ids);
    }

    @Override
    public boolean exists(Long id) {
        return repo.existsById(id);
    }

    @Override
    public boolean exists(T entity) {
        return repo.exists(Example.of(entity, ExampleMatcher.matching().withIgnoreNullValues()));
    }

    @Override
    public boolean exists(Map<String, Object> params) {
        return repo.exists(params);
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        repo.logicalDelete(id);
    }

    @Transactional
    @Override
    public void deleteByIds(Collection<Long> ids) {
        repo.logicalDeleteAll(ids);
    }

}
