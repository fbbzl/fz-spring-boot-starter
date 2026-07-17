package io.github.fbbzl.starter.redisson.config;

import io.github.fbbzl.starter.redisson.limiter.aspect.RedissonRateLimitAspect;
import io.github.fbbzl.starter.redisson.repeat.RedissonSubmitOnceAspect;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class RedissonConfigurationWebApplicationTypeTest
{

    private final WebApplicationContextRunner servletContextRunner = new WebApplicationContextRunner()
            .withBean(RedissonClient.class, () -> mock(RedissonClient.class))
            .withConfiguration(AutoConfigurations.of(RedissonConfiguration.class));

    private final ReactiveWebApplicationContextRunner reactiveContextRunner = new ReactiveWebApplicationContextRunner()
            .withBean(RedissonClient.class, () -> mock(RedissonClient.class))
            .withConfiguration(AutoConfigurations.of(RedissonConfiguration.class));

    @Test
    void shouldLoadServletAspectsInServletApplication()
    {
        servletContextRunner.run(context -> assertThat(context)
                .hasSingleBean(RedissonRateLimitAspect.class)
                .hasSingleBean(RedissonSubmitOnceAspect.class));
    }

    @Test
    void shouldNotLoadServletAspectsInReactiveApplication()
    {
        reactiveContextRunner.run(context -> assertThat(context)
                .doesNotHaveBean(RedissonRateLimitAspect.class)
                .doesNotHaveBean(RedissonSubmitOnceAspect.class));
    }
}
