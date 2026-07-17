package io.github.fbbzl.starter.jpa.repository;

import io.github.fbbzl.starter.jpa.JpaTestApplication;
import io.github.fbbzl.starter.jpa.entity.TestRepoEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes = JpaTestApplication.class)
class BaseRepositoryImplSoftDeleteQueryTest
{

    @Autowired
    private TestRepoEntityRepository repository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void shouldSoftDeleteAll()
    {
        save("alice", 20);
        save("bob", 30);
        entityManager.flush();

        repository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        assertThat(repository.findAll()).isEmpty();
        assertThat(countIncludingDeleted()).isEqualTo(2L);
    }

    @Test
    void shouldExcludeDeletedRecordsInIdsQuery()
    {
        TestRepoEntity alive = save("alive", 20);
        TestRepoEntity deleted = save("deleted", 30);
        entityManager.flush();

        repository.delete(deleted.getId());
        entityManager.flush();
        entityManager.clear();

        TestRepoEntity query = new TestRepoEntity();
        List<Long> ids = repository.ids(query, 100);

        assertThat(ids).containsExactly(alive.getId());
    }

    @Test
    void shouldExcludeDeletedRecordsInSelectForUpdate()
    {
        TestRepoEntity alive = save("alive", 20);
        TestRepoEntity deleted = save("deleted", 30);
        entityManager.flush();

        repository.delete(deleted.getId());
        entityManager.flush();
        entityManager.clear();

        repository.selectForUpdate(List.of(alive.getId(), deleted.getId()));

        // Locking succeeded; the main assertion is that no exception is thrown for the deleted record.
        assertThat(repository.findById(alive.getId())).isPresent();
        assertThat(repository.findById(deleted.getId())).isEmpty();
    }

    @Test
    void shouldNotIncrementDeletedRecord()
    {
        TestRepoEntity deleted = save("deleted", 30);
        entityManager.flush();
        repository.delete(deleted.getId());
        entityManager.flush();
        entityManager.clear();

        repository.increment("age", 10, List.of(deleted.getId()));
        entityManager.flush();
        entityManager.clear();

        Integer age = ((Number) entityManager.createNativeQuery(
                                                   "select age from test_repo_entity where id = ?")
                                    .setParameter(1, deleted.getId())
                                    .getSingleResult()).intValue();
        assertThat(age).isEqualTo(30);
    }

    private TestRepoEntity save(String name, int age)
    {
        TestRepoEntity entity = new TestRepoEntity();
        entity.setName(name);
        entity.setAge(age);
        entityManager.persist(entity);
        return entity;
    }

    private Long countIncludingDeleted()
    {
        Object result = entityManager.createNativeQuery("select count(*) from test_repo_entity")
                                   .getSingleResult();
        return ((Number) result).longValue();
    }
}
