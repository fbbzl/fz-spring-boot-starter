package io.github.fbbzl.starter.web.customizer;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.fbbzl.starter.web.BaseCrudController;
import io.github.fbbzl.starter.web.Q;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.customizers.GlobalOperationCustomizer;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.web.method.HandlerMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

/**
 * Resolves request body schemas for inherited generic controller methods.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026/5/14 23:30
 */
public class QOperationCustomizer implements GlobalOperationCustomizer, GlobalOpenApiCustomizer
{
    private static final String APPLICATION_JSON     = org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
    private static final String MULTIPART_FORM_DATA = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
    private static final String FILES_PROPERTY      = "files";
    private static final String REF_SEPARATOR       = "/";
    private static final String TYPE_ARRAY          = "array";
    private static final String TYPE_BOOLEAN        = "boolean";
    private static final String TYPE_INTEGER        = "integer";
    private static final String TYPE_NUMBER         = "number";
    private static final String TYPE_OBJECT         = "object";
    private static final String TYPE_STRING         = "string";
    private static final String FORMAT_DATE         = "date";
    private static final String FORMAT_DATE_TIME    = "date-time";

    private final Map<String, Schema> referencedSchemas = MapUtil.newConcurrentHashMap();

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod)
    {
        if (ObjectUtil.isNull(operation)) {
            return operation;
        }
        if (!supportsController(handlerMethod)) {
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

    protected boolean supportsController(HandlerMethod handlerMethod)
    {
        return BaseCrudController.class.isAssignableFrom(handlerMethod.getBeanType());
    }

    @Override
    public void customise(OpenAPI openApi)
    {
        if (ObjectUtil.isNull(openApi)) {
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

        Map<String, Schema> componentSchemas = schemas;
        componentSchemas.forEach(referencedSchemas::putIfAbsent);
        referencedSchemas.forEach((schemaName, referencedSchema) -> {
            Schema existingSchema = componentSchemas.get(schemaName);
            if (ObjectUtil.isNull(existingSchema)) {
                componentSchemas.put(schemaName, referencedSchema);
                return;
            }
            applySchemaExample(existingSchema, referencedSchema.getExample());
        });
        applyComponentExamples(componentSchemas);
        applyOpenApiExamples(openApi);
    }

    private void applyComponentExamples(Map<String, Schema> componentSchemas)
    {
        if (MapUtil.isEmpty(componentSchemas)) {
            return;
        }

        componentSchemas.values().forEach(schema -> {
            Object example = buildExample(schema, new HashSet<>());
            if (!isNullExample(example)) {
                applySchemaExample(schema, example);
            }
        });
    }

    private void applyOpenApiExamples(OpenAPI openApi)
    {
        if (ObjectUtil.isNull(openApi) || ObjectUtil.isNull(openApi.getPaths())) {
            return;
        }

        openApi.getPaths().values().forEach(pathItem -> {
            if (ObjectUtil.isNull(pathItem)) {
                return;
            }
            pathItem.readOperations().forEach(this::applyOperationExamples);
        });
    }

    private void applyOperationExamples(Operation operation)
    {
        if (ObjectUtil.isNull(operation)) {
            return;
        }

        if (ObjectUtil.isNotNull(operation.getRequestBody())
            && MapUtil.isNotEmpty(operation.getRequestBody().getContent())) {
            operation.getRequestBody().getContent().forEach((contentType, mediaType) -> {
                if (ObjectUtil.isNull(mediaType) || ObjectUtil.isNull(mediaType.getSchema())) {
                    return;
                }
                applyExample(mediaType, mediaType.getSchema(), contentType);
            });
        }

        if (MapUtil.isEmpty(operation.getResponses())) {
            return;
        }
        operation.getResponses().values().forEach(apiResponse -> {
            if (ObjectUtil.isNull(apiResponse) || MapUtil.isEmpty(apiResponse.getContent())) {
                return;
            }
            apiResponse.getContent().forEach((contentType, mediaType) -> {
                if (ObjectUtil.isNull(mediaType) || ObjectUtil.isNull(mediaType.getSchema())) {
                    return;
                }
                applyExample(mediaType, mediaType.getSchema(), contentType);
            });
        });
    }

    @Nullable
    protected MethodParameter findRequestBodyParameter(HandlerMethod handlerMethod)
    {
        MethodParameter fallback = null;
        for (MethodParameter methodParameter : handlerMethod.getMethodParameters()) {
            MethodParameter typedParameter = methodParameter.withContainingClass(handlerMethod.getBeanType());
            if (isExplicitRequestBodyParameter(methodParameter)) {
                return typedParameter;
            }
            Class<?> parameterType = typedParameter.getParameterType();
            if (fallback == null && supportsFallbackRequestBodyParameter(parameterType)) {
                fallback = typedParameter;
            }
        }
        return fallback;
    }

    protected boolean isExplicitRequestBodyParameter(MethodParameter methodParameter)
    {
        return methodParameter.hasParameterAnnotation(org.springframework.web.bind.annotation.RequestBody.class);
    }

    protected boolean supportsFallbackRequestBodyParameter(Class<?> parameterType)
    {
        return Q.class.isAssignableFrom(parameterType) || Q.FQ.class.isAssignableFrom(parameterType);
    }

    @Nullable
    protected Type resolveParameterType(MethodParameter methodParameter, Class<?> controllerClass)
    {
        Type actualType = GenericTypeResolver.resolveType(methodParameter.getGenericParameterType(), controllerClass);
        if (ObjectUtil.isNull(actualType) || TypeUtil.isUnknown(actualType) || TypeUtil.hasTypeVariable(actualType)) {
            return null;
        }
        return actualType;
    }

    @Nullable
    protected Schema<?> resolveRequestBodySchema(Type requestBodyType, MethodParameter methodParameter)
    {
        return resolveSchema(requestBodyType, methodParameter.getParameterAnnotations(), true);
    }

    protected String resolveContentType(MethodParameter methodParameter)
    {
        if (isMultipartFormDataParameter(methodParameter)) {
            return MULTIPART_FORM_DATA;
        }
        return APPLICATION_JSON;
    }

    protected boolean isMultipartFormDataParameter(MethodParameter methodParameter)
    {
        return Q.FQ.class.isAssignableFrom(methodParameter.getParameterType());
    }

    protected boolean isMultipartFormData(String contentType)
    {
        return MULTIPART_FORM_DATA.equals(contentType);
    }

    protected void applyMultipartFormDataSchema(Schema<?> schema)
    {
        Schema<?> multipartSchema = dereference(schema);
        if (ObjectUtil.isNull(multipartSchema) || MapUtil.isEmpty(multipartSchema.getProperties())) {
            return;
        }

        Schema<?> filesSchema = (Schema<?>) multipartSchema.getProperties().get(FILES_PROPERTY);
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
        multipartSchema.addProperty(FILES_PROPERTY, binaryFilesSchema);
    }

    @Nullable
    protected Schema<?> resolveSchema(Type type, Annotation[] annotations, boolean resolveAsRef)
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

    protected void applyGenericPropertyAnnotations(Type type, Schema<?> schema)
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

    private void applySchemaAnnotation(@Nullable io.swagger.v3.oas.annotations.media.Schema schemaAnnotation, Schema<?> schema)
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

    @Nullable
    private Schema<?> dereference(Schema<?> schema)
    {
        if (ObjectUtil.isNull(schema) || StrUtil.isBlank(schema.get$ref())) {
            return schema;
        }
        String schemaName = StrUtil.subAfter(schema.get$ref(), REF_SEPARATOR, true);
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

    @Nullable
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

        content.forEach((mediaTypeName, mediaType) -> {
            if (ObjectUtil.isNotNull(mediaType)) {
                mediaType.setSchema(schema);
                applyExample(mediaType, schema, mediaTypeName);
            }
        });
    }

    private void applyExample(MediaType mediaType, Schema<?> schema, String contentType)
    {
        if (!isJsonContentType(contentType) || ObjectUtil.isNull(schema)) {
            return;
        }

        Object example = buildExample(schema, new HashSet<>());
        if (isNullExample(example)) example = new LinkedHashMap<>();

        Map<String, Example> examples = new LinkedHashMap<>();
        examples.put("default", new Example().value(example));
        mediaType.setExample(example);
        mediaType.setExamples(examples);
        applySchemaExample(schema, example);
    }

    private boolean isJsonContentType(String contentType)
    {
        return APPLICATION_JSON.equals(contentType) || StrUtil.startWith(contentType, APPLICATION_JSON + ";");
    }

    private void applySchemaExample(Schema<?> schema, Object example)
    {
        if (ObjectUtil.isNull(schema) || isNullExample(example)) {
            return;
        }
        if (isNullExample(schema.getExample())) {
            schema.setExample(example);
        }

        Schema<?> actualSchema = dereference(schema);
        if (ObjectUtil.isNotNull(actualSchema) && isNullExample(actualSchema.getExample())) {
            actualSchema.setExample(example);
        }
    }

    @Nullable
    private Object buildExample(Schema<?> schema, Set<String> visitedSchemas)
    {
        if (ObjectUtil.isNull(schema)) {
            return null;
        }

        String schemaKey = ObjectUtil.defaultIfNull(schema.get$ref(), String.valueOf(System.identityHashCode(schema)));
        if (!visitedSchemas.add(schemaKey)) {
            return null;
        }

        Schema<?> actualSchema = dereference(schema);
        if (!isNullExample(actualSchema.getExample())) {
            return actualSchema.getExample();
        }
        if (ObjectUtil.isNotNull(actualSchema.getExamples()) && !actualSchema.getExamples().isEmpty()) {
            Object example = actualSchema.getExamples().get(0);
            if (!isNullExample(example)) {
                return example;
            }
        }
        if (!isNullExample(actualSchema.getDefault())) {
            return actualSchema.getDefault();
        }
        if (ObjectUtil.isNotNull(actualSchema.getEnum()) && !actualSchema.getEnum().isEmpty()) {
            return actualSchema.getEnum().get(0);
        }

        Object composedExample = buildComposedExample(actualSchema, visitedSchemas);
        if (ObjectUtil.isNotNull(composedExample)) {
            return composedExample;
        }

        if (actualSchema instanceof ArraySchema arraySchema) {
            Object itemExample = buildExample(arraySchema.getItems(), visitedSchemas);
            return ObjectUtil.isNull(itemExample) ? List.of() : List.of(itemExample);
        }

        if (MapUtil.isNotEmpty(actualSchema.getProperties())) {
            Map<String, Object> example = new LinkedHashMap<>();
            actualSchema.getProperties().forEach((name, property) -> {
                if (!(property instanceof Schema<?> propertySchema)
                    || Boolean.TRUE.equals(propertySchema.getReadOnly())) {
                    return;
                }

                Object propertyExample = buildExample(propertySchema, new HashSet<>(visitedSchemas));
                if (!isNullExample(propertyExample)) {
                    example.put(name, propertyExample);
                }
            });
            return example;
        }

        return defaultExample(actualSchema);
    }

    @Nullable
    private Object buildComposedExample(Schema<?> schema, Set<String> visitedSchemas)
    {
        if (ObjectUtil.isNull(schema)) {
            return null;
        }

        if (ObjectUtil.isNotNull(schema.getAllOf()) && !schema.getAllOf().isEmpty()) {
            Map<String, Object> example = new LinkedHashMap<>();
            for (Schema<?> composedSchema : schema.getAllOf()) {
                Object composedExample = buildExample(composedSchema, new HashSet<>(visitedSchemas));
                if (composedExample instanceof Map<?, ?> composedMap) {
                    composedMap.forEach((key, value) -> {
                        if (key instanceof String fieldName && !isNullExample(value)) {
                            example.put(fieldName, value);
                        }
                    });
                }
            }
            return example.isEmpty() ? null : example;
        }

        if (ObjectUtil.isNotNull(schema.getOneOf()) && !schema.getOneOf().isEmpty()) {
            return buildExample(schema.getOneOf().get(0), visitedSchemas);
        }
        if (ObjectUtil.isNotNull(schema.getAnyOf()) && !schema.getAnyOf().isEmpty()) {
            return buildExample(schema.getAnyOf().get(0), visitedSchemas);
        }
        return null;
    }

    @Nullable
    private Object defaultExample(Schema<?> schema)
    {
        if (ObjectUtil.isNull(schema)) {
            return null;
        }

        String type = resolveSchemaType(schema);
        if (TYPE_STRING.equals(type)) {
            if (FORMAT_DATE.equals(schema.getFormat())) {
                return "2026-01-01";
            }
            if (FORMAT_DATE_TIME.equals(schema.getFormat())) {
                return "2026-01-01T00:00:00";
            }
            return TYPE_STRING;
        }
        if (TYPE_INTEGER.equals(type)) {
            return 0;
        }
        if (TYPE_NUMBER.equals(type)) {
            return 0;
        }
        if (TYPE_BOOLEAN.equals(type)) {
            return Boolean.TRUE;
        }
        if (TYPE_OBJECT.equals(type)) {
            return new LinkedHashMap<>();
        }
        if (TYPE_ARRAY.equals(type)) {
            return List.of();
        }
        return new LinkedHashMap<>();
    }

    private boolean isNullExample(Object value)
    {
        if (ObjectUtil.isNull(value)) {
            return true;
        }
        if (value instanceof JsonNode jsonNode) {
            return jsonNode.isNull() || jsonNode.isMissingNode();
        }
        return false;
    }

    @Nullable
    private String resolveSchemaType(Schema<?> schema)
    {
        if (ObjectUtil.isNull(schema)) {
            return null;
        }

        String type = schema.getType();
        if (StrUtil.isNotBlank(type)) {
            return type;
        }

        Set<String> types = schema.getTypes();
        if (ObjectUtil.isNull(types) || types.isEmpty()) {
            return null;
        }

        if (types.contains(TYPE_STRING)) return TYPE_STRING;
        if (types.contains(TYPE_INTEGER)) return TYPE_INTEGER;
        if (types.contains(TYPE_NUMBER)) return TYPE_NUMBER;
        if (types.contains(TYPE_BOOLEAN)) return TYPE_BOOLEAN;
        if (types.contains(TYPE_ARRAY)) return TYPE_ARRAY;
        if (types.contains(TYPE_OBJECT)) return TYPE_OBJECT;
        return types.iterator().next();
    }
}
