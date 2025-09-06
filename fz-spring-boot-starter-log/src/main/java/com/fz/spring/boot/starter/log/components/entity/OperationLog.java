package com.fz.spring.boot.starter.log.components.entity;

import com.fz.springboot.starter.jpa.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Comment;
import org.hibernate.validator.constraints.Length;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/9/4 17:09
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Comment("操作日志表")
@Schema(description = "操作日志")
@Table(name = "lj_operation_log", schema = "lj_cloud")
public class OperationLog extends BaseEntity {

    @Length(max = 64)
    @Comment("操作类型")
    @Schema(description = "操作类型")
    String type;

    @Length(max = 255)
    @Comment("操作描述")
    @Schema(description = "操作描述")
    String description;

    @Comment("操作人ID")
    @Schema(description = "操作人ID")
    Long operatorId;

    @Length(max = 64)
    @Comment("操作人姓名")
    @Schema(description = "操作人姓名")
    String operatorName;

    @Comment("错误信息")
    @Schema(description = "错误信息")
    @Column(length = 1000)
    String errorMsg;
}
