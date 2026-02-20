package com.fz.springboot.starter.web.annotation.validation;

import com.fz.springboot.starter.web.annotation.validation.Excel.ExcelValidator;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotNull;
import org.fz.erwin.exception.Throws;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static cn.hutool.core.text.CharSequenceUtil.endWithAnyIgnoreCase;

/**
 * Excel validation
 *
 * @author fengbinbin
 * @version 1.0
 * @since 4/1/2023
 */

@Target( {ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ExcelValidator.class)
public @interface Excel {

    String message() default "not a valid excel";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Component
    class ExcelValidator implements ConstraintValidator<Excel, MultipartFile> {
        @Override
        public boolean isValid(@NotNull MultipartFile file, ConstraintValidatorContext context) {
            try {
                Throws.ifNull(file, () -> "Uploaded file is null");
                Throws.ifTrue(file.isEmpty(), () -> "Uploaded file is empty, 0 bytes");
                Throws.ifFalse(endWithAnyIgnoreCase(file.getOriginalFilename(), ".xlsx", ".xls"), () -> "Only Excel files in .xlsx/.xls format are supported");
            }
            catch (IllegalArgumentException illegalArg) {
                resetConstraintViolationWithTemplate(context, illegalArg.getMessage());
                return false;
            }
            catch (Exception unKnown) {
                resetConstraintViolationWithTemplate(context, "unknown exception occur while do message-config validation");
                return false;
            }
            return true;
        }

        private void resetConstraintViolationWithTemplate(ConstraintValidatorContext context, String messageTemplate) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(messageTemplate).addConstraintViolation();
        }
    }

}
