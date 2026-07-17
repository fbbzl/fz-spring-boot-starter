package io.github.fbbzl.starter.jpa;

import io.github.fbbzl.starter.pojo.entity.BaseTableEntity;
import io.github.fbbzl.starter.pojo.validation.group.CRUD;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
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
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseJpaEntity<ID extends Serializable> implements BaseTableEntity<ID>
{

    @Id
    @NotNull(groups = CRUD.U.class)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    ID id;

    @CreatedBy
    @Column
    ID createdBy;

    @CreatedDate
    @Column
    LocalDateTime createdAt;

    @LastModifiedBy
    @Column
    ID updatedBy;

    @LastModifiedDate
    @Column
    LocalDateTime updatedAt;

    @Column
    LocalDateTime deletedAt;

}
