package io.github.fbbzl.starter.web.config;

import jakarta.validation.MessageInterpolator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.validation.ValidationConfigurationCustomizer;
import org.springframework.boot.validation.MessageInterpolatorFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.Locale;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/6/5
 */
@AutoConfiguration
@ConditionalOnClass(ValidationConfigurationCustomizer.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ValidationMessageConfiguration
{

    private static final String BASENAME = "io.github.fbbzl.starter.web.validation.ValidationMessages";

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ValidationConfigurationCustomizer webValidationMessageCustomizer(ApplicationContext applicationContext)
    {
        return configuration -> {
            MessageSource messageSource = new FallbackMessageSource(applicationContext, starterValidationMessageSource());
            MessageInterpolator messageInterpolator = new MessageInterpolatorFactory(messageSource).getObject();
            configuration.messageInterpolator(messageInterpolator);
        };
    }

    private static MessageSource starterValidationMessageSource()
    {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename(BASENAME);
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }

    private record FallbackMessageSource(MessageSource primary, MessageSource fallback) implements MessageSource
    {

        @Override
        public String getMessage(String code, Object[] args, String defaultMessage, Locale locale)
        {
            String primaryMessage = primary.getMessage(code, args, null, locale);
            if (hasMessage(code, primaryMessage)) return primaryMessage;

            String fallbackMessage = fallback.getMessage(code, args, null, locale);
            return hasMessage(code, fallbackMessage) ? fallbackMessage : defaultMessage;
        }

        @Override
        public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException
        {
            String message = getMessage(code, args, null, locale);
            if (message == null) throw new NoSuchMessageException(code, locale);
            return message;
        }

        @Override
        public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException
        {
            String[] codes = resolvable.getCodes();
            if (codes != null)
                for (String code : codes)
                {
                    String message = getMessage(code, resolvable.getArguments(), null, locale);
                    if (message != null) return message;
                }

            String defaultMessage = resolvable.getDefaultMessage();
            if (defaultMessage != null) return defaultMessage;

            throw new NoSuchMessageException(codes == null || codes.length == 0 ? null : codes[0], locale);
        }

        private static boolean hasMessage(String code, String message)
        {
            return message != null && !message.equals(code);
        }
    }
}
