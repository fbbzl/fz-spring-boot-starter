package io.github.fbbzl.starter.web.registrar;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import io.github.fbbzl.starter.web.annotation.exchange.EnableHttpExchangeClientScan;
import io.github.fbbzl.starter.web.annotation.exchange.HttpExchangeClient;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.Role;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.beans.Introspector;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.hutool.core.text.CharSequenceUtil.format;
import static cn.hutool.core.text.CharSequenceUtil.isNotBlank;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/5/12 18:00
 */
@Slf4j
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HttpExchangeClientRegistrar implements ImportBeanDefinitionRegistrar, BeanFactoryAware, EnvironmentAware, ResourceLoaderAware
{
    private static final String REST_CLIENT_BEAN_NAME_SUFFIX = "{}RestClient";
    private static final String HTTP_SERVICE_PROXY_FACTORY_BEAN_NAME_SUFFIX = "{}HttpServiceProxyFactory";

    ConfigurableListableBeanFactory beanFactory;
    Environment                     environment;
    ResourceLoader                  resourceLoader;

    @Override
    public void setBeanFactory(@NonNull BeanFactory beanFactory) throws BeansException
    {
        Assert.isInstanceOf(ConfigurableListableBeanFactory.class, beanFactory, "HttpExchangeClientRegistrar requires a ConfigurableListableBeanFactory");
        this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
    }

    @Override
    public void setEnvironment(@NonNull Environment environment)
    {
        this.environment = environment;
    }

    @Override
    public void setResourceLoader(@NonNull ResourceLoader resourceLoader)
    {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(@NonNull AnnotationMetadata importingClassMetadata, @NonNull BeanDefinitionRegistry registry)
    {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false, environment)
        {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition)
            {
                return beanDefinition.getMetadata().isInterface();
            }
        };
        if (resourceLoader != null) scanner.setResourceLoader(resourceLoader);
        scanner.addIncludeFilter(new AnnotationTypeFilter(HttpExchangeClient.class));

        for (String basePackage : basePackages(importingClassMetadata))
            for (BeanDefinition candidate : scanner.findCandidateComponents(basePackage))
                registerClient(registry, candidate);
    }

    private List<String> basePackages(AnnotationMetadata importingClassMetadata)
    {
        Map<String, Object> attributes = importingClassMetadata.getAnnotationAttributes(EnableHttpExchangeClientScan.class.getName());
        if (attributes == null)
            return defaultBasePackages();

        Set<String> basePackages = new LinkedHashSet<>();
        String[] scanBasePackages = (String[]) attributes.get("scanBasePackages");
        addBasePackages(basePackages, scanBasePackages);

        Class<?>[] basePackageClasses = (Class<?>[]) attributes.get("scanBasePackageClasses");
        addBasePackageClasses(basePackages, basePackageClasses);

        if (CollUtil.isEmpty(basePackages))
            basePackages.add(ClassUtils.getPackageName(importingClassMetadata.getClassName()));
        return List.copyOf(basePackages);
    }

    private List<String> defaultBasePackages()
    {
        if (AutoConfigurationPackages.has(beanFactory))
            return AutoConfigurationPackages.get(beanFactory);
        return List.of(ClassUtils.getPackageName(HttpExchangeClientRegistrar.class));
    }

    private void addBasePackages(Set<String> basePackages, String[] packageNames)
    {
        if (ArrayUtil.isEmpty(packageNames)) return;

        for (String packageName : packageNames) {
            String resolvedPackageName = environment.resolvePlaceholders(packageName);
            if (isNotBlank(resolvedPackageName))
                basePackages.add(resolvedPackageName);
        }
    }

    private void addBasePackageClasses(Set<String> basePackages, Class<?>[] basePackageClasses)
    {
        if (ArrayUtil.isEmpty(basePackageClasses)) return;

        for (Class<?> basePackageClass : basePackageClasses)
            basePackages.add(ClassUtils.getPackageName(basePackageClass));
    }

    private void registerClient(BeanDefinitionRegistry registry, BeanDefinition candidate)
    {
        try {
            ClassLoader        classLoader = resourceLoader == null ? ClassUtils.getDefaultClassLoader() : resourceLoader.getClassLoader();
            String             clientClassName = candidate.getBeanClassName();
            Assert.notNull(clientClassName, "Http exchange client bean class name must not be null");
            Class<?>           clientType  = ClassUtils.forName(clientClassName, classLoader);
            HttpExchangeClient annotation = clientType.getAnnotation(HttpExchangeClient.class);
            Assert.notNull(annotation, "HttpExchangeClient annotation must not be null");

            String clientBeanName              = Introspector.decapitalize(clientType.getSimpleName());
            String restClientBeanName          = format(REST_CLIENT_BEAN_NAME_SUFFIX, clientBeanName);
            String httpServiceProxyFactoryName = format(HTTP_SERVICE_PROXY_FACTORY_BEAN_NAME_SUFFIX, clientBeanName);

            this.doRegisterRestClient(registry, restClientBeanName, annotation);
            this.doRegisterHttpServiceProxyFactory(registry, httpServiceProxyFactoryName, restClientBeanName);
            this.doRegisterHttpExchangeClient(registry, clientBeanName, clientType, httpServiceProxyFactoryName);
        }
        catch (ClassNotFoundException e) {
            throw new IllegalStateException("Failed to load http exchange client class: " + candidate.getBeanClassName(), e);
        }
    }

    private void doRegisterRestClient(
            BeanDefinitionRegistry registry,
            String restClientBeanName,
            HttpExchangeClient annotation)
    {
        if (registry.containsBeanDefinition(restClientBeanName)) return;

        RootBeanDefinition beanDefinition = new RootBeanDefinition(RestClient.class);
        beanDefinition.setInstanceSupplier(() -> HttpExchangeClient.Helper.buildRestClient(annotation, beanFactory, environment));
        registry.registerBeanDefinition(restClientBeanName, beanDefinition);
    }

    private void doRegisterHttpServiceProxyFactory(
            BeanDefinitionRegistry registry,
            String httpServiceProxyFactoryName,
            String restClientBeanName)
    {
        if (registry.containsBeanDefinition(httpServiceProxyFactoryName)) return;

        RootBeanDefinition beanDefinition = new RootBeanDefinition(HttpServiceProxyFactory.class);
        beanDefinition.setInstanceSupplier(() -> HttpServiceProxyFactory.builder()
                                                                        .exchangeAdapter(RestClientAdapter.create(beanFactory.getBean(restClientBeanName, RestClient.class)))
                                                                        .build());
        registry.registerBeanDefinition(httpServiceProxyFactoryName, beanDefinition);
    }

    private void doRegisterHttpExchangeClient(
            BeanDefinitionRegistry registry,
            String clientBeanName,
            Class<?> clientType,
            String httpServiceProxyFactoryName)
    {
        if (registry.containsBeanDefinition(clientBeanName)) {
            log.debug("Skip http exchange client bean registration, bean already exists: {}", clientBeanName);
            return;
        }

        RootBeanDefinition beanDefinition = new RootBeanDefinition(clientType);
        beanDefinition.setInstanceSupplier(() -> createClientProxy(clientType, httpServiceProxyFactoryName));

        registry.registerBeanDefinition(clientBeanName, beanDefinition);
    }

    private Object createClientProxy(Class<?> clientType, String httpServiceProxyFactoryName)
    {
        Object client = beanFactory.getBean(httpServiceProxyFactoryName, HttpServiceProxyFactory.class).createClient(clientType);
        return ProxyFactory.getProxy(
                clientType,
                (MethodInterceptor) invocation -> {
                    Method   method = invocation.getMethod();
                    Object[] args   = invocation.getArguments();
                    if (method.getDeclaringClass() == Object.class) {
                        Object proxy = ((ProxyMethodInvocation) invocation).getProxy();
                        return switch (method.getName()) {
                            case "toString" -> clientType.getName() + " proxy";
                            case "hashCode" -> System.identityHashCode(proxy);
                            case "equals" -> args.length > 0 && proxy == args[0];
                            default -> method.invoke(client, args);
                        };
                    }

                    try {
                        return method.invoke(client, args);
                    }
                    catch (InvocationTargetException e) {
                        throw e.getTargetException();
                    }
                });
    }

}
