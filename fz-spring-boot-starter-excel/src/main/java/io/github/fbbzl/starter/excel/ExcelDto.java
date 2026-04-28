package io.github.fbbzl.starter.excel;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

import static cn.hutool.core.io.FileMagicNumber.XLSX;
import static cn.hutool.core.text.CharSequenceUtil.appendIfMissing;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/1/21 23:53
 */

@Schema(description = "excel download general settings object")
public record ExcelDto<PARAM>(
        @Schema(description = "query param")
        PARAM param,

        @NotNull(message = "{ExcelDto.fileName}")
        @Size(max = 255, message = "{ExcelDto.fileName.length}")
        @Schema(description = "file name")
        String fileName,

        @Size(max = 255, message = "{ExcelDto.sheetName.length}")
        @Schema(description = "sheet name")
        String sheetName
) {

    public static void setResponseHeader(HttpServletResponse response, ExcelDto<?> excelCfg)
    {
        response.setContentType(XLSX.getMimeType());

        String filename = appendIfMissing(excelCfg.fileName(), "." + XLSX.getExtension());

        response.setHeader("Content-Disposition", "attachment;filename=" + filename);
    }

    public static <EO extends BaseEo> void doExport(
            HttpServletResponse response,
            Collection<EO> excelData,
            Class<EO> excelClass,
            ExcelDto<?> config) throws IOException
    {
        try (OutputStream os = response.getOutputStream()) {
            EasyExcelFactory.write(os)
                            .head(excelClass)
                            .sheet(config.sheetName())
                            .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                            .doWrite(excelData);
        }
    }

    public static <EO extends BaseEo> List<EO> doRead(InputStream inputStream, Class<EO> excelClass)
    {
        return EasyExcelFactory.read(inputStream)
                               .head(excelClass)
                               .headRowNumber(1)
                               .sheet()
                               .doReadSync();
    }

}
