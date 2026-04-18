package com.fz.starter.jpa.repository;


import com.fz.starter.dal.BaseDal;
import com.fz.starter.pojo.entity.BaseTableEntity;
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
public interface BaseRepository<ENTITY extends BaseTableEntity<ID>, ID extends Serializable>
        extends JpaRepository<ENTITY, ID>,
                JpaSpecificationExecutor<ENTITY>,
                BaseDal<ENTITY, ID> {}
