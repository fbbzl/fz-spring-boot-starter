package io.github.fbbzl.starter.messages;

import cn.hutool.extra.spring.SpringUtil;
import lombok.experimental.UtilityClass;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * message resource util
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026/2/14 14:10
 */
@UtilityClass
public class M
{

    public static String of(String code, Object... args)
    {
        MessageSource messageSource = SpringUtil.getBean(MessageSource.class);
        if (messageSource == null) return code;
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }
}
