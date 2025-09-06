package com.fz.springboot.starter.jpa;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fz.springboot.starter.jpa.validation.CRUD;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/22 14:24
 */

@Getter
@Setter
@DynamicInsert
@DynamicUpdate
@FieldNameConstants
@MappedSuperclass
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Serializable {

    private static final Boolean DEFAULT_IS_DELETED = false;

    @Id
    @NotNull(groups = CRUD.U.class)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    Long id;

    @CreatedDate
    @Column(nullable = false, columnDefinition = "datetime(3)")
    LocalDateTime createTime;

    @LastModifiedDate
    @Column(nullable = false, columnDefinition = "datetime(3)")
    LocalDateTime updateTime;

    @JsonIgnore
    @ColumnDefault("0")
    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    Boolean isDeleted = DEFAULT_IS_DELETED;

    /**
     * @author fengbinbin
     * @version 1.0
     * @since 2025/8/22 14:36
     */
    @Getter
    @Setter
    @MappedSuperclass
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @EntityListeners(AuditingEntityListener.class)
    public static class BaseAppEntity extends BaseEntity {

        @NotNull
        @Column(nullable = false)
        Long appId;

    }

    /**
     * @author fengbinbin
     * @version 1.0
     * @since 2025/8/22 14:34
     */
    @Getter
    @Setter
    @MappedSuperclass
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @EntityListeners(AuditingEntityListener.class)
    public static class BaseTenantEntity extends BaseEntity {

        @NotNull
        @Column(nullable = false)
        Long tenantId;

    }

    /**
     * @author fengbinbin
     * @version 1.0
     * @since 2025/8/22 14:34
     */
    @Getter
    @Setter
    @MappedSuperclass
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @EntityListeners(AuditingEntityListener.class)
    public static class BaseAppTenantEntity extends BaseEntity {

        @NotNull
        @Column(nullable = false)
        Long appId;

        @NotNull
        @Column(nullable = false)
        Long tenantId;

    }

}
