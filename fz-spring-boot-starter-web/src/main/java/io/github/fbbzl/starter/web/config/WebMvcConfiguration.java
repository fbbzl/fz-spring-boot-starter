package io.github.fbbzl.starter.web.config;

import io.github.fbbzl.starter.web.advice.WebExceptionAdvice;
import io.github.fbbzl.starter.web.advice.WebRequestAdvice;
import io.github.fbbzl.starter.web.advice.WebResponseOperateAdvice;
import io.github.fbbzl.starter.web.advice.WebResponseWrapAdvice;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Import;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 4/12/2026 12:23
 */
@Import({
        WebExceptionAdvice.class,
        WebRequestAdvice.class,
        WebResponseOperateAdvice.class,
        WebResponseWrapAdvice.class
})
@AutoConfiguration
@ConditionalOnWebApplication
public class WebMvcConfiguration
{

}
