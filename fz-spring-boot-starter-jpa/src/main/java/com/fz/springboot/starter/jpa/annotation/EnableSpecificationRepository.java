package com.fz.springboot.starter.jpa.annotation;

import com.fz.springboot.starter.jpa.RepositoryFactoryBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.core.annotation.AliasFor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author fbb
 * @version 1.0
 * @since 2020/1/6/006 17:22
 */
@Target(TYPE)
@Retention(RUNTIME)
@EntityScan
@EnableJpaRepositories(repositoryFactoryBeanClass = RepositoryFactoryBean.class)
public @interface EnableSpecificationRepository {

    @AliasFor(annotation = EntityScan.class, attribute = "basePackages")
    String[] entityPackages() default "";

    @AliasFor(annotation = EnableJpaRepositories.class, attribute = "basePackages")
    String[] repositoryPackages() default "";

}
