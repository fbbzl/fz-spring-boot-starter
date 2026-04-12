package ${moduleName}.service.bo;

import io.swagger.v3.oas.annotations.media.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import com.fz.starter.pojo.bo.BaseBo;
import lombok.experimental.FieldNameConstants;

import static lombok.AccessLevel.PRIVATE;

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

    /**
     * ${field.comment}
     */
    @Schema(description = "${field.comment}"<#if field.minLength??>, minLength = ${field.minLength}</#if><#if field.maxLength??>, maxLength = ${field.maxLength}</#if>)
    ${field.javaType} ${field.name};
    </#if>
</#list>

}