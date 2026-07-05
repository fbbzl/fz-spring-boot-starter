package io.github.fbbzl.starter.jpa.entity;

import io.github.fbbzl.starter.jpa.BaseJpaEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "test_repo_entity")
public class TestRepoEntity extends BaseJpaEntity<Long>
{
    private String name;
    private Integer age;
}
