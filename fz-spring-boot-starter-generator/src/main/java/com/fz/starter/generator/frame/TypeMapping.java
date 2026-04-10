package com.fz.starter.generator.frame;

import java.util.Map;

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

    class DefaultTypeMapping implements TypeMapping
    {
        @Override
        public Map<String, String> typeMapping()
        {
            return Map.ofEntries(
                    Map.entry("bigint", "Long"),
                    Map.entry("int", "Integer"),
                    Map.entry("integer", "Integer"),
                    Map.entry("varchar", "String"),
                    Map.entry("char", "String"),
                    Map.entry("text", "String"),
                    Map.entry("longtext", "String"),
                    Map.entry("datetime", "LocalDateTime"),
                    Map.entry("timestamp", "LocalDateTime"),
                    Map.entry("date", "LocalDate"),
                    Map.entry("decimal", "BigDecimal"),
                    Map.entry("numeric", "BigDecimal"),
                    Map.entry("tinyint", "Boolean"),
                    Map.entry("float", "Float"),
                    Map.entry("double", "Double"),
                    Map.entry("real", "Double"),
                    Map.entry("json", "String"),
                    Map.entry("boolean", "Boolean"));
        }
    }

}
