package com.fz.starter.web.config;

import com.fz.starter.web.advice.WebExceptionAdvice;
import com.fz.starter.web.advice.WebRequestAdvice;
import com.fz.starter.web.advice.WebResponseAdvice;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 4/12/2026 12:23
 */
@Import({
        WebExceptionAdvice.class,
        WebRequestAdvice.class,
        WebResponseAdvice.class
})
@AutoConfiguration
public class WebMvcConfiguration {}
