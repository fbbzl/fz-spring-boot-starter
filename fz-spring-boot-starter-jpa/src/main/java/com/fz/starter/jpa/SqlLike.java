package com.fz.starter.jpa;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;

import static java.lang.String.format;

/**
 * The enum Sql like.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/22 15:30
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public enum SqlLike {

    /**
     * Left sql like.
     */
    LEFT("%%%s"),
    /**
     * Right sql like.
     */
    RIGHT("%s%%"),
    /**
     * Default sql like.
     */
    DEFAULT("%%%s%%"),
    ;

    String pattern;

    /**
     * Left string.
     *
     * @param str the str
     * @return the string
     */
    public static String left(Object str) {
        return format(LEFT.pattern, str);
    }

    /**
     * Right string.
     *
     * @param str the str
     * @return the string
     */
    public static String right(Object str) {
        return format(RIGHT.pattern, str);
    }

    /**
     * Contain string.
     *
     * @param str the str
     * @return the string
     */
    public static String contain(Object str) {
        return format(DEFAULT.pattern, str);
    }

}