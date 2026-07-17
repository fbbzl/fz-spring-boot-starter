package io.github.fbbzl.starter.mybatisplus;

import org.apache.ibatis.annotations.Mapper;

/**
 * Test mapper for {@link TestEntity}.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026/7/16
 */
@Mapper
public interface TestEntityMapper extends BaseMybatisPlusMapper<TestEntity, Long>
{
}
