package io.github.fbbzl.starter.web;

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
import io.github.fbbzl.starter.web.Q.FQ;
import io.github.fbbzl.starter.web.Q.OQ;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

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
        SERVICE extends BaseCrudExcelService<ID, ENTITY, DTO, BO, EO, DAL, STRUCT_MAPPER>,
        DTO     extends BaseDto<ID>,
        BO      extends BaseBo<ID>,
        EO      extends BaseEo,
        DAL     extends BaseDal<ENTITY, ID>,
        STRUCT_MAPPER extends BaseStructMapper<ENTITY, DTO, BO, EO>>
        extends BaseCrudController<ID, ENTITY, SERVICE, DTO, BO, DAL, STRUCT_MAPPER>
{

    @Autowired
    HttpServletResponse response;

    Class<EO> excelClass = Generics.getGenericSuperType(this.getClass(), BaseCrudExcelController.class, 5);

    @Operation(description = "Get an excel template", summary = "The Excel template you need to use to get Excel upload data")
    @PostMapping("excel/template")
    public void excelTemplate(
            @NotNull
            @Validated(CRUD.R.class)
            @RequestBody Q<ExcelDto<DTO>> req) throws IOException
    {
        ExcelDto<DTO> excelDto = req.getData();
        ExcelDto.setResponseHeader(response, excelDto);
        ExcelDto.doExport(response, emptyList(), excelClass, excelDto);
    }

    @AuditMethod(saveParam = false, saveResult = false)
    @Operation(description = "Excel to import", summary = "Excel data to import data")
    @PostMapping("excel/import")
    public void importExcel(
            @NotNull
            @Validated(CRUD.C.class)
            FQ req) throws IOException
    {
        List<EO> readData = ExcelDto.doRead(req.getSingleFile().getInputStream(), excelClass);
        service.importExcel(readData);
    }

    @AuditMethod(saveParam = false, saveResult = false)
    @Operation(description = "Excel export", summary = "Excel excel data")
    @PostMapping({"excel/export", "excel/export/{limit}"})
    public void exportExcel(
            @Nullable
            @Positive(message = "limit must be positive")
            @Parameter(name = "limit", description = "export data limit")
            @PathVariable(value = "limit", required = false)
            Integer limit,
            @NotNull
            @Validated(CRUD.R.class)
            @RequestBody OQ<ExcelDto<DTO>> req) throws IOException
    {
        ExcelDto<DTO> excelDto = req.getData();
        ExcelDto.setResponseHeader(response, excelDto);
        ExcelDto.doExport(response, service.exportExcel(excelDto.param(), defaultIfNull(limit, this.defaultLimit()), req.getOrders()), excelClass, excelDto);
    }
}
