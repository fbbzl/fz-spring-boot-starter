package io.github.fbbzl.starter.redisson.config;


import io.github.fbbzl.starter.redisson.limiter.aspect.RedissonRateLimitAspect;
import io.github.fbbzl.starter.redisson.repeat.RedissonSubmitOnceAspect;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.ConstantDelay;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
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
@AutoConfiguration(beforeName = {
        "org.redisson.spring.starter.RedissonAutoConfigurationV2",
        "org.redisson.spring.starter.RedissonAutoConfigurationV4"
})
@ConditionalOnClass({RedisProperties.class, RedissonPoolProperties.class})
@EnableConfigurationProperties(RedissonPoolProperties.class)
public class RedissonConfiguration
{
    @Primary
    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(RedissonClient.class)
    public RedissonClient redissonClientSingleServer(@NonNull RedisProperties redisProperties, @NonNull RedissonPoolProperties poolProperties)
    {
        Config config = new Config()
                .setTcpKeepAlive(poolProperties.getKeepAlive());

        if (isNotBlank(redisProperties.getUsername())) config.setUsername(redisProperties.getUsername());
        if (isNotBlank(redisProperties.getPassword())) config.setPassword(redisProperties.getPassword());

        SingleServerConfig singleServerConfig = config.useSingleServer()
                                                      .setAddress(buildAddress(redisProperties))
                                                      .setDatabase(redisProperties.getDatabase())
                                                      .setConnectionMinimumIdleSize(poolProperties.getConnectionMinimumIdleSize())
                                                      .setConnectionPoolSize(poolProperties.getConnectionPoolSize())
                                                      .setSubscriptionConnectionMinimumIdleSize(poolProperties.getSubscriptionConnectionMinimumIdleSize())
                                                      .setSubscriptionConnectionPoolSize(poolProperties.getSubscriptionConnectionPoolSize())
                                                      .setIdleConnectionTimeout(poolProperties.getIdleConnectionTimeout())
                                                      .setRetryAttempts(poolProperties.getRetryAttempts())
                                                      .setRetryDelay(new ConstantDelay(Duration.ofMillis(poolProperties.getRetryInterval())))
                                                      .setPingConnectionInterval(poolProperties.getPingConnectionInterval());

        if (redisProperties.getConnectTimeout() != null) singleServerConfig.setConnectTimeout(toInt(redisProperties.getConnectTimeout().toMillis()));
        if (redisProperties.getTimeout() != null) singleServerConfig.setTimeout(toInt(redisProperties.getTimeout().toMillis()));

        return Redisson.create(config);
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public RedissonRateLimitAspect redissonRateLimitAspect(RedissonClient redissonClient)
    {
        return new RedissonRateLimitAspect(redissonClient);
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public RedissonSubmitOnceAspect redissonSubmitOnceAspect(RedissonClient redissonClient)
    {
        return new RedissonSubmitOnceAspect(redissonClient);
    }

    private String buildAddress(RedisProperties redisProperties)
    {
        String scheme = redisProperties.getSsl() != null && redisProperties.getSsl().isEnabled() ? "rediss://" : "redis://";
        return format("{}{}:{}", scheme, redisProperties.getHost(), redisProperties.getPort());
    }
}
