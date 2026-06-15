package io.github.fbbzl.starter.auth.jwt.config;

import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

import static cn.hutool.core.text.CharSequenceUtil.*;
import static lombok.AccessLevel.PROTECTED;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/5/2 13:53
 */
@Data
@ConfigurationProperties(prefix = "jwt")
@FieldDefaults(level = PROTECTED)
public class JwtProperties
{

    String               header  = "Authorization";
    String               prefix  = "Bearer ";
    String               issuer  = "pms";
    Duration             expires = Duration.ofHours(2);
    String               secret;
    JtiStorageProperties jti     = new JtiStorageProperties();

    @Data
    @FieldDefaults(level = PROTECTED)
    public static class JtiStorageProperties
    {

        String   key = "APPLICATION:JTI:{}";
        Duration ttl = Duration.ofHours(2);

        public String formatKey(String jti)
        {
            if (isBlank(jti))
                return EMPTY;
            else
                return format(key, jti);
        }
    }
}
