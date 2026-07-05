package io.github.fbbzl.starter.jpa.repository;


import io.github.fbbzl.starter.dal.BaseDal;
import io.github.fbbzl.starter.jpa.BaseJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

/**
 *
 * @author fbb
 * @version 1.0
 * @since 2020/1/1/001 13:10
 */
@NoRepositoryBean
public interface BaseRepository<ENTITY extends BaseJpaEntity<ID>, ID extends Serializable>
        extends JpaRepository<ENTITY, ID>,
                JpaSpecificationExecutor<ENTITY>,
                BaseDal<ENTITY, ID> {}
