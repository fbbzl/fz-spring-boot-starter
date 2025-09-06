package com.fz.springboot.starter.jpa.repository;

import com.fz.springboot.starter.jpa.BaseEntity;
import com.fz.springboot.starter.jpa.Specifications;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 *
 * @author fbb
 * @version 1.0
 * @since 2020/1/1/001 13:10
 */
@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity> extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {

    /**
     * @see Specifications#byAuto(EntityManager, BaseEntity)
     */
    List<T> find(T entity);

    /**
     * @see Specifications#byAuto(EntityManager, BaseEntity)
     */
    Page<T> find(T entity, Pageable pageable);

    List<T> find(String property, Object value);

    List<T> find(Map<String, Object> valueMap);

    Page<T> find(Map<String, Object> valueMap, Pageable pageable);

    boolean exists(Map<String, Object> valueMap);

    /**
     * Logical deletion is equivalent to physical deletion, only to maximize the value of the data!!
     *
     * @param id primary key
     */
    void logicalDelete(Long id);

    void logicalDeleteAll(Collection<Long> id);
}
