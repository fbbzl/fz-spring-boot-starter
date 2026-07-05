package io.github.fbbzl.starter.jpa.repository;

import io.github.fbbzl.starter.jpa.JpaTestApplication;
import io.github.fbbzl.starter.jpa.entity.TestRepoEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes = JpaTestApplication.class)
class BaseRepositoryImplUpdateTest
{

    @Autowired
    private TestRepoEntityRepository repository;

    @Test
    void shouldUpdateExistingEntity()
    {
        TestRepoEntity entity = new TestRepoEntity();
        entity.setName("alice");
        entity.setAge(20);
        TestRepoEntity saved = repository.saveAndFlush(entity);

        TestRepoEntity update = new TestRepoEntity();
        update.setId(saved.getId());
        update.setName("bob");
        update.setAge(null);

        int affected = repository.update(update);

        assertThat(affected).isOne();

        Optional<TestRepoEntity> result = repository.findById(saved.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("bob");
        assertThat(result.get().getAge()).isEqualTo(20);
    }
}
