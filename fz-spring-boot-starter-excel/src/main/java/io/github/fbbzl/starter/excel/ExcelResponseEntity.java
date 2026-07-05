package io.github.fbbzl.starter.excel;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static cn.hutool.core.io.FileMagicNumber.XLSX;
import static cn.hutool.core.text.CharSequenceUtil.appendIfMissing;
import static cn.hutool.core.text.CharSequenceUtil.isBlank;

/**
 * Excel download response entity with RFC 5987 encoded filename.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026/7/2
 */
public class ExcelResponseEntity extends ResponseEntity<byte[]>
{

    private static final int    MAX_FILENAME_BYTES = 255;
    private static final String DEFAULT_FILENAME   = "export";

    private ExcelResponseEntity(byte[] body, String fileName)
    {
        super(body, headers(fileName, body.length), HttpStatus.OK);
    }

    public static ExcelResponseEntity of(byte[] body, String fileName)
    {
        return new ExcelResponseEntity(body, fileName);
    }

    private static HttpHeaders headers(String fileName, long contentLength)
    {
        String normalized = appendIfMissing(sanitizeFileName(fileName), "." + XLSX.getExtension());
        String encoded    = URLEncoder.encode(normalized, StandardCharsets.UTF_8).replace("+", "%20");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(XLSX.getMimeType()));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded);
        headers.setContentLength(contentLength);
        return headers;
    }

    /**
     * Sanitize user-supplied filename to avoid response header injection and path traversal.
     * <p>Removes control characters (per RFC 5987), path separators and ".." sequences,
     * then truncates so the UTF-8 byte representation does not exceed {@value #MAX_FILENAME_BYTES}
     * bytes. Falls back to {@value #DEFAULT_FILENAME} if the sanitized result is blank.
     *
     * @param fileName raw filename
     * @return sanitized filename without extension
     */
    private static String sanitizeFileName(String fileName)
    {
        if (isBlank(fileName)) {
            return DEFAULT_FILENAME;
        }

        String sanitized = fileName
                .replaceAll("[\\x00-\\x1F\\x7F]", "")
                .replaceAll("[\\\\/:*?\\\"<>|]", "")
                .replace("..", "");

        // Replacing separators first may leave residual ".." (e.g. ".../.." -> ".."),
        // so strip it again after separators are gone.
        sanitized = sanitized.replace("..", "");

        sanitized = truncateToUtf8ByteLength(sanitized, MAX_FILENAME_BYTES);

        return isBlank(sanitized) ? DEFAULT_FILENAME : sanitized;
    }

    /**
     * Truncate a string so that its UTF-8 byte representation does not exceed maxBytes,
     * without cutting a multi-byte character in half.
     */
    private static String truncateToUtf8ByteLength(String value, int maxBytes)
    {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= maxBytes) {
            return value;
        }

        int end = maxBytes;
        // walk backwards until we are at a valid UTF-8 character boundary
        while (end > 0 && (bytes[end] & 0xC0) == 0x80) {
            end--;
        }

        return new String(bytes, 0, end, StandardCharsets.UTF_8);
    }
}
