package io.github.fbbzl.starter.web.config;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.StaticMessageSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationMessageConfigurationTest
{

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ValidationAutoConfiguration.class, ValidationMessageConfiguration.class));

    @Test
    void shouldUseApplicationMessageSourceBeforeStarterFallback()
    {
        contextRunner.withUserConfiguration(ApplicationMessageSourceConfiguration.class)
                .run(context -> {
                    Validator validator = context.getBean(Validator.class);

                    Set<ConstraintViolation<Request>> violations = validator.validate(new Request(null));

                    assertThat(violations)
                            .singleElement()
                            .extracting(ConstraintViolation::getMessage)
                            .isEqualTo("application data required");
                });
    }

    @Test
    void shouldUseStarterFallbackWhenApplicationMessageSourceMisses()
    {
        contextRunner.run(context -> {
            Validator validator = context.getBean(Validator.class);

            Set<ConstraintViolation<Request>> violations = validator.validate(new Request(null));

            assertThat(violations)
                    .singleElement()
                    .extracting(ConstraintViolation::getMessage)
                    .isEqualTo("request data can not be null");
        });
    }

    private record Request(@NotNull(message = "{Q.data}") String data)
    {
    }

    @Configuration(proxyBeanMethods = false)
    static class ApplicationMessageSourceConfiguration
    {

        @Bean
        MessageSource messageSource()
        {
            StaticMessageSource messageSource = new StaticMessageSource();
            messageSource.addMessage("Q.data", LocaleContextHolder.getLocale(), "application data required");
            return messageSource;
        }
    }
}
