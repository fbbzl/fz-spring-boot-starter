package io.github.fbbzl.starter.webflux.controller;

import cn.hutool.db.Page;
import cn.hutool.db.PageResult;
import io.github.fbbzl.starter.dal.BaseDal;
import io.github.fbbzl.starter.pojo.bo.BaseBo;
import io.github.fbbzl.starter.pojo.dto.BaseDto;
import io.github.fbbzl.starter.pojo.entity.BaseTableEntity;
import io.github.fbbzl.starter.pojo.mapstruct.BaseCrudStructMapper;
import io.github.fbbzl.starter.webflux.BaseCrudController;
import io.github.fbbzl.starter.webflux.BaseCrudService;
import io.github.fbbzl.starter.webflux.Q;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = BaseCrudControllerTest.TestController.class)
@SuppressWarnings("unchecked")
class BaseCrudControllerTest
{

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private TestService service;

    @Test
    void shouldGetById()
    {
        when(service.byId(1L)).thenReturn(newBo(1L, "alice"));

        webTestClient.get().uri("/test/1")
                     .exchange()
                     .expectStatus().isOk()
                     .expectBody()
                     .json("{\"id\":1,\"name\":\"alice\"}");
    }

    @Test
    void shouldGetByIds()
    {
        when(service.byIds(Set.of(1L, 2L))).thenReturn(List.of(newBo(1L, "alice"), newBo(2L, "bob")));

        webTestClient.post().uri("/test/ids")
                     .bodyValue(Q.of(Set.of(1L, 2L)))
                     .exchange()
                     .expectStatus().isOk()
                     .expectBody()
                     .json("[{\"id\":1,\"name\":\"alice\"},{\"id\":2,\"name\":\"bob\"}]");
    }

    @Test
    void shouldListWithoutLimit()
    {
        when(service.list(any(TestDto.class), eq(5000), any(), any())).thenReturn(List.of(newBo(1L, "alice")));

        webTestClient.post().uri("/test/list")
                     .bodyValue(Q.OQ.of(new TestDto("alice")))
                     .exchange()
                     .expectStatus().isOk()
                     .expectBody()
                     .json("[{\"id\":1,\"name\":\"alice\"}]");
    }

    @Test
    void shouldListWithLimit()
    {
        when(service.list(any(TestDto.class), eq(10), any(), any())).thenReturn(List.of(newBo(1L, "alice")));

        webTestClient.post().uri("/test/list/10")
                     .bodyValue(Q.OQ.of(new TestDto("alice")))
                     .exchange()
                     .expectStatus().isOk()
                     .expectBody()
                     .json("[{\"id\":1,\"name\":\"alice\"}]");
    }

    @Test
    void shouldPage()
    {
        PageResult<TestBo> pageResult = new PageResult<>(0, 10, 1);
        pageResult.add(newBo(1L, "alice"));
        when(service.page(any(Page.class), any(TestDto.class))).thenReturn(pageResult);

        webTestClient.post().uri("/test/page")
                     .bodyValue(Q.PQ.of(new TestDto("alice"), Page.of(0, 10)))
                     .exchange()
                     .expectStatus().isOk()
                     .expectBody()
                     .json("{\"page\":0,\"pageSize\":10,\"totalPage\":1,\"total\":1,\"records\":[{\"id\":1,\"name\":\"alice\"}]}");
    }

    @Test
    void shouldTreeWithoutLimit()
    {
        when(service.tree(eq(1L), any(TestDto.class), eq(5000), any(), any())).thenReturn(List.of());

        webTestClient.post().uri("/test/tree/1")
                     .bodyValue(Q.OQ.of(new TestDto("alice")))
                     .exchange()
                     .expectStatus().isOk()
                     .expectBody()
                     .json("[]");
    }

    @Test
    void shouldTreeWithLimit()
    {
        when(service.tree(eq(1L), any(TestDto.class), eq(10), any(), any())).thenReturn(List.of());

        webTestClient.post().uri("/test/tree/1/10")
                     .bodyValue(Q.OQ.of(new TestDto("alice")))
                     .exchange()
                     .expectStatus().isOk()
                     .expectBody()
                     .json("[]");
    }

    @Test
    void shouldExistsById()
    {
        when(service.exists(1L)).thenReturn(true);

        webTestClient.get().uri("/test/exists/1")
                     .exchange()
                     .expectStatus().isOk()
                     .expectBody(Boolean.class)
                     .isEqualTo(true);
    }

    @Test
    void shouldExistsByDto()
    {
        when(service.exists(any(TestDto.class))).thenReturn(true);

        webTestClient.post().uri("/test/exists")
                     .bodyValue(Q.of(new TestDto("alice")))
                     .exchange()
                     .expectStatus().isOk()
                     .expectBody(Boolean.class)
                     .isEqualTo(true);
    }

    @Test
    void shouldCount()
    {
        when(service.count(any(TestDto.class))).thenReturn(5L);

        webTestClient.post().uri("/test/count")
                     .bodyValue(Q.of(new TestDto("alice")))
                     .exchange()
                     .expectStatus().isOk()
                     .expectBody()
                     .json("5");
    }

    @Test
    void shouldIdsWithoutLimit()
    {
        when(service.ids(any(TestDto.class))).thenReturn(List.of(1L, 2L));

        webTestClient.post().uri("/test/ids/query")
                     .bodyValue(Q.of(new TestDto("alice")))
                     .exchange()
                     .expectStatus().isOk()
                     .expectBody()
                     .json("[1,2]");
    }

