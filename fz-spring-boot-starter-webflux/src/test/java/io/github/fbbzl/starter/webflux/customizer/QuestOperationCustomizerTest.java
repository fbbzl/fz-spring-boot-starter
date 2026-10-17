package io.github.fbbzl.starter.webflux.customizer;

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

    private void customize(String status, ApiResponse response)
    {
        Operation operation = new Operation().responses(new ApiResponses().addApiResponse(status, response));
        new QuestOperationCustomizer().customise(new OpenAPI().path("/test", new PathItem().post(operation)));
    }
}
