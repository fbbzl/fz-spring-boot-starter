package io.github.fbbzl.starter.webflux.codec;

import cn.crane4j.core.executor.AsyncBeanOperationExecutor;
import cn.crane4j.core.support.OperateTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.fbbzl.starter.webflux.Q;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@SuppressWarnings("unchecked")

/**
 * Tests for {@link Crane4jJsonDecoder}.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026/7/3
 */
@ExtendWith(MockitoExtension.class)
class Crane4jJsonDecoderTest
{

    @Mock
    private OperateTemplate              operateTemplate;
    @Mock
    private AsyncBeanOperationExecutor asyncBeanOperationExecutor;

    private Crane4jJsonDecoder decoder;

    @BeforeEach
    void setUp()
    {
        decoder = new Crane4jJsonDecoder(new ObjectMapper(), operateTemplate, asyncBeanOperationExecutor);
    }

    @Test
    void shouldAssembleQRequestData()
    {
        String json = """
                {
                  "data": {"name": "foo"},
                  "timestamp": 0
                }
                """;
        DataBuffer     buffer = new DefaultDataBufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
        ResolvableType type   = ResolvableType.forClassWithGenerics(Q.class, TestDto.class);

        Object result = decoder.decode(Flux.just(buffer), type, MediaType.APPLICATION_JSON, Map.of())
                               .single()
                               .block();

        assertThat(result).isInstanceOf(Q.class);
        Q<?> q = (Q<?>) result;
        assertThat(q.getData()).isInstanceOf(TestDto.class);
        assertThat(((TestDto) q.getData()).name()).isEqualTo("foo");
        verify(operateTemplate).execute(any(TestDto.class), eq(asyncBeanOperationExecutor), any());
    }

    @Test
    void shouldNotAssembleNonQBody()
    {
        String         json   = "{\"name\":\"bar\"}";
        DataBuffer     buffer = new DefaultDataBufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
        ResolvableType type   = ResolvableType.forClass(TestDto.class);

        Object result = decoder.decode(Flux.just(buffer), type, MediaType.APPLICATION_JSON, Map.of())
                               .single()
                               .block();

        assertThat(result).isInstanceOf(TestDto.class);
        verifyNoInteractions(operateTemplate);
    }

    @Test
    void shouldSkipAssemblyWhenQDataIsNull()
    {
        String json = """
                {
                  "data": null,
                  "timestamp": 0
                }
                """;
        DataBuffer     buffer = new DefaultDataBufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
        ResolvableType type   = ResolvableType.forClassWithGenerics(Q.class, TestDto.class);

        Object result = decoder.decode(Flux.just(buffer), type, MediaType.APPLICATION_JSON, Map.of())
                               .single()
                               .block();

        assertThat(result).isInstanceOf(Q.class);
        verifyNoInteractions(operateTemplate);
    }

    public record TestDto(String name) {}
}
