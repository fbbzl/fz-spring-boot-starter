package com.fz.springboot.starter.dict.components.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fz.springboot.starter.jpa.BaseEntity;
import com.fz.springboot.starter.jpa.converter.JsonStringConverter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.validator.constraints.Length;

import java.util.Map;

import static com.fz.springboot.starter.jpa.SqlConstants.NOT_DELETED;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/9/3 11:28
 */

@Getter
@Setter
@Entity
@SQLRestriction(NOT_DELETED)
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Accessors(chain = true)
@Comment("系统字典表")
@Schema(description = "系统字典表")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "lj_sys_dict", schema = "lj_cloud",
       uniqueConstraints = {@UniqueConstraint(name = "uk_code", columnNames = SysDict.Fields.code)})
public class SysDict extends BaseEntity {

    @NotNull(message = "{SysDict.code}")
    @Comment("字典code")
    @Length(min = 1, max = 255)
    @Schema(description = "字典code", minLength = 1, maxLength = 255, example = "0")
    @Column(nullable = false, unique = true)
    String code;

    @NotNull(message = "{SysDict.value}")
    @Comment("字典value")
    @Column(nullable = false)
    @Length(min = 1, max = 255)
    @Schema(description = "字典的值", minLength = 1, maxLength = 255, example = "1")
    String value;

    @NotNull(message = "{SysDict.type}")
    @Comment("字典type")
    @Column(nullable = false)
    @Length(min = 1, max = 255)
    @Schema(description = "字典的type", minLength = 1, maxLength = 255, example = "user-type")
    String type;

    @NotNull(message = "{SysDict.value}")
    @Comment("字典描述")
    @Column(nullable = false)
    @Length(min = 1, max = 255)
    @Schema(description = "字典描述")
    String description;

    @JsonProperty("extends")
    @Convert(converter = JsonStringConverter.class)
    @Comment("字典扩展属性")
    @Length(min = 1, max = 255)
    @Schema(description = "字典扩展属性")
    @Column(name = "extend", columnDefinition = "JSON")
    Map<String, Object> extend;

}