    @Test
    void shouldIdsWithLimit()
    {
        when(service.ids(any(TestDto.class), eq(10))).thenReturn(List.of(1L, 2L));

        webTestClient.post().uri("/test/ids/query/10")
                     .bodyValue(Q.of(new TestDto("alice")))
                     .exchange()
                     .expectStatus().isOk()
                     .expectBody()
                     .json("[1,2]");
    }

    @Test
    void shouldCreate()
    {
        when(service.create(any(TestDto.class))).thenReturn(newBo(1L, "alice"));

        webTestClient.post().uri("/test")
                     .bodyValue(Q.of(new TestDto("alice")))
                     .exchange()
                     .expectStatus().isOk()
                     .expectBody()
                     .json("{\"id\":1,\"name\":\"alice\"}");
    }

    @Test
    void shouldCreateBatch()
    {
        when(service.create(any(List.class))).thenReturn(List.of(newBo(1L, "alice")));

        webTestClient.post().uri("/test/batch")
                     .bodyValue(Q.of(List.of(new TestDto("alice"))))
                     .exchange()
                     .expectStatus().isOk()
                     .expectBody()
                     .json("[{\"id\":1,\"name\":\"alice\"}]");
    }

    @Test
    void shouldUpdate()
    {
        when(service.update(any(TestDto.class))).thenReturn(1);

        webTestClient.put().uri("/test")
                     .bodyValue(Q.of(new TestDto(1L, "bob")))
                     .exchange()
                     .expectStatus().isOk()
                     .expectBody()
                     .json("1");
    }

    @Test
    void shouldUpdateBatch()
    {
        when(service.update(any(List.class))).thenReturn(2);

        webTestClient.put().uri("/test/batch")
                     .bodyValue(Q.of(List.of(new TestDto(1L, "bob"), new TestDto(2L, "charlie"))))
                     .exchange()
                     .expectStatus().isOk()
                     .expectBody()
                     .json("2");
    }

    @Test
    void shouldPatch()
    {
        when(service.patch(argThat(map -> "bob".equals(map.get("name"))))).thenReturn(newBo(1L, "bob"));

        webTestClient.patch().uri("/test/1")
                     .bodyValue(Q.of(new TestDto("bob")))
                     .exchange()
                     .expectStatus().isOk()
                     .expectBody()
                     .json("{\"id\":1,\"name\":\"bob\"}");
    }

    @Test
    void shouldDeleteById()
    {
        webTestClient.delete().uri("/test/1")
                     .exchange()
                     .expectStatus().isOk()
                     .expectBody().isEmpty();

        verify(service).delete(1L);
    }

    @Test
    void shouldDeleteByIds()
    {
        webTestClient.method(org.springframework.http.HttpMethod.DELETE).uri("/test/ids")
                     .bodyValue(Q.of(Set.of(1L, 2L)))
                     .exchange()
                     .expectStatus().isOk()
                     .expectBody().isEmpty();

        verify(service).delete(Set.of(1L, 2L));
    }

    private static TestBo newBo(Long id, String name)
    {
        TestBo bo = new TestBo();
        bo.setId(id);
        bo.name = name;
        return bo;
    }

    @RestController
    @RequestMapping("/test")
    static class TestController extends BaseCrudController<Long, TestEntity, TestService, TestDto, TestBo>
    {
        TestController(TestService service)
        {
            this.service = service;
        }
    }

    static abstract class TestService extends BaseCrudService<Long, TestEntity, TestDto, TestBo, BaseDal<TestEntity, Long>, TestStructMapper>
    {
    }

    interface TestStructMapper extends BaseCrudStructMapper<TestEntity, TestDto, TestBo>
    {
    }

    static class TestDto extends BaseDto<Long>
    {
        public String name;

        TestDto()
        {
        }

        TestDto(String name)
        {
            this.name = name;
        }

        TestDto(Long id, String name)
        {
            this.name = name;
            setId(id);
        }
    }

    static class TestBo extends BaseBo<Long>
    {
        public String name;
    }

    static class TestEntity implements BaseTableEntity<Long>
    {
        private Long          id;
        public String          name;
        private LocalDateTime  createdAt;
        private Long           createdBy;
        private LocalDateTime  updatedAt;
        private Long           updatedBy;
        private LocalDateTime  deletedAt;

        @Override
        public Long getId()
        {
            return id;
        }

        @Override
        public void setId(Long id)
        {
            this.id = id;
        }

        @Override
        public LocalDateTime getCreatedAt()
        {
            return createdAt;
        }

        @Override
        public void setCreatedAt(LocalDateTime createdAt)
        {
            this.createdAt = createdAt;
        }

        @Override
        public Long getCreatedBy()
        {
            return createdBy;
        }

        @Override
        public void setCreatedBy(Long createdBy)
        {
            this.createdBy = createdBy;
        }

        @Override
        public LocalDateTime getUpdatedAt()
        {
            return updatedAt;
        }

        @Override
        public void setUpdatedAt(LocalDateTime updatedAt)
        {
            this.updatedAt = updatedAt;
        }

        @Override
        public Long getUpdatedBy()
        {
            return updatedBy;
        }

        @Override
        public void setUpdatedBy(Long updatedBy)
        {
            this.updatedBy = updatedBy;
        }

        @Override
        public LocalDateTime getDeletedAt()
        {
            return deletedAt;
        }

        @Override
        public void setDeletedAt(LocalDateTime deletedAt)
        {
            this.deletedAt = deletedAt;
        }
    }
}
