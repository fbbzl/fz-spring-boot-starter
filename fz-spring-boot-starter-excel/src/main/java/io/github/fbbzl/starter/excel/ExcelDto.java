package io.github.fbbzl.starter.excel;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/1/21 23:53
 */

// excel download general settings object
public record ExcelDto<PARAM>(
        // query param
        PARAM param,

        @NotNull(message = "{ExcelDto.fileName}")
        @Size(max = 255, message = "{ExcelDto.fileName.length}")
        // file name
        String fileName,

        @Size(max = 255, message = "{ExcelDto.sheetName.length}")
        // sheet name
        String sheetName
) {

    public static <EO extends BaseEo> byte[] doExportToBytes(
            Collection<EO> excelData,
            Class<EO> excelClass,
            ExcelDto<?> config)
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        doWrite(os, excelData, excelClass, config);
        return os.toByteArray();
    }

    private static <EO extends BaseEo> void doWrite(
            OutputStream os,
            Collection<EO> excelData,
            Class<EO> excelClass,
            ExcelDto<?> config)
    {
        EasyExcelFactory.write(os)
                        .head(excelClass)
                        .sheet(config.sheetName())
                        .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                        .doWrite(excelData);
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
