package io.github.fbbzl.starter.web.annotation.exchange;

import io.github.fbbzl.starter.core.exception.BizException;
import io.github.fbbzl.starter.core.exception.ExceptionVerb;
import io.github.fbbzl.starter.web.annotation.exchange.HttpExchangeClient.Retry.RetryInterceptor;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.lang.NonNull;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static cn.hutool.core.convert.Convert.toStr;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/5/12 18:00
 */
@Target(ElementType.TYPE)
@Retention(RUNTIME)
@Documented
public @interface HttpExchangeClient
{
    String baseUrl();

    String connectTimeout() default "5s";

    String readTimeout() default "10s";

    Header[] defaultHeaders() default {};

    Retry retry() default @Retry;

    interface Helper
    {
        static RestClient buildRestClient(HttpExchangeClient annotation, ConfigurableListableBeanFactory beanFactory, Environment environment)
        {
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout((int) resolveDuration(annotation.connectTimeout(), beanFactory, environment).toMillis());
            requestFactory.setReadTimeout((int) resolveDuration(annotation.readTimeout(), beanFactory, environment).toMillis());

            RestClient.Builder builder =
                    beanFactory.getBeanProvider(RestClient.Builder.class)
                               .getIfAvailable(RestClient::builder)
                               .clone()
                               .requestFactory(requestFactory)
                               .baseUrl(resolveString(annotation.baseUrl(), beanFactory, environment))
                               .defaultHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                               .defaultStatusHandler(HttpStatusCode::is5xxServerError,
                                                     (request, response) -> {
                                                         throw new BizException(ExceptionVerb.ILLEGAL_STATE, response.getStatusCode().toString());
                                                     });
            setDefaultHeaders(builder, annotation.defaultHeaders(), beanFactory, environment);

            Retry retry = annotation.retry();
            if (retry.maxAttempts() > 0) builder.requestInterceptor(retryInterceptor(retry, beanFactory, environment));

            return builder.build();
        }

        static void setDefaultHeaders(RestClient.Builder builder, Header[] defaultHeaders, ConfigurableListableBeanFactory beanFactory, Environment environment)
        {
            for (Header defaultHeader : defaultHeaders) {
                String   header       = resolveString(defaultHeader.header(), beanFactory, environment);
                String[] headerValues = new String[defaultHeader.headerValue().length];
                for (int i = 0; i < defaultHeader.headerValue().length; i++)
                    headerValues[i] = resolveString(defaultHeader.headerValue()[i], beanFactory, environment);

                builder.defaultHeader(header, headerValues);
            }
        }

        static ClientHttpRequestInterceptor retryInterceptor(Retry retry, ConfigurableListableBeanFactory beanFactory, Environment environment)
        {
            RetryTemplate retryTemplate =
                    RetryTemplate.builder()
                                 .maxAttempts(retry.maxAttempts())
                                 .fixedBackoff(resolveDuration(retry.backoff(), beanFactory, environment))
                                 .retryOn(List.of(ResourceAccessException.class))
                                 .build();

            return new RetryInterceptor(retryTemplate);
        }

        static Duration resolveDuration(String value, ConfigurableListableBeanFactory beanFactory, Environment environment)
        {
            Object resolved = resolveValue(value, beanFactory, environment);
            if (resolved instanceof Duration duration) return duration;
            return DurationStyle.detectAndParse(toStr(resolved));
        }

        static String resolveString(String value, ConfigurableListableBeanFactory beanFactory, Environment environment)
        {
            return toStr(resolveValue(value, beanFactory, environment));
        }

        static Object resolveValue(String value, ConfigurableListableBeanFactory beanFactory, Environment environment)
        {
            String                 resolved = environment.resolveRequiredPlaceholders(value);
            BeanExpressionResolver resolver = beanFactory.getBeanExpressionResolver();
            if (resolver == null) return resolved;
            return resolver.evaluate(resolved, new BeanExpressionContext(beanFactory, null));
        }
    }

    /**
     * Default HTTP header for an exchange client.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026/5/12 21:15
     */
    @Target({})
    @Retention(RUNTIME)
    @Documented
    @interface Header
    {
        String header();

        String[] headerValue();
    }

    /**
     * @author fengbinbin
     * @version 1.0
     * @since 2026/5/12 18:00
     */
    @Target({})
    @Retention(RUNTIME)
    @Documented
    @interface Retry
    {
        int maxAttempts() default -1;

        String backoff() default "1s";

        @Slf4j
        @RequiredArgsConstructor
        @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
        class RetryInterceptor implements ClientHttpRequestInterceptor
        {
            RetryTemplate retryTemplate;

            @NonNull
            @Override
            public ClientHttpResponse intercept(HttpRequest request, @NonNull byte[] body, @NonNull ClientHttpRequestExecution execution) throws IOException
            {
                ClientHttpResponse response = retryTemplate.execute(context -> {
                    log.info("Sending Request: {} {}, RetryCount: {}", request.getMethod(), request.getURI(), context.getRetryCount());
                    return execution.execute(request, body);
                });
                log.info("Received Response: {} -> {}", request.getURI(), response.getStatusCode());
                if (log.isDebugEnabled()) return logResponseBody(response);
                return response;
            }

            private ClientHttpResponse logResponseBody(ClientHttpResponse response) throws IOException
            {
                byte[] responseBody = response.getBody().readAllBytes();
                log.debug("Received Response Body: {}", new String(responseBody, getCharset(response)));
                return new RepeatableBodyClientHttpResponse(response, responseBody);
            }

            private Charset getCharset(ClientHttpResponse response)
            {
                MediaType contentType = response.getHeaders().getContentType();
                if (contentType == null || contentType.getCharset() == null) return StandardCharsets.UTF_8;
                return contentType.getCharset();
            }

            private interface RawStatusCode
            {
                int getRawStatusCode() throws IOException;
            }

            private record RepeatableBodyClientHttpResponse(
                    @Delegate(excludes = RawStatusCode.class) ClientHttpResponse delegate,
                    byte[] body)
                    implements ClientHttpResponse
            {
                @NonNull
                @Override
                public InputStream getBody()
                {
                    return new ByteArrayInputStream(body);
                }

                @Override
                public boolean equals(Object that)
                {
                    if (this == that) return true;
                    return that instanceof RepeatableBodyClientHttpResponse(ClientHttpResponse thatDelegate, byte[] thatBody)
                           && Objects.equals(delegate, thatDelegate)
                           && Arrays.equals(body, thatBody);
                }

                @Override
                public int hashCode()
                {
                    return 31 * Objects.hashCode(delegate) + Arrays.hashCode(body);
                }

                @NonNull
                @Override
                public String toString()
                {
                    return "RepeatableBodyClientHttpResponse[delegate=" + delegate
                           + ", bodyLength=" + body.length
                           + ", bodyHash=" + Arrays.hashCode(body)
                           + ']';
                }
            }
        }
    }
}
