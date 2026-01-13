package com.fz.springboot.starter.pojo.eo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/1/21 23:53
 */

@Schema(description = "excel download general settings object")
public record ExcelConfig<T>(
        @Schema(description = "query param")
        T param,

        @NotNull(message = "{ExcelConfig.fileName}")
        @Size(max = 255, message = "{ExcelConfig.fileName.length}")
        @Schema(description = "file name")
        String fileName,

        @Size(max = 255, message = "{ExcelConfig.sheetName.length}")
        @Schema(description = "sheet name")
        String sheetName
) {}