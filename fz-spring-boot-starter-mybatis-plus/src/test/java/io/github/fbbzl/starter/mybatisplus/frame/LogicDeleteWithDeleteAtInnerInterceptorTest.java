package io.github.fbbzl.starter.mybatisplus.frame;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link LogicDeleteWithDeleteAtInnerInterceptor}.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026/7/16
 */
class LogicDeleteWithDeleteAtInnerInterceptorTest
{

    private final LogicDeleteWithDeleteAtInnerInterceptor interceptor = new LogicDeleteWithDeleteAtInnerInterceptor();

    @Test
    void shouldConvertNowFunctionToParameter()
    {
        String original = "UPDATE test_entity SET deleted_at = now() WHERE id = ? AND deleted_at = null";
        LogicDeleteWithDeleteAtInnerInterceptor.Context context = new LogicDeleteWithDeleteAtInnerInterceptor.Context();

        String parsed = interceptor.parserMulti(original, context);

        assertThat(parsed).isEqualTo("UPDATE test_entity SET deleted_at = ? WHERE id = ? AND deleted_at IS NULL");
        assertThat(context.value).isNotNull();
        assertThat(context.parameterIndex).isZero();
    }

    @Test
    void shouldConvertQuotedNowLiteralToParameter()
    {
        String original = "UPDATE test_entity SET deleted_at = 'now()' WHERE id = ? AND deleted_at = 'null'";
        LogicDeleteWithDeleteAtInnerInterceptor.Context context = new LogicDeleteWithDeleteAtInnerInterceptor.Context();

        String parsed = interceptor.parserMulti(original, context);

        assertThat(parsed).isEqualTo("UPDATE test_entity SET deleted_at = ? WHERE id = ? AND deleted_at IS NULL");
        assertThat(context.value).isNotNull();
    }

    @Test
    void shouldConvertNullStringInSelect()
    {
        String original = "SELECT id, name FROM test_entity WHERE deleted_at = 'null' AND name = ?";
        LogicDeleteWithDeleteAtInnerInterceptor.Context context = new LogicDeleteWithDeleteAtInnerInterceptor.Context();

        String parsed = interceptor.parserMulti(original, context);

        assertThat(parsed).isEqualTo("SELECT id, name FROM test_entity WHERE deleted_at IS NULL AND name = ?");
        assertThat(context.value).isNull();
    }

    @Test
    void shouldKeepParameterIndexForMultiColumnUpdate()
    {
        String original = "UPDATE test_entity SET updated_at = now(), deleted_at = 'now()' WHERE id = ?";
        LogicDeleteWithDeleteAtInnerInterceptor.Context context = new LogicDeleteWithDeleteAtInnerInterceptor.Context();

        String parsed = interceptor.parserMulti(original, context);

        assertThat(parsed).isEqualTo("UPDATE test_entity SET updated_at = now(), deleted_at = ? WHERE id = ?");
        assertThat(context.value).isNotNull();
        assertThat(context.parameterIndex).isZero();
    }

    @Test
    void shouldNotTouchOtherColumns()
    {
        String original = "UPDATE test_entity SET name = ? WHERE id = ?";
        LogicDeleteWithDeleteAtInnerInterceptor.Context context = new LogicDeleteWithDeleteAtInnerInterceptor.Context();

        String parsed = interceptor.parserMulti(original, context);

        assertThat(parsed).isEqualTo(original);
        assertThat(context.value).isNull();
    }
}
