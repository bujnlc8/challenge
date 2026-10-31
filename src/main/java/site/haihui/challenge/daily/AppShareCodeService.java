package site.haihui.challenge.daily;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import site.haihui.challenge.common.exception.BaseException;
import site.haihui.challenge.utils.MiniAppCode;

/** Public generic app code only; three fixed environment slots, with no user or question input. */
@Service
public class AppShareCodeService {
    private static final Duration SUCCESS_TTL = Duration.ofDays(1);
    private static final Duration FAILURE_TTL = Duration.ofMinutes(1);
    private final DailyWechatGateway gateway;
    private final Clock clock;
    private final Map<String, CachedCode> codes = new HashMap<>();

    public AppShareCodeService(DailyWechatGateway gateway, @Qualifier("dailyChallengeClock") Clock clock) {
        this.gateway = gateway;
        this.clock = clock;
    }

    public synchronized Map<String, Object> shareCode(String environment) {
        MiniAppCode.requireEnvironment(environment);
        CachedCode cached = codes.get(environment);
        Instant now = clock.instant();
        if (cached != null && now.isBefore(cached.expiresAt)) {
            if (cached.payload == null)
                throw unavailable();
            return cached.payload;
        }
        try {
            Map<String, Object> payload = MiniAppCode.imagePayload(gateway.appShareCode(environment));
            payload.put("page", "pages/index/index");
            payload.put("env_version", environment);
            payload = Collections.unmodifiableMap(payload);
            codes.put(environment, new CachedCode(payload, now.plus(SUCCESS_TTL)));
            return payload;
        } catch (RuntimeException e) {
            // Bound repeated upstream work even during token/WeChat failures on this anonymous endpoint.
            codes.put(environment, new CachedCode(null, now.plus(FAILURE_TTL)));
            throw unavailable();
        }
    }

    private BaseException unavailable() {
        return new BaseException(400, "小程序码生成失败，请稍后重试");
    }

    private static final class CachedCode {
        final Map<String, Object> payload;
        final Instant expiresAt;
        CachedCode(Map<String, Object> payload, Instant expiresAt) {
            this.payload = payload;
            this.expiresAt = expiresAt;
        }
    }
}
