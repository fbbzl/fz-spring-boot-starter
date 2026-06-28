package io.github.fbbzl.starter.webflux.customizer;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.TypeUtil;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.fbbzl.starter.webflux.BaseCrudController;
import io.github.fbbzl.starter.webflux.Q;
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
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.customizers.GlobalOperationCustomizer;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.method.HandlerMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Consumer;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

/**
 * Resolves request body schemas for inherited generic controller methods.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026/5/14 23:30
 */
@SuppressWarnings({"rawtypes"})
public class QuestOperationCustomizer implements GlobalOperationCustomizer, GlobalOpenApiCustomizer
{
    private static final String FILES_PROPERTY      = "files";
    private static final String WILDCARD_MEDIA_TYPE = "*/*";
    private static final String HTTP_BAD_REQUEST    = "400";
    private static final String HTTP_SERVER_ERROR   = "500";
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
        if (operation == null) {
            return null;
        }
        if (!supportsController(handlerMethod)) {
            return operation;
        }

        MethodParameter requestBodyParameter = findRequestBodyParameter(handlerMethod);
        if (requestBodyParameter == null) {
            return operation;
        }

        Type requestBodyType = resolveParameterType(requestBodyParameter, handlerMethod.getBeanType());
        if (requestBodyType == null) {
            return operation;
        }

        Schema<?> schema = resolveRequestBodySchema(requestBodyType, requestBodyParameter);
        if (schema == null) {
            return operation;
        }

