package io.github.fbbzl.starter.web.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

/**
 * Marks early-created framework beans as infrastructure to avoid BeanPostProcessorChecker warnings.
 * @author fengbinbin
 * @version 1.0
 * @since 2026/4/22 14:24
 */

@Configuration(proxyBeanMethods = false)
public class InfrastructureBeanRoleConfiguration
{
    private InfrastructureBeanRoleConfiguration() {
        /* This utility class should not be instantiated */
    }

    private static final String[] INFRASTRUCTURE_BEAN_NAMES = {
            "stringOrNumberMigrationVersionConverter",
            "cn.crane4j.spring.boot.config.Crane4jAutoConfiguration"
    };

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public static BeanFactoryPostProcessor infrastructureBeanRolePostProcessor()
    {
        return beanFactory -> {
            for (String beanName : INFRASTRUCTURE_BEAN_NAMES)
            {
                if (beanFactory.containsBeanDefinition(beanName))
                {
                    beanFactory.getBeanDefinition(beanName)
                               .setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
                }
            }
        };
    }
}
