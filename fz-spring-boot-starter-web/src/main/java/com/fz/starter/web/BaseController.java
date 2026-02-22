package com.fz.starter.web;

import static cn.hutool.core.io.FileMagicNumber.XLSX;
import static cn.hutool.core.text.CharSequenceUtil.appendIfMissing;
import static java.util.Collections.emptyList;
import static lombok.AccessLevel.PROTECTED;

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
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping("base/{id}")
    public R<BO> baseById(
            @NotNull
            @PathVariable("id")
            @Parameter(name = "id", description = "the primary key of the query", required = true, example = "1")
            Long id) {
        return R.ok(service.baseById(id).orElseThrow(ExceptionVerb.RESOURCE_NOT_FOUND.on(entityClass, id)));
    }

    @Operation(description = "query based on the primary key set", summary = "query based on the primary key set")
    @PostMapping("base/ids")
    public R<List<BO>> baseByIds(
            @NotNull
            @Parameter(description = "request object contains ids", required = true)
            @RequestBody
            Q<Set<Long>> req) {
        return R.ok(service.baseByIds(req.getData()));
    }

    @Operation(description = "List queries, null fields do not participate in the query", summary = "query baseList")
    @PostMapping("base/list")
    public R<List<BO>> baseList(
            @NotNull
            @Validated(CRUD.R.class)
            @Parameter(description = "request object", required = true)
            @RequestBody
            Q<DTO> req) {
        return R.ok(service.baseList(req.getData()));
    }

    @Operation(description = "For paginated queries, null fields do not participate in queries", summary = "pagination query")
    @PostMapping("base/page")
    public PR<BO> basePage(
            @NotNull
            @Validated(CRUD.R.class)
            @Parameter(description = "pagination request object", required = true)
            @RequestBody
            PQ<DTO> req) {
        return PR.ok(service.basePage(req.getData(), req.getPage()));
    }

    @Operation(description = "specifies whether primary key data baseExists", summary = "specifies whether primary key data baseExists")
    @GetMapping("base/exists/{id}")
    public R<Boolean> baseExists(
            @NotNull
            @PathVariable("id")
            @Parameter(name = "id", description = "the primary key of the query", required = true, example = "1")
            Long id) {
        return R.ok(service.baseExists(id));
    }

    @Operation(description = "Specify whether the condition data baseExists, and null fields will not participate in the query", summary = "specifies whether conditional data baseExists")
    @PostMapping("base/exists")
    public R<Boolean> baseExists(
            @NotNull
            @Validated(CRUD.R.class)
            @Parameter(description = "request data", required = true)
            @RequestBody
            Q<DTO> req) {
        return R.ok(service.baseExists(req.getData()));
    }

    @Operation(description = "create data", summary = "baseCreate data")
    @PostMapping("base")
    public R<BO> baseCreate(
            @NotNull
            @Validated(CRUD.C.class)
            @Parameter(description = "creating data", required = true)
            @RequestBody
            Q<DTO> req) {
        return R.ok(service.baseCreate(req.getData()));
    }

    @Operation(description = "create data batch", summary = "baseCreate data batch")
    @PostMapping("base/batch")
    public R<Integer> baseCreateBatch(
            @NotNull
            @Parameter(description = "creating batch data", required = true)
            @RequestBody
            Q<Collection<DTO>> req) {
        return R.ok(service.baseCreate(req.getData()));
    }

    @Operation(description = "update without null fields", summary = "do baseUpdate ignore null field value")
    @PatchMapping("base")
    public R<Integer> baseUpdate(
            @NotNull
            @Validated(CRUD.U.class)
            @Parameter(name = "req", description = "updating data", required = true)
            @RequestBody
            Q<DTO> req) {
        return R.ok(service.baseUpdate(req.getData()));
    }

    @Operation(description = " batch updates without null fields", summary = "do batch baseUpdate ignore null field value")
    @PatchMapping("base/batch")
    public R<Integer> baseUpdateBatch(
            @NotNull
            @Parameter(name = "req", description = "batch updating data", required = true)
            @RequestBody
            Q<Collection<DTO>> req) {
        return R.ok(service.baseUpdate(req.getData()));
    }

    @Operation(description = "Deleting data is a logical deletion, but this tombstone deletion is equivalent to physical deletion, and the tombstone is only to maximize the value of the data", summary = "baseDelete data")
    @DeleteMapping("base/{id}")
    public R<Void> baseDelete(
            @NotNull
            @PathVariable("id")
            @Parameter(name = "id", description = "The primary key of the record that needs to be deleted", required = true, example = "1")
            Long id) {
        service.baseDelete(id);
        return R.ok();
    }

    @Operation(description = "Deleting data is a logical deletion, but this tombstone deletion is equivalent to physical deletion, and the tombstone is only to maximize the value of the data", summary = "baseDelete data")
    @DeleteMapping("base/ids")
    public R<Void> baseDelete(
            @NotNull
            @Parameter(description = "request ids", required = true)
            @RequestBody
            Q<Set<Long>> req) {
        service.baseDelete(req.getData());
        return R.ok();
    }

    @Operation(description = "get an excel template", summary = "The Excel template you need to use to get Excel upload data")
    @PostMapping("base/template")
    public void baseTemplate(
            @NotNull
            @Validated(CRUD.R.class)
            @Parameter(description = "download excel template request", required = true)
            @RequestBody Q<ExcelConfig<DTO>> req) throws IOException {
        ExcelConfig<DTO> excelCfg = req.getData();
        this.setResponseHeader(excelCfg);
        this.doBaseExport(emptyList(), excelCfg);
    }

    @Operation(description = "excel upload to import", summary = "upload excel data to import data")
    @PostMapping("base/import")
    public R<Integer> baseImport(
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
        return R.ok(service.baseImport(importData));
    }

    @Operation(description = "excel export", summary = "export excel data")
    @PostMapping("base/export")
    public void baseExport(
            @NotNull
            @Validated(CRUD.R.class)
            @Parameter(description = "export excel request", required = true)
            @RequestBody Q<ExcelConfig<DTO>> req) throws IOException {
        ExcelConfig<DTO> excelCfg = req.getData();
        this.setResponseHeader(excelCfg);
        this.doBaseExport(service.baseExport(excelCfg.param()), excelCfg);
    }

    //******************************************       protected start      ******************************************//

    protected void setResponseHeader(ExcelConfig<?> excelCfg) {
        response.setContentType(XLSX.getMimeType());

        String filename = appendIfMissing(excelCfg.fileName(), "." + XLSX);

        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + filename);
    }

    protected void doBaseExport(Collection<EO> excelData, ExcelConfig<DTO> config) throws IOException {
        try (OutputStream os = response.getOutputStream()) {
            EasyExcel.write(os)
                     .head(excelClass)
                     .sheet(config.sheetName())
                     .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                     .doWrite(excelData);
        }
    }
}
