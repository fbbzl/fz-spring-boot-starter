package io.github.fbbzl.starter.jpa.config;

import io.github.fbbzl.starter.jpa.entity.TestRepoEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes = {
        JpaConfigurationAuditingBeanNameTest.TestApplication.class,
        JpaConfiguration.class,
        JpaConfigurationAuditingBeanNameTest.CustomAuditingConfiguration.class
})
class JpaConfigurationAuditingBeanNameTest
{

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void shouldKeepAuditingReferenceBeansWhenApplicationDefinesSameTypesWithDifferentNames()
    {
        assertThat(applicationContext.containsBean(JpaConfiguration.AUDITOR_BEAN_NAME)).isTrue();
        assertThat(applicationContext.containsBean(JpaConfiguration.DATETIME_PROVIDER_BEAN_NAME)).isTrue();
        assertThat(applicationContext.containsBean("customAuditor")).isTrue();
        assertThat(applicationContext.containsBean("customDateTimeProvider")).isTrue();
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomAuditingConfiguration
    {

        @Bean
        AuditorAware<Long> customAuditor()
        {
            return () -> Optional.of(42L);
        }

        @Bean
        DateTimeProvider customDateTimeProvider()
        {
            return () -> Optional.of(LocalDateTime.of(2026, 7, 11, 0, 0));
        }
    }

    @SpringBootApplication(scanBasePackageClasses = TestRepoEntity.class)
    @AutoConfigurationPackage(basePackageClasses = TestRepoEntity.class)
    static class TestApplication
    {
    }
}
