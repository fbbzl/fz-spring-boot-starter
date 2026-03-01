package com.fz.starter.generator.frame.context;

import lombok.Data;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/29 14:32
 */
@Data
public class Field {
    private String  name;
    private String  javaType;
    private String  comment;
    private String  example;
    private Integer minLength;
    private Integer maxLength;
    private Boolean lengthValidation;
    private Boolean patternValidation;
    private String  patternType;
    private String  isNullable;

}