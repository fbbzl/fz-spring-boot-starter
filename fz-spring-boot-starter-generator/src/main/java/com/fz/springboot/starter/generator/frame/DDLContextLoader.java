package com.fz.springboot.starter.generator.frame;

import cn.hutool.core.map.MapUtil;
import com.fz.springboot.starter.generator.frame.context.Field;
import com.fz.springboot.starter.generator.frame.context.Index;
import com.fz.springboot.starter.generator.frame.context.Table;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * used to obtain ddl information for database tables
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/29 14:32
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class DDLContextLoader {

    static Map<String, String> typeMapping = Map.ofEntries(
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

    final JdbcTemplate jdbcTemplate;

    @Value("${code.generator.schema:}")
    private String schema;

    public Table getTableContext(String tableName) {
        try {
            String tableSql = """
                          SELECT table_name, table_comment 
                          FROM information_schema.tables 
                          WHERE table_schema = DATABASE() 
                          AND table_name = ?
                          """;
            List<Map<String, Object>> tables = jdbcTemplate.queryForList(tableSql, tableName);

            if (tables.isEmpty()) return null;

            Map<String, Object> table = tables.iterator().next();

            String tableComment = MapUtil.getStr(table, "table_comment");

            return new Table()
                    .setTableName(tableName)
                    .setTableComment(tableComment != null ? tableComment : tableName)
                    .setSchemaName(schema)
                    .setFields(getColumns(tableName))
                    .setIndexes(getIndexesForTable(tableName));
        } catch (Exception e) {
            log.error("获取表 {} 信息失败", tableName, e);
            return null;
        }
    }


    private List<Field> getColumns(String tableName) {
        String fieldSql = """
                          SELECT column_name, data_type, column_comment, column_default, is_nullable,
                                 character_maximum_length, numeric_precision, numeric_scale
                          FROM information_schema.columns 
                          WHERE table_schema = DATABASE() 
                          AND table_name = ?
                          ORDER BY ordinal_position
                          """;

        List<Map<String, Object>> columns = jdbcTemplate.queryForList(fieldSql, tableName);
        List<Field>               fields  = new ArrayList<>();

        for (Map<String, Object> column : columns) {
            Field fieldInfo = new Field();

            String columnName = MapUtil.getStr(column, "column_name"),
                    dataType = MapUtil.getStr(column, "data_type"),
                    columnComment = MapUtil.getStr(column, "column_comment"),
                    isNullable = MapUtil.getStr(column, "is_nullable");

            fieldInfo.setName(columnName);
            fieldInfo.setJavaType(typeMapping.getOrDefault(dataType.toLowerCase(), "String"));
            fieldInfo.setComment(columnComment != null ? columnComment : columnName);
            fieldInfo.setExample(getExampleValue(dataType));
            fieldInfo.setIsNullable(isNullable);

            // 设置长度验证
            Object maxLengthObj = column.get("character_maximum_length");
            if (maxLengthObj != null) {
                Integer maxLength = ((Long) maxLengthObj).intValue();
                fieldInfo.setMaxLength(maxLength);
                fieldInfo.setLengthValidation(true);
                if ("NO".equals(isNullable)) {
                    fieldInfo.setMinLength(1);
                }
                else {
                    fieldInfo.setMinLength(0);
                }
            }

            fields.add(fieldInfo);
        }

        return fields;
    }

    private List<Index> getIndexesForTable(String tableName) {
        String indexSql = """
                      SELECT index_name, column_name, non_unique, index_type
                      FROM information_schema.statistics
                      WHERE table_schema = DATABASE()
                      AND table_name = ?
                      ORDER BY index_name, seq_in_index
                      """;

        List<Map<String, Object>> indexRows = jdbcTemplate.queryForList(indexSql, tableName);
        Map<String, Index> indexMap = new LinkedHashMap<>();

        for (Map<String, Object> row : indexRows) {
            String indexName = MapUtil.getStr(row, "index_name");
            String columnName = MapUtil.getStr(row, "column_name");
            Boolean nonUnique = MapUtil.getBool(row, "non_unique");

            // 排除主键索引
            if ("PRIMARY".equalsIgnoreCase(indexName)) {
                continue;
            }

            indexMap.computeIfAbsent(indexName, k -> {
                Index index = new Index();
                index.setName(indexName);
                index.setColumns(new ArrayList<>());
                index.setUnique(!nonUnique); // 非唯一索引取反为唯一索引
                return index;
            }).getColumns().add(columnName);
        }

        return new ArrayList<>(indexMap.values());
    }


    /**
     * 获取示例值
     */
    private String getExampleValue(String dataType) {
        return switch (dataType.toLowerCase()) {
            case "bigint" -> "1";
            case "int", "integer" -> "1";
            case "varchar", "char", "text" -> "示例文本";
            case "datetime", "timestamp" -> "2025-08-29 14:32:00";
            case "date" -> "2025-08-29";
            case "decimal", "numeric" -> "100.00";
            case "tinyint" -> "1";
            default -> "示例值";
        };
    }
}
