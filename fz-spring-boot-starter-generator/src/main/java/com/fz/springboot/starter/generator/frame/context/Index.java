package com.fz.springboot.starter.generator.frame.context;

import lombok.Data;

import java.util.List;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/29 14:32
 */

@Data
public class Index {
    /**
     * 索引名称
     */
    private String       name;
    /**
     * 索引涉及的列名列表
     */
    private List<String> columns;
    /**
     * 是否是唯一索引
     */
    private Boolean      unique;

}
