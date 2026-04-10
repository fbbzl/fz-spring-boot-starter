package com.fz.starter.web;

import cn.hutool.core.lang.tree.Tree;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.fz.starter.core.util.Generics;
import com.fz.starter.dal.BaseDal;
import com.fz.starter.pojo.bo.BaseBo;
import com.fz.starter.pojo.dto.BaseDto;
import com.fz.starter.pojo.entity.BaseTableEntity;
import com.fz.starter.pojo.eo.BaseEo;
import com.fz.starter.pojo.eo.ExcelDownload;
import com.fz.starter.pojo.mapstruct.BaseStructMapper;
import com.fz.starter.pojo.validation.group.CRUD;
import com.fz.starter.web.Q.FQ;
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
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static cn.hutool.core.io.FileMagicNumber.XLSX;
import static cn.hutool.core.text.CharSequenceUtil.appendIfMissing;
import static java.util.Collections.emptyList;
import static lombok.AccessLevel.PROTECTED;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/22 14:24
 */
@Slf4j
@Validated
@FieldDefaults(level = PROTECTED)
public abstract class BaseController<
        ID     extends Serializable,
        ENTITY extends BaseTableEntity<ID>,
        DTO    extends BaseDto<ID>,
        BO     extends BaseBo<ID>,
        EO     extends BaseEo>
{

    @Autowired BaseService<ID, ENTITY, DTO, BO, EO, ? extends BaseDal<ENTITY, ID>, ? extends BaseStructMapper<ENTITY, DTO, BO, EO>> service;
    @Autowired HttpServletRequest  request;
    @Autowired HttpServletResponse response;

    Class<ENTITY> entityClass = Generics.getGenericSuperType(this.getClass(), BaseController.class, 1);
    Class<EO>     excelClass  = Generics.getGenericSuperType(this.getClass(), BaseController.class, 4);

    @Operation(description = "Based on the primary key query, it does not contain data that has been logically deleted", summary = "Query by primary key")
    @GetMapping("{id}")
    public R<Optional<BO>> byId(
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
    @PostMapping("list")
    public R<List<BO>> list(
            @NotNull
            @Validated(CRUD.R.class)
            @Parameter(description = "request", required = true)
            @RequestBody
            Q<DTO> req)
    {
        return R.ok(service.list(req.getData()));
    }

    @Operation(description = "Limited list query, null fields do not participate in the query", summary = "Limited list Query")
    @PostMapping( "limit/{limit}")
    public R<List<BO>> limit(
            @Positive(message = "limit must be positive")
            @Parameter(description = "list data limit", required = true)
            @PathVariable("limit")
            Integer limit,
            @NotNull
            @Validated(CRUD.R.class)
            @Parameter(description = "request", required = true)
            @RequestBody
            Q<DTO> req)
    {
        return R.ok(service.limit(req.getData(), limit));
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
    @PostMapping("tree/{rootId}")
    public R<List<Tree<ID>>> tree(
            @NotNull
            @PathVariable("rootId")
            @Parameter(name = "rootId", description = "the root-id of the tree", required = true, example = "1")
            ID rootId,
            @NotNull
            @Validated(CRUD.R.class)
            @Parameter(description = "tree request", required = true)
            @RequestBody
            Q<DTO> req)
    {
        return R.ok(service.tree(rootId, req.getData()));
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
            @RequestBody Q<ExcelDownload<DTO>> req) throws IOException
    {
        ExcelDownload<DTO> excelCfg = req.getData();
        this.setResponseHeader(excelCfg);
        this.doExport(emptyList(), excelCfg);
    }

    @Operation(description = "Excel to import", summary = "Excel data to import data")
    @PostMapping("excel/import")
    public R<Integer> importExcel(
            @NotNull
            @Validated(CRUD.C.class)
            @Parameter(description = "excel import object", required = true)
            FQ<DTO> req) throws IOException
    {
        List<EO> importData =
                EasyExcelFactory.read(req.getSingleFile().getInputStream())
                                .head(excelClass)
                                .headRowNumber(1)
                                .sheet()
                                .doReadSync();
        return R.ok(service.importExcel(importData));
    }

    @Operation(description = "Excel export", summary = "Excel excel data")
    @PostMapping("excel/export")
    public void exportExcel(
            @NotNull
            @Validated(CRUD.R.class)
            @Parameter(description = "export excel request", required = true)
            @RequestBody Q<ExcelDownload<DTO>> req) throws IOException
    {
        ExcelDownload<DTO> excelCfg = req.getData();
        this.setResponseHeader(excelCfg);
        this.doExport(service.exportExcel(excelCfg.param()), excelCfg);
    }

    //******************************************       protected start      ******************************************//

    protected void setResponseHeader(ExcelDownload<?> excelCfg)
    {
        response.setContentType(XLSX.getMimeType());

        String filename = appendIfMissing(excelCfg.fileName(), "." + XLSX);

        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + filename);
    }

    protected void doExport(Collection<EO> excelData, ExcelDownload<DTO> config) throws IOException
    {
        try (OutputStream os = response.getOutputStream()) {
            EasyExcelFactory.write(os)
                            .head(excelClass)
                            .sheet(config.sheetName())
                            .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                            .doWrite(excelData);
        }
    }
}
