package com.fz.spring.boot.starter.log;

import cn.hutool.core.util.ClassUtil;
import com.fz.spring.boot.starter.log.frame.annotation.EnableOperationLog;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.domain.EntityScanPackages;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/9/4 21:24
 */

public class LogConfiguration implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(@NonNull AnnotationMetadata importingClassMetadata,
                                        @NonNull BeanDefinitionRegistry registry)
    {
        EntityScanPackages.register(registry, ClassUtil.getPackage(EnableOperationLog.class));
    }
}
