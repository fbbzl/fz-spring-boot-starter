package com.fz.starter.web;

import cn.hutool.core.lang.tree.Tree;
import com.fz.starter.audit.frame.annotation.AuditMethod;
import com.fz.starter.core.util.Generics;
import com.fz.starter.dal.BaseDal;
import com.fz.starter.excel.BaseEo;
import com.fz.starter.excel.ExcelDto;
import com.fz.starter.pojo.bo.BaseBo;
import com.fz.starter.pojo.dto.BaseDto;
import com.fz.starter.pojo.entity.BaseTableEntity;
import com.fz.starter.pojo.mapstruct.BaseStructMapper;
import com.fz.starter.pojo.validation.group.CRUD;
import com.fz.starter.web.Q.FQ;
import com.fz.starter.web.Q.OQ;
import com.fz.starter.web.Q.PQ;
import com.fz.starter.web.R.PR;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static cn.hutool.core.util.ObjectUtil.defaultIfNull;
import static java.util.Collections.emptyList;
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
public abstract class BaseController<
        ID extends Serializable,
        ENTITY extends BaseTableEntity<ID>,
        DTO extends BaseDto<ID>,
        BO extends BaseBo<ID>,
        EO extends BaseEo>
{

    @Autowired
    BaseService<ID, ENTITY, DTO, BO, EO, ? extends BaseDal<ENTITY, ID>, ? extends BaseStructMapper<ENTITY, DTO, BO, EO>> service;
    @Autowired
    HttpServletRequest                                                                                                   request;
    @Autowired
    HttpServletResponse                                                                                                  response;

    Class<ENTITY> entityClass = Generics.getGenericSuperType(this.getClass(), BaseController.class, 1);
    Class<EO>     excelClass  = Generics.getGenericSuperType(this.getClass(), BaseController.class, 4);

    @Operation(description = "Based on the primary key query, it does not contain data that has been logically deleted", summary = "Query by primary key")
    @GetMapping("{id}")
    public R<BO> byId(
            @NotNull
            @PathVariable("id")
            @Parameter(name = "id", description = "the primary key of the query", required = true, example = "1")
            ID id)
    {
        return R.ok(service.byId(id));
    }

    @Operation(description = "Query by primary key set", summary = "Query by primary key set")
    @PostMapping("ids")
    public R<List<BO>> byIds(
            @NotNull
            @Parameter(description = "request contains ids", required = true, example = "1,2,3")
            @RequestBody
            Q<Set<ID>> req)
    {
        return R.ok(service.byIds(req.getData()));
    }

    @Operation(description = "List query, null fields do not participate in the query", summary = "List Query")
    @PostMapping({"list", "list/{limit}"})
    public R<List<BO>> list(
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
        return R.ok(service.list(req.getData(), limit, req.getOrders(), req.getRanges()));
    }

    @Operation(description = "For paginated query, null fields do not participate in query", summary = "Page query")
    @PostMapping("page")
    public PR<BO> page(
            @NotNull
            @Validated(CRUD.R.class)
            @Parameter(description = "pagination request", required = true)
            @RequestBody
            PQ<DTO> req)
    {
        return PR.ok(service.page(req.getPage(), req.getData()));
    }

    @Operation(description = "For tree query, null fields do not participate in query", summary = "Tree query, If it weren't be a tree type data, there would be no result")
    @PostMapping({"tree/{rootId}", "tree/{rootId}/{limit}"})
    public R<List<Tree<ID>>> tree(
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
        return R.ok(service.tree(rootId, req.getData(), defaultIfNull(limit, this.defaultLimit()), req.getOrders()));
    }

    @Operation(description = "Specify whether primary key data exists", summary = "Specifies whether primary key data exists")
    @GetMapping("exists/{id}")
    public R<Boolean> exists(
            @NotNull
            @PathVariable("id")
            @Parameter(name = "id", description = "the primary key of the query", required = true, example = "1")
            ID id)
    {
        return R.ok(service.exists(id));
    }

    @Operation(description = "Specify whether the condition data exists, and null fields will not participate in the query", summary = "Specifies whether conditional data exists")
    @PostMapping("exists")
    public R<Boolean> exists(
            @NotNull
            @Validated(CRUD.R.class)
            @Parameter(description = "request data", required = true)
            @RequestBody
            Q<DTO> req)
    {
        return R.ok(service.exists(req.getData()));
    }

    @AuditMethod
    @Operation(description = "Create data", summary = "Create data")
    @PostMapping
    public R<BO> create(
            @NotNull
            @Validated(CRUD.C.class)
            @Parameter(description = "creating data", required = true)
            @RequestBody
            Q<DTO> req)
    {
        return R.ok(service.create(req.getData()));
    }

    @AuditMethod(saveParam = false, saveResult = false)
    @Operation(description = "Create data batch", summary = "Create data batch")
    @PostMapping("batch")
    public R<Integer> createBatch(
            @NotNull
            @Parameter(description = "creating batch data", required = true)
            @RequestBody
            Q<Collection<DTO>> req)
    {
        return R.ok(service.create(req.getData()));
    }

    @AuditMethod
    @Operation(description = "Update without null fields", summary = "Do update ignore null field value")
    @PutMapping
    public R<Integer> update(
            @NotNull
            @Validated(CRUD.U.class)
            @Parameter(name = "req", description = "updating data", required = true)
            @RequestBody
            Q<DTO> req)
    {
        return R.ok(service.update(req.getData()));
    }

    @AuditMethod(saveParam = false, saveResult = false)
    @Operation(description = "Batch updates without null fields", summary = "Do batch update ignore null field value")
    @PutMapping("batch")
    public R<Integer> updateBatch(
            @NotNull
            @Parameter(name = "req", description = "batch updating data", required = true)
            @RequestBody
            Q<Collection<DTO>> req)
    {
        return R.ok(service.update(req.getData()));
    }

    @AuditMethod
    @Operation(description = "Deleting data is a logical deletion, but this tombstone deletion is equivalent to physical deletion, and the tombstone is only to maximize the value of the data", summary = "Delete data by primary key")
    @DeleteMapping("{id}")
    public R<Void> delete(
            @NotNull
            @PathVariable("id")
            @Parameter(name = "id", description = "The primary key of the record that needs to be deleted", required = true, example = "1")
            ID id)
    {
        service.delete(id);
        return R.ok();
    }

    @AuditMethod
    @Operation(description = "Deleting data is a logical deletion, but this tombstone deletion is equivalent to physical deletion, and the tombstone is only to maximize the value of the data", summary = "Delete data by primary key set")
    @DeleteMapping("ids")
    public R<Void> delete(
            @NotNull
            @Parameter(description = "request ids", required = true, example = "1,2,3")
            @RequestBody
            Q<Set<ID>> req)
    {
        service.delete(req.getData());
        return R.ok();
    }

    @Operation(description = "Get an excel template", summary = "The Excel template you need to use to get Excel upload data")
    @PostMapping("excel/template")
    public void excelTemplate(
            @NotNull
            @Validated(CRUD.R.class)
            @Parameter(description = "download excel template request", required = true)
            @RequestBody Q<ExcelDto<DTO>> req) throws IOException
    {
        ExcelDto<DTO> excelDto = req.getData();
        ExcelDto.setResponseHeader(response, excelDto);
        ExcelDto.doExport(response, emptyList(), excelClass, excelDto);
    }

    @AuditMethod(saveParam = false, saveResult = false)
    @Operation(description = "Excel to import", summary = "Excel data to import data")
    @PostMapping("excel/import")
    public R<Integer> importExcel(
            @NotNull
            @Validated(CRUD.C.class)
            @Parameter(description = "excel import object", required = true)
            FQ<DTO> req) throws IOException
    {
        List<EO> importData = ExcelDto.doImport(req.getSingleFile().getInputStream(), excelClass);
        return R.ok(service.importExcel(importData));
    }

    @AuditMethod(saveParam = false, saveResult = false)
    @Operation(description = "Excel export", summary = "Excel excel data")
    @PostMapping({"excel/export", "excel/export/{limit}"})
    public void exportExcel(
            @Nullable
            @Positive(message = "limit must be positive")
            @Parameter(description = "export data limit")
            @PathVariable(value = "limit", required = false)
            Integer limit,
            @NotNull
            @Validated(CRUD.R.class)
            @Parameter(description = "export excel request", required = true)
            @RequestBody OQ<ExcelDto<DTO>> req) throws IOException
    {
        ExcelDto<DTO> excelDto = req.getData();
        ExcelDto.setResponseHeader(response, excelDto);
        ExcelDto.doExport(response, service.exportExcel(excelDto.param(), defaultIfNull(limit, this.defaultLimit()), req.getOrders()), excelClass, excelDto);
    }

    protected Integer defaultLimit()
    {
        return 5000;
    }
}
