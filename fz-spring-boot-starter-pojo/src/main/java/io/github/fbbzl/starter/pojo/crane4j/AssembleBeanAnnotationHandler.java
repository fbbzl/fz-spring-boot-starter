package io.github.fbbzl.starter.pojo.crane4j;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.handler.AbstractStandardAssembleAnnotationHandler;
import cn.crane4j.core.parser.handler.strategy.PropertyMappingStrategyManager;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.Crane4jGlobalSorter;
import cn.crane4j.core.util.StringUtils;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.AnnotatedElement;
import java.util.Comparator;

/**
 * Resolve {@link AssembleBean} to a Crane4j assemble operation.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026/5/21
 */
public class AssembleBeanAnnotationHandler extends AbstractStandardAssembleAnnotationHandler<AssembleBean>
{
    private final ApplicationContext applicationContext;

    public AssembleBeanAnnotationHandler(
            AnnotationFinder annotationFinder,
            Crane4jGlobalConfiguration globalConfiguration,
            PropertyMappingStrategyManager propertyMappingStrategyManager,
            ApplicationContext applicationContext)
    {
        this(annotationFinder, globalConfiguration, Crane4jGlobalSorter.comparator(), propertyMappingStrategyManager, applicationContext);
    }

    public AssembleBeanAnnotationHandler(
            AnnotationFinder annotationFinder,
            Crane4jGlobalConfiguration globalConfiguration,
            Comparator<KeyTriggerOperation> operationComparator,
            PropertyMappingStrategyManager propertyMappingStrategyManager,
            ApplicationContext applicationContext)
    {
        super(AssembleBean.class, annotationFinder, operationComparator, globalConfiguration, propertyMappingStrategyManager);
        this.applicationContext = applicationContext;
    }

    @Override
    protected String getContainerNamespace(StandardAssembleAnnotation<AssembleBean> standardAnnotation)
    {
        Container<?> container = applicationContext.getBean(standardAnnotation.getAnnotation().bean());
        String       namespace = container.getNamespace();
        if (StringUtils.isEmpty(namespace)) return Container.EMPTY_CONTAINER_NAMESPACE;
        return namespace;
    }

    @Override
    protected StandardAssembleAnnotation<AssembleBean> getStandardAnnotation(
            BeanOperations beanOperations,
            AnnotatedElement element,
            AssembleBean annotation)
    {
        return StandardAssembleAnnotationAdapter.<AssembleBean>builder()
                                                .annotatedElement(element)
                                                .annotation(annotation)
                                                .id(annotation.id())
                                                .key(annotation.key())
                                                .keyResolver(annotation.keyResolver())
                                                .keyDesc(annotation.keyDesc())
                                                .sort(annotation.sort())
                                                .groups(annotation.groups())
                                                .keyType(annotation.keyType())
                                                .handler(annotation.handler())
                                                .handlerType(annotation.handlerType())
                                                .mappingTemplates(annotation.propTemplates())
                                                .props(annotation.props())
                                                .prop(annotation.prop())
                                                .propertyMappingStrategy(annotation.propertyMappingStrategy())
                                                .build();
    }
}
