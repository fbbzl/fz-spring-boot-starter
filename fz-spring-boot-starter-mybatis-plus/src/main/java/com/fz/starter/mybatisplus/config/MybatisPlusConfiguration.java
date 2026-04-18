package com.fz.starter.mybatisplus.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.toolkit.JdbcUtils;
import com.fz.starter.mybatisplus.frame.BaseMetaObjectHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/2/14 14:10
 */

@AutoConfiguration
public class MybatisPlusConfiguration
{

    @Bean
    @ConditionalOnMissingBean
    public BaseMetaObjectHandler<Long> defaultNoLogin()
    {
        return new BaseMetaObjectHandler<>()
        {
            @Override
            public Long getCurrentUserId()
            {
                return 0L;
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor(DataSource dataSource) throws SQLException
    {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        try (Connection connection = dataSource.getConnection()) {
            DbType dbType = JdbcUtils.getDbType(connection.getMetaData().getURL());

            PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(dbType);
            paginationInnerInterceptor.setOverflow(true);
            paginationInnerInterceptor.setMaxLimit(1000L);

            interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
            interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
            interceptor.addInnerInterceptor(paginationInnerInterceptor);
        }

        return interceptor;
    }
}
