package com.fz.springboot.starter.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

import static cn.hutool.core.date.DatePattern.NORM_DATETIME_PATTERN;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/22 14:24
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
        return Jackson2ObjectMapperBuilder.json()
                                          // basic config
                                          .timeZone(TimeZone.getTimeZone("GMT+8"))
                                          .dateFormat(new SimpleDateFormat(NORM_DATETIME_PATTERN))

                                          // Serialization
                                          .featuresToDisable(
                                                  SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                                                  SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
                                          .featuresToEnable(
                                                  SerializationFeature.CLOSE_CLOSEABLE)
                                          // Deserialization
                                          .featuresToDisable(
                                                  DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                                                  DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                                          .featuresToEnable(
                                                  DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT,
                                                  DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
                                          // Mapper
                                          .featuresToEnable(
                                                  MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS,
                                                  MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES,
                                                  MapperFeature.ALLOW_COERCION_OF_SCALARS)
                                          // Parser
                                          .featuresToEnable(
                                                  JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS,
                                                  JsonParser.Feature.ALLOW_SINGLE_QUOTES,
                                                  JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES,
                                                  JsonParser.Feature.ALLOW_YAML_COMMENTS)
                                          .serializationInclusion(JsonInclude.Include.NON_NULL)
                                          .modulesToInstall(new JavaTimeModule())
                                          .serializerByType(LocalDateTime.class, new LocalDateTimeSerializer(
                                                  DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                                          .deserializerByType(LocalDateTime.class, new LocalDateTimeDeserializer(
                                                  DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

}
