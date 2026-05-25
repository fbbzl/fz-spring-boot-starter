package io.github.fbbzl.starter.auth.jwt.storage;


import cn.hutool.jwt.JWT;
import io.github.fbbzl.starter.auth.jwt.config.JwtProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;

import static cn.hutool.core.text.CharSequenceUtil.isBlank;
import static cn.hutool.core.text.CharSequenceUtil.isNotBlank;
import static io.github.fbbzl.starter.auth.jwt.JwtFactory.jti;
import static io.github.fbbzl.starter.auth.jwt.JwtFactory.jwt;


/**
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026/4/29 16:10
 */
@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class JtiStorage
{
    @Value("#{jwtProperties.jti}")
    JwtProperties.JtiStorageProperties jtiStorageProps;

    @Nullable
    public final JWT check(HttpServletRequest request)
    {
        JWT    jwt = jwt(request);
        String jti = jti(jwt);

        if (isNotBlank(jti) && valid(jti)) {
            return jwt;
        }

        return null;
    }

    public final void delete(HttpServletRequest request)
    {
        String jti = jti(jwt(request, false));
        if (isBlank(jti)) return;

        delete(jti);
    }

    public abstract boolean valid(String jti);

    public abstract void store(String jti);

    public abstract void delete(String jti);
}
