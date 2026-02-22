package com.fz.starter.jpa;

import com.fz.starter.pojo.entity.BaseTableEntity;
import com.fz.starter.pojo.validation.CRUD;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.SoftDelete;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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

    @CreatedBy
    @Column
    Long createBy;

    @CreatedDate
    @Column
    LocalDateTime createTime;

    @LastModifiedBy
    @Column
    Long updateBy;

    @LastModifiedDate
    @Column
    LocalDateTime updateTime;

}
