package com.fz.starter.jpa.config;

import com.fz.starter.jpa.annotation.EnableSpecificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.fz.erwin.exception.Throws;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.ArrayUtils.EMPTY_STRING_ARRAY;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/2/15 0:15
 */

@Slf4j
@EnableSpecificationRepository
@AutoConfiguration
@EnableJpaAuditing(
        auditorAwareRef = JpaConfiguration.AUDITOR_BEAN_NAME,
        dateTimeProviderRef = JpaConfiguration.DATETIME_PROVIDER_BEAN_NAME
)
@AutoConfigureBefore(JpaRepositoriesAutoConfiguration.class)
public class JpaConfiguration {

    static final String AUDITOR_BEAN_NAME           = "def_auditor";
    static final String DATETIME_PROVIDER_BEAN_NAME = "def_dateTimeProvider";

    @Bean
    @ConditionalOnMissingBean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            DataSource dataSource,
            BeanFactory beanFactory) {
        List<String> basePackages = AutoConfigurationPackages.get(beanFactory);
        Throws.ifEmpty(basePackages, () -> "Unable to determine the JPA entity scanning base package! Make sure that the main class is labeled @SpringBootApplication");
        log.info("jpa starter scans packages -> {}", basePackages);

        return builder
                .dataSource(dataSource)
                .packages(basePackages.toArray(EMPTY_STRING_ARRAY))
                .build();
    }

    @Bean(AUDITOR_BEAN_NAME)
    @ConditionalOnMissingBean
    public AuditorAware<Long> auditor() {
        return () -> Optional.of(0L);
    }

    @Bean(DATETIME_PROVIDER_BEAN_NAME)
    @ConditionalOnMissingBean
    public DateTimeProvider dateTimeProvider() {
        return () -> Optional.of(LocalDateTime.now());
    }

}