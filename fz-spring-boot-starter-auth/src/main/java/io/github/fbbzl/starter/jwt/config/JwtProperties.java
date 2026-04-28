package io.github.fbbzl.starter.jwt.config;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.time.Duration;

import static cn.hutool.core.text.CharSequenceUtil.*;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/5/2 13:53
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
@FieldDefaults(level = AccessLevel.PROTECTED)
public class JwtProperties
{

    String   header  = "Authorization";
    String   prefix  = "Bearer ";
    String   issuer  = "pms";
    Duration expires = Duration.ofHours(2);
    String   secret  = "s!d_=55%^h*&*()_+|!@$[]23uy04u5.,><';`~`~!@#$%^&*()_+~||@#%u!!$%p@#g/?;:,.trlu";

    JtiStorageProperties jti = new JtiStorageProperties();

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class JtiStorageProperties
    {

        String   key = "APPLICATION:JTI:{}";
        Duration ttl = Duration.ofHours(2);

        @Nullable
        public String formatKey(String jti)
        {
            if (isBlank(jti))
                return EMPTY;
            else
                return format(key, jti);
        }
    }
}
