package com.fz.springboot.starter.web;

import jakarta.validation.*;
import lombok.experimental.UtilityClass;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/1/21 22:56
 */
@UtilityClass
public final class Validators {

    private static final Validator VALIDATOR;

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        VALIDATOR = factory.getValidator();
    }

    public static <T> Set<ConstraintViolation<T>> validate(T obj) {
        return VALIDATOR.validate(obj);
    }

    public static <T> Set<ConstraintViolation<T>> validate(T obj, Class<?>... groups) {
        return VALIDATOR.validate(obj, groups);
    }

    public static <T> void validateAndThrow(T obj) {
        Set<ConstraintViolation<T>> violations = validate(obj);
        if (!violations.isEmpty()) {
            String errorMsg = violations.stream()
                                        .map(ConstraintViolation::getMessage)
                                        .collect(Collectors.joining("；"));
            throw new IllegalArgumentException(errorMsg);
        }
    }

    public static <T> void validateAndThrow(T obj, Class<?>... groups) {
        Set<ConstraintViolation<T>> violations = validate(obj, groups);
        if (!violations.isEmpty()) {
            String errorMsg = violations.stream()
                                        .map(ConstraintViolation::getMessage)
                                        .collect(Collectors.joining("；"));
            throw new IllegalArgumentException(errorMsg);
        }
    }

    public static <T> void validateAndThrow(T obj, Function<String, String> messageFunction, Class<?>... groups) {
        Set<ConstraintViolation<T>> violations = validate(obj, groups);
        if (!violations.isEmpty()) {
            String errorMsg =
                    violations.stream()
                              .map(ConstraintViolation::getMessage)
                              .collect(Collectors.joining("；"));
            throw new IllegalArgumentException(messageFunction.apply(errorMsg));
        }
    }

    public static <T> void validateAndThrow(Iterable<T> objs) {
        objs.forEach(Validators::validateAndThrow);
    }

    public static <T> void validateAndThrow(Iterable<T> objs, Class<?>... groups) {
        objs.forEach(obj -> validateAndThrow(obj, groups));
    }

    public static <T> Set<ConstraintViolation<T>> validateProperty(T obj, String... properties) {
        ValidatorContext            context    = Validation.buildDefaultValidatorFactory().usingContext();
        Validator                   validator  = context.getValidator();
        Set<ConstraintViolation<T>> violations = new HashSet<>(4);
        for (String property : properties) {
            violations.addAll(validator.validateProperty(obj, property));
        }
        return violations;
    }

    public static <T> Set<ConstraintViolation<T>> validateProperty(T obj, Class<?>[] groups, String... properties) {
        ValidatorContext            context    = Validation.buildDefaultValidatorFactory().usingContext();
        Validator                   validator  = context.getValidator();
        Set<ConstraintViolation<T>> violations = new HashSet<>(4);
        for (String property : properties) {
            violations.addAll(validator.validateProperty(obj, property, groups));
        }
        return violations;
    }

    public static <T> Set<ConstraintViolation<T>> validateValue(Class<T> beanType, String propertyName, Object value, Class<?>... groups) {
        return VALIDATOR.validateValue(beanType, propertyName, value, groups);
    }

    public static <T> void validateValueAndThrow(Class<T> beanType, String propertyName, Object value, Class<?>... groups) {
        Set<ConstraintViolation<T>> violations = validateValue(beanType, propertyName, value, groups);
        if (!violations.isEmpty()) {
            String errorMsg = violations.stream()
                                        .map(ConstraintViolation::getMessage)
                                        .collect(Collectors.joining("；"));
            throw new IllegalArgumentException(errorMsg);
        }
    }

    public static <T> void validateValueAndThrow(Class<T> beanType, String propertyName, Object value, Function<String, String> messageFunction, Class<?>... groups) {
        Set<ConstraintViolation<T>> violations = validateValue(beanType, propertyName, value, groups);
        if (!violations.isEmpty()) {
            String errorMsg = violations.stream()
                                        .map(ConstraintViolation::getMessage)
                                        .collect(Collectors.joining("；"));
            throw new IllegalArgumentException(messageFunction.apply(errorMsg));
        }
    }
}
