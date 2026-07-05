package io.github.fbbzl.starter.webflux.codec;

import cn.crane4j.core.executor.AsyncBeanOperationExecutor;
import cn.crane4j.core.support.Grouped;
import cn.crane4j.core.support.OperateTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.fbbzl.starter.webflux.Q;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.AbstractDecoder;
import org.springframework.core.codec.Decoder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

/**
 * Jackson JSON decoder wrapper that runs crane4j assembly on deserialized
 * {@link Q} request wrappers without blocking the Netty event loop.
 *
 * <p>By replacing the default Jackson decoder, every JSON request body is
 * decoded normally; when the root object is a {@code Q} and carries data, the
 * data object is assembled by crane4j on {@code Schedulers.boundedElastic()}.
 * This avoids the ordering problem of custom {@code HandlerMethodArgumentResolver}
 * beans, which are always evaluated after the built-in {@code @RequestBody}
 * resolver.</p>
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026/7/3
 */
public class Crane4jJsonDecoder extends AbstractDecoder<Object>
{

    private final Decoder<Object>              delegate;
    private final OperateTemplate              operateTemplate;
    private final AsyncBeanOperationExecutor asyncBeanOperationExecutor;

    public Crane4jJsonDecoder(
            ObjectMapper objectMapper,
            OperateTemplate operateTemplate,
            AsyncBeanOperationExecutor asyncBeanOperationExecutor)
    {
        super(MediaType.APPLICATION_JSON, new MediaType("application", "*+json"));
        this.delegate = new Jackson2JsonDecoder(objectMapper);
        this.operateTemplate = operateTemplate;
        this.asyncBeanOperationExecutor = asyncBeanOperationExecutor;
    }

    @Override
    public boolean canDecode(ResolvableType elementType, MimeType mimeType)
    {
        return delegate.canDecode(elementType, mimeType);
    }

    @Override
    public Flux<Object> decode(
            org.reactivestreams.Publisher<DataBuffer> inputStream,
            ResolvableType elementType,
            MimeType mimeType,
            Map<String, Object> hints)
    {
        return delegate.decode(inputStream, elementType, mimeType, hints)
                       .flatMap(this::assembleIfNeeded);
    }

    private Mono<Object> assembleIfNeeded(Object body)
    {
        if (body instanceof Q<?> q && q.getData() != null) {
            return Mono.fromRunnable(() ->
                            operateTemplate.execute(q.getData(), asyncBeanOperationExecutor, Grouped.alwaysMatch()))
                       .subscribeOn(Schedulers.boundedElastic())
                       .thenReturn(q);
        }
        return Mono.just(body);
    }
}
