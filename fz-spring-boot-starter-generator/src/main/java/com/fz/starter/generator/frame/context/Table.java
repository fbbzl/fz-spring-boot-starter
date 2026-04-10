package com.fz.starter.generator.frame.context;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/29 14:32
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Table
{

    String      tableName;
    String      tableComment;
    String      schemaName;
    List<Field> fields;
    List<Index> indexes;
}