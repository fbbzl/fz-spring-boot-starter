package com.fz.starter.web;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.fz.starter.core.exception.ExceptionVerb;
import com.fz.starter.core.util.Generics;
import com.fz.starter.pojo.Q;
import com.fz.starter.pojo.Q.FQ;
import com.fz.starter.pojo.Q.PQ;
import com.fz.starter.pojo.R;
import com.fz.starter.pojo.R.PR;
import com.fz.starter.pojo.bo.BaseBo;
import com.fz.starter.pojo.dto.BaseDto;
import com.fz.starter.pojo.entity.BaseTableEntity;
import com.fz.starter.pojo.eo.BaseEo;
import com.fz.starter.pojo.eo.ExcelConfig;
import com.fz.starter.pojo.mapstruct.BaseStructMapper;
import com.fz.starter.pojo.validation.CRUD;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
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
        ENTITY extends BaseTableEntity,
        DTO    extends BaseDto<ENTITY>,
        BO     extends BaseBo<ENTITY>,
        EO     extends BaseEo<ENTITY>> {

    @Autowired BaseService<ENTITY, DTO, BO, EO, ? extends BaseStructMapper<ENTITY, DTO, BO, EO>> service;

    @Autowired HttpServletRequest  request;
    @Autowired HttpServletResponse response;

    Class<ENTITY> entityClass = Generics.getGenericType(this.getClass(), BaseController.class, 0);
    Class<EO>     excelClass  = Generics.getGenericType(this.getClass(), BaseController.class, 3);

    @Operation(description = "Based on the primary key query, it does not contain data that has been logically deleted", summary = "query based on the primary key")
    @GetMapping("{id}")
    public R<BO> byId(
            @NotNull
            @PathVariable("id")
            @Parameter(name = "id", description = "the primary key of the query", required = true, example = "1")
            Long id) {
        return R.ok(service.byId(id).orElseThrow(ExceptionVerb.RESOURCE_NOT_FOUND.on(entityClass, id)));
    }

    @Operation(description = "query based on the primary key set", summary = "query based on the primary key set")
    @PostMapping("ids")
    public R<List<BO>> byIds(
            @NotNull
            @Parameter(description = "request object contains ids", required = true)
            @RequestBody
            Q<Set<Long>> req) {
        return R.ok(service.byIds(req.getData()));
    }

    @Operation(description = "List queries, null fields do not participate in the query", summary = "query list")
    @PostMapping("list")
    public R<List<BO>> list(
            @NotNull
            @Validated(CRUD.R.class)
            @Parameter(description = "request object", required = true)
            @RequestBody
            Q<DTO> req) {
        return R.ok(service.list(req.getData()));
    }

    @Operation(description = "For paginated queries, null fields do not participate in queries", summary = "pagination query")
    @PostMapping("page")
    public PR<BO> page(
            @NotNull
            @Validated(CRUD.R.class)
            @Parameter(description = "pagination request object", required = true)
            @RequestBody
            PQ<DTO> req) {
        return PR.ok(service.page(req.getData(), req.getPage()));
    }

    @Operation(description = "Specify whether primary key data exists", summary = "specifies whether primary key data exists")
    @GetMapping("exists/{id}")
    public R<Boolean> exists(
            @NotNull
            @PathVariable("id")
            @Parameter(name = "id", description = "the primary key of the query", required = true, example = "1")
            Long id) {
        return R.ok(service.exists(id));
    }

    @Operation(description = "Specify whether the condition data exists, and null fields will not participate in the query", summary = "specifies whether conditional data exists")
    @PostMapping("exists")
    public R<Boolean> exists(
            @NotNull
            @Validated(CRUD.R.class)
            @Parameter(description = "request data", required = true)
            @RequestBody
            Q<DTO> req) {
        return R.ok(service.exists(req.getData()));
    }

    @Operation(description = "create data", summary = "create data")
    @PostMapping
    public R<BO> create(
            @NotNull
            @Validated(CRUD.C.class)
            @Parameter(description = "creating data", required = true)
            @RequestBody
            Q<DTO> req) {
        return R.ok(service.create(req.getData()));
    }

    @Operation(description = "create data batch", summary = "create data batch")
    @PostMapping("batch")
    public R<Integer> createBatch(
            @NotNull
            @Parameter(description = "creating batch data", required = true)
            @RequestBody
            Q<Collection<DTO>> req) {
        return R.ok(service.create(req.getData()));
    }

    @Operation(description = "update without null fields", summary = "do update ignore null field value")
    @PatchMapping
    public R<Integer> update(
            @NotNull
            @Validated(CRUD.U.class)
            @Parameter(name = "req", description = "updating data", required = true)
            @RequestBody
            Q<DTO> req) {
        return R.ok(service.update(req.getData()));
    }

    @Operation(description = " batch updates without null fields", summary = "do batch update ignore null field value")
    @PatchMapping("batch")
    public R<Integer> updateBatch(
            @NotNull
            @Parameter(name = "req", description = "batch updating data", required = true)
            @RequestBody
            Q<Collection<DTO>> req) {
        return R.ok(service.update(req.getData()));
    }

    @Operation(description = "Deleting data is a logical deletion, but this tombstone deletion is equivalent to physical deletion, and the tombstone is only to maximize the value of the data", summary = "delete data")
    @DeleteMapping("{id}")
    public R<Void> delete(
            @NotNull
            @PathVariable("id")
            @Parameter(name = "id", description = "The primary key of the record that needs to be deleted", required = true, example = "1")
            Long id) {
        service.delete(id);
        return R.ok();
    }

    @Operation(description = "Deleting data is a logical deletion, but this tombstone deletion is equivalent to physical deletion, and the tombstone is only to maximize the value of the data", summary = "delete data")
    @DeleteMapping("ids")
    public R<Void> delete(
            @NotNull
            @Parameter(description = "request ids", required = true)
            @RequestBody
            Q<Set<Long>> req) {
        service.delete(req.getData());
        return R.ok();
    }

    @Operation(description = "get an excel template", summary = "The Excel template you need to use to get Excel upload data")
    @PostMapping("excel/template")
    public void excelTemplate(
            @NotNull
            @Validated(CRUD.R.class)
            @Parameter(description = "download excel template request", required = true)
            @RequestBody Q<ExcelConfig<DTO>> req) throws IOException {
        ExcelConfig<DTO> excelCfg = req.getData();
        this.setResponseHeader(excelCfg);
        this.doExport(emptyList(), excelCfg);
    }

    @Operation(description = "excel to import", summary = "excel data to import data")
    @PostMapping("excel/import")
    public R<Integer> importExcel(
            @NotNull
            @Validated(CRUD.C.class)
            @Parameter(description = "excel import object", required = true)
            FQ<DTO> req) throws IOException {
        List<EO> importData =
                EasyExcel.read(req.getFile().getInputStream())
                         .head(excelClass)
                         .headRowNumber(1)
                         .sheet()
                         .doReadSync();
        return R.ok(service.importExcel(importData));
    }

    @Operation(description = "excel export", summary = "export excel data")
    @PostMapping("excel/export")
    public void exportExcel(
            @NotNull
            @Validated(CRUD.R.class)
            @Parameter(description = "export excel request", required = true)
            @RequestBody Q<ExcelConfig<DTO>> req) throws IOException {
        ExcelConfig<DTO> excelCfg = req.getData();
        this.setResponseHeader(excelCfg);
        this.doExport(service.exportExcel(excelCfg.param()), excelCfg);
    }

    //******************************************       protected start      ******************************************//

    protected void setResponseHeader(ExcelConfig<?> excelCfg) {
        response.setContentType(XLSX.getMimeType());

        String filename = appendIfMissing(excelCfg.fileName(), "." + XLSX);

        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + filename);
    }

    protected void doExport(Collection<EO> excelData, ExcelConfig<DTO> config) throws IOException {
        try (OutputStream os = response.getOutputStream()) {
            EasyExcel.write(os)
                     .head(excelClass)
                     .sheet(config.sheetName())
                     .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                     .doWrite(excelData);
        }
    }
}
