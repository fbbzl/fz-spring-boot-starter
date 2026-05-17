package io.github.fbbzl.starter.webflux;

import cn.hutool.core.lang.tree.Tree;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import io.github.fbbzl.starter.audit.frame.annotation.AuditMethod;
import io.github.fbbzl.starter.core.util.Generics;
import io.github.fbbzl.starter.dal.BaseDal;
import io.github.fbbzl.starter.excel.BaseEo;
import io.github.fbbzl.starter.excel.ExcelDto;
import io.github.fbbzl.starter.pojo.bo.BaseBo;
import io.github.fbbzl.starter.pojo.dto.BaseDto;
import io.github.fbbzl.starter.pojo.entity.BaseTableEntity;
import io.github.fbbzl.starter.pojo.mapstruct.BaseStructMapper;
import io.github.fbbzl.starter.pojo.validation.group.CRUD;
import io.github.fbbzl.starter.webflux.Q.FQ;
import io.github.fbbzl.starter.webflux.Q.OQ;
import io.github.fbbzl.starter.webflux.Q.PQ;
import io.github.fbbzl.starter.webflux.R.PR;
import io.github.fbbzl.starter.webflux.annotation.IgnoreResponseWrap;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static cn.hutool.core.io.FileMagicNumber.XLSX;
import static cn.hutool.core.text.CharSequenceUtil.appendIfMissing;
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
        ID      extends Serializable,
        ENTITY  extends BaseTableEntity<ID>,
        SERVICE extends BaseService<ID, ENTITY, DTO, BO, EO, ? extends BaseDal<ENTITY, ID>, ? extends BaseStructMapper<ENTITY, DTO, BO, EO>>,
        DTO     extends BaseDto<ID>,
        BO      extends BaseBo<ID>,
        EO      extends BaseEo>
{

    @Autowired
    SERVICE service;

    Class<ENTITY> entityClass = Generics.getGenericSuperType(this.getClass(), BaseController.class, 1);
    Class<EO>     excelClass  = Generics.getGenericSuperType(this.getClass(), BaseController.class, 5);

    @Operation(description = "Based on the primary key query, it does not contain data that has been logically deleted", summary = "Query by primary key")
    @GetMapping("{id}")
    public BO byId(
            @NotNull
            @PathVariable("id")
            @Parameter(name = "id", description = "the primary key of the query", required = true, example = "1")
            ID id)
    {
        return service.byId(id);
    }

    @Operation(description = "Query by primary key set", summary = "Query by primary key set")
    @PostMapping("ids")
    public List<BO> byIds(
            @NotNull
            @Parameter(description = "request contains ids", required = true, example = "1,2,3")
            @RequestBody
            Q<Set<ID>> req)
    {
        return service.byIds(req.getData());
    }

    @Operation(description = "List query, null fields do not participate in the query", summary = "List Query")
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

    @Operation(description = "For paginated query, null fields do not participate in query", summary = "Page query")
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

    @Operation(description = "For tree query, null fields do not participate in query", summary = "Tree query, If it weren't be a tree type data, there would be no result")
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

    @Operation(description = "Specify whether primary key data exists", summary = "Specifies whether primary key data exists")
    @GetMapping("exists/{id}")
    public boolean exists(
            @NotNull
            @PathVariable("id")
            @Parameter(name = "id", description = "the primary key of the query", required = true, example = "1")
            ID id)
    {
        return service.exists(id);
    }

    @Operation(description = "Specify whether the condition data exists, and null fields will not participate in the query", summary = "Specifies whether conditional data exists")
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

    @AuditMethod
    @Operation(description = "Create data", summary = "Create data")
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
    @Operation(description = "Create data batch", summary = "Create data batch")
    @PostMapping("batch")
    public void createBatch(
            @NotNull
            @Parameter(description = "creating batch data", required = true)
            @RequestBody
            Q<Collection<DTO>> req)
    {
        service.create(req.getData());
    }

    @AuditMethod
    @Operation(description = "Update without null fields", summary = "Do update ignore null field value")
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
    @Operation(description = "Batch updates without null fields", summary = "Do batch update ignore null field value")
    @PutMapping("batch")
    public void updateBatch(
            @NotNull
            @Parameter(name = "req", description = "batch updating data", required = true)
            @RequestBody
            Q<Collection<DTO>> req)
    {
        service.update(req.getData());
    }

    @AuditMethod
    @Operation(description = "Deleting data is a logical deletion, but this tombstone deletion is equivalent to physical deletion, and the tombstone is only to maximize the value of the data", summary = "Delete data by primary key")
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
    @Operation(description = "Deleting data is a logical deletion, but this tombstone deletion is equivalent to physical deletion, and the tombstone is only to maximize the value of the data", summary = "Delete data by primary key set")
    @DeleteMapping("ids")
    public void delete(
            @NotNull
            @Parameter(description = "request ids", required = true, example = "1,2,3")
            @RequestBody
            Q<Set<ID>> req)
    {
        service.delete(req.getData());
    }

    @Operation(description = "Get an excel template", summary = "The Excel template you need to use to get Excel upload data")
    @PostMapping("excel/template")
    @IgnoreResponseWrap
    public Mono<Void> excelTemplate(
            @NotNull
            @Validated(CRUD.R.class)
            @Parameter(description = "download excel template request", required = true)
            @RequestBody Q<ExcelDto<DTO>> req,
            ServerHttpResponse response)
    {
        ExcelDto<DTO> excelDto = req.getData();
        return writeExcel(response, emptyList(), excelDto);
    }

    @AuditMethod(saveParam = false, saveResult = false)
    @Operation(description = "Excel to import", summary = "Excel data to import data")
    @PostMapping(value = "excel/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<R<Void>> importExcel(
            @NotNull
            @Validated(CRUD.C.class)
            @Parameter(description = "excel import object", required = true)
            @ModelAttribute FQ req)
    {
        return readFile(req.getSingleFile())
                .publishOn(Schedulers.boundedElastic())
                .map(bytes -> ExcelDto.doRead(new ByteArrayInputStream(bytes), excelClass))
                .doOnNext(service::importExcel)
                .thenReturn(R.ok());
    }

    @AuditMethod(saveParam = false, saveResult = false)
    @Operation(description = "Excel export", summary = "Excel excel data")
    @PostMapping({"excel/export", "excel/export/{limit}"})
    @IgnoreResponseWrap
    public Mono<Void> exportExcel(
            @Nullable
            @Positive(message = "limit must be positive")
            @Parameter(description = "export data limit")
            @PathVariable(value = "limit", required = false)
            Integer limit,
            @NotNull
            @Validated(CRUD.R.class)
            @Parameter(description = "export excel request", required = true)
            @RequestBody OQ<ExcelDto<DTO>> req,
            ServerHttpResponse response)
    {
        ExcelDto<DTO> excelDto = req.getData();
        return Mono.fromCallable(() -> service.exportExcel(excelDto.param(), defaultIfNull(limit, this.defaultLimit()), req.getOrders()))
                   .subscribeOn(Schedulers.boundedElastic())
                   .flatMap(excelData -> writeExcel(response, excelData, excelDto));
    }

    protected Integer defaultLimit()
    {
        return 5000;
    }

    private Mono<byte[]> readFile(FilePart file)
    {
        return DataBufferUtils.join(file.content())
                              .map(dataBuffer -> {
                                  try {
                                      byte[] bytes = new byte[dataBuffer.readableByteCount()];
                                      dataBuffer.read(bytes);
                                      return bytes;
                                  }
                                  finally {
                                      DataBufferUtils.release(dataBuffer);
                                  }
                              });
    }

    private Mono<Void> writeExcel(ServerHttpResponse response, Collection<EO> excelData, ExcelDto<?> config)
    {
        return Mono.fromCallable(() -> {
                       ByteArrayOutputStream os = new ByteArrayOutputStream();
                       EasyExcelFactory.write(os)
                                       .head(excelClass)
                                       .sheet(config.sheetName())
                                       .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                                       .doWrite(excelData);
                       return os.toByteArray();
                   })
                   .subscribeOn(Schedulers.boundedElastic())
                   .flatMap(bytes -> {
                       setExcelResponseHeader(response, config);
                       DataBuffer buffer = response.bufferFactory().wrap(bytes);
                       return response.writeWith(Mono.just(buffer));
                   });
    }

    private void setExcelResponseHeader(ServerHttpResponse response, ExcelDto<?> excelCfg)
    {
        response.getHeaders().setContentType(MediaType.parseMediaType(XLSX.getMimeType()));
        String filename = appendIfMissing(excelCfg.fileName(), "." + XLSX.getExtension());
        response.getHeaders().set(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + filename);
    }
}
