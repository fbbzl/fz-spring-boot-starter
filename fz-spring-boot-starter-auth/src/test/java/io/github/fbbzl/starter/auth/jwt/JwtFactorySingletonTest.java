package io.github.fbbzl.starter.auth.jwt;

import io.github.fbbzl.starter.auth.jwt.config.JwtProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtFactorySingletonTest
{

    @AfterEach
    void tearDown()
    {
        JwtFactory.reset();
    }

    @Test
    void shouldReplaceSingletonWhenReinitialized()
    {
        JwtProperties firstProps = new JwtProperties();
        firstProps.setSecret("first-secret");
        JwtFactory first = new JwtFactory(firstProps);
        first.init();
        assertThat(JwtFactory.current()).isSameAs(first);

        JwtProperties secondProps = new JwtProperties();
        secondProps.setSecret("second-secret");
        JwtFactory second = new JwtFactory(secondProps);
        second.init();

        assertThat(JwtFactory.current()).isSameAs(second);
        assertThat(JwtFactory.current()).isNotSameAs(first);
    }
}
