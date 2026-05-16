package io.github.fbbzl.starter.dal.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/5/16 10:24
 */
public class BeanSearcherEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered
{
    private static final String PROPERTY_SOURCE_NAME = "beanSearcherDefaults";

    private static final Map<String, Object> DEFAULT_PROPERTIES = createDefaultProperties();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application)
    {
        MutablePropertySources propertySources = environment.getPropertySources();
        MapPropertySource propertySource = new MapPropertySource(PROPERTY_SOURCE_NAME, DEFAULT_PROPERTIES);
        if (propertySources.contains(PROPERTY_SOURCE_NAME))
            propertySources.replace(PROPERTY_SOURCE_NAME, propertySource);
        else
            propertySources.addLast(propertySource);
    }

    @Override
    public int getOrder()
    {
        return Ordered.LOWEST_PRECEDENCE;
    }

    private static Map<String, Object> createDefaultProperties()
    {
        Map<String, Object> defaults = new LinkedHashMap<>();
        defaults.put("bean-searcher.params.pagination.default-size", 10);
        defaults.put("bean-searcher.params.pagination.start",        0);
        defaults.put("bean-searcher.field-convertor.zone-id",        "GMT+8");
        defaults.put("bean-searcher.params.convertor.zone-id",       "GMT+8");
        return Collections.unmodifiableMap(defaults);
    }
}
