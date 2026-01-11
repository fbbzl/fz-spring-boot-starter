package com.fz.springboot.starter.web.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/9/2 11:28
 */

@Data
@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class BaseDto<T> {
    @JsonIgnore
    public abstract T getEntity();
}