package io.github.fbbzl.starter.webflux.advice;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

class WebFluxExceptionAdviceResponseStatusTest
{

    private final WebTestClient webTestClient = WebTestClient.bindToController(new ResponseStatusExceptionTestController())
                                                             .controllerAdvice(new WebExceptionAdvice())
                                                             .build();

    @Test
    void shouldHandleResponseStatusExceptionWithCustomStatus()
    {
        webTestClient.get().uri("/test/response-status")
                     .exchange()
                     .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS)
                     .expectBody()
                     .jsonPath("$.code").isEqualTo("429")
                     .jsonPath("$.success").isEqualTo(false)
                     .jsonPath("$.message").isEqualTo("rate limit exceeded");
    }

    @Test
    void shouldHandleResponseStatusExceptionWithNonStandardStatus()
    {
        webTestClient.get().uri("/test/non-standard-status")
                     .exchange()
                     .expectStatus().isEqualTo(599)
                     .expectBody()
                     .jsonPath("$.code").isEqualTo("599")
                     .jsonPath("$.success").isEqualTo(false)
                     .jsonPath("$.message").isEqualTo("non-standard error");
    }

    @RestController
    static class ResponseStatusExceptionTestController
    {

        @GetMapping("/test/response-status")
        Object throwResponseStatus()
        {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "rate limit exceeded");
        }

        @GetMapping("/test/non-standard-status")
        Object throwNonStandardStatus()
        {
            throw new ResponseStatusException(HttpStatusCode.valueOf(599), "non-standard error");
        }
    }
}
