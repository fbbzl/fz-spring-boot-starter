package ${moduleName}.service.eo;

import ${moduleName}.dal.entity.${className};
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import com.fz.starter.pojo.eo.BaseEo;

import static lombok.AccessLevel.PRIVATE;

/**
* ${className} eo
*
* @author ${author}
* @version 1.0
* @since ${date}
*/

@Data
@EqualsAndHashCode(callSuper = false)
@FieldDefaults(level = PRIVATE)
public class ${className}Eo extends BaseEo<${className}> {
<#-- 遍历字段生成实体属性 -->
<#list fields as field>
    <#-- 排除父类字段 id, createTime, createBy, updateTime, updateBy, deleted 字段 -->
    <#if !["id", "createTime", "createBy", "updateTime", "updateBy", "deleted"]?seq_contains(field.name)>

        /**
        * ${field.comment}
        */
        @ExcelProperty("${field.comment}")
        ${field.javaType} ${field.name};
    </#if>
</#list>


}