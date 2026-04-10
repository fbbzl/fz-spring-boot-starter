package com.fz.starter.generator.frame.context;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/29 14:32
 */

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Index
{
    String       name;
    List<String> columns;
    Boolean      unique;

}
