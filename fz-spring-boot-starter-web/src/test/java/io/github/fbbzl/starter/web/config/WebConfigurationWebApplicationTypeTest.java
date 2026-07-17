package io.github.fbbzl.starter.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.fbbzl.starter.web.advice.WebExceptionAdvice;
import io.github.fbbzl.starter.web.advice.WebRequestAdvice;
import io.github.fbbzl.starter.web.customizer.QuestOperationCustomizer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import static org.assertj.core.api.Assertions.assertThat;

class WebConfigurationWebApplicationTypeTest
{

    private final ReactiveWebApplicationContextRunner reactiveContextRunner = new ReactiveWebApplicationContextRunner()
            .withBean(ObjectMapper.class, ObjectMapper::new)
            .withConfiguration(AutoConfigurations.of(
                    WebMvcConfiguration.class,
                    OpenApiConfiguration.class,
                    JacksonConfiguration.class
            ));

    @Test
    void shouldNotLoadServletMvcBeansInReactiveApplication()
    {
        reactiveContextRunner.run(context -> assertThat(context)
                .doesNotHaveBean(WebExceptionAdvice.class)
                .doesNotHaveBean(WebRequestAdvice.class)
                .doesNotHaveBean(QuestOperationCustomizer.class)
                .doesNotHaveBean(Jackson2ObjectMapperBuilder.class));
    }
}
