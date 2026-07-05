package io.github.fbbzl.starter.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class ExcelResponseEntityTest
{

    @Test
    void shouldBuildRfc5987EncodedExcelResponse()
    {
        byte[] body       = new byte[] { 0x01, 0x02, 0x03 };
        String fileName   = "用户报表";
        String encoded    = java.net.URLEncoder.encode(fileName + ".xlsx", StandardCharsets.UTF_8).replace("+", "%20");

        ExcelResponseEntity entity = ExcelResponseEntity.of(body, fileName);

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(entity.getBody()).isSameAs(body);
        assertThat(entity.getHeaders().getContentType())
                .isEqualTo(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        assertThat(entity.getHeaders().getContentLength()).isEqualTo(body.length);
        assertThat(entity.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .isEqualTo("attachment; filename*=UTF-8''" + encoded);
    }

    @Test
    void shouldNotDuplicateXlsxExtension()
    {
        ExcelResponseEntity entity = ExcelResponseEntity.of(new byte[] {}, "report.xlsx");

        assertThat(entity.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .contains("report.xlsx");
        assertThat(entity.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .doesNotContain("xlsx.xlsx");
    }

    @Test
    void shouldSanitizePathTraversalInFileName()
    {
        ExcelResponseEntity entity = ExcelResponseEntity.of(new byte[] {}, "../../../etc/passwd");

        String disposition = entity.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
        assertThat(disposition).doesNotContain("../", "/", "\\");
        assertThat(disposition).contains("etcpasswd.xlsx");
    }

    @Test
    void shouldStripControlCharactersFromFileName()
    {
        ExcelResponseEntity entity = ExcelResponseEntity.of(new byte[] {}, "report\r\n\t.xlsx");

        String disposition = entity.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
        assertThat(disposition).contains("report.xlsx");
        assertThat(disposition).doesNotContain("\r", "\n", "\t");
    }

    @Test
    void shouldUseDefaultNameForBlankFileName()
    {
        ExcelResponseEntity entity = ExcelResponseEntity.of(new byte[] {}, "   ");

        assertThat(entity.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .contains("export.xlsx");
    }

    @Test
    void shouldStripWindowsReservedCharactersFromFileName()
    {
        ExcelResponseEntity entity = ExcelResponseEntity.of(new byte[] {}, "report:name|*?\"<>");

        String disposition = entity.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
        String filenamePart = disposition.substring(disposition.indexOf("UTF-8''") + "UTF-8''".length());
        assertThat(filenamePart).contains("report");
        assertThat(filenamePart).contains(".xlsx");
        assertThat(filenamePart).doesNotContain(":", "|", "?", "*", "\"", "<", ">");
    }

    @Test
    void shouldTruncateOverlongFileNameWithoutBreakingMultiByteCharacters()
    {
        String longName = "用".repeat(100); // each CJK char is 3 bytes in UTF-8

        ExcelResponseEntity entity = ExcelResponseEntity.of(new byte[] {}, longName);

        String disposition = entity.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
        assertThat(disposition).contains(".xlsx");

        // The sanitized filename (without the disposition prefix) is truncated to
        // at most 255 UTF-8 bytes before URL encoding. After encoding each byte as
        // "%XX" the encoded length can be up to 3x the raw byte length.
        String encodedNameWithExt = disposition.substring("attachment; filename*=UTF-8''".length());
        String encodedName        = encodedNameWithExt.substring(0, encodedNameWithExt.length() - ".xlsx".length());
        String decodedName        = java.net.URLDecoder.decode(encodedName, StandardCharsets.UTF_8);

        assertThat(decodedName.getBytes(StandardCharsets.UTF_8).length)
                .as("decoded filename (without extension) should not exceed 255 UTF-8 bytes")
                .isLessThanOrEqualTo(255);
        // encoded form should not contain the UTF-8 replacement character
        assertThat(disposition).doesNotContain("%EF%BF%BD");
    }

    @Test
    void shouldAllowNullSheetNameWhenExportingExcel()
    {
        ExcelDto<Void> excelDto = new ExcelDto<>(null, "report", null);

        assertThatNoException().isThrownBy(() -> ExcelDto.doExportToBytes(java.util.Collections.emptyList(), DummyEo.class, excelDto));
    }

    public static class DummyEo extends BaseEo
    {
        @ExcelProperty("Name")
        private String name;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
