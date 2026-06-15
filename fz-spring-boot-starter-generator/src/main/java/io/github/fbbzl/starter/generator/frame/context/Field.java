package io.github.fbbzl.starter.generator.frame.context;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/29 14:32
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Field
{
    String  name;
    String  javaType;
    String  comment;
    String  example;
    Integer minLength;
    Integer maxLength;
    Boolean lengthValidation;
    Boolean patternValidation;
    String  patternType;
    String  isNullable;

}