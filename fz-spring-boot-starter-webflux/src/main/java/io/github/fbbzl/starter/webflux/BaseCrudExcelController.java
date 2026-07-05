package io.github.fbbzl.starter.webflux;

import io.github.fbbzl.starter.audit.frame.annotation.AuditMethod;
import io.github.fbbzl.starter.excel.BaseEo;
import io.github.fbbzl.starter.excel.ExcelDto;
import io.github.fbbzl.starter.excel.ExcelResponseEntity;
import io.github.fbbzl.starter.pojo.bo.BaseBo;
import io.github.fbbzl.starter.pojo.dto.BaseDto;
import io.github.fbbzl.starter.pojo.entity.BaseTableEntity;
import io.github.fbbzl.starter.pojo.validation.group.CRUD;
import io.github.fbbzl.starter.webflux.Q.FQ;
import io.github.fbbzl.starter.webflux.Q.OQ;
import io.github.fbbzl.starter.webflux.annotation.IgnoreResponseWrap;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.groups.Default;
import org.fz.erwin.lang.Generics;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.Objects;

import static cn.hutool.core.util.ObjectUtil.defaultIfNull;
import static java.util.Collections.emptyList;

/**
 * Reactive CRUD controller base with Excel template, import, and export endpoints.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026/5/18
 */
public abstract class BaseCrudExcelController<
        ID      extends Serializable,
        ENTITY  extends BaseTableEntity<ID>,
        SERVICE extends BaseCrudExcelService<ID, ENTITY, DTO, BO, EO, ?, ?>,
        DTO     extends BaseDto<ID>,
        BO      extends BaseBo<ID>,
        EO      extends BaseEo>
        extends BaseCrudController<ID, ENTITY, SERVICE, DTO, BO>
{

    Class<EO> excelClass = Generics.getGenericSuperType(this.getClass(), BaseCrudExcelController.class, 5);

    @Operation(description = "[BASE] Get an excel template", summary = "[BASE] The Excel template you need to use to get Excel upload data")
    @PostMapping("excel/template")
    @IgnoreResponseWrap
    public Mono<ExcelResponseEntity> excelTemplate(
            @NotNull
            @Validated({Default.class, CRUD.R.class})
            @Parameter(description = "download excel template request", required = true)
            @RequestBody Q<ExcelDto<DTO>> req)
    {
        ExcelDto<DTO> excelDto = Objects.requireNonNull(req.getData(), "excelDto is required");
        return Mono.fromCallable(() -> ExcelDto.doExportToBytes(emptyList(), excelClass, excelDto))
                   .subscribeOn(Schedulers.boundedElastic())
                   .map(bytes -> ExcelResponseEntity.of(bytes, excelDto.fileName()));
    }

    @AuditMethod(saveParam = false, saveResult = false)
    @Operation(description = "[BASE] Excel to import", summary = "[BASE] Excel data to import data")
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
    @Operation(description = "[BASE] Excel export", summary = "[BASE] Excel excel data")
    @PostMapping({"excel/export", "excel/export/{limit}"})
    @IgnoreResponseWrap
    public Mono<ExcelResponseEntity> exportExcel(
            @Nullable
            @Positive(message = "limit must be positive")
            @Parameter(description = "export data limit")
            @PathVariable(value = "limit", required = false)
            Integer limit,
            @NotNull
            @Validated({Default.class, CRUD.R.class})
            @Parameter(description = "export excel request", required = true)
            @RequestBody OQ<ExcelDto<DTO>> req)
    {
        ExcelDto<DTO> excelDto = Objects.requireNonNull(req.getData(), "excelDto is required");
        int           exportLimit = Math.min(defaultIfNull(limit, this.defaultLimit()), this.defaultLimit());
        return Mono.fromCallable(() -> service.exportExcel(excelDto.param(), exportLimit, req.getOrders()))
                   .subscribeOn(Schedulers.boundedElastic())
                   .map(excelData -> ExcelDto.doExportToBytes(excelData, excelClass, excelDto))
                   .map(bytes -> ExcelResponseEntity.of(bytes, excelDto.fileName()));
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
}
