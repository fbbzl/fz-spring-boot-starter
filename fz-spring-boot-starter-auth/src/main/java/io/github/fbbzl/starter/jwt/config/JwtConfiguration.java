package io.github.fbbzl.starter.jwt.config;

import io.github.fbbzl.starter.jwt.JwtFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/5/2 13:55
 */

@AutoConfiguration
@ConditionalOnWebApplication
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfiguration
{

    @Bean
    public JwtFactory jwtFactory(JwtProperties jwtProperties)
    {
        return new JwtFactory(jwtProperties);
    }
}
