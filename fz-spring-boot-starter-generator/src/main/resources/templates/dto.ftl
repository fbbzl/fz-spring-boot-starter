package ${moduleName}.controller.dto;

import io.swagger.v3.oas.annotations.media.*;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import io.github.fbbzl.starter.pojo.dto.BaseDto;
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
 * ${className} dto
 *
 * @author ${author}
 * @version 1.0
 * @since ${date}
 */

@Data
@FieldNameConstants
@EqualsAndHashCode(callSuper = false)
@Schema(description = "${tableComment} dto")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ${className}Dto extends BaseDto<${primaryKeyType}> {
<#-- 遍历字段生成实体属性 -->
<#list fields as field>
    <#-- 排除父类字段 id, createTime, createBy, updateTime, updateBy, deleted 字段 -->
    <#if !["id", "createTime", "createBy", "updateTime", "updateBy", "deleted"]?seq_contains(field.name)>

    <#if field.isNullable == "NO">
    @NotNull(message = "{${className}.${field.name}.null}")
    <#else>
    @Nullable
    </#if>
    @Schema(description = "${field.comment}"<#if field.example??>, example = "${field.example?j_string}"</#if><#if field.minLength??>, minLength = ${field.minLength?c}</#if><#if field.maxLength??>, maxLength = ${field.maxLength?c}</#if>)
    <#if field.lengthValidation??>
    @Length(min = ${field.minLength!1}, max = ${field.maxLength?c})
    </#if>
    ${field.javaType} ${field.name};
    </#if>
</#list>
}
