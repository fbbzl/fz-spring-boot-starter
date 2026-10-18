package io.github.fbbzl.starter.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cn.hutool.json.JSONUtil;
import io.github.fbbzl.starter.audit.BaseAuditLog;
import io.github.fbbzl.starter.audit.frame.annotation.AuditMethod;
import io.github.fbbzl.starter.audit.frame.annotation.AuditModule;
import io.github.fbbzl.starter.audit.frame.aspect.BaseAuditLogAspect;
import io.github.fbbzl.starter.web.Q;
import io.github.fbbzl.starter.web.config.OpenApiConfiguration;
import io.github.fbbzl.starter.web.customizer.QuestOperationCustomizer;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// R-OAPI-01/02: the public documentation endpoint must still describe inherited CRUD and local methods.
@WebMvcTest
@ContextConfiguration(classes = {OpenApiCompatibilityIntegrationTest.ApiController.class,
        SpringDocConfiguration.class, SpringDocConfigProperties.class,
        SpringDocWebMvcConfiguration.class, OpenApiConfiguration.class})
class OpenApiCompatibilityIntegrationTest
{
    @Autowired MockMvc client;
    @Autowired ObjectMapper mapper;
    @MockitoBean BaseCrudControllerTest.TestService service;

    @Test
    void shouldKeepHostCustomizerAsTheOnlyBean()
    {
        QuestOperationCustomizer replacement = new QuestOperationCustomizer();
        var runner = new WebApplicationContextRunner().withConfiguration(AutoConfigurations.of(OpenApiConfiguration.class));
        runner.run(context -> assertThat(context).hasSingleBean(QuestOperationCustomizer.class));
        runner.withBean(QuestOperationCustomizer.class, () -> replacement)
                .run(context -> {
                    assertThat(context).hasSingleBean(QuestOperationCustomizer.class);
                    assertThat(context.getBean(QuestOperationCustomizer.class)).isSameAs(replacement);
                });
    }

    @Test
    void shouldPublishGenericMultipartAndExplicitDocumentation() throws Exception
    {
        JsonNode doc = mapper.readTree(client.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        JsonNode create = doc.at("/paths/~1compatibility/post/requestBody/content/application~1json/schema");
        JsonNode data = resolve(doc, create).at("/properties/data");
        assertThat(resolve(doc, data).at("/properties/name").isMissingNode()).isFalse();

        JsonNode upload = doc.at("/paths/~1compatibility~1upload/post/requestBody/content");
        assertThat(upload.size()).isEqualTo(1);
        JsonNode files = resolve(doc, upload.path("multipart/form-data").path("schema")).at("/properties/files");
        assertThat(files.at("/items/format").asText()).isEqualTo("binary");

        JsonNode explicit = doc.at("/paths/~1compatibility~1explicit/post");
        assertThat(explicit.at("/requestBody/content/application~1json/examples/business/value/data/name").asText())
                .isEqualTo("original");
        assertThat(explicit.at("/responses/400/content/application~1problem+json/schema/$ref").asText())
                .endsWith("/BusinessError");
        assertThat(explicit.at("/responses/400/content/application~1json").isMissingNode()).isTrue();
    }

    private JsonNode resolve(JsonNode doc, JsonNode schema)
    {
        return schema.has("$ref") ? doc.at(schema.path("$ref").asText().substring(1)) : schema;
    }

    // R-AUDIT-01/02: run inherited advice through AOP and never touch the infrastructure objects.
    @ParameterizedTest
    @MethodSource("infrastructure")
    void shouldSkipDirectInfrastructureWithoutChangingBusinessFields(Object infrastructure)
    {
        Capture capture = new Capture();
        AspectJProxyFactory factory = new AspectJProxyFactory(new AuditedService());
        factory.addAspect(capture);
        AuditedService proxy = factory.getProxy();
        Map<String, String> fields = Map.of("password", "fake-password", "token", "fake-token");

        assertThat(proxy.execute(infrastructure, fields)).isSameAs(infrastructure);

        assertThat(capture.saved).isNotNull();
        assertThat(capture.saved.getModule()).isEqualTo("compatibility");
        assertThat(capture.saved.getStatus()).isEqualTo(BaseAuditLog.STATUS_SUCCESS);
        assertThat(capture.saved.getResult()).isNull();
        assertThat(JSONUtil.parseArray(capture.saved.getParam())).hasSize(1);
        assertThat(JSONUtil.parseArray(capture.saved.getParam()).getJSONObject(0).getStr("password")).isEqualTo("fake-password");
        assertThat(JSONUtil.parseArray(capture.saved.getParam()).getJSONObject(0).getStr("token")).isEqualTo("fake-token");
        assertThat(fields).containsExactlyInAnyOrderEntriesOf(Map.of("password", "fake-password", "token", "fake-token"));
        if (infrastructure instanceof Object[] values) verifyNoInteractions(values);
        else verifyNoInteractions(infrastructure);
    }

    static Stream<Arguments> infrastructure()
    {
        return Stream.<Object>of(mock(HttpServletRequest.class), mock(HttpServletResponse.class),
                mock(Part.class), mock(WebRequest.class), mock(MultipartFile.class),
                new MultipartFile[]{mock(MultipartFile.class)}).map(value -> Arguments.of(value));
    }

    @AuditModule("compatibility")
    static class AuditedService
    {
        @AuditMethod
        public Object execute(Object infrastructure, Map<String, String> fields) { return infrastructure; }
    }

    @Aspect
    static class Capture extends BaseAuditLogAspect<Long, AuditRecord>
    {
        AuditRecord saved;
        @Override protected AuditRecord buildAuditLog(AuditMethod annotation) { return new AuditRecord(); }
        @Override public void save(AuditRecord log) { saved = log; }
    }

    @lombok.Data
    @lombok.EqualsAndHashCode(callSuper = true)
    static class AuditRecord extends BaseAuditLog<Long>
    {
        Long id, createdBy, updatedBy;
        java.time.LocalDateTime createdAt, updatedAt, deletedAt;
    }

    @RestController
    @RequestMapping("/compatibility")
    static class ApiController extends BaseCrudControllerTest.TestController
    {
        ApiController(BaseCrudControllerTest.TestService service) { super(service); }

        @PostMapping(value = "/upload", consumes = "multipart/form-data")
        public void upload(Q.FQ files) { }

        @PostMapping("/explicit")
        @ApiResponse(responseCode = "400", content = @Content(mediaType = "application/problem+json",
                schema = @Schema(implementation = BusinessError.class)))
        public void explicit(
                @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
                        mediaType = "application/json", examples = @ExampleObject(
                        name = "business", value = "{\"data\":{\"name\":\"original\"}}")))
                @org.springframework.web.bind.annotation.RequestBody Q<BaseCrudControllerTest.TestDto> body) { }
    }

    static class BusinessError { public String reason; }
}
