package com.fz.starter.jpa;


import com.fz.starter.jpa.repository.BaseRepositoryImpl;
import com.fz.starter.pojo.entity.BaseTableEntity;
import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import java.io.Serializable;

/**
 * @author fbb
 * @version 1.0
 * @since 2020/1/1/001 13:40
 */
@SuppressWarnings("all")
public class RepositoryFactoryBean<T extends JpaRepository<S, ID>, S, ID extends Serializable> extends JpaRepositoryFactoryBean<T, S, ID> {

    public RepositoryFactoryBean(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    @Override
    protected RepositoryFactorySupport createRepositoryFactory(EntityManager entityManager) {
        return new BaseRepositoryFactory(entityManager);
    }

    public static class BaseRepositoryFactory<T extends BaseTableEntity, ID extends Serializable>
            extends JpaRepositoryFactory {

        BaseRepositoryFactory(EntityManager entityManager) {
            super(entityManager);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected SimpleJpaRepository<T, ID> getTargetRepository(RepositoryInformation information,
                                                                 EntityManager entityManager)
        {
            return new BaseRepositoryImpl(information.getDomainType(), entityManager);
        }

        @Override
        protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
            return BaseRepositoryImpl.class;
        }
    }

}
