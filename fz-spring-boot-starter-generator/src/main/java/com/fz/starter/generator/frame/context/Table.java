package com.fz.starter.generator.frame.context;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/29 14:32
 */
@Data
@Accessors(chain = true)
public class Table {

    private String      tableName;
    private String      tableComment;
    private String      schemaName;
    private List<Field> fields;
    private List<Index> indexes;
}