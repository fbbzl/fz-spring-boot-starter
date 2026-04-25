package com.fz.starter.dal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PRIVATE;

/**
 * range query condition
 */
@Getter
@Setter
@ToString
@Schema(description = "range query condition")
@FieldDefaults(level = PRIVATE)
public class Range
{

    @Size(max = 128, message = "{RQ.Range.field.size}")
    @NotNull(message = "{RQ.Range.field}")
    @Schema(description = "range query field")
    String field;

    @Schema(description = "range query start value")
    Object start;

    @Schema(description = "range query end value")
    Object end;

    @Schema(description = "whether the range query is closed interval")
    Boolean close = Boolean.TRUE;

    public static <VALUE extends Comparable<? super VALUE>> Range of(String field, VALUE start, VALUE end)
    {
        return of(field, start, end, true);
    }

    public static <VALUE extends Comparable<? super VALUE>> Range of(String field, VALUE start, VALUE end, Boolean close)
    {
        Range range = new Range();
        range.setField(field);
        range.setStart(start);
        range.setEnd(end);
        range.setClose(close);
        return range;
    }
}