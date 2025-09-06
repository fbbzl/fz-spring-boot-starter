package ${moduleName}.repository.entity;

import jpa.com.fz.springboot.starter.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import org.hibernate.annotations.Comment;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.FieldNameConstants;
import static lombok.AccessLevel.PRIVATE;

<#-- 动态导入依赖 -->
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
@Getter
@Setter
@Entity
@Schema(description = "${tableComment}")
@Comment("${tableComment}")
@FieldNameConstants
@FieldDefaults(level = PRIVATE)
@Table(name = "${tableName}", schema = "${schemaName}"<#if hasUniqueIndexes>,
    uniqueConstraints = {
    <#list uniqueIndexes as index>
        @UniqueConstraint(
        name = "${index.name}",
        columnNames = {
        <#list index.columns as column>
            "${column}"<#sep>, </#sep>
        </#list>
        }
        )<#sep>,</#sep>
    </#list>
    }</#if><#if hasNormalIndexes>,
    indexes = {
    <#list normalIndexes as index>
        @Index(
        name = "${index.name}",
        columnList = "<#list index.columns as column>${column}<#sep>, </#sep></#list>"
        )<#sep>,</#sep>
    </#list>
    }</#if>
)
public class ${className} extends BaseEntity {

<#-- 遍历字段生成实体属性 -->
<#list fields as field>
<#-- 排除 id, createTime, isDeleted, updateTime 字段 -->
    <#if !["id", "createTime", "isDeleted", "updateTime"]?seq_contains(field.name)>
        <#if field.isNullable == "NO">
        @NotNull(message = "{${className}.${field.name}}")
        </#if>
        @Schema(description = "${field.comment}"<#if field.minLength??>, minLength = ${field.minLength}</#if><#if field.maxLength??>, maxLength = ${field.maxLength}</#if>)
        @Comment("${field.comment}")
        <#if field.lengthValidation??>
        @Length(min = ${field.minLength!1}, max = ${field.maxLength})
        </#if>
        private ${field.javaType} ${field.name};

    </#if>
</#list>
}
