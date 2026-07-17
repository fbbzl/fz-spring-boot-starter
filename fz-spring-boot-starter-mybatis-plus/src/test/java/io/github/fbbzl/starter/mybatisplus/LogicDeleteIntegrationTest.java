package io.github.fbbzl.starter.mybatisplus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for timestamp-based logical delete using a real MySQL instance.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026/7/16
 */
@SpringBootTest(classes = MybatisPlusTestApplication.class)
@ActiveProfiles("test")
@Sql(scripts = "/cleanup-test-entity.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class LogicDeleteIntegrationTest
{

    @Autowired
    private TestEntityMapper mapper;

    @Autowired
    private JdbcTemplate     jdbcTemplate;

    @Test
    void shouldInsertWithoutDeletedAt()
    {
        TestEntity entity = new TestEntity();
        entity.setName("alice");

        mapper.create(entity);

        assertThat(entity.getId()).isNotNull();
        assertThat(entity.getDeletedAt()).isNull();

        TestEntity selected = mapper.byId(entity.getId());
        assertThat(selected).isNotNull();
        assertThat(selected.getName()).isEqualTo("alice");
        assertThat(selected.getDeletedAt()).isNull();
    }

    @Test
    void shouldSoftDeleteById()
    {
        TestEntity entity = new TestEntity();
        entity.setName("bob");
        mapper.create(entity);
        Long id = entity.getId();

        mapper.delete(id);

        assertThat(mapper.byId(id)).isNull();

        Boolean physicallyExists = jdbcTemplate.queryForObject(
                "select count(*) > 0 from test_entity where id = ?",
                Boolean.class, id);
        assertThat(physicallyExists).isTrue();

        java.time.LocalDateTime deletedAt = jdbcTemplate.queryForObject(
                "select deleted_at from test_entity where id = ?",
                java.time.LocalDateTime.class, id);
        assertThat(deletedAt).isNotNull();
    }

    @Test
    void shouldFilterDeletedRecordsOnSelect()
    {
        TestEntity alive = new TestEntity();
        alive.setName("alive");
        mapper.create(alive);

        TestEntity deleted = new TestEntity();
        deleted.setName("deleted");
        mapper.create(deleted);
        mapper.delete(deleted.getId());

        List<TestEntity> all = mapper.selectList(null);

        assertThat(all).hasSize(1);
        assertThat(all.get(0).getName()).isEqualTo("alive");
    }

    @Test
    void shouldOnlyDeleteOnce()
    {
        TestEntity entity = new TestEntity();
        entity.setName("once");
        mapper.create(entity);
        Long id = entity.getId();

        mapper.delete(id);
        int affectedRows = mapper.deleteById(id);

        assertThat(affectedRows).isZero();
    }
}
