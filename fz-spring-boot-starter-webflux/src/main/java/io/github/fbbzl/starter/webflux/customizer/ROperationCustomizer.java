package io.github.fbbzl.starter.webflux.customizer;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;
import io.github.fbbzl.starter.webflux.BaseCrudController;
import io.github.fbbzl.starter.webflux.Q;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.customizers.GlobalOperationCustomizer;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.MethodParameter;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.*;
import java.util.*;

/**
 * Resolves request body schemas for inherited generic controller methods.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026/5/14 23:30
 */
public class ROperationCustomizer implements GlobalOperationCustomizer, GlobalOpenApiCustomizer
{
    private static final String APPLICATION_JSON     = org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
    private static final String MULTIPART_FORM_DATA = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

    private final Map<String, Schema> referencedSchemas = MapUtil.newConcurrentHashMap();

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod)
    {
        if (ObjectUtil.isNull(operation)) {
            return operation;
        }
        if (!BaseCrudController.class.isAssignableFrom(handlerMethod.getBeanType())) {
            return operation;
        }

        MethodParameter requestBodyParameter = findRequestBodyParameter(handlerMethod);
        if (ObjectUtil.isNull(requestBodyParameter)) {
            return operation;
        }

        Type requestBodyType = resolveParameterType(requestBodyParameter, handlerMethod.getBeanType());
        if (ObjectUtil.isNull(requestBodyType)) {
            return operation;
        }

        Schema<?> schema = resolveRequestBodySchema(requestBodyType, requestBodyParameter);
        if (ObjectUtil.isNull(schema)) {
            return operation;
        }

        RequestBody requestBody = operation.getRequestBody();
        if (ObjectUtil.isNull(requestBody)) {
            requestBody = new RequestBody();
            operation.setRequestBody(requestBody);
        }

        String contentType = resolveContentType(requestBodyParameter);
        if (isMultipartFormData(contentType)) {
            applyMultipartFormDataSchema(schema);
        }
        applySchema(requestBody, schema, contentType, isMultipartFormData(contentType));
        return operation;
    }

    @Override
    public void customise(OpenAPI openApi)
    {
        if (ObjectUtil.isNull(openApi) || MapUtil.isEmpty(referencedSchemas)) {
            return;
        }

        Components components = openApi.getComponents();
        if (ObjectUtil.isNull(components)) {
            components = new Components();
            openApi.setComponents(components);
        }

        Map<String, Schema> schemas = components.getSchemas();
        if (ObjectUtil.isNull(schemas)) {
            schemas = new LinkedHashMap<>();
            components.setSchemas(schemas);
        }

        referencedSchemas.forEach(schemas::putIfAbsent);
    }

    private MethodParameter findRequestBodyParameter(HandlerMethod handlerMethod)
    {
        MethodParameter fallback = null;
        for (MethodParameter methodParameter : handlerMethod.getMethodParameters()) {
            MethodParameter typedParameter = methodParameter.withContainingClass(handlerMethod.getBeanType());
            if (methodParameter.hasParameterAnnotation(org.springframework.web.bind.annotation.RequestBody.class)) {
                return typedParameter;
            }
            Class<?> parameterType = typedParameter.getParameterType();
            if (fallback == null && isSupportedRequestParameter(parameterType)) {
                fallback = typedParameter;
            }
        }
        return fallback;
    }

    private boolean isSupportedRequestParameter(Class<?> parameterType)
    {
        return Q.class.isAssignableFrom(parameterType) || Q.FQ.class.isAssignableFrom(parameterType);
    }

    private Type resolveParameterType(MethodParameter methodParameter, Class<?> controllerClass)
    {
        Type actualType = GenericTypeResolver.resolveType(methodParameter.getGenericParameterType(), controllerClass);
        if (ObjectUtil.isNull(actualType) || TypeUtil.isUnknown(actualType) || TypeUtil.hasTypeVariable(actualType)) {
            return null;
        }
        return actualType;
    }

    private Schema<?> resolveRequestBodySchema(Type requestBodyType, MethodParameter methodParameter)
    {
        return resolveSchema(requestBodyType, methodParameter.getParameterAnnotations(), true);
    }

    private String resolveContentType(MethodParameter methodParameter)
    {
        if (Q.FQ.class.isAssignableFrom(methodParameter.getParameterType())) {
            return MULTIPART_FORM_DATA;
        }
        return APPLICATION_JSON;
    }

    private boolean isMultipartFormData(String contentType)
    {
        return MULTIPART_FORM_DATA.equals(contentType);
    }

    private void applyMultipartFormDataSchema(Schema<?> schema)
    {
        Schema<?> multipartSchema = dereference(schema);
        if (ObjectUtil.isNull(multipartSchema) || MapUtil.isEmpty(multipartSchema.getProperties())) {
            return;
        }

        Schema<?> filesSchema = (Schema<?>) multipartSchema.getProperties().get("files");
        if (ObjectUtil.isNull(filesSchema)) {
            return;
        }

        Schema<?> binaryFileSchema = new Schema<>()
                .type("string")
                .format("binary");
        ArraySchema binaryFilesSchema = new ArraySchema();
        binaryFilesSchema.setDescription(filesSchema.getDescription());
        binaryFilesSchema.setMaxItems(filesSchema.getMaxItems());
        binaryFilesSchema.setMinItems(filesSchema.getMinItems());
        binaryFilesSchema.items(binaryFileSchema);
        multipartSchema.addProperty("files", binaryFilesSchema);
    }

    private Schema<?> resolveSchema(Type type, java.lang.annotation.Annotation[] annotations, boolean resolveAsRef)
    {
        AnnotatedType annotatedType = new AnnotatedType(type).resolveAsRef(resolveAsRef);
        if (ObjectUtil.isNotNull(annotations)) {
            annotatedType.ctxAnnotations(annotations);
        }

        ResolvedSchema resolvedSchema = ModelConverters.getInstance().resolveAsResolvedSchema(annotatedType);
        if (ObjectUtil.isNull(resolvedSchema) || ObjectUtil.isNull(resolvedSchema.schema)) {
            return null;
        }

        if (MapUtil.isNotEmpty(resolvedSchema.referencedSchemas)) {
            referencedSchemas.putAll(resolvedSchema.referencedSchemas);
        }
        applyGenericPropertyAnnotations(type, resolvedSchema.schema);
        return resolvedSchema.schema;
    }

    private void applyGenericPropertyAnnotations(Type type, Schema<?> schema)
    {
        applyGenericPropertyAnnotations(type, schema, new LinkedHashMap<>(), new HashSet<>());
    }

    private void applyGenericPropertyAnnotations(
            Type type,
            Schema<?> schema,
            Map<TypeVariable<?>, Type> typeVariables,
            Set<String> visitedTypes)
    {
        if (ObjectUtil.isNull(type) || ObjectUtil.isNull(schema)) {
            return;
        }

        Type resolvedType = resolveTypeVariable(type, typeVariables);
        Schema<?> actualSchema = dereference(schema);
        String visitedKey = resolvedType.getTypeName() + ":" + ObjectUtil.defaultIfNull(schema.get$ref(), "");
        if (!visitedTypes.add(visitedKey)) {
            return;
        }

        if (resolvedType instanceof ParameterizedType parameterizedType) {
            Class<?> rawClass = resolveRawClass(parameterizedType.getRawType());
            if (ObjectUtil.isNull(rawClass)) {
                return;
            }

            Map<TypeVariable<?>, Type> childTypeVariables = resolveTypeVariables(parameterizedType, typeVariables);
            if (Collection.class.isAssignableFrom(rawClass)) {
                applyCollectionItemAnnotations(parameterizedType, schema, childTypeVariables, visitedTypes);
                return;
            }

            applyClassFieldAnnotations(rawClass, actualSchema, childTypeVariables, visitedTypes);
            return;
        }

        if (resolvedType instanceof Class<?> clazz) {
            if (clazz.isArray()) {
                applyArrayItemAnnotations(clazz, schema, typeVariables, visitedTypes);
                return;
            }
            applyClassFieldAnnotations(clazz, actualSchema, typeVariables, visitedTypes);
        }
    }

    private void applyCollectionItemAnnotations(
            ParameterizedType parameterizedType,
            Schema<?> schema,
            Map<TypeVariable<?>, Type> typeVariables,
            Set<String> visitedTypes)
    {
        if (!(schema instanceof ArraySchema arraySchema) || ObjectUtil.isNull(arraySchema.getItems())) {
            return;
        }
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        if (actualTypeArguments.length == 0) {
            return;
        }
        applyGenericPropertyAnnotations(actualTypeArguments[0], arraySchema.getItems(), typeVariables, visitedTypes);
    }

    private void applyArrayItemAnnotations(
            Class<?> clazz,
            Schema<?> schema,
            Map<TypeVariable<?>, Type> typeVariables,
            Set<String> visitedTypes)
    {
        if (!(schema instanceof ArraySchema arraySchema) || ObjectUtil.isNull(arraySchema.getItems())) {
            return;
        }
        applyGenericPropertyAnnotations(clazz.getComponentType(), arraySchema.getItems(), typeVariables, visitedTypes);
    }

    private void applyClassFieldAnnotations(
            Class<?> clazz,
            Schema<?> schema,
            Map<TypeVariable<?>, Type> typeVariables,
            Set<String> visitedTypes)
    {
        if (ObjectUtil.isNull(schema) || MapUtil.isEmpty(schema.getProperties())) {
            return;
        }

        for (Class<?> currentClass = clazz;
             ObjectUtil.isNotNull(currentClass) && !Object.class.equals(currentClass);
             currentClass = currentClass.getSuperclass()) {
            for (Field field : currentClass.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                Schema<?> propertySchema = (Schema<?>) schema.getProperties().get(field.getName());
                if (ObjectUtil.isNull(propertySchema)) {
                    continue;
                }

                applySchemaAnnotation(field.getAnnotation(io.swagger.v3.oas.annotations.media.Schema.class), propertySchema);
                Type fieldType = resolveTypeVariable(field.getGenericType(), typeVariables);
                applyGenericPropertyAnnotations(fieldType, propertySchema, typeVariables, visitedTypes);
            }
        }
    }

    private void applySchemaAnnotation(io.swagger.v3.oas.annotations.media.Schema schemaAnnotation, Schema<?> schema)
    {
        if (ObjectUtil.isNull(schemaAnnotation) || ObjectUtil.isNull(schema)) {
            return;
        }
        if (StrUtil.isNotBlank(schemaAnnotation.description())) {
            schema.setDescription(schemaAnnotation.description());
        }
        if (StrUtil.isNotBlank(schemaAnnotation.title())) {
            schema.setTitle(schemaAnnotation.title());
        }
        if (StrUtil.isNotBlank(schemaAnnotation.format())) {
            schema.setFormat(schemaAnnotation.format());
        }
        if (StrUtil.isNotBlank(schemaAnnotation.example())) {
            schema.setExample(schemaAnnotation.example());
        }
        if (StrUtil.isNotBlank(schemaAnnotation.defaultValue())) {
            schema.setDefault(schemaAnnotation.defaultValue());
        }
        if (schemaAnnotation.deprecated()) {
            schema.setDeprecated(Boolean.TRUE);
        }
        if (schemaAnnotation.nullable()) {
            schema.setNullable(Boolean.TRUE);
        }
    }

    private Schema<?> dereference(Schema<?> schema)
    {
        if (ObjectUtil.isNull(schema) || StrUtil.isBlank(schema.get$ref())) {
            return schema;
        }
        String schemaName = StrUtil.subAfter(schema.get$ref(), "/", true);
        return ObjectUtil.defaultIfNull(referencedSchemas.get(schemaName), schema);
    }

    private Map<TypeVariable<?>, Type> resolveTypeVariables(
            ParameterizedType parameterizedType,
            Map<TypeVariable<?>, Type> parentTypeVariables)
    {
        Map<TypeVariable<?>, Type> typeVariables = new LinkedHashMap<>(parentTypeVariables);
        Class<?> rawClass = resolveRawClass(parameterizedType.getRawType());
        if (ObjectUtil.isNull(rawClass)) {
            return typeVariables;
        }

        TypeVariable<?>[] variables = rawClass.getTypeParameters();
        Type[] actualTypes = parameterizedType.getActualTypeArguments();
        for (int index = 0; index < variables.length && index < actualTypes.length; index++) {
            typeVariables.put(variables[index], resolveTypeVariable(actualTypes[index], parentTypeVariables));
        }
        return typeVariables;
    }

    private Type resolveTypeVariable(Type type, Map<TypeVariable<?>, Type> typeVariables)
    {
        if (type instanceof TypeVariable<?> typeVariable) {
            return ObjectUtil.defaultIfNull(typeVariables.get(typeVariable), typeVariable);
        }
        if (type instanceof ParameterizedType parameterizedType) {
            Type[] actualTypes = parameterizedType.getActualTypeArguments();
            Type[] resolvedActualTypes = new Type[actualTypes.length];
            for (int index = 0; index < actualTypes.length; index++) {
                resolvedActualTypes[index] = resolveTypeVariable(actualTypes[index], typeVariables);
            }
            return new ResolvedParameterizedType(parameterizedType.getOwnerType(),
                                                 parameterizedType.getRawType(),
                                                 resolvedActualTypes);
        }
        return type;
    }

    private Class<?> resolveRawClass(Type rawType)
    {
        if (rawType instanceof Class<?> clazz) {
            return clazz;
        }
        return null;
    }

    private record ResolvedParameterizedType(Type ownerType, Type rawType, Type[] actualTypeArguments) implements ParameterizedType
    {

        @Override
        public Type[] getActualTypeArguments()
        {
            return actualTypeArguments;
        }

        @Override
        public Type getRawType()
        {
            return rawType;
        }

        @Override
        public Type getOwnerType()
        {
            return ownerType;
        }

        @Override
        public String getTypeName()
        {
            StringBuilder typeName = new StringBuilder(rawType.getTypeName());
            if (actualTypeArguments.length == 0) {
                return typeName.toString();
            }

            typeName.append("<");
            for (int index = 0; index < actualTypeArguments.length; index++) {
                if (index > 0) {
                    typeName.append(", ");
                }
                typeName.append(actualTypeArguments[index].getTypeName());
            }
            typeName.append(">");
            return typeName.toString();
        }
    }

    private void applySchema(RequestBody requestBody, Schema<?> schema, String contentType, boolean exclusiveContentType)
    {
        Content content = requestBody.getContent();
        if (MapUtil.isEmpty(content)) {
            content = new Content();
            requestBody.setContent(content);
        }
        if (exclusiveContentType) {
            content.clear();
        }

        if (!content.containsKey(contentType)) {
            content.addMediaType(contentType, new MediaType());
        }

        for (MediaType mediaType : content.values()) {
            if (ObjectUtil.isNotNull(mediaType)) {
                mediaType.setSchema(schema);
            }
        }
    }
}
