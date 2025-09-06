package com.fz.springboot.starter.dict;

import cn.hutool.core.util.ClassUtil;
import com.fz.springboot.starter.dict.components.SysDictController;
import com.fz.springboot.starter.dict.components.SysDictRepository;
import com.fz.springboot.starter.dict.components.SysDictService;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.domain.EntityScanPackages;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.Primary;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/9/3 11:28
 */

public class DictConfiguration implements ImportBeanDefinitionRegistrar {

    @Bean
    @Primary
    public SysDictController sysDictController(SysDictService service) {
        return new SysDictController(service);
    }

    @Bean
    @Primary
    public SysDictService sysDictService(SysDictRepository repository) {
        return new SysDictService(repository);
    }

    @Override
    public void registerBeanDefinitions(@NonNull AnnotationMetadata importingClassMetadata,
                                        @NonNull BeanDefinitionRegistry registry)
    {
        EntityScanPackages.register(registry, ClassUtil.getPackage(DictConfiguration.class));
    }
}