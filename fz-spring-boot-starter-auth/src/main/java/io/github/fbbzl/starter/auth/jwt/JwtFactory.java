package io.github.fbbzl.starter.auth.jwt;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.UUID;
import cn.hutool.json.JSONObject;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import io.github.fbbzl.starter.auth.jwt.config.JwtProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import io.github.fbbzl.starter.core.util.Throws;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

import static cn.hutool.core.text.CharSequenceUtil.EMPTY;
import static cn.hutool.core.text.CharSequenceUtil.isBlank;
import static cn.hutool.core.util.ObjectUtil.hasNull;
import static cn.hutool.jwt.RegisteredPayload.*;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/5/1 22:21
 */

@Slf4j
@FieldDefaults(level = AccessLevel.PROTECTED)
public class JwtFactory
{

    JwtProperties props;
    static volatile JwtFactory self;

    {
        self = this;
    }

    public JwtFactory(JwtProperties props)
    {
        this.props = props;
    }

    public JWTSigner getSigner(JwtProperties props)
    {
        return JWTSignerUtil.hs256(props.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public static JWT create(@NotNull Object bean)
    {
        JwtFactory factory = self();
        return create(UUID.randomUUID().toString(), bean, factory.getSigner(factory.props));
    }

    public static JWT create(String subject, @NotNull Object bean)
    {
        JwtFactory factory = self();
        return create(subject, bean, factory.getSigner(factory.props));
    }

    public static JWT create(String subject, @NotNull Object bean, JWTSigner signer)
    {
        JwtFactory factory = self();
        Instant    now     = Instant.now();
        Duration   expires = factory.props.getExpires();
        return JWT.create()
                  .addPayloads(BeanUtil.beanToMap(bean))
                  .setPayload(ISSUED_AT, now.getEpochSecond())
                  .setPayload(EXPIRES_AT, now.plus(expires).getEpochSecond())
                  .setIssuer(factory.props.getIssuer())
                  .setSubject(subject)
                  .setJWTId(UUID.randomUUID().toString())
                  .setSigner(signer);
    }

    @Nullable
    public static JWT jwt(HttpServletRequest request)
    {
        return jwt(request, true);
    }

    @Nullable
    public static JWT jwt(HttpServletRequest request, boolean checkExpired)
    {
        JwtFactory factory = self();
        String     token   = token(request);
        if (isBlank(token)) return null;

        try {
            JWT       jwt    = JWTUtil.parseToken(token);
            JWTSigner signer = factory.getSigner(factory.props);
            jwt.setSigner(signer);

            return verify(jwt, signer) && (!checkExpired || !isExpired(jwt)) ? jwt : null;
        }
        catch (RuntimeException error) {
            log.error("jwt parse error", error);
            return null;
        }
    }

    @Nullable
    private static String token(HttpServletRequest request)
    {
        if (request == null) return null;

        JwtFactory factory = self();
        String     token   = request.getHeader(factory.props.getHeader());
        if (isBlank(token)) return null;

        String prefix = factory.props.getPrefix();
        if (isBlank(prefix)) return token.trim();
        if (!token.regionMatches(true, 0, prefix, 0, prefix.length())) return null;

        token = token.substring(prefix.length()).trim();
        return isBlank(token) ? null : token;
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
        Long   sec = Convert.toLong(exp);
        return sec == null || Instant.now().getEpochSecond() > sec;
    }

    private static boolean verify(JWT jwt, JWTSigner signer)
    {
        try {
            return jwt.verify(signer);
        } catch (RuntimeException error) {
            log.debug("jwt verify failed", error);
            return false;
        }
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
        return payloadsBean(jwt(request), beanType);
    }

    @Nullable
    public static <BEAN> BEAN payloadsBean(JWT jwt, Class<BEAN> beanType)
    {
        if (hasNull(jwt, beanType)) return null;
        JSONObject payloads = jwt.getPayloads();
        if (payloads == null) return null;

        return BeanUtil.toBean(payloads, beanType);
    }

    private static JwtFactory self()
    {
        Throws.ifNull(self, "jwt factory is not initialized");
        return self;
    }

}
