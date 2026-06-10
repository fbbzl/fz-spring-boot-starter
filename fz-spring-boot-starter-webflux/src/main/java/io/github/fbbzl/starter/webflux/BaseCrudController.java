package io.github.fbbzl.starter.webflux;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.lang.tree.Tree;
import io.github.fbbzl.starter.audit.frame.annotation.AuditMethod;
import io.github.fbbzl.starter.core.util.Generics;
import io.github.fbbzl.starter.pojo.bo.BaseBo;
import io.github.fbbzl.starter.pojo.dto.BaseDto;
import io.github.fbbzl.starter.pojo.dto.BaseDto.Fields;
import io.github.fbbzl.starter.pojo.entity.BaseTableEntity;
import io.github.fbbzl.starter.pojo.validation.group.CRUD;
import io.github.fbbzl.starter.webflux.Q.OQ;
import io.github.fbbzl.starter.webflux.Q.PQ;
import io.github.fbbzl.starter.webflux.R.PR;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.hutool.core.util.ObjectUtil.defaultIfNull;
import static lombok.AccessLevel.PROTECTED;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/22 14:24
 */
@Slf4j
@Validated
@SuppressWarnings("all")
@FieldDefaults(level = PROTECTED)
public abstract class BaseCrudController<
        ID      extends Serializable,
        ENTITY  extends BaseTableEntity<ID>,
        SERVICE extends BaseCrudService<ID, ENTITY, DTO, BO, ?, ?>,
        DTO     extends BaseDto<ID>,
        BO      extends BaseBo<ID>>
{

    @Autowired
    SERVICE service;

    Class<ENTITY> entityClass = Generics.getGenericSuperType(this.getClass(), BaseCrudController.class, 1);

    @Operation(description = "[BASE] Based on the primary key query, it does not contain data that has been logically deleted", summary = "[BASE] Query by primary key")
    @GetMapping("{id}")
    public BO byId(
            @NotNull
            @PathVariable("id")
            @Parameter(name = "id", description = "the primary key of the query", required = true, example = "1")
            ID id)
    {
        return service.byId(id);
    }

    @Operation(description = "[BASE] Query by primary key set", summary = "[BASE] Query by primary key set")
    @PostMapping("ids")
    public List<BO> byIds(
            @NotNull
            @Validated(CRUD.R.class)
            @Parameter(description = "request contains ids", required = true, example = "1,2,3")
            @RequestBody
            Q<Set<ID>> req)
    {
        return service.byIds(req.getData());
    }

    @Operation(description = "[BASE] List query, null fields do not participate in the query", summary = "[BASE] List Query")
    @PostMapping({"list", "list/{limit}"})
    public List<BO> list(
            @Nullable
            @Positive(message = "limit must be positive")
            @Parameter(description = "list data limit")
            @PathVariable(value = "limit", required = false)
            Integer limit,
            @NotNull
            @Validated(CRUD.R.class)
            @Parameter(description = "request", required = true)
            @RequestBody
            OQ<DTO> req)
    {
        return service.list(req.getData(), defaultIfNull(limit, this.defaultLimit()), req.getOrders(), req.getRanges());
    }

    @Operation(description = "[BASE] For paginated query, null fields do not participate in query", summary = "[BASE] Page query")
    @PostMapping("page")
    public PR<BO> page(
            @NotNull
            @Validated(CRUD.R.class)
            @Parameter(description = "pagination request", required = true)
            @RequestBody
            PQ<DTO> req)
    {
        return PR.of(service.page(req.getPage(), req.getData()));
    }

    @Operation(description = "[BASE] For tree query, null fields do not participate in query", summary = "[BASE] Tree query, If it weren't be a tree type data, there would be no result")
    @PostMapping({"tree/{rootId}", "tree/{rootId}/{limit}"})
    public List<Tree<ID>> tree(
            @NotNull
            @PathVariable("rootId")
            @Parameter(name = "rootId", description = "the root-id of the tree", required = true, example = "1")
            ID rootId,
            @Nullable
            @Positive(message = "limit must be positive")
            @Parameter(description = "tree data limit")
            @PathVariable(value = "limit", required = false)
            Integer limit,
            @NotNull
            @Validated(CRUD.R.class)
            @Parameter(description = "tree request", required = true)
            @RequestBody
            OQ<DTO> req)
    {
        return service.tree(rootId, req.getData(), defaultIfNull(limit, this.defaultLimit()), req.getOrders(), req.getRanges());
    }

    @Operation(description = "[BASE] Specify whether primary key data exists", summary = "[BASE] Specifies whether primary key data exists")
    @GetMapping("exists/{id}")
    public boolean exists(
            @NotNull
            @PathVariable("id")
            @Parameter(name = "id", description = "the primary key of the query", required = true, example = "1")
            ID id)
    {
        return service.exists(id);
    }

    @Operation(description = "[BASE] Specify whether the condition data exists, and null fields will not participate in the query", summary = "[BASE] Specifies whether conditional data exists")
    @PostMapping("exists")
    public boolean exists(
            @NotNull
            @Validated(CRUD.R.class)
            @Parameter(description = "request data", required = true)
            @RequestBody
            Q<DTO> req)
    {
        return service.exists(req.getData());
    }

    @Operation(description = "[BASE] Count by condition, null fields do not participate in the query", summary = "[BASE] Count by condition")
    @PostMapping("count")
    public long count(
            @NotNull
            @Validated(CRUD.R.class)
            @Parameter(description = "count request data", required = true)
            @RequestBody
            Q<DTO> req)
    {
        return service.count(req.getData());
    }

    @Operation(description = "[BASE] Query IDs by condition, null fields do not participate", summary = "[BASE] Query IDs by condition")
    @PostMapping({"ids/query", "ids/query/{limit}"})
    public List<ID> ids(
            @Nullable
            @Positive(message = "limit must be positive")
            @Parameter(description = "ids query limit")
            @PathVariable(value = "limit", required = false)
            Integer limit,
            @NotNull
            @Validated(CRUD.R.class)
            @Parameter(description = "query request data", required = true)
            @RequestBody
            Q<DTO> req)
    {
        if (limit != null) return service.ids(req.getData(), limit);
        else               return service.ids(req.getData());
    }
    @Operation(description = "[BASE] Compare current data with incoming data, return field differences", summary = "[BASE] Diff by primary key")
    @PostMapping("diff/{id}")
    public List<Map<String, Object>> diff(
            @NotNull
            @PathVariable("id")
            @Parameter(name = "id", description = "the primary key of the record to diff", required = true, example = "1")
            ID id,
            @NotNull
            @Validated(CRUD.R.class)
            @Parameter(description = "diff request data", required = true)
            @RequestBody
            Q<DTO> req)
    {
        return service.diff(id, req.getData());
    }

    @AuditMethod
    @Operation(description = "[BASE] Create data", summary = "[BASE] Create data")
    @PostMapping
    public BO create(
            @NotNull
            @Validated(CRUD.C.class)
            @Parameter(description = "creating data", required = true)
            @RequestBody
            Q<DTO> req)
    {
        return service.create(req.getData());
    }

    @AuditMethod(saveParam = false, saveResult = false)
    @Operation(description = "[BASE] Create data batch", summary = "[BASE] Create data batch")
    @PostMapping("batch")
    public void createBatch(
            @NotNull
            @Validated(CRUD.C.class)
            @Parameter(description = "creating batch data", required = true)
            @RequestBody
            Q<Collection<DTO>> req)
    {
        service.create(req.getData());
    }

    @AuditMethod
    @Operation(description = "[BASE] Update without null fields", summary = "[BASE] Do update ignore null field value")
    @PutMapping
    public BO update(
            @NotNull
            @Validated(CRUD.U.class)
            @Parameter(name = "req", description = "updating data", required = true)
            @RequestBody
            Q<DTO> req)
    {
        return service.update(req.getData());
    }

    @AuditMethod(saveParam = false, saveResult = false)
    @Operation(description = "[BASE] Batch updates without null fields", summary = "[BASE] Do batch update ignore null field value")
    @PutMapping("batch")
    public void updateBatch(
            @NotNull
            @Validated(CRUD.U.class)
            @Parameter(name = "req", description = "batch updating data", required = true)
            @RequestBody
            Q<Collection<DTO>> req)
    {
        service.update(req.getData());
    }

    @AuditMethod
    @Operation(description = "[BASE] Update by map without null fields", summary = "[BASE] Do update by map ignore null field value")
    @PatchMapping("{id}")
    public BO patch(
            @NotNull
            @PathVariable("id")
            @Parameter(name = "id", description = "The primary key of the record that needs to be update", required = true, example = "1")
            ID id,
            @NotNull
            @Validated(CRUD.U.class)
            @RequestBody
            Q<Dict> req)
    {
        Map<String, Object> data = req.getData();
        data.put(Fields.id, id);
        return service.patch(data);
    }

    @AuditMethod
    @Operation(description = "[BASE] Deleting data is a logical deletion, but this tombstone deletion is equivalent to physical deletion, and the tombstone is only to maximize the value of the data", summary = "[BASE] Delete data by primary key")
    @DeleteMapping("{id}")
    public void delete(
            @NotNull
            @PathVariable("id")
            @Parameter(name = "id", description = "The primary key of the record that needs to be deleted", required = true, example = "1")
            ID id)
    {
        service.delete(id);
    }

    @AuditMethod
    @Operation(description = "[BASE] Deleting data is a logical deletion, but this tombstone deletion is equivalent to physical deletion, and the tombstone is only to maximize the value of the data", summary = "[BASE] Delete data by primary key set")
    @DeleteMapping("ids")
    public void delete(
            @NotNull
            @Validated(CRUD.D.class)
            @Parameter(description = "request ids", required = true, example = "1,2,3")
            @RequestBody
            Q<Set<ID>> req)
    {
        service.delete(req.getData());
    }

    protected Integer defaultLimit()
    {
        return 5000;
    }
}
