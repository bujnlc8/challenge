package site.haihui.challenge.daily;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import site.haihui.challenge.common.auth.LoginInterceptor;
import site.haihui.challenge.controller.CommonController;

class AppShareCodeTest {
    DailyWechatGateway gateway;
    AppShareCodeService service;
    MutableClock clock;
    byte[] png = new byte[] {(byte) 137, 80, 78, 71, 13, 10, 26, 10, 1};

    @BeforeEach
    void setup() {
        gateway = mock(DailyWechatGateway.class);
        clock = new MutableClock();
        service = new AppShareCodeService(gateway, clock);
        when(gateway.appShareCode(anyString())).thenReturn(png);
    }
    @Test
    void publicEndpointWorksWithoutLoginAndUsesDefaultRelease() throws Exception {
        CommonController controller = new CommonController();
        ReflectionTestUtils.setField(controller, "appShareCodeService", service);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).addInterceptors(new LoginInterceptor()).build();
        mvc.perform(post("/common/share-code"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(10000))
            .andExpect(jsonPath("$.data.page").value("pages/index/index"))
            .andExpect(jsonPath("$.data.env_version").value("release"))
            .andExpect(jsonPath("$.data.mime_type").value("image/png"))
            .andExpect(jsonPath("$.data.image_base64").isNotEmpty());
        verify(gateway).appShareCode("release");
    }
    @Test
    void cachesOnlyThreeAllowedEnvironmentsSeparately() {
        for (String env : Arrays.asList("release", "trial", "develop")) {
            Map<String, Object> result = service.shareCode(env);
            assertEquals(env, result.get("env_version"));
            assertEquals("pages/index/index", result.get("page"));
            assertEquals(result, service.shareCode(env));
            verify(gateway, times(1)).appShareCode(env);
        }
        assertThrows(RuntimeException.class, () -> service.shareCode("other"));
        verify(gateway, times(3)).appShareCode(anyString());
    }
    @Test
    void refreshesSuccessfulImageAfterOneDay() {
        service.shareCode("release");
        clock.advance(Duration.ofHours(23));
        service.shareCode("release");
        verify(gateway, times(1)).appShareCode("release");
        clock.advance(Duration.ofHours(1));
        service.shareCode("release");
        verify(gateway, times(2)).appShareCode("release");
    }
    @Test
    void concurrentPublicRequestsOnlyGenerateOneCode() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            List<Callable<Map<String, Object>>> calls = new ArrayList<>();
            for (int i = 0; i < 12; i++) calls.add(() -> service.shareCode("trial"));
            for (Future<Map<String, Object>> result : pool.invokeAll(calls))
                assertEquals("trial", result.get().get("env_version"));
            verify(gateway, times(1)).appShareCode("trial");
        } finally {
            pool.shutdownNow();
        }
    }
    @Test
    void supportsJpegAndRejectsWechatJsonWithFailureCooldown() {
        when(gateway.appShareCode("trial")).thenReturn(new byte[] {(byte) 255, (byte) 216, (byte) 255, 1});
        assertEquals("image/jpeg", service.shareCode("trial").get("mime_type"));
        when(gateway.appShareCode("release")).thenReturn("{\"errcode\":40001}".getBytes(StandardCharsets.UTF_8));
        assertThrows(RuntimeException.class, () -> service.shareCode("release"));
        assertThrows(RuntimeException.class, () -> service.shareCode("release"));
        verify(gateway, times(1)).appShareCode("release");
        clock.advance(Duration.ofSeconds(60));
        when(gateway.appShareCode("release")).thenReturn(png);
        assertEquals("image/png", service.shareCode("release").get("mime_type"));
        verify(gateway, times(2)).appShareCode("release");
    }
    @Test
    void transientGatewayFailureAlsoHasBoundedCooldown() {
        when(gateway.appShareCode("develop")).thenThrow(new IllegalStateException("unavailable"));
        assertThrows(RuntimeException.class, () -> service.shareCode("develop"));
        assertThrows(RuntimeException.class, () -> service.shareCode("develop"));
        verify(gateway, times(1)).appShareCode("develop");
    }
    static class MutableClock extends Clock {
        Instant instant = Instant.parse("2026-11-01T00:00:00Z");
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }
        public Clock withZone(ZoneId zone) {
            return this;
        }
        public Instant instant() {
            return instant;
        }
        void advance(Duration duration) {
            instant = instant.plus(duration);
        }
    }
}
