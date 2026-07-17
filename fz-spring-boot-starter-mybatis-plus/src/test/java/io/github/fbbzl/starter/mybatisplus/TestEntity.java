package io.github.fbbzl.starter.mybatisplus;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PRIVATE;

/**
 * Test entity for MyBatis-Plus soft delete integration tests.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026/7/16
 */
@Data
@FieldDefaults(level = PRIVATE)
@EqualsAndHashCode(callSuper = true)
@TableName("test_entity")
public class TestEntity extends BaseMybatisPlusEntity<Long>
{

    String name;
}
