package io.github.fbbzl.starter.webflux.annotation.validation;

import io.github.fbbzl.starter.webflux.annotation.validation.Excel.ExcelValidator;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotNull;
import io.github.fbbzl.starter.core.util.Throws;
import org.springframework.http.codec.multipart.FilePart;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;

import static cn.hutool.core.io.FileMagicNumber.XLS;
import static cn.hutool.core.io.FileMagicNumber.XLSX;
import static cn.hutool.core.text.CharSequenceUtil.endWithAnyIgnoreCase;

/**
 * Excel validation
 *
 * @author fengbinbin
 * @version 1.0
 * @since 4/1/2023
 */

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ExcelValidator.class)
public @interface Excel
{

    String message() default "not a valid excel";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class ExcelValidator implements ConstraintValidator<Excel, FilePart>
    {

        static final String[] excelExtensions = {XLSX.getExtension(), XLS.getExtension()};

        @Override
        public boolean isValid(@NotNull FilePart file, ConstraintValidatorContext context)
        {
            try {
                Throws.ifNull(file, "Uploaded file is null");
                Throws.ifFalse(endWithAnyIgnoreCase(file.filename(), excelExtensions),
                               "Only Excel files in {} format are supported", Arrays.toString(excelExtensions));
            }
            catch (IllegalArgumentException illegalArg) {
                this.resetConstraintViolationWithTemplate(context, illegalArg.getMessage());
                return false;
            }
            catch (Exception unKnown) {
                this.resetConstraintViolationWithTemplate(context, "unknown exception occur while do message-config validation");
                return false;
            }
            return true;
        }

        private void resetConstraintViolationWithTemplate(ConstraintValidatorContext context, String messageTemplate)
        {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(messageTemplate).addConstraintViolation();
        }
    }

}
