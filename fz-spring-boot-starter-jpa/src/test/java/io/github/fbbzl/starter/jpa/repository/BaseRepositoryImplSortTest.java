package io.github.fbbzl.starter.jpa.repository;

import io.github.fbbzl.starter.jpa.JpaTestApplication;
import io.github.fbbzl.starter.jpa.entity.TestRepoEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes = JpaTestApplication.class)
class BaseRepositoryImplSortTest
{

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void shouldTreatNullDirectionAsAsc() throws Exception
    {
        BaseRepositoryImpl<TestRepoEntity, Long> impl = new BaseRepositoryImpl<>(TestRepoEntity.class, entityManager);
        Method toSort = BaseRepositoryImpl.class.getDeclaredMethod("toSort", cn.hutool.db.sql.Order[].class);
        toSort.setAccessible(true);

        Sort sort = (Sort) toSort.invoke(impl, (Object) new cn.hutool.db.sql.Order[]{new cn.hutool.db.sql.Order("name", null)});

        List<Sort.Order> orders = sort.toList();
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getDirection()).isEqualTo(Sort.Direction.ASC);
        assertThat(orders.get(0).getProperty()).isEqualTo("name");
    }

    @Test
    void shouldKeepExplicitDescDirection() throws Exception
    {
        BaseRepositoryImpl<TestRepoEntity, Long> impl = new BaseRepositoryImpl<>(TestRepoEntity.class, entityManager);
        Method toSort = BaseRepositoryImpl.class.getDeclaredMethod("toSort", cn.hutool.db.sql.Order[].class);
        toSort.setAccessible(true);

        Sort sort = (Sort) toSort.invoke(impl, (Object) new cn.hutool.db.sql.Order[]{new cn.hutool.db.sql.Order("name", cn.hutool.db.sql.Direction.DESC)});

        List<Sort.Order> orders = sort.toList();
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getDirection()).isEqualTo(Sort.Direction.DESC);
    }
}
