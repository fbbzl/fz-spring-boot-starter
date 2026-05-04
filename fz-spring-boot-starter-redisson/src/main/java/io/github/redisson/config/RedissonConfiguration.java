package io.github.redisson.config;


import io.github.redisson.RedissonPoolProperties;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.ConstantDelay;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static cn.hutool.core.convert.Convert.toInt;
import static cn.hutool.core.text.CharSequenceUtil.format;
import static cn.hutool.core.text.CharSequenceUtil.isNotBlank;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/4/22 10:20
 */
@Configuration
@ConditionalOnClass({RedisProperties.class, RedissonPoolProperties.class})
@EnableConfigurationProperties(RedissonPoolProperties.class)
public class RedissonConfiguration
{
    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(RedissonClient.class)
    public RedissonClient redissonClient(RedisProperties redisProperties, RedissonPoolProperties poolProperties)
    {
        Config config = new Config()
                .setTcpKeepAlive(poolProperties.getKeepAlive());

        if (isNotBlank(redisProperties.getUsername())) config.setUsername(redisProperties.getUsername());
        if (isNotBlank(redisProperties.getPassword())) config.setPassword(redisProperties.getPassword());

        config.useSingleServer()
              .setAddress(buildAddress(redisProperties))
              .setDatabase(redisProperties.getDatabase())
              .setConnectTimeout(toInt(redisProperties.getConnectTimeout().toMillis()))
              .setTimeout(toInt(redisProperties.getTimeout().toMillis()))
              .setConnectionMinimumIdleSize(poolProperties.getConnectionMinimumIdleSize())
              .setConnectionPoolSize(poolProperties.getConnectionPoolSize())
              .setSubscriptionConnectionMinimumIdleSize(poolProperties.getSubscriptionConnectionMinimumIdleSize())
              .setSubscriptionConnectionPoolSize(poolProperties.getSubscriptionConnectionPoolSize())
              .setIdleConnectionTimeout(poolProperties.getIdleConnectionTimeout())
              .setRetryAttempts(poolProperties.getRetryAttempts())
              .setRetryDelay(new ConstantDelay(Duration.ofMillis(poolProperties.getRetryInterval())))
              .setPingConnectionInterval(poolProperties.getPingConnectionInterval());

        return Redisson.create(config);
    }

    private String buildAddress(RedisProperties redisProperties)
    {
        return format("redis://{}:{}", redisProperties.getHost(), redisProperties.getPort());
    }
}
