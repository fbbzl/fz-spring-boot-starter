package io.github.fbbzl.starter.core.exception;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

import static cn.hutool.core.text.CharSequenceUtil.format;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/22 20:24
 */
@Getter
public class BizException extends RuntimeException
{

    final ExceptionVerb verb;

    public BizException(ExceptionVerb verb, String template, Object... params)
    {
        super(format(template, params));
        this.verb = verb;
    }

    public BizException(ExceptionVerb verb, Object message, Throwable cause)
    {
        super(ObjectUtil.toString(message), cause);
        this.verb = verb;
    }

}