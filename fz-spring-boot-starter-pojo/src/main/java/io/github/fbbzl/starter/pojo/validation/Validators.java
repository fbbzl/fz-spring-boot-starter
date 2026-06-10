package io.github.fbbzl.starter.pojo.validation;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.extra.spring.SpringUtil;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;

import static java.util.stream.Collectors.joining;


/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/1/21 22:56
 */
@Slf4j
@UtilityClass
public final class Validators
{
    private static final ValidatorFactory VALIDATOR_FACTORY;
    private static final Validator        VALIDATOR;

    static {
        VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try { VALIDATOR_FACTORY.close(); }
            catch (Exception e) { log.warn("Failed to close ValidatorFactory", e); }
        }));
        Validator validator;
        try {
            validator = SpringUtil.getBean(Validator.class);
        } catch (RuntimeException e) {
            validator = VALIDATOR_FACTORY.getValidator();
        }
        VALIDATOR = validator;
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

    public static <BEAN> Set<ConstraintViolation<BEAN>> validateNonNullProperty(BEAN obj, Class<?>... groups)
    {
        Map<String, Object> beanMap = BeanUtil.beanToMap(obj, false, true);
        return validateProperty(obj, groups, beanMap.keySet().toArray(String[]::new));
    }

    public static <BEAN> void validateNonNullPropertyAndThrow(BEAN obj, Class<?>... groups)
    {
        Set<ConstraintViolation<BEAN>> violations = validateNonNullProperty(obj, groups);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(collectErrorMessages(violations));
        }
    }

    public static <BEAN> Set<ConstraintViolation<BEAN>> validateProperty(BEAN obj, String... properties)
    {
        Set<ConstraintViolation<BEAN>> violations = HashSet.newHashSet(4);
        for (String property : properties) {
            violations.addAll(VALIDATOR.validateProperty(obj, property));
        }
        return violations;
    }

    public static <BEAN> Set<ConstraintViolation<BEAN>> validateProperty(BEAN obj, Class<?>[] groups, String... properties)
    {
        Set<ConstraintViolation<BEAN>> violations = HashSet.newHashSet(4);
        for (String property : properties) {
            violations.addAll(VALIDATOR.validateProperty(obj, property, groups));
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
