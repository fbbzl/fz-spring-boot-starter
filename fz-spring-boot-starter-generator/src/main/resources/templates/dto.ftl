package ${moduleName}.controller.dto;

import ${moduleName}.dal.entity.${className};
import io.swagger.v3.oas.annotations.media.*;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import com.fz.starter.pojo.dto.BaseDto;

<#if hasLengthValidation?? && hasLengthValidation>
import org.hibernate.validator.constraints.Length;
</#if>
<#if hasPatternValidation?? && hasPatternValidation>
import jakarta.validation.constraints.Pattern;
import cn.hutool.core.lang.RegexPool;
</#if>

/**
* ${className} dto
*
* @author ${author}
* @version 1.0
* @since ${date}
*/

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "${tableComment} request")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ${className}Dto extends BaseDto<${className}> {
<#-- 遍历字段生成实体属性 -->
<#list fields as field>
    <#-- 排除父类字段 id, createTime, createBy, updateTime, updateBy, deleted 字段 -->
    <#if !["id", "createTime", "createBy", "updateTime", "updateBy", "deleted"]?seq_contains(field.name)>

        /**
         * ${field.comment}
         */
        <#if field.isNullable == "NO">
        @NotNull(message = "{${className}.${field.name}}")
        </#if>
        @Schema(description = "${field.comment}"<#if field.minLength??>, minLength = ${field.minLength}</#if><#if field.maxLength??>, maxLength = ${field.maxLength}</#if>)
        <#if field.lengthValidation??>
        @Length(min = ${field.minLength!1}, max = ${field.maxLength})
        </#if>
        ${field.javaType} ${field.name};
    </#if>
</#list>
}
