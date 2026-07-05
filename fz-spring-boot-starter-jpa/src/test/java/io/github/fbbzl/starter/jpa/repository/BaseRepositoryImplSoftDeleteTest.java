package io.github.fbbzl.starter.jpa.repository;

import io.github.fbbzl.starter.jpa.JpaTestApplication;
import io.github.fbbzl.starter.jpa.entity.TestRepoEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes = JpaTestApplication.class)
class BaseRepositoryImplSoftDeleteTest
{

    @Autowired
    private TestRepoEntityRepository repository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void shouldSoftDeleteById()
    {
        TestRepoEntity entity = new TestRepoEntity();
        entity.setName("alice");
        entity.setAge(20);
        TestRepoEntity saved = repository.saveAndFlush(entity);
        Long id = saved.getId();

        repository.delete(id);
        entityManager.flush();
        entityManager.clear();

        Optional<TestRepoEntity> result = repository.findById(id);
        assertThat(result).isEmpty();

        Long physicalCount = countIncludingDeleted(id);
        assertThat(physicalCount).isOne();
    }

    @Test
    void shouldNotPhysicallyDeleteById()
    {
        TestRepoEntity entity = new TestRepoEntity();
        entity.setName("bob");
        entity.setAge(30);
        TestRepoEntity saved = repository.saveAndFlush(entity);
        Long id = saved.getId();

        repository.delete(id);
        entityManager.flush();
        entityManager.clear();

        Long physicalCount = countIncludingDeleted(id);
        assertThat(physicalCount).isOne();
    }

    private Long countIncludingDeleted(Long id)
    {
        Object result = entityManager.createNativeQuery(
                                              "select count(*) from test_repo_entity where id = ?")
                                     .setParameter(1, id)
                                     .getSingleResult();
        return ((Number) result).longValue();
    }
}
