package io.github.fbbzl.starter.pojo.validation;

import cn.hutool.extra.spring.SpringUtil;
import jakarta.validation.*;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.UnaryOperator;

import static java.util.stream.Collectors.joining;


/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/1/21 22:56
 */
@UtilityClass
public final class Validators
{
    private static final ValidatorFactory VALIDATOR_FACTORY;
    private static final Validator        VALIDATOR;

    static {
        VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();
        VALIDATOR         = SpringUtil.getBean(Validator.class);
    }

    public static <BEAN> Set<ConstraintViolation<BEAN>> validate(BEAN obj)
    {
        return VALIDATOR.validate(obj);
    }

    public static <BEAN> Set<ConstraintViolation<BEAN>> validate(BEAN obj, Class<?>... groups)
    {
        return VALIDATOR.validate(obj, groups);
    }

    public static <BEAN> void validateAndThrow(BEAN obj)
    {
        Set<ConstraintViolation<BEAN>> violations = validate(obj);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(collectErrorMessages(violations));
        }
    }

    public static <BEAN> void validateAndThrow(BEAN obj, Class<?>... groups)
    {
        Set<ConstraintViolation<BEAN>> violations = validate(obj, groups);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(collectErrorMessages(violations));
        }
    }

    public static <BEAN> void validateAndThrow(BEAN obj, UnaryOperator<String> messageFunction, Class<?>... groups)
    {
        Set<ConstraintViolation<BEAN>> violations = validate(obj, groups);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(messageFunction.apply(collectErrorMessages(violations)));
        }
    }

    public static <BEAN> void validateAndThrow(Iterable<BEAN> obs)
    {
        obs.forEach(Validators::validateAndThrow);
    }

    public static <BEAN> void validateAndThrow(Iterable<BEAN> obs, Class<?>... groups)
    {
        obs.forEach(obj -> validateAndThrow(obj, groups));
    }

    public static <BEAN> Set<ConstraintViolation<BEAN>> validateProperty(BEAN obj, String... properties)
    {
        ValidatorContext            context    = VALIDATOR_FACTORY.usingContext();
        Validator                   validator  = context.getValidator();
        Set<ConstraintViolation<BEAN>> violations = new HashSet<>(4);
        for (String property : properties) {
            violations.addAll(validator.validateProperty(obj, property));
        }
        return violations;
    }

    public static <BEAN> Set<ConstraintViolation<BEAN>> validateProperty(BEAN obj, Class<?>[] groups, String... properties)
    {
        ValidatorContext            context    = VALIDATOR_FACTORY.usingContext();
        Validator                   validator  = context.getValidator();
        Set<ConstraintViolation<BEAN>> violations = new HashSet<>(4);
        for (String property : properties) {
            violations.addAll(validator.validateProperty(obj, property, groups));
        }
        return violations;
    }

    public static <BEAN> Set<ConstraintViolation<BEAN>> validateValue(Class<BEAN> beanType, String propertyName, Object value, Class<?>... groups)
    {
        return VALIDATOR.validateValue(beanType, propertyName, value, groups);
    }

    public static <BEAN> void validateValueAndThrow(Class<BEAN> beanType, String propertyName, Object value, Class<?>... groups)
    {
        Set<ConstraintViolation<BEAN>> violations = validateValue(beanType, propertyName, value, groups);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(collectErrorMessages(violations));
        }
    }

    public static <BEAN> void validateValueAndThrow(Class<BEAN> beanType, String propertyName, Object value, UnaryOperator<String> messageFunction, Class<?>... groups)
    {
        Set<ConstraintViolation<BEAN>> violations = validateValue(beanType, propertyName, value, groups);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(messageFunction.apply(collectErrorMessages(violations)));
        }
    }

    static <BEAN> String collectErrorMessages(Collection<ConstraintViolation<BEAN>> violations)
    {
        return violations.stream()
                         .map(ConstraintViolation::getMessage)
                         .collect(joining(";"));
    }
}
