package io.github.fbbzl.starter.webflux.customizer;

import io.github.fbbzl.starter.webflux.Q;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class QuestOperationCustomizerTest
{
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldPreserveExplicitRequestAndResponseExamples(boolean named)
    {
        Object example = Map.of("id", 42);
        MediaType mediaType = new MediaType().schema(new ObjectSchema());
        if (named) mediaType.addExamples("business", new Example().value(example));
        else mediaType.example(example);
        Content content = new Content().addMediaType("application/json", mediaType);
        Operation operation = new Operation().requestBody(new RequestBody().content(content))
                .responses(new ApiResponses().addApiResponse("200", new ApiResponse().content(content)));

        new QuestOperationCustomizer().customise(new OpenAPI().path("/test", new PathItem().post(operation)));

        if (named) assertThat(mediaType.getExamples()).containsOnlyKeys("business");
        else assertThat(mediaType.getExample()).isSameAs(example);
    }

    @ParameterizedTest
    @ValueSource(strings = {"400", "500"})
    void shouldPreserveDeclaredErrorResponse(String status)
    {
        MediaType mediaType = new MediaType().schema(new ObjectSchema().description("custom error"));
        Content content = new Content().addMediaType("application/problem+json", mediaType);
        ApiResponse response = new ApiResponse().content(content);
        customize(status, response);

        assertThat(response.getContent()).containsOnlyKeys("application/problem+json");
        assertThat(mediaType.getSchema().getDescription()).isEqualTo("custom error");

        ApiResponse reference = new ApiResponse().$ref("#/components/responses/Error");
        customize(status, reference);
        assertThat(reference.getContent()).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"400", "500"})
    void shouldSupplyMissingErrorResponseContent(String status)
    {
        ApiResponse response = new ApiResponse();
        customize(status, response);

        MediaType mediaType = response.getContent().get("application/json");
        assertThat(mediaType.getSchema().getProperties()).containsKeys("code", "success", "message", "data");
        Map<?, ?> example = (Map<?, ?>) mediaType.getExample();
        assertThat(example.get("success")).isEqualTo(false);
        assertThat(example.get("code")).isEqualTo(status);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldKeepInheritedGenericAndMultipartResolution(boolean multipart) throws Exception
    {
        QuestOperationCustomizer customizer = new QuestOperationCustomizer() {
            @Override
            protected boolean supportsController(org.springframework.web.method.HandlerMethod method)
            {
                return true;
            }
        };
        var method = GenericController.class.getMethod(multipart ? "upload" : "save", multipart ? Q.FQ.class : Q.class);
        var handler = new org.springframework.web.method.HandlerMethod(new Controller(), method);
        Operation operation = customizer.customize(new Operation(), handler);
        OpenAPI api = new OpenAPI().path("/generic", new PathItem().post(operation));
        customizer.customise(api);

        String contentType = multipart ? "multipart/form-data" : "application/json";
        assertThat(operation.getRequestBody().getContent()).containsOnlyKeys(contentType);
        var reference = operation.getRequestBody().getContent().get(contentType).getSchema().get$ref();
        io.swagger.v3.oas.models.media.Schema<?> schema = api.getComponents().getSchemas().get(reference.substring(reference.lastIndexOf('/') + 1));
        if (multipart) assertThat(schema.getProperties().get("files").getItems().getFormat()).isEqualTo("binary");
        else {
            var data = schema.getProperties().get("data");
            io.swagger.v3.oas.models.media.Schema<?> payload = api.getComponents().getSchemas().get(data.get$ref().substring(data.get$ref().lastIndexOf('/') + 1));
            assertThat(payload.getProperties().get("title").getExample()).isEqualTo("business example");
        }
    }

    public static class GenericController<T>
    {
        public void save(Q<T> request) {}
        public void upload(Q.FQ request) {}
    }

    public static class Controller extends GenericController<Payload> {}

    public static class Payload
    {
        @io.swagger.v3.oas.annotations.media.Schema(example = "business example")
        public String title;
    }

    private void customize(String status, ApiResponse response)
    {
        Operation operation = new Operation().responses(new ApiResponses().addApiResponse(status, response));
        new QuestOperationCustomizer().customise(new OpenAPI().path("/test", new PathItem().post(operation)));
    }
}
