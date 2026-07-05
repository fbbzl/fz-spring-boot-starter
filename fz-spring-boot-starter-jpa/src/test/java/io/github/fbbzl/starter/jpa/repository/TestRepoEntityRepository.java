package io.github.fbbzl.starter.jpa.repository;

import io.github.fbbzl.starter.jpa.entity.TestRepoEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface TestRepoEntityRepository extends BaseRepository<TestRepoEntity, Long>
{
}
