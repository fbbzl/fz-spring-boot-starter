package io.github.fbbzl.starter.webflux.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.TimeZone;

import static cn.hutool.core.date.DatePattern.NORM_DATETIME_FORMATTER;
import static cn.hutool.core.date.DatePattern.NORM_DATETIME_PATTERN;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/22 14:24
 */

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class JacksonConfiguration
{

    @Bean
    @Primary
    public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder()
    {
        return Jackson2ObjectMapperBuilder.json()
                                          // basic config
                                          .timeZone(TimeZone.getTimeZone("GMT+8"))
                                          .dateFormat(new SimpleDateFormat(NORM_DATETIME_PATTERN))
                                          // Serialization
                                          .featuresToDisable(
                                                  SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                                                  SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
                                          // Deserialization
                                          .featuresToDisable(
                                                  DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                                                  DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                                          // Mapper
                                          // Parser
                                          .serializationInclusion(JsonInclude.Include.NON_NULL)
                                          .modulesToInstall(new JavaTimeModule())
                                          .serializerByType(
                                                  LocalDateTime.class, new LocalDateTimeSerializer(NORM_DATETIME_FORMATTER))
                                          .deserializerByType(
                                                  LocalDateTime.class, new LocalDateTimeDeserializer(NORM_DATETIME_FORMATTER));
    }

}
