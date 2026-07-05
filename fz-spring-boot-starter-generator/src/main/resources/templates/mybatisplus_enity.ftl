package ${moduleName}.dal.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.github.fbbzl.starter.mybatisplus.BaseMybatisPlusEntity;
import io.github.fbbzl.starter.pojo.validation.group.CRUD;
import lombok.*;
import jakarta.validation.constraints.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.FieldNameConstants;
import static lombok.AccessLevel.PRIVATE;
import org.springframework.lang.Nullable;

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
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@FieldDefaults(level = PRIVATE)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ${className} extends BaseMybatisPlusEntity<${primaryKeyType}> {
<#-- 遍历字段生成实体属性 -->
<#list fields as field>
    <#-- 排除父类字段 id, createTime, createBy, updateTime, updateBy, deleted, deleteAt 字段 -->
    <#if !["id", "createTime", "createBy", "updateTime", "updateBy", "deleted", "deleteAt"]?seq_contains(field.name)>

    /**
     * ${field.comment}
     */
    <#if field.isNullable == "NO">
    @NotNull(groups = CRUD.C.class, message = "{${className}.${field.name}.null}")
    <#else>
    @Nullable
    </#if>
    <#if field.lengthValidation??>
    @Length(groups = CRUD.C.class, min = ${field.minLength!1}, max = ${field.maxLength?c}, message="{${className}.${field.name}.illegal}")
    </#if>
    ${field.javaType} ${field.name};
    </#if>
</#list>

}
