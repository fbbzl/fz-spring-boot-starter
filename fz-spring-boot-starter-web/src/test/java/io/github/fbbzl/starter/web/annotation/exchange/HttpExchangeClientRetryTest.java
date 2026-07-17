package io.github.fbbzl.starter.web.annotation.exchange;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResourceAccessException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class HttpExchangeClientRetryTest
{

    @Test
    void shouldNotRetryOn4xxClientError() throws IOException
    {
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        ClientHttpResponse         response  = mock(ClientHttpResponse.class);
        when(response.getStatusCode()).thenReturn(HttpStatusCode.valueOf(400));
        when(response.getBody()).thenReturn(new ByteArrayInputStream("bad request".getBytes(StandardCharsets.UTF_8)));
        when(execution.execute(any(), any())).thenReturn(response);

        var interceptor = HttpExchangeClient.Helper.retryInterceptor(retryAnnotation(3, "1ms"), beanFactory(), emptyEnvironment());

        ClientHttpResponse result = interceptor.intercept(request(), body(), execution);
        assertThat(result.getStatusCode().value()).isEqualTo(400);

        verify(execution, times(1)).execute(any(), any());
    }

    @Test
    void shouldRetryOnResourceAccessException() throws IOException
    {
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        ClientHttpResponse         response  = mock(ClientHttpResponse.class);
        when(response.getStatusCode()).thenReturn(HttpStatusCode.valueOf(200));
        when(response.getBody()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(execution.execute(any(), any()))
                .thenThrow(new ResourceAccessException("connection refused"))
                .thenReturn(response);

        var interceptor = HttpExchangeClient.Helper.retryInterceptor(retryAnnotation(3, "1ms"), beanFactory(), emptyEnvironment());

        ClientHttpResponse result = interceptor.intercept(request(), body(), execution);
        assertThat(result.getStatusCode().value()).isEqualTo(200);

        verify(execution, times(2)).execute(any(), any());
    }

    private static ConfigurableListableBeanFactory beanFactory()
    {
        return mock(ConfigurableListableBeanFactory.class);
    }

    private static HttpExchangeClient.Retry retryAnnotation(int maxAttempts, String backoff)
    {
        return (HttpExchangeClient.Retry) Proxy.newProxyInstance(
                HttpExchangeClientRetryTest.class.getClassLoader(),
                new Class[]{HttpExchangeClient.Retry.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "maxAttempts" -> maxAttempts;
                    case "backoff" -> backoff;
                    case "annotationType" -> HttpExchangeClient.Retry.class;
                    case "toString" -> "@Retry";
                    case "hashCode" -> 0;
                    case "equals" -> proxy == args[0];
                    default -> method.getDefaultValue();
                });
    }

    private static Environment emptyEnvironment()
    {
        return (Environment) Proxy.newProxyInstance(
                HttpExchangeClientRetryTest.class.getClassLoader(),
                new Class[]{Environment.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "resolveRequiredPlaceholders" -> args[0];
                    case "getProperty", "getRequiredProperty" -> null;
                    case "containsProperty" -> false;
                    default -> method.getDefaultValue();
                });
    }

    private static ClientHttpRequest request()
    {
        ClientHttpRequest request = mock(ClientHttpRequest.class);
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(request.getURI()).thenReturn(URI.create("http://localhost/test"));
        return request;
    }

    private static byte[] body()
    {
        return new byte[0];
    }
}
