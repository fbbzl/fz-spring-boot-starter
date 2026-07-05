package io.github.fbbzl.starter.jpa.entity;

import io.github.fbbzl.starter.pojo.entity.BaseTableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "test_spec_entity")
public class TestSpecEntity implements BaseTableEntity<Long>
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Integer age;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long createBy;

    private Long updateBy;
}
