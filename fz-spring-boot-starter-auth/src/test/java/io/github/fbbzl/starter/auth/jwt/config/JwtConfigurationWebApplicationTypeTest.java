package io.github.fbbzl.starter.auth.jwt.config;

import io.github.fbbzl.starter.auth.jwt.JwtFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class JwtConfigurationWebApplicationTypeTest
{

    private final WebApplicationContextRunner servletContextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JwtConfiguration.class));

    private final ReactiveWebApplicationContextRunner reactiveContextRunner = new ReactiveWebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JwtConfiguration.class));

    @Test
    void shouldLoadJwtBeansInServletApplication()
    {
        servletContextRunner.run(context -> assertThat(context)
                .hasSingleBean(JwtProperties.class)
                .hasSingleBean(JwtFactory.class));
    }

    @Test
    void shouldNotLoadServletJwtBeansInReactiveApplication()
    {
        reactiveContextRunner.run(context -> assertThat(context)
                .doesNotHaveBean(JwtProperties.class)
                .doesNotHaveBean(JwtFactory.class));
    }
}
