package io.github.fbbzl.starter.generator.frame;

import java.util.Map;

import static java.util.Map.entry;

/**
 * map db type and java type for generating code
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026/4/4 10:20
 */
public interface TypeMapping
{

    /**
     * key is database type, value is java type
     */
    Map<String, String> typeMapping();

    /**
     * key is java simple type name, value is fully qualified import path
     */
    Map<String, String> typeImportMapping();

    class DefaultTypeMapping implements TypeMapping
    {
        @Override
        public Map<String, String> typeMapping()
        {
            return Map.ofEntries(
                     entry("bigint", "Long"),
                     entry("int", "Integer"),
                     entry("integer", "Integer"),
                     entry("varchar", "String"),
                     entry("char", "String"),
                     entry("text", "String"),
                     entry("longtext", "String"),
                     entry("datetime", "LocalDateTime"),
                     entry("timestamp", "LocalDateTime"),
                     entry("date", "LocalDate"),
                     entry("decimal", "BigDecimal"),
                     entry("numeric", "BigDecimal"),
                     entry("tinyint", "Boolean"),
                     entry("float", "Float"),
                     entry("double", "Double"),
                     entry("real", "Double"),
                     entry("json", "String"),
                     entry("boolean", "Boolean"));
        }

        @Override
        public Map<String, String> typeImportMapping()
        {
            return Map.of(
                    "LocalDateTime", "java.time.LocalDateTime",
                    "LocalDate", "java.time.LocalDate",
                    "BigDecimal", "java.math.BigDecimal");
        }
    }
}
