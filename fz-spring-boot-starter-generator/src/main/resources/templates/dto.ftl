package ${moduleName}.controller.dto;

import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import io.github.fbbzl.starter.pojo.dto.BaseDto;
import io.github.fbbzl.starter.pojo.validation.group.CRUD;
import jakarta.validation.groups.Default;
import org.springframework.lang.Nullable;
import lombok.experimental.FieldNameConstants;

<#if hasLengthValidation?? && hasLengthValidation>
import org.hibernate.validator.constraints.Length;
</#if>
<#if hasPatternValidation?? && hasPatternValidation>
import jakarta.validation.constraints.Pattern;
import cn.hutool.core.lang.RegexPool;
</#if>
<#if extraImports?? && extraImports?size gt 0>
<#list extraImports as imp>import ${imp};
</#list></#if>

/**
 * ${tableComment?replace("*/", "* /")} dto
 *
 * @author ${author}
 * @version 1.0
 * @since ${date}
 */

@Data
@FieldNameConstants
@EqualsAndHashCode(callSuper = false)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ${className}Dto extends BaseDto<${primaryKeyType}> {
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
    <#if field.isNullable == "NO">
    @NotNull(groups = {Default.class, CRUD.C.class}, message = "{${className}.${field.name}.null}")
    <#else>
    @Nullable
    </#if>
    <#if field.lengthValidation??>
    @Length(groups = {Default.class, CRUD.C.class}, min = ${field.minLength!1}, max = ${field.maxLength?c})
    </#if>
    ${field.javaType} ${field.name};
    </#if>
</#list>
}
