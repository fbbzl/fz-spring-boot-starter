package com.fz.springboot.starter.web.service;

import com.fz.springboot.starter.jpa.BaseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
public interface IService<T extends BaseEntity> {

    List<T> find(T entity);

    List<T> find(Map<String, Object> params);

    Page<T> find(T entity, Pageable pageable);

    Page<T> find(Map<String, Object> params, Pageable pageable);

    Optional<T> findById(Long id);

    List<T> findByIds(List<Long> ids);

    boolean exists(Long id);

    boolean exists(T entity);

    boolean exists(Map<String, Object> params);

    T create(T entity);

    T update(T entity);

    List<T> updateAll(List<T> entities);

    void deleteById(Long id);

    void deleteByIds(Collection<Long> ids);

}
