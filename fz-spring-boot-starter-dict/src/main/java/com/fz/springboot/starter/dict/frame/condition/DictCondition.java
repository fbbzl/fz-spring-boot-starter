package com.fz.springboot.starter.dict.frame.condition;

import cn.hutool.core.util.ArrayUtil;
import com.fz.springboot.starter.dict.DictConfiguration;
import com.fz.springboot.starter.dict.frame.annotation.EnableDict;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.lang.NonNull;

import java.util.Map;

import static cn.hutool.core.text.CharSequenceUtil.lowerFirst;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/9/3 11:28
 */

public class DictCondition implements Condition {

    @Override
    public boolean matches(@NonNull ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
        // 1. Check for annotations on the current metadata
        Map<String, Object> attributes = metadata.getAnnotationAttributes(EnableDict.class.getName());
        if (attributes != null) {
            return true;
        }

        try {
            // 2. Check for enable dict annotated beans
            ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
            if (beanFactory == null) {
                return false;
            }

            String[] annotatedBeans = beanFactory.getBeanNamesForAnnotation(EnableDict.class);
            if (ArrayUtil.length(annotatedBeans) > 0) {
                return true;
            }

            // 3. Check if a specific configuration bean exists
            return beanFactory.containsBean(lowerFirst(DictConfiguration.class.getName()));
        }
        catch (Exception e) {
            return false;
        }
    }
}
