package io.github.fbbzl.starter.web.controller;

import cn.hutool.db.Page;
import cn.hutool.db.PageResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.fbbzl.starter.dal.BaseDal;
import io.github.fbbzl.starter.pojo.bo.BaseBo;
import io.github.fbbzl.starter.pojo.dto.BaseDto;
import io.github.fbbzl.starter.pojo.entity.BaseTableEntity;
import io.github.fbbzl.starter.pojo.mapstruct.BaseCrudStructMapper;
import io.github.fbbzl.starter.web.BaseCrudController;
import io.github.fbbzl.starter.web.BaseCrudService;
import io.github.fbbzl.starter.web.advice.WebResponseWrapAdvice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BaseCrudControllerTest
{

    private MockMvc       mockMvc;
    private TestService   service;

    @BeforeEach
    void setUp() throws Exception
    {
        service = mock(TestService.class);
        TestController controller = new TestController(service);

        GenericApplicationContext context = new GenericApplicationContext();
        context.refresh();
        AutoConfigurationPackages.register(context, TestController.class.getPackageName());

        WebResponseWrapAdvice advice = new WebResponseWrapAdvice(new ObjectMapper());
        Field applicationContextField = WebResponseWrapAdvice.class.getSuperclass().getDeclaredField("applicationContext");
        applicationContextField.setAccessible(true);
        applicationContextField.set(advice, context);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                                 .setControllerAdvice(advice)
                                 .build();
    }

    @Test
    void shouldGetById() throws Exception
    {
        when(service.byId(1L)).thenReturn(newBo(1L, "alice"));

        mockMvc.perform(get("/test/1"))
               .andExpect(status().isOk())
               .andExpect(content().json("{\"code\":\"200\",\"success\":true,\"message\":\"ok\",\"data\":{\"id\":1,\"name\":\"alice\"}}"));
    }

    @Test
    void shouldGetByIds() throws Exception
    {
        when(service.byIds(Set.of(1L, 2L))).thenReturn(List.of(newBo(1L, "alice"), newBo(2L, "bob")));

        mockMvc.perform(post("/test/ids")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"data\":[1,2]}"))
               .andExpect(status().isOk())
               .andExpect(content().json("{\"code\":\"200\",\"success\":true,\"message\":\"ok\",\"data\":[{\"id\":1,\"name\":\"alice\"},{\"id\":2,\"name\":\"bob\"}]}"));
    }

    @Test
    void shouldListWithoutLimit() throws Exception
    {
        when(service.list(any(TestDto.class), eq(5000), any(), any())).thenReturn(List.of(newBo(1L, "alice")));

        mockMvc.perform(post("/test/list")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"data\":{\"name\":\"alice\"}}"))
               .andExpect(status().isOk())
               .andExpect(content().json("{\"code\":\"200\",\"success\":true,\"message\":\"ok\",\"data\":[{\"id\":1,\"name\":\"alice\"}]}"));
    }

    @Test
    void shouldListWithLimit() throws Exception
    {
        when(service.list(any(TestDto.class), eq(10), any(), any())).thenReturn(List.of(newBo(1L, "alice")));

        mockMvc.perform(post("/test/list/10")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"data\":{\"name\":\"alice\"}}"))
               .andExpect(status().isOk())
               .andExpect(content().json("{\"code\":\"200\",\"success\":true,\"message\":\"ok\",\"data\":[{\"id\":1,\"name\":\"alice\"}]}"));
    }

    @Test
    void shouldPage() throws Exception
    {
        PageResult<TestBo> pageResult = new PageResult<>(0, 10, 1);
        pageResult.add(newBo(1L, "alice"));
        when(service.page(any(Page.class), any(TestDto.class))).thenReturn(pageResult);

        mockMvc.perform(post("/test/page")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"data\":{\"name\":\"alice\"},\"page\":{\"pageNumber\":0,\"pageSize\":10}}"))
               .andExpect(status().isOk())
               .andExpect(content().json("{\"code\":\"200\",\"success\":true,\"message\":\"ok\",\"data\":{\"page\":0,\"pageSize\":10,\"totalPage\":1,\"total\":1,\"records\":[{\"id\":1,\"name\":\"alice\"}]}}"));
    }

    @Test
    void shouldTreeWithoutLimit() throws Exception
    {
        when(service.tree(eq(1L), any(TestDto.class), eq(5000), any(), any())).thenReturn(List.of());

        mockMvc.perform(post("/test/tree/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"data\":{\"name\":\"alice\"}}"))
               .andExpect(status().isOk())
               .andExpect(content().json("{\"code\":\"200\",\"success\":true,\"message\":\"ok\",\"data\":[]}"));
    }

    @Test
    void shouldTreeWithLimit() throws Exception
    {
        when(service.tree(eq(1L), any(TestDto.class), eq(10), any(), any())).thenReturn(List.of());

        mockMvc.perform(post("/test/tree/1/10")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"data\":{\"name\":\"alice\"}}"))
               .andExpect(status().isOk())
               .andExpect(content().json("{\"code\":\"200\",\"success\":true,\"message\":\"ok\",\"data\":[]}"));
    }

    @Test
    void shouldExistsById() throws Exception
    {
        when(service.exists(1L)).thenReturn(true);

        mockMvc.perform(get("/test/exists/1"))
               .andExpect(status().isOk())
               .andExpect(content().json("{\"code\":\"200\",\"success\":true,\"message\":\"ok\",\"data\":true}"));
    }

    @Test
    void shouldExistsByDto() throws Exception
    {
        when(service.exists(any(TestDto.class))).thenReturn(true);

        mockMvc.perform(post("/test/exists")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"data\":{\"name\":\"alice\"}}"))
               .andExpect(status().isOk())
               .andExpect(content().json("{\"code\":\"200\",\"success\":true,\"message\":\"ok\",\"data\":true}"));
    }

    @Test
    void shouldCount() throws Exception
    {
        when(service.count(any(TestDto.class))).thenReturn(5L);

        mockMvc.perform(post("/test/count")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"data\":{\"name\":\"alice\"}}"))
               .andExpect(status().isOk())
               .andExpect(content().json("{\"code\":\"200\",\"success\":true,\"message\":\"ok\",\"data\":5}"));
    }

    @Test
    void shouldIdsWithoutLimit() throws Exception
    {
        when(service.ids(any(TestDto.class))).thenReturn(List.of(1L, 2L));

        mockMvc.perform(post("/test/ids/query")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"data\":{\"name\":\"alice\"}}"))
               .andExpect(status().isOk())
               .andExpect(content().json("{\"code\":\"200\",\"success\":true,\"message\":\"ok\",\"data\":[1,2]}"));
    }

    @Test
    void shouldIdsWithLimit() throws Exception
    {
        when(service.ids(any(TestDto.class), eq(10))).thenReturn(List.of(1L, 2L));

        mockMvc.perform(post("/test/ids/query/10")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"data\":{\"name\":\"alice\"}}"))
               .andExpect(status().isOk())
               .andExpect(content().json("{\"code\":\"200\",\"success\":true,\"message\":\"ok\",\"data\":[1,2]}"));
    }

    @Test
    void shouldCreate() throws Exception
    {
        when(service.create(any(TestDto.class))).thenReturn(newBo(1L, "alice"));

        mockMvc.perform(post("/test")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"data\":{\"name\":\"alice\"}}"))
               .andExpect(status().isOk())
               .andExpect(content().json("{\"code\":\"200\",\"success\":true,\"message\":\"ok\",\"data\":{\"id\":1,\"name\":\"alice\"}}"));
    }

    @Test
    void shouldCreateBatch() throws Exception
    {
        when(service.create(any(List.class))).thenReturn(List.of(newBo(1L, "alice")));

        mockMvc.perform(post("/test/batch")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"data\":[{\"name\":\"alice\"}]}"))
               .andExpect(status().isOk())
               .andExpect(content().json("{\"code\":\"200\",\"success\":true,\"message\":\"ok\",\"data\":[{\"id\":1,\"name\":\"alice\"}]}"));
    }

    @Test
    void shouldUpdate() throws Exception
    {
        when(service.update(any(TestDto.class))).thenReturn(1);

        mockMvc.perform(put("/test")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"data\":{\"id\":1,\"name\":\"bob\"}}"))
               .andExpect(status().isOk())
               .andExpect(content().json("{\"code\":\"200\",\"success\":true,\"message\":\"ok\",\"data\":1}"));
    }

    @Test
    void shouldUpdateBatch() throws Exception
    {
        when(service.update(any(List.class))).thenReturn(2);

        mockMvc.perform(put("/test/batch")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"data\":[{\"id\":1,\"name\":\"bob\"},{\"id\":2,\"name\":\"charlie\"}]}"))
               .andExpect(status().isOk())
               .andExpect(content().json("{\"code\":\"200\",\"success\":true,\"message\":\"ok\",\"data\":2}"));
    }

    @Test
    void shouldPatch() throws Exception
    {
        when(service.patch(argThat(map -> "bob".equals(map.get("name"))))).thenReturn(newBo(1L, "bob"));

        mockMvc.perform(patch("/test/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"data\":{\"name\":\"bob\"}}"))
               .andExpect(status().isOk())
               .andExpect(content().json("{\"code\":\"200\",\"success\":true,\"message\":\"ok\",\"data\":{\"id\":1,\"name\":\"bob\"}}"));
    }

    @Test
    void shouldDeleteById() throws Exception
    {
        mockMvc.perform(delete("/test/1"))
               .andExpect(status().isOk())
               .andExpect(content().json("{\"code\":\"200\",\"success\":true,\"message\":\"ok\",\"data\":null}"));

        verify(service).delete(1L);
    }

    @Test
    void shouldDeleteByIds() throws Exception
    {
        mockMvc.perform(delete("/test/ids")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"data\":[1,2]}"))
               .andExpect(status().isOk())
               .andExpect(content().json("{\"code\":\"200\",\"success\":true,\"message\":\"ok\",\"data\":null}"));

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
