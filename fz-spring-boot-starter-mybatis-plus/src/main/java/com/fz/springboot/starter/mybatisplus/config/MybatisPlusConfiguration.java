package com.fz.springboot.starter.mybatisplus.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.toolkit.JdbcUtils;
import com.fz.springboot.starter.mybatisplus.frame.BaseMetaObjectHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/2/14 14:10
 */

@AutoConfiguration
public class MybatisPlusConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public BaseMetaObjectHandler defaultNoLogin() {
        return new BaseMetaObjectHandler() {
            @Override
            protected Long getCurrentLoginUserId() {
                return 0L;
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor(DataSource dataSource) throws Exception {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        DbType dbType = JdbcUtils.getDbType(dataSource.getConnection().getMetaData().getURL());

        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(dbType);
        paginationInnerInterceptor.setOverflow(true);
        paginationInnerInterceptor.setMaxLimit(1000L);

        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        return interceptor;
    }
}
