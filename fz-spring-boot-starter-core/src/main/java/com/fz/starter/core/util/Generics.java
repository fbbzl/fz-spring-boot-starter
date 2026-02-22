package com.fz.starter.core.util;

import lombok.experimental.UtilityClass;
import org.springframework.core.ResolvableType;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/2/6 22:35
 */

@UtilityClass
public class Generics {

    @SuppressWarnings("unchecked")
    public <C> Class<C> getGenericType(Class<?> currentClass, Class<?> rootSuperClass, int genericIndex) {
        ResolvableType currentType = ResolvableType.forClass(currentClass);

        while (true) {
            currentType.getSuperType();
            ResolvableType superType = currentType.getSuperType();

            if (superType.getRawClass() == rootSuperClass) {
                ResolvableType entityType = superType.getGeneric(genericIndex);

                return (Class<C>) entityType.resolve();
                // break if found rootSuperClass is super class
            }

            currentType = superType;
        }
    }
}
