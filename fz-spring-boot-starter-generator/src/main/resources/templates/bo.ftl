package ${moduleName}.service.bo;

import io.swagger.v3.oas.annotations.media.*;
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
 * ${className} business object
 *
 * @author ${author}
 * @version 1.0
 * @since ${date}
 */

@Data
@FieldNameConstants
@EqualsAndHashCode(callSuper = false)
@FieldDefaults(level = PRIVATE)
@Schema(description = "${tableComment} bo")
public class ${className}Bo extends BaseBo<${primaryKeyType}> {
<#-- 遍历字段生成实体属性 -->
<#list fields as field>
    <#-- 排除父类字段 id, createTime, createBy, updateTime, updateBy, deleted 字段 -->
    <#if !["id", "createTime", "createBy", "updateTime", "updateBy", "deleted"]?seq_contains(field.name)>

    @Schema(description = "${field.comment}"<#if field.example??>, example = "${field.example?j_string}"</#if><#if field.minLength??>, minLength = ${field.minLength?c}</#if><#if field.maxLength??>, maxLength = ${field.maxLength?c}</#if>)
    ${field.javaType} ${field.name};
    </#if>
</#list>

}