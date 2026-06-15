package ${moduleName}.dal.entity;

import io.github.fbbzl.starter.jpa.BaseJpaEntity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.Comment;
import io.github.fbbzl.starter.pojo.validation.group.CRUD;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.FieldNameConstants;
import org.hibernate.proxy.HibernateProxy;
import java.util.Objects;
import org.springframework.lang.Nullable;

import static lombok.AccessLevel.PRIVATE;

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
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@Table(name = "${tableName}", schema = "${schemaName}")
public class ${className} extends BaseJpaEntity<${primaryKeyType}> {
<#-- 遍历字段生成实体属性 -->
<#list fields as field>
    <#-- 排除父类字段 id, createTime, createBy, updateTime, updateBy, deleted 字段 -->
    <#if !["id", "createTime", "createBy", "updateTime", "updateBy", "deleted"]?seq_contains(field.name)>

    /**
     * ${field.comment}
     */
    <#if field.isNullable == "NO">
    @NotNull(groups = CRUD.C.class, message = "{${className}.${field.name}.null}")
    <#else>
    @Nullable
    </#if>
    @Comment("${field.comment}")
    <#if field.lengthValidation??>
    @Length(groups = CRUD.C.class, min = ${field.minLength!1}, max = ${field.maxLength?c}, message = "{${className}.${field.name}.illegal}")
    </#if>
    @ToString.Include
    ${field.javaType} ${field.name};
    </#if>
</#list>

    @Override
    public final boolean equals(Object that) {
        if (this == that) return true;
        if (that == null) return false;
        Class<?> oEffectiveClass    = that instanceof HibernateProxy thatHibernateProxy ? thatHibernateProxy.getHibernateLazyInitializer().getPersistentClass() : that.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy thisHibernateProxy ? thisHibernateProxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        ${className} thatEntity = (${className}) that;
        return getId() != null && Objects.equals(getId(), thatEntity.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy thisHibernateProxy ? thisHibernateProxy.getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
