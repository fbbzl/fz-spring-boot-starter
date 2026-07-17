package io.github.fbbzl.starter.web.advice;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.fbbzl.starter.web.R;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WebResponseWrapAdviceTest
{

    private MockMvc mockMvc;

    @BeforeEach
    void setUp()
    {
        GenericApplicationContext context = new GenericApplicationContext();
        context.refresh();
        AutoConfigurationPackages.register(context, WrapTestController.class.getPackageName());

        WebResponseWrapAdvice advice = new WebResponseWrapAdvice(new ObjectMapper());
        advice.applicationContext = context;

        mockMvc = MockMvcBuilders.standaloneSetup(new WrapTestController())
                                 .setControllerAdvice(advice)
                                 .build();
    }

    @Test
    void shouldWrapOkResponse() throws Exception
    {
        mockMvc.perform(get("/test/ok"))
               .andExpect(status().isOk())
               .andExpect(content().json("{\"code\":\"200\",\"success\":true,\"message\":\"ok\",\"data\":\"hello\"}"));
    }

    @Test
    void shouldNotWrapErrorResponse() throws Exception
    {
        mockMvc.perform(get("/test/error"))
               .andExpect(status().isBadRequest())
               .andExpect(content().string(""));
    }

    @Test
    void shouldNotWrapNonStandardErrorResponse() throws Exception
    {
        mockMvc.perform(get("/test/non-standard"))
               .andExpect(status().is(599))
               .andExpect(content().string(""));
    }

    @RestController
    static class WrapTestController
    {

        @GetMapping("/test/ok")
        R<String> ok()
        {
            return R.ok("hello");
        }

        @GetMapping("/test/error")
        String error()
        {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "raw error body");
        }

        @GetMapping("/test/non-standard")
        String nonStandard()
        {
            throw new ResponseStatusException(HttpStatusCode.valueOf(599), "non-standard body");
        }
    }
}
