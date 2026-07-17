package io.github.fbbzl.starter.web.advice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WebExceptionAdviceResponseStatusTest
{

    private MockMvc mockMvc;

    @BeforeEach
    void setUp()
    {
        mockMvc = MockMvcBuilders.standaloneSetup(new ResponseStatusExceptionTestController())
                                 .setControllerAdvice(new WebExceptionAdvice())
                                 .build();
    }

    @Test
    void shouldHandleResponseStatusExceptionWithCustomStatus() throws Exception
    {
        mockMvc.perform(get("/test/response-status"))
               .andExpect(status().isTooManyRequests())
               .andExpect(jsonPath("$.code").value("429"))
               .andExpect(jsonPath("$.success").value(false))
               .andExpect(jsonPath("$.message").value("rate limit exceeded"));
    }

    @Test
    void shouldHandleResponseStatusExceptionWithNonStandardStatus() throws Exception
    {
        mockMvc.perform(get("/test/non-standard-status"))
               .andExpect(status().is(599))
               .andExpect(jsonPath("$.code").value("599"))
               .andExpect(jsonPath("$.success").value(false))
               .andExpect(jsonPath("$.message").value("non-standard error"));
    }

    @Test
    void shouldHandleIllegalArgumentExceptionAsBadRequest() throws Exception
    {
        mockMvc.perform(get("/test/illegal-argument"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.code").value("400"))
               .andExpect(jsonPath("$.success").value(false))
               .andExpect(jsonPath("$.message").value(containsString("bad input")));
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

        @GetMapping("/test/illegal-argument")
        Object throwIllegalArgument()
        {
            throw new IllegalArgumentException("bad input");
        }
    }
}
