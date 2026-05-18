package io.github.fbbzl.starter.webflux;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import io.github.fbbzl.starter.audit.frame.annotation.AuditMethod;
import io.github.fbbzl.starter.core.util.Generics;
import io.github.fbbzl.starter.excel.BaseEo;
import io.github.fbbzl.starter.excel.ExcelDto;
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
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.Collection;

import static cn.hutool.core.io.FileMagicNumber.XLSX;
import static cn.hutool.core.text.CharSequenceUtil.appendIfMissing;
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
