package io.github.fbbzl.starter.web;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.tree.Tree;
import io.github.fbbzl.starter.audit.frame.annotation.AuditMethod;
import io.github.fbbzl.starter.pojo.bo.BaseBo;
import io.github.fbbzl.starter.pojo.dto.BaseDto;
import io.github.fbbzl.starter.pojo.entity.BaseTableEntity;
import io.github.fbbzl.starter.pojo.validation.group.CRUD;
import io.github.fbbzl.starter.web.Q.OQ;
import io.github.fbbzl.starter.web.Q.PQ;
import io.github.fbbzl.starter.web.R.PR;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.groups.Default;
import lombok.experimental.FieldDefaults;
import org.fz.erwin.lang.Generics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static cn.hutool.core.util.ObjectUtil.defaultIfNull;
import static lombok.AccessLevel.PROTECTED;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/22 14:24
 */
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

    /**
     * [BASE] Based on the primary key query, it does not contain data that has been logically deleted
     */
    @GetMapping("{id}")
    public BO byId(
            @NotNull
            @PathVariable("id")
            // the primary key of the query
            ID id)
    {
        return service.byId(id);
    }

    /**
     * [BASE] Query by primary key set
     */
    @PostMapping("ids")
    public List<BO> byIds(
            @NotNull
            @Validated(CRUD.R.class)
            @RequestBody
            Q<Set<ID>> req)
    {
        return service.byIds(req.getData());
    }

    /**
     * [BASE] List query, null fields do not participate in the query
     */
    @PostMapping({"list", "list/{limit}"})
    public List<BO> list(
            @Nullable
            @Positive(message = "limit must be positive")
            // list data limit
            @PathVariable(value = "limit", required = false)
            Integer limit,
            @NotNull
            @Validated(CRUD.R.class)
            @RequestBody
            OQ<DTO> req)
    {
        return service.list(req.getData(), Math.min(defaultIfNull(limit, this.defaultLimit()), this.defaultLimit()), req.getOrders(), req.getRanges());
    }

    /**
     * [BASE] For paginated query, null fields do not participate in query
     */
    @PostMapping("page")
    public PR<BO> page(
            @NotNull
            @Validated(CRUD.R.class)
            @RequestBody
            PQ<DTO> req)
    {
        return PR.of(service.page(req.getPage(), req.getData()));
    }

    /**
     * [BASE] For tree query, null fields do not participate in query
     */
    @PostMapping({"tree/{rootId}", "tree/{rootId}/{limit}"})
    public List<Tree<ID>> tree(
            @NotNull
            @PathVariable("rootId")
            // the root-id of the tree
            ID rootId,
            @Nullable
            @Positive(message = "limit must be positive")
            // tree data limit
            @PathVariable(value = "limit", required = false)
            Integer limit,
            @NotNull
            @Validated(CRUD.R.class)
            @RequestBody
            OQ<DTO> req)
    {
        return service.tree(rootId, req.getData(), Math.min(defaultIfNull(limit, this.defaultLimit()), this.defaultLimit()), req.getOrders(), req.getRanges());
    }

    /**
     * [BASE] Specify whether primary key data exists
     */
    @GetMapping("exists/{id}")
    public boolean exists(
            @NotNull
            @PathVariable("id")
            // the primary key of the query
            ID id)
    {
        return service.exists(id);
    }

    /**
     * [BASE] Specify whether the condition data exists, and null fields will not participate in the query
     */
    @PostMapping("exists")
    public boolean exists(
            @NotNull
            @Validated(CRUD.R.class)
            @RequestBody
            Q<DTO> req)
    {
        return service.exists(req.getData());
    }

    /**
     * [BASE] Count by condition, null fields do not participate in the query
     */
    @PostMapping("count")
    public long count(
            @NotNull
            @Validated(CRUD.R.class)
            @RequestBody
            Q<DTO> req)
    {
        return service.count(req.getData());
    }

    /**
     * [BASE] Query IDs by condition, null fields do not participate
     */
    @PostMapping({"ids/query", "ids/query/{limit}"})
    public List<ID> ids(
            @Nullable
            @Positive(message = "limit must be positive")
            // ids query limit
            @PathVariable(value = "limit", required = false)
            Integer limit,
            @NotNull
            @Validated(CRUD.R.class)
            @RequestBody
            Q<DTO> req)
    {
        if (limit != null) return service.ids(req.getData(), Math.min(limit, this.defaultLimit()));
        else               return service.ids(req.getData());
    }
    @AuditMethod
    /**
     * [BASE] Create data
     */
    @PostMapping
    public BO create(
            @NotNull
            @Validated(CRUD.C.class)
            @RequestBody
            Q<DTO> req)
    {
        return service.create(req.getData());
    }

    /**
     * [BASE] Create data batch
     */
    @AuditMethod(saveParam = false, saveResult = false)
    @PostMapping("batch")
    public List<BO> createBatch(
            @NotNull
            @Validated(CRUD.C.class)
            @RequestBody
            Q<Collection<DTO>> req)
    {
        return service.create(req.getData());
    }

    @AuditMethod
    /**
     * [BASE] Update without null fields
     */
    @PutMapping
    public int update(
            @NotNull
            @Validated(CRUD.U.class)
            @RequestBody
            Q<DTO> req)
    {
        return service.update(req.getData());
    }

    /**
     * [BASE] Batch updates without null fields
     */
    @AuditMethod(saveParam = false, saveResult = false)
    @PutMapping("batch")
    public int updateBatch(
            @NotNull
            @Validated(CRUD.U.class)
            @RequestBody
            Q<Collection<DTO>> req)
    {
        return service.update(req.getData());
    }

    @AuditMethod
    /**
     * [BASE] Update by map without null fields
     */
    @PatchMapping("{id}")
    public BO patch(
            @NotNull
            @PathVariable("id")
            // The primary key of the record that needs to be update
            ID id,
            @Validated({Default.class})
            @NotNull
            @RequestBody
            Q<DTO> req)
    {
        DTO dto = req.getData();
        dto.setId(id);
        return service.patch(BeanUtil.beanToMap(dto, false, false));
    }

    @AuditMethod
    /**
     * [BASE] Deleting data is a logical deletion, but this tombstone deletion is equivalent to physical deletion, and the tombstone is only to maximize the value of the data
     */
    @DeleteMapping("{id}")
    public void delete(
            @NotNull
            @PathVariable("id")
            // The primary key of the record that needs to be deleted
            ID id)
    {
        service.delete(id);
    }

    @AuditMethod
    @DeleteMapping("ids")
    public void delete(
            @NotNull
            @Validated(CRUD.D.class)
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
