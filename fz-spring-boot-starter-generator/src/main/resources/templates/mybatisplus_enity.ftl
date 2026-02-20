package ${moduleName}.dal.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fz.springboot.starter.mybatisplus.BaseMybatisPlusEntity;
import lombok.*;
import jakarta.validation.constraints.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.FieldNameConstants;
import static lombok.AccessLevel.PRIVATE;

<#if hasLengthValidation?? && hasLengthValidation>
    import org.hibernate.validator.constraints.Length;
</#if>
<#if hasPatternValidation?? && hasPatternValidation>
    import jakarta.validation.constraints.Pattern;
    import cn.hutool.core.lang.RegexPool;
</#if>

/**
*
* ${className} entity
*
* ${tableComment}
*
* @author ${author}
* @version 1.0
* @since ${date}
*/
@TableName("${tableName}")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@FieldDefaults(level = PRIVATE)
@EqualsAndHashCode(callSuper = true)
public class ${className} extends BaseMybatisPlusEntity {
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
        <#if field.lengthValidation??>
        @Length(min = ${field.minLength!1}, max = ${field.maxLength})
        </#if>
        ${field.javaType} ${field.name};
    </#if>
</#list>

}