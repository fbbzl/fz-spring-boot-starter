package io.github.fbbzl.starter.webflux.annotation.exchange;

import io.github.fbbzl.starter.core.exception.BizException;
import io.github.fbbzl.starter.core.exception.ExceptionVerb;
import io.netty.channel.ChannelOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.net.ConnectException;
import java.time.Duration;
import java.util.List;

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
        Logger LOG = LoggerFactory.getLogger(HttpExchangeClient.class);

        static WebClient buildWebClient(HttpExchangeClient annotation, ConfigurableListableBeanFactory beanFactory, Environment environment)
        {
            Duration connectTimeout = resolveDuration(annotation.connectTimeout(), beanFactory, environment);
            Duration readTimeout    = resolveDuration(annotation.readTimeout(), beanFactory, environment);
            HttpClient httpClient = HttpClient.create()
                                              .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Math.toIntExact(connectTimeout.toMillis()))
                                              .responseTimeout(readTimeout);

            WebClient.Builder builder =
                    beanFactory.getBeanProvider(WebClient.Builder.class)
                               .getIfAvailable(WebClient::builder)
                               .clone()
                               .clientConnector(new ReactorClientHttpConnector(httpClient))
                               .baseUrl(resolveString(annotation.baseUrl(), beanFactory, environment))
                               .defaultHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            setDefaultHeaders(builder, annotation.defaultHeaders(), beanFactory, environment);

            Retry retry = annotation.retry();
            if (retry.maxAttempts() > 0) builder.filter(retryFilter(retry, beanFactory, environment));
            builder.filter(serverErrorFilter());

            return builder.build();
        }

        static void setDefaultHeaders(WebClient.Builder builder, Header[] defaultHeaders, ConfigurableListableBeanFactory beanFactory, Environment environment)
        {
            for (Header defaultHeader : defaultHeaders) {
                String   header       = resolveString(defaultHeader.header(), beanFactory, environment);
                String[] headerValues = new String[defaultHeader.headerValue().length];
                for (int i = 0; i < defaultHeader.headerValue().length; i++)
                    headerValues[i] = resolveString(defaultHeader.headerValue()[i], beanFactory, environment);

                builder.defaultHeader(header, headerValues);
            }
        }

        static ExchangeFilterFunction retryFilter(Retry retry, ConfigurableListableBeanFactory beanFactory, Environment environment)
        {
            Duration backoff = resolveDuration(retry.backoff(), beanFactory, environment);
            return (request, next) -> next.exchange(request)
                                         .retryWhen(reactor.util.retry.Retry.fixedDelay(Math.max(retry.maxAttempts() - 1, 0), backoff)
                                                                           .filter(Helper::retryable)
                                                                           .doBeforeRetry(signal -> LOG.info("Sending Request: {} {}, RetryCount: {}",
                                                                                                             request.method(),
                                                                                                             request.url(),
                                                                                                             signal.totalRetries())));
        }

        static ExchangeFilterFunction serverErrorFilter()
        {
            return ExchangeFilterFunction.ofResponseProcessor(response -> {
                if (!response.statusCode().is5xxServerError()) {
                    return Mono.just(response);
                }

                HttpStatusCode statusCode = response.statusCode();
                return response.bodyToMono(String.class)
                               .defaultIfEmpty(statusCode.toString())
                               .flatMap(message -> Mono.error(new BizException(ExceptionVerb.ILLEGAL_STATE, message)));
            });
        }

        static boolean retryable(Throwable throwable)
        {
            return List.of(ConnectException.class).stream().anyMatch(type -> type.isInstance(throwable))
                   || throwable instanceof java.util.concurrent.TimeoutException
                   || throwable instanceof org.springframework.web.reactive.function.client.WebClientRequestException
                   || throwable instanceof BizException;
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
    }
}