        RequestBody requestBody = operation.getRequestBody();
        if (requestBody == null) {
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
        if (openApi == null) {
            return;
        }

        Components components = openApi.getComponents();
        if (components == null) {
            components = new Components();
            openApi.setComponents(components);
        }

        Map<String, Schema> schemas = components.getSchemas();
        if (schemas == null) {
            schemas = new LinkedHashMap<>();
            components.setSchemas(schemas);
        }

        Map<String, Schema> componentSchemas = schemas;
        componentSchemas.forEach(referencedSchemas::putIfAbsent);
        referencedSchemas.forEach((schemaName, referencedSchema) -> {
            Schema existingSchema = componentSchemas.get(schemaName);
            if (existingSchema == null) {
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
        if (openApi == null || openApi.getPaths() == null) {
            return;
        }

        openApi.getPaths().values().forEach(pathItem -> {
            if (pathItem == null) {
                return;
            }
            pathItem.readOperations().forEach(this::applyOperationExamples);
        });
    }

    private void applyOperationExamples(Operation operation)
    {
        if (operation == null) {
            return;
        }
        applyRequestBodyExamples(operation.getRequestBody());
        applyResponseExamples(operation.getResponses());
    }

    private void applyRequestBodyExamples(RequestBody requestBody)
    {
        if (requestBody == null || MapUtil.isEmpty(requestBody.getContent())) {
            return;
        }
        requestBody.getContent().forEach(this::applyMediaTypeExample);
    }

    private void applyResponseExamples(Map<String, ApiResponse> responses)
    {
        if (MapUtil.isEmpty(responses)) {
            return;
        }
        responses.forEach(this::applyApiResponseExample);
    }

    private void applyApiResponseExample(String statusCode, ApiResponse apiResponse)
    {
        if (apiResponse == null) {
            return;
        }
        if (isErrorResponseStatus(statusCode)) {
            applyErrorResponseExample(apiResponse, statusCode);
        }
        applyResponseContentExamples(apiResponse.getContent());
    }

    private void applyResponseContentExamples(Content content)
    {
        if (MapUtil.isEmpty(content)) {
            return;
        }
        content.forEach(this::applyMediaTypeExample);
    }

    private void applyMediaTypeExample(String contentType, MediaType mediaType)
    {
        if (mediaType == null || mediaType.getSchema() == null) {
            return;
        }
        applyExample(mediaType, mediaType.getSchema(), contentType);
    }

    private boolean isErrorResponseStatus(String statusCode)
    {
        return HTTP_BAD_REQUEST.equals(statusCode) || HTTP_SERVER_ERROR.equals(statusCode);
    }

    private void applyErrorResponseExample(ApiResponse apiResponse, String statusCode)
    {
        Content content = apiResponse.getContent();
        if (MapUtil.isEmpty(content)) {
            content = new Content();
            apiResponse.setContent(content);
        }

        MediaType mediaType = content.get(APPLICATION_JSON_VALUE);
        if (mediaType == null) {
            mediaType = content.get(WILDCARD_MEDIA_TYPE);
        }
        if (mediaType == null) {
            mediaType = new MediaType();
        }

        content.remove(WILDCARD_MEDIA_TYPE);
        content.addMediaType(APPLICATION_JSON_VALUE, mediaType);
        mediaType.setSchema(errorResponseSchema());

        Object               example  = errorResponseExample(statusCode);
        Map<String, Example> examples = new LinkedHashMap<>();
        examples.put("default", new Example().value(example));
        mediaType.setExample(example);
        mediaType.setExamples(examples);
        applySchemaExample(mediaType.getSchema(), example);
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
        if (TypeUtil.isUnknown(actualType) || TypeUtil.hasTypeVariable(actualType)) {
            return null;
        }
        return actualType;
    }

    @Nullable
    protected Schema resolveRequestBodySchema(Type requestBodyType, MethodParameter methodParameter)
    {
        return resolveSchema(requestBodyType, methodParameter.getParameterAnnotations());
    }

    protected String resolveContentType(MethodParameter methodParameter)
    {
        if (isMultipartFormDataParameter(methodParameter)) {
            return MULTIPART_FORM_DATA_VALUE;
        }
        return APPLICATION_JSON_VALUE;
    }

    protected boolean isMultipartFormDataParameter(MethodParameter methodParameter)
    {
        return Q.FQ.class.isAssignableFrom(methodParameter.getParameterType());
    }

    protected boolean isMultipartFormData(String contentType)
    {
        return MULTIPART_FORM_DATA_VALUE.equals(contentType);
    }

    protected void applyMultipartFormDataSchema(Schema<?> schema)
    {
        Schema<?> multipartSchema = dereference(schema);
        if (MapUtil.isEmpty(multipartSchema.getProperties())) {
            return;
        }

        Schema<?> filesSchema = multipartSchema.getProperties().get(FILES_PROPERTY);
        if (filesSchema == null) {
            return;
        }

        Schema<?> binaryFileSchema = new Schema<>()
                .type(TYPE_STRING)
                .format("binary");
        ArraySchema binaryFilesSchema = new ArraySchema();
        binaryFilesSchema.setDescription(filesSchema.getDescription());
        binaryFilesSchema.setMaxItems(filesSchema.getMaxItems());
        binaryFilesSchema.setMinItems(filesSchema.getMinItems());
        binaryFilesSchema.items(binaryFileSchema);
        multipartSchema.addProperty(FILES_PROPERTY, binaryFilesSchema);
    }

    @Nullable
    protected Schema resolveSchema(Type type, Annotation[] annotations)
    {
        AnnotatedType annotatedType = new AnnotatedType(type).resolveAsRef(true);
        if (annotations != null) {
            annotatedType.ctxAnnotations(annotations);
        }

        ResolvedSchema resolvedSchema = ModelConverters.getInstance().resolveAsResolvedSchema(annotatedType);
        if (resolvedSchema == null || resolvedSchema.schema == null) {
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
        if (type == null || schema == null) {
            return;
        }

        Type      resolvedType = resolveTypeVariable(type, typeVariables);
        Schema<?> actualSchema = dereference(schema);
        String    visitedKey   = resolvedType.getTypeName() + ":" + (schema.get$ref() != null ? schema.get$ref() : "");
        if (!visitedTypes.add(visitedKey)) {
            return;
        }

        if (resolvedType instanceof ParameterizedType parameterizedType) {
            Class<?> rawClass = resolveRawClass(parameterizedType.getRawType());
            if (rawClass == null) {
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
        if (!(schema instanceof ArraySchema arraySchema) || arraySchema.getItems() == null) {
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
        if (!(schema instanceof ArraySchema arraySchema) || arraySchema.getItems() == null) {
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
        if (schema == null || MapUtil.isEmpty(schema.getProperties())) {
            return;
        }

        for (Class<?> currentClass = clazz;
             currentClass != null && !Object.class.equals(currentClass);
             currentClass = currentClass.getSuperclass()) {
            for (Field field : currentClass.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                Schema<?> propertySchema = schema.getProperties().get(field.getName());
                if (propertySchema != null) {
                    applySchemaAnnotation(field.getAnnotation(io.swagger.v3.oas.annotations.media.Schema.class), propertySchema);
                    Type fieldType = resolveTypeVariable(field.getGenericType(), typeVariables);
                    applyGenericPropertyAnnotations(fieldType, propertySchema, typeVariables, visitedTypes);
                }
            }
        }
    }

    private void applySchemaAnnotation(@Nullable io.swagger.v3.oas.annotations.media.Schema schemaAnnotation, Schema<?> schema)
    {
        if (schemaAnnotation == null || schema == null) {
            return;
        }
        setIfNotBlank(schema::setDescription, schemaAnnotation.description());
        setIfNotBlank(schema::setTitle, schemaAnnotation.title());
        setIfNotBlank(schema::setFormat, schemaAnnotation.format());
        setIfNotBlank(schema::setExample, schemaAnnotation.example());
        setIfNotBlank(schema::setDefault, schemaAnnotation.defaultValue());
        if (schemaAnnotation.deprecated()) {
            schema.setDeprecated(Boolean.TRUE);
        }
        if (schemaAnnotation.nullable()) {
            schema.setNullable(Boolean.TRUE);
        }
    }

    private void setIfNotBlank(Consumer<String> setter, String value)
    {
        if (CharSequenceUtil.isNotBlank(value)) setter.accept(value);
    }

    private Schema<?> dereference(Schema<?> schema)
    {
        if (CharSequenceUtil.isBlank(schema.get$ref())) {
            return schema;
        }
        String    schemaName     = CharSequenceUtil.subAfter(schema.get$ref(), REF_SEPARATOR, true);
        Schema<?> resolvedSchema = referencedSchemas.get(schemaName);
        return resolvedSchema != null ? resolvedSchema : schema;
    }

    private Map<TypeVariable<?>, Type> resolveTypeVariables(
            ParameterizedType parameterizedType,
            Map<TypeVariable<?>, Type> parentTypeVariables)
    {
        Map<TypeVariable<?>, Type> typeVariables = new LinkedHashMap<>(parentTypeVariables);
        Class<?>                   rawClass      = resolveRawClass(parameterizedType.getRawType());
        if (rawClass == null) {
            return typeVariables;
        }

        TypeVariable<?>[] variables   = rawClass.getTypeParameters();
        Type[]            actualTypes = parameterizedType.getActualTypeArguments();
        for (int index = 0; index < variables.length && index < actualTypes.length; index++) {
            typeVariables.put(variables[index], resolveTypeVariable(actualTypes[index], parentTypeVariables));
        }
        return typeVariables;
    }

    private Type resolveTypeVariable(Type type, Map<TypeVariable<?>, Type> typeVariables)
    {
        if (type instanceof TypeVariable<?> typeVariable) {
            Type resolvedType = typeVariables.get(typeVariable);
            return resolvedType != null ? resolvedType : typeVariable;
        }
        if (type instanceof ParameterizedType parameterizedType) {
            Type[] actualTypes         = parameterizedType.getActualTypeArguments();
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

    private record ResolvedParameterizedType(Type ownerType, Type rawType,
                                             Type[] actualTypeArguments) implements ParameterizedType
    {
        @NonNull
        @Override
        public Type[] getActualTypeArguments()
        {
            return actualTypeArguments;
        }

        @NonNull
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

        @Override
        public boolean equals(Object o)
        {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ResolvedParameterizedType(Type type, Type rawType1, Type[] typeArguments))) {
                return false;
            }
            return Objects.equals(ownerType, type)
                   && Objects.equals(rawType, rawType1)
                   && Arrays.equals(actualTypeArguments, typeArguments);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(ownerType, rawType, Arrays.hashCode(actualTypeArguments));
        }

        @NonNull
        @Override
        public String toString()
        {
            return "ResolvedParameterizedType[" +
                   "ownerType=" + ownerType +
                   ", rawType=" + rawType +
                   ", actualTypeArguments=" + Arrays.toString(actualTypeArguments) +
                   ']';
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
            if (mediaType != null) {
                mediaType.setSchema(schema);
                applyExample(mediaType, schema, mediaTypeName);
            }
        });
    }

    private void applyExample(MediaType mediaType, Schema<?> schema, String contentType)
    {
        if (!isJsonContentType(contentType) || schema == null) {
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
        return APPLICATION_JSON_VALUE.equals(contentType) || CharSequenceUtil.startWith(contentType, APPLICATION_JSON_VALUE + ";");
    }

    private void applySchemaExample(Schema<?> schema, Object example)
    {
        if (schema == null || isNullExample(example)) {
            return;
        }
        if (isNullExample(schema.getExample())) {
            schema.setExample(example);
        }

        Schema<?> actualSchema = dereference(schema);
        if (isNullExample(actualSchema.getExample())) {
            actualSchema.setExample(example);
        }
    }

    @Nullable
    private Object buildExample(Schema<?> schema, Set<String> visitedSchemas)
    {
        if (schema == null) {
            return null;
        }

        String schemaKey = schema.get$ref() != null ? schema.get$ref() : String.valueOf(System.identityHashCode(schema));
        if (!visitedSchemas.add(schemaKey)) {
            return null;
        }

        Schema<?> actualSchema = dereference(schema);
        Object    example      = resolveExplicitExample(actualSchema);
        if (example != null) {
            return example;
        }

        example = buildComposedExample(actualSchema, visitedSchemas);
        if (example != null) {
            return example;
        }

        if (actualSchema instanceof ArraySchema arraySchema) {
            return buildArrayExample(arraySchema, visitedSchemas);
        }
        if (MapUtil.isNotEmpty(actualSchema.getProperties())) {
            return buildObjectExample(actualSchema, visitedSchemas);
        }
        return resolveDefaultExample(actualSchema);
    }

    @Nullable
    private Object resolveExplicitExample(Schema<?> schema)
    {
        Object explicitExample = exampleValue(schema.getExample());
        if (!isNullExample(explicitExample)) {
            return explicitExample;
        }
        if (schema.getExamples() != null && !schema.getExamples().isEmpty()) {
            Object example = exampleValue(schema.getExamples().getFirst());
            if (!isNullExample(example)) {
                return example;
            }
        }
        if (schema.getEnum() != null && !schema.getEnum().isEmpty()) {
            return schema.getEnum().getFirst();
        }
        return null;
    }

    private Object buildArrayExample(ArraySchema arraySchema, Set<String> visitedSchemas)
    {
        Object itemExample = buildExample(arraySchema.getItems(), visitedSchemas);
        if (itemExample == null || isNullExample(itemExample)) {
            return List.of();
        }
        return List.of(itemExample);
    }

    private Map<String, Object> buildObjectExample(Schema<?> schema, Set<String> visitedSchemas)
    {
        Map<String, Object> example = new LinkedHashMap<>();
        schema.getProperties().forEach((name, property) -> {
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

    @Nullable
    private Object resolveDefaultExample(Schema<?> schema)
    {
        Object defaultValue = defaultValueExample(schema);
        if (!isNullExample(defaultValue)) {
            return defaultValue;
        }
        return defaultExample(schema);
    }

    @Nullable
    private Object buildComposedExample(Schema<?> schema, Set<String> visitedSchemas)
    {
        if (schema == null) {
            return null;
        }

        if (schema.getAllOf() != null && !schema.getAllOf().isEmpty()) {
            return buildAllOfExample(schema.getAllOf(), visitedSchemas);
        }
        if (schema.getOneOf() != null && !schema.getOneOf().isEmpty()) {
            return buildExample(schema.getOneOf().getFirst(), visitedSchemas);
        }
        if (schema.getAnyOf() != null && !schema.getAnyOf().isEmpty()) {
            return buildExample(schema.getAnyOf().getFirst(), visitedSchemas);
        }
        return null;
    }

    @Nullable
    private Object buildAllOfExample(List<Schema> allOfSchemas, Set<String> visitedSchemas)
    {
        Map<String, Object> example = new LinkedHashMap<>();
        for (Schema composedSchema : allOfSchemas) {
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

    @Nullable
    private Object defaultExample(Schema<?> schema)
    {
        if (schema == null) {
            return null;
        }

        String type = resolveSchemaType(schema);
        if (type == null) {
            return new LinkedHashMap<>();
        }
        return switch (type) {
            case TYPE_STRING -> {
                if (FORMAT_DATE.equals(schema.getFormat())) {
                    yield "2026-01-01";
                }
                if (FORMAT_DATE_TIME.equals(schema.getFormat())) {
                    yield "2026-01-01T00:00:00";
                }
                yield TYPE_STRING;
            }
            case TYPE_INTEGER, TYPE_NUMBER -> 0;
            case TYPE_BOOLEAN -> Boolean.TRUE;
            case TYPE_ARRAY -> List.of();
            default -> new LinkedHashMap<>();
        };
    }

    private Schema<?> errorResponseSchema()
    {
        Schema<?> schema = new Schema<>().type(TYPE_OBJECT).description("generic response");
        schema.addProperty("code", new Schema<>().type(TYPE_STRING).description("response code"));
        schema.addProperty("success", new Schema<>().type(TYPE_BOOLEAN).description("if is success, true:success, false:fail"));
        schema.addProperty("message", new Schema<>().type(TYPE_STRING).description("response message"));
        schema.addProperty("data", new Schema<>().nullable(true).description("response data"));
        return schema;
    }

    private Map<String, Object> errorResponseExample(String statusCode)
    {
        Map<String, Object> example = new LinkedHashMap<>();
        example.put("code", statusCode);
        example.put("success", Boolean.FALSE);
        example.put("message", HTTP_BAD_REQUEST.equals(statusCode) ? "Bad Request" : "Internal Server Error");
        example.put("data", null);
        return example;
    }

    private boolean isNullExample(Object value)
    {
        Object actualValue = exampleValue(value);
        if (actualValue == null) {
            return true;
        }
        if (actualValue instanceof JsonNode jsonNode) {
            return jsonNode.isNull() || jsonNode.isMissingNode();
        }
        return false;
    }

    @Nullable
    private Object exampleValue(Object value)
    {
        if (value instanceof Example example) {
            return example.getValue();
        }
        return value;
    }

    @Nullable
    private Object defaultValueExample(Schema<?> schema)
    {
        if (schema == null) {
            return null;
        }

        Object defaultValue = exampleValue(schema.getDefault());
        if (isNullExample(defaultValue)) {
            return null;
        }

        String type = resolveSchemaType(schema);
        if (defaultValue instanceof String defaultText && defaultText.isEmpty() && !TYPE_STRING.equals(type)) {
            return null;
        }
        if (type == null) {
            return defaultValue;
        }
        return switch (type) {
            case TYPE_INTEGER, TYPE_NUMBER -> defaultValue instanceof Number ? defaultValue : null;
            case TYPE_BOOLEAN -> defaultValue instanceof Boolean ? defaultValue : null;
            case TYPE_ARRAY -> defaultValue instanceof Collection<?> || Objects.requireNonNull(defaultValue).getClass().isArray() ? defaultValue : null;
            case TYPE_OBJECT -> defaultValue instanceof Map<?, ?> ? defaultValue : null;
            default -> defaultValue;
        };
    }

    @Nullable
    private String resolveSchemaType(Schema<?> schema)
    {
        if (schema == null) {
            return null;
        }

        String type = schema.getType();
        if (CharSequenceUtil.isNotBlank(type)) {
            return type;
        }

        Set<String> types = schema.getTypes();
        if (types == null || types.isEmpty()) {
            return null;
        }

        return switch (types) {
            case Set<String> s when s.contains(TYPE_STRING) -> TYPE_STRING;
            case Set<String> s when s.contains(TYPE_INTEGER) -> TYPE_INTEGER;
            case Set<String> s when s.contains(TYPE_NUMBER) -> TYPE_NUMBER;
            case Set<String> s when s.contains(TYPE_BOOLEAN) -> TYPE_BOOLEAN;
            case Set<String> s when s.contains(TYPE_ARRAY) -> TYPE_ARRAY;
            case Set<String> s when s.contains(TYPE_OBJECT) -> TYPE_OBJECT;
            default -> types.iterator().next();
        };
    }
}
