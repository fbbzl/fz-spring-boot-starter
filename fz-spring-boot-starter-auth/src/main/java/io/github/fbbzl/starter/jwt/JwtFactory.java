package io.github.fbbzl.starter.jwt;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.UUID;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import io.github.fbbzl.starter.jwt.config.JwtProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.fz.erwin.exception.Throws;
import org.springframework.lang.Nullable;

import java.time.Duration;
import java.time.Instant;

import static cn.hutool.core.text.CharSequenceUtil.EMPTY;
import static cn.hutool.core.util.ObjectUtil.hasNull;
import static cn.hutool.jwt.RegisteredPayload.*;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/5/1 22:21
 */


@FieldDefaults(level = AccessLevel.PROTECTED)
public class JwtFactory
{

    JwtProperties props;
    JWTSigner         signer;
    static JwtFactory self;

    {
        self = this;
    }

    public JwtFactory(JwtProperties props)
    {
        this.props  = props;
        this.signer = getSigner(props);
    }

    public JWTSigner getSigner(JwtProperties props)
    {
        return JWTSignerUtil.hs256(props.getSecret().getBytes());
    }

    public static JWT create(@NotNull Object bean)
    {
        return create(UUID.randomUUID().toString(), bean, JWTSignerUtil.hs256(self.props.getSecret().getBytes()));
    }

    public static JWT create(String subject, @NotNull Object bean)
    {
        return create(subject, bean, JWTSignerUtil.hs256(self.props.getSecret().getBytes()));
    }

    public static JWT create(String subject, @NotNull Object bean, JWTSigner signer)
    {
        Instant  now     = Instant.now();
        Duration expires = self.props.getExpires();
        return JWT.create()
                  .setPayload(ISSUED_AT, now.toEpochMilli())
                  .setPayload(EXPIRES_AT, now.plus(expires))
                  .addPayloads(BeanUtil.beanToMap(bean))
                  .setIssuer(self.props.getIssuer())
                  .setSubject(subject)
                  .setJWTId(UUID.randomUUID().toString())
                  .setSigner(signer);
    }

    @Nullable
    public static JWT jwt(HttpServletRequest request)
    {
        String token = request.getHeader(self.props.getHeader());
        Throws.ifBlank(token, () -> "token can not be null or blank");

        token = token.substring(self.props.getPrefix().length()).trim();

        JWT jwt = JWTUtil.parseToken(token);
        jwt.setSigner(self.signer);

        return jwt.verify() && !isExpired(jwt) ? jwt : null;
    }

    @Nullable
    public static String jti(HttpServletRequest request)
    {
        return jti(jwt(request));
    }

    @Nullable
    public static String jti(JWT jwt)
    {
        if (jwt == null) return EMPTY;

        Object jti = payload(jwt, JWT_ID);
        if (jti == null) return EMPTY;

        return jti.toString();
    }

    public static boolean isExpired(HttpServletRequest request)
    {
        return isExpired(jwt(request));
    }

    public static boolean isExpired(JWT jwt)
    {
        Object exp = payload(jwt, EXPIRES_AT);
        return exp == null || Instant.now().toEpochMilli() > Convert.toLong(exp);
    }

    @Nullable
    public static Object payload(HttpServletRequest request, String claim)
    {
        return payload(jwt(request), claim);
    }

    @Nullable
    public static Object payload(JWT jwt, String claim)
    {
        return jwt != null ? jwt.getPayload(claim) : null;
    }

    @Nullable
    public static <BEAN> BEAN payloadsBean(HttpServletRequest request, Class<BEAN> beanType)
    {
        if (hasNull(request, beanType)) return null;

        return BeanUtil.toBean(jwt(request), beanType);
    }

    @Nullable
    public static <BEAN> BEAN payloadsBean(JWT jwt, Class<BEAN> beanType)
    {
        if (hasNull(jwt, beanType)) return null;

        return BeanUtil.toBean(jwt.getPayloads(), beanType);
    }

}
