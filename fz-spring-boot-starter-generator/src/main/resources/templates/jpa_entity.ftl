package ${moduleName}.dal.entity;

import com.fz.springboot.starter.jpa.BaseJpaEntity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.Comment;
import lombok.*;
import lombok.experimental.*;
import org.hibernate.proxy.HibernateProxy;
import java.util.Objects;

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
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
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
public class ${className} extends BaseJpaEntity {
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
        @Comment("${field.comment}")
        <#if field.lengthValidation??>
        @Length(min = ${field.minLength!1}, max = ${field.maxLength})
        </#if>
        ${field.javaType} ${field.name};
    </#if>
</#list>

        @Override
        public final boolean equals(Object that) {
            if (this == that) return true;
            if (that == null) return false;
            Class<?> oEffectiveClass    = that instanceof HibernateProxy ? ((HibernateProxy) that).getHibernateLazyInitializer().getPersistentClass() : that.getClass();
            Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
            if (thisEffectiveClass != oEffectiveClass) return false;
            ${className} thatEntity = (${className}) that;
            return getId() != null && Objects.equals(getId(), thatEntity.getId());
        }

        @Override
        public final int hashCode() {
            return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
        }
}
