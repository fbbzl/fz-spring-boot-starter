package com.fz.starter.jpa.repository;


import com.fz.starter.dal.BaseDal;
import com.fz.starter.jpa.BaseJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

/**
 *
 * @author fbb
 * @version 1.0
 * @since 2020/1/1/001 13:10
 */
@NoRepositoryBean
public interface BaseRepository<ENTITY extends BaseJpaEntity>
        extends JpaRepository<ENTITY, Long>,
                JpaSpecificationExecutor<ENTITY>,
                BaseDal<ENTITY> {}
