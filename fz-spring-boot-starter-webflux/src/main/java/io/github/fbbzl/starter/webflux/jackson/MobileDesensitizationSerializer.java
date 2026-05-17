package io.github.fbbzl.starter.webflux.jackson;

import cn.hutool.core.util.DesensitizedUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Mobile phone desensitization serializer.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026/5/15
 */
public class MobileDesensitizationSerializer extends JsonSerializer<String> {

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(DesensitizedUtil.mobilePhone(value));
    }
}
