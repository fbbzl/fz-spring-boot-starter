package io.github.redisson.config;


import io.github.redisson.RObjectInjectPostProcessor;
import io.github.redisson.RedissonPoolProperties;
import io.github.redisson.limiter.aspect.RedissonRateLimitAspect;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.ConstantDelay;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.lang.NonNull;

import java.time.Duration;

import static cn.hutool.core.convert.Convert.toInt;
import static cn.hutool.core.text.CharSequenceUtil.format;
import static cn.hutool.core.text.CharSequenceUtil.isNotBlank;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/4/22 10:20
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(RedissonPoolProperties.class)
@ConditionalOnBean({RedisProperties.class,
                    RedissonPoolProperties.class})
public class RedissonConfiguration
{

    @Primary
    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(RedissonClient.class)
    public RedissonClient redissonClient(@NonNull RedisProperties redisProperties, @NonNull RedissonPoolProperties poolProperties)
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

    @Bean
    @ConditionalOnWebApplication
    @ConditionalOnBean(RedissonClient.class)
    public RedissonRateLimitAspect redissonRateLimitAspect(RedissonClient redissonClient)
    {
        return new RedissonRateLimitAspect(redissonClient);
    }
    @Bean
    public RObjectInjectPostProcessor rObjectInjectPostProcessor(RedissonClient redissonClient)
    {
        return new RObjectInjectPostProcessor(redissonClient);
    }

    private String buildAddress(RedisProperties redisProperties)
    {
        return format("redis://{}:{}", redisProperties.getHost(), redisProperties.getPort());
    }
}
