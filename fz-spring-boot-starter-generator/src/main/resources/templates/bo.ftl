package ${moduleName}.service.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import io.github.fbbzl.starter.pojo.bo.BaseBo;
import lombok.experimental.FieldNameConstants;

import static lombok.AccessLevel.PRIVATE;
<#if extraImports?? && extraImports?size gt 0>
<#list extraImports as imp>import ${imp};
</#list></#if>

/**
 * ${tableComment?replace("*/", "* /")} business object
 *
 * @author ${author}
 * @version 1.0
 * @since ${date}
 */

@Data
@FieldNameConstants
@EqualsAndHashCode(callSuper = false)
@FieldDefaults(level = PRIVATE)
public class ${className}Bo extends BaseBo<${primaryKeyType}> {
<#-- 遍历字段生成实体属性 -->
<#list fields as field>
    <#-- 排除父类字段 id, createdAt, createdBy, updatedAt, updatedBy, deletedAt 字段 -->
    <#if !["id", "createdAt", "createdBy", "updatedAt", "updatedBy", "deletedAt"]?seq_contains(field.name)>
    /**
     * ${field.comment?replace("*/", "* /")}
<#if field.example??>
     * Example: ${field.example?j_string?replace("*/", "* /")}
</#if>
<#if field.minLength??>
     * Min length: ${field.minLength?c}
</#if>
<#if field.maxLength??>
     * Max length: ${field.maxLength?c}
</#if>
     */

    ${field.javaType} ${field.name};
    </#if>
</#list>

}