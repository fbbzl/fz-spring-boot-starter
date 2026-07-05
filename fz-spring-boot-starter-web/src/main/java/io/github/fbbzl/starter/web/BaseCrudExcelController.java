package io.github.fbbzl.starter.web;

import io.github.fbbzl.starter.audit.frame.annotation.AuditMethod;
import io.github.fbbzl.starter.excel.BaseEo;
import io.github.fbbzl.starter.excel.ExcelDto;
import io.github.fbbzl.starter.excel.ExcelResponseEntity;
import io.github.fbbzl.starter.pojo.bo.BaseBo;
import io.github.fbbzl.starter.pojo.dto.BaseDto;
import io.github.fbbzl.starter.pojo.entity.BaseTableEntity;
import io.github.fbbzl.starter.pojo.validation.group.CRUD;
import io.github.fbbzl.starter.web.Q.FQ;
import io.github.fbbzl.starter.web.Q.OQ;
import io.github.fbbzl.starter.web.annotation.IgnoreResponseWrap;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.groups.Default;
import org.fz.erwin.lang.Generics;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import static cn.hutool.core.util.ObjectUtil.defaultIfNull;
import static java.util.Collections.emptyList;

/**
 * CRUD controller base with Excel template, import, and export endpoints.
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
    @IgnoreResponseWrap
    @PostMapping("excel/template")
    public ExcelResponseEntity excelTemplate(
            @NotNull
            @Validated({Default.class, CRUD.R.class})
            @RequestBody Q<ExcelDto<DTO>> req)
    {
        ExcelDto<DTO> excelDto = Objects.requireNonNull(req.getData(), "excelDto is required");
        byte[]         bytes     = ExcelDto.doExportToBytes(emptyList(), excelClass, excelDto);
        return ExcelResponseEntity.of(bytes, excelDto.fileName());
    }

    @AuditMethod(saveParam = false, saveResult = false)
    @Operation(description = "[BASE] Excel to import", summary = "[BASE] Excel data to import data")
    @PostMapping("excel/import")
    public void importExcel(
            @NotNull
            @Validated(CRUD.C.class)
            FQ req) throws IOException
    {
        MultipartFile file = req.getSingleFile();

        try (InputStream in = file.getInputStream()) {
            List<EO> readData = ExcelDto.doRead(in, excelClass);
            service.importExcel(readData);
        }
    }

    @AuditMethod(saveParam = false, saveResult = false)
    @Operation(description = "[BASE] Excel export", summary = "[BASE] Excel excel data")
    @IgnoreResponseWrap
    @PostMapping({"excel/export", "excel/export/{limit}"})
    public ExcelResponseEntity exportExcel(
            @Nullable
            @Positive(message = "limit must be positive")
            @Parameter(name = "limit", description = "export data limit")
            @PathVariable(value = "limit", required = false)
            Integer limit,
            @NotNull
            @Validated({Default.class, CRUD.R.class})
            @RequestBody OQ<ExcelDto<DTO>> req)
    {
        ExcelDto<DTO> excelDto = Objects.requireNonNull(req.getData(), "excelDto is required");
        int           exportLimit = Math.min(defaultIfNull(limit, this.defaultLimit()), this.defaultLimit());
        List<EO>      excelData   = service.exportExcel(excelDto.param(), exportLimit, req.getOrders());
        byte[]        bytes       = ExcelDto.doExportToBytes(excelData, excelClass, excelDto);
        return ExcelResponseEntity.of(bytes, excelDto.fileName());
    }
}
