package io.github.fbbzl.starter.auth.jwt.config;

import io.github.fbbzl.starter.auth.jwt.JwtFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/5/2 13:55
 */

@AutoConfiguration
@ConditionalOnWebApplication
public class JwtConfiguration
{

    @Bean
    @ConditionalOnMissingBean(JwtProperties.class)
    public JwtProperties jwtProperties()
    {
        return new JwtProperties();
    }

    @Bean
    public JwtFactory jwtFactory(JwtProperties jwtProperties)
    {
        return new JwtFactory(jwtProperties);
    }
}
