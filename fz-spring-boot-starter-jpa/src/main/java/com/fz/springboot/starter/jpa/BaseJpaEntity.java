package com.fz.springboot.starter.jpa;

import com.fz.springboot.starter.pojo.entity.BaseTableEntity;
import com.fz.springboot.starter.pojo.validation.CRUD;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.SoftDelete;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/2/8 15:33
 */

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@FieldNameConstants
@FieldDefaults(level = AccessLevel.PRIVATE)
@MappedSuperclass
@SoftDelete
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseJpaEntity implements BaseTableEntity {

    @Id
    @NotNull(groups = CRUD.U.class)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    Long id;

    @Column
    @CreatedBy
    Long createBy;

    @CreatedDate
    @Column(nullable = false, columnDefinition = "datetime(3)")
    LocalDateTime createTime;

    @Column
    @LastModifiedBy
    Long updateBy;

    @LastModifiedDate
    @Column(nullable = false, columnDefinition = "datetime(3)")
    LocalDateTime updateTime;

}
