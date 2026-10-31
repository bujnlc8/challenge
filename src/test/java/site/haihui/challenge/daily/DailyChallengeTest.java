package site.haihui.challenge.daily;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import site.haihui.challenge.dto.weixin.MiniMessageBody;
import site.haihui.challenge.dto.weixin.MiniResponse;

class DailyChallengeTest {
    JdbcTemplate jdbc;
    DailyChallengeStore store;
    DailyWechatGateway gateway;
    DailyChallengeService service;
    Clock clock = Clock.fixed(Instant.parse("2026-10-31T23:59:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setup() throws Exception {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE");
        jdbc = new JdbcTemplate(ds);
        jdbc.execute("CREATE TABLE question(id INT PRIMARY KEY,status INT)");
        jdbc.execute("CREATE TABLE `user`(id INT PRIMARY KEY,openid VARCHAR(512),app_type INT)");
        jdbc.update("INSERT INTO question VALUES(10,1),(20,1),(30,0)");
        jdbc.update("INSERT INTO `user` VALUES(1,'openid-one',1),(2,'openid-two',1)");
        try (Connection connection = ds.getConnection()) {
            ScriptUtils.executeSqlScript(connection, new FileSystemResource("sql/daily-challenge.sql"));
        }
        store = new DailyChallengeStore(jdbc, new DataSourceTransactionManager(ds));
        gateway = mock(DailyWechatGateway.class);
        service = new DailyChallengeService(store, gateway, clock, "release");
    }
    @Test
    void dailyQuestionIsPinnedAndContainsNoAnswer() {
        Map<String, Object> first = service.today();
        assertEquals("2026-11-01", first.get("date"));
        assertTrue(Arrays.asList(10, 20).contains(first.get("question_id")));
        jdbc.update("INSERT INTO question VALUES(40,1)");
        assertEquals(first, service.today());
        assertFalse(first.containsKey("answer"));
        assertEquals("2026-11-01 08:00:00", first.get("start_time"));
    }
    @Test
    void noQuestionDoesNotPickHiddenQuestion() {
        jdbc.update("UPDATE question SET status=0");
        assertThrows(RuntimeException.class, service::today);
    }
    @Test
    void hiddenPinnedQuestionIsUnavailableWithoutChoosingAnotherDailyQuestion() {
        int pinned = (Integer) service.today().get("question_id");
        accept();
        jdbc.update("UPDATE question SET status=0 WHERE id=?", pinned);
        assertPinnedQuestionIsUnavailable(pinned);
    }

    @Test
    void deletedPinnedQuestionIsUnavailableWithoutChoosingAnotherDailyQuestion() {
        int pinned = (Integer) service.today().get("question_id");
        accept();
        jdbc.update("DELETE FROM question WHERE id=?", pinned);
        assertPinnedQuestionIsUnavailable(pinned);
    }

    private void assertPinnedQuestionIsUnavailable(int pinned) {
        assertThrows(site.haihui.challenge.common.exception.BaseException.class, service::today);
        service = atEight();
        service.sendToday();
        verifyNoInteractions(gateway);
        assertEquals(pinned, jdbc.queryForObject("SELECT question_id FROM daily_challenge", Integer.class));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM daily_challenge", Integer.class));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM daily_reminder_delivery", Integer.class));
        assertEquals(1, service.reminder(1).get("credits"));
    }

    @Test
    void enabledPreferenceAloneDoesNotCreateCredit() {
        service.setReminder(1, true);
        assertEquals(0, service.reminder(1).get("credits"));
        assertNull(store.claim(1, LocalDate.of(2026, 11, 1), 10));
    }
    @Test
    void acceptedCreditIsIdempotentAndRequiresCorrectResultAndTemplate() {
        service.accept(1, "request-00000001", DailyChallengeService.TEMPLATE_ID, "accept");
        service.accept(1, "request-00000001", DailyChallengeService.TEMPLATE_ID, "accept");
        assertEquals(1, service.reminder(1).get("credits"));
        assertThrows(RuntimeException.class,
            () -> service.accept(1, "request-00000002", DailyChallengeService.TEMPLATE_ID, "reject"));
        assertThrows(RuntimeException.class, () -> service.accept(1, "request-00000003", "other", "accept"));
        assertEquals(1, service.reminder(1).get("credits"));
    }
    @Test
    void disabledPreferenceKeepsUnusedCreditWithoutSending() {
        accept();
        service.setReminder(1, false);
        assertNull(store.claim(1, LocalDate.of(2026, 11, 1), 10));
        assertEquals(1, service.reminder(1).get("credits"));
    }
    @Test
    void shanghaiEightOclockGateAndPayload() {
        accept();
        service.sendToday();
        verifyNoInteractions(gateway);
        service = atEight();
        when(gateway.send(any())).thenReturn(response(0));
        service.sendToday();
        service.sendToday();
        org.mockito.ArgumentCaptor<MiniMessageBody> captor = org.mockito.ArgumentCaptor.forClass(MiniMessageBody.class);
        verify(gateway, times(1)).send(captor.capture());
        MiniMessageBody body = captor.getValue();
        assertEquals("openid-one", body.getTouser());
        assertEquals(DailyChallengeService.TEMPLATE_ID, body.getTemplateId());
        assertEquals("每日挑战", body.getData().get("thing1").getValue());
        assertEquals("2026-11-01 08:00:00", body.getData().get("time7").getValue());
        assertTrue(body.getPage().startsWith("pages/answer/answer?question_id="));
        assertEquals("formal", body.getMiniprogramState());
        assertEquals("CONSUMED", jdbc.queryForObject("SELECT state FROM daily_reminder_credit", String.class));
    }
    @Test
    void definiteWechatFailureReturnsCreditButNeverRetriesSameDate() {
        accept();
        when(gateway.send(any())).thenReturn(response(40037));
        service = atEight();
        service.sendToday();
        service.sendToday();
        assertEquals(1, service.reminder(1).get("credits"));
        verify(gateway, times(1)).send(any());
        assertEquals("FAILED", jdbc.queryForObject("SELECT status FROM daily_reminder_delivery", String.class));
    }
    @Test
    void noWechatEntitlementInvalidatesCredit() {
        accept();
        when(gateway.send(any())).thenReturn(response(43101));
        service = atEight();
        service.sendToday();
        assertEquals("INVALID", jdbc.queryForObject("SELECT state FROM daily_reminder_credit", String.class));
    }
    @Test
    void networkTimeoutRetainsReservationAndNeverRetries() {
        accept();
        when(gateway.send(any())).thenThrow(new IllegalStateException("timeout"));
        service = atEight();
        service.sendToday();
        service.sendToday();
        verify(gateway, times(1)).send(any());
        assertEquals("RESERVED", jdbc.queryForObject("SELECT state FROM daily_reminder_credit", String.class));
        assertEquals("UNKNOWN", jdbc.queryForObject("SELECT status FROM daily_reminder_delivery", String.class));
    }
    @Test
    void concurrentClaimsCannotReserveOrSendTwice() throws Exception {
        accept();
        service.accept(1, "request-00000002", DailyChallengeService.TEMPLATE_ID, "accept");
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            List<Callable<DailyChallengeStore.Claim>> calls = new ArrayList<>();
            for (int i = 0; i < 12; i++) calls.add(() -> store.claim(1, LocalDate.of(2026, 11, 1), 10));
            int claims = 0;
            for (Future<DailyChallengeStore.Claim> f : pool.invokeAll(calls))
                if (f.get() != null)
                    claims++;
            assertEquals(1, claims);
            assertEquals(1, service.reminder(1).get("credits"));
            assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM daily_reminder_delivery", Integer.class));
        } finally {
            pool.shutdownNow();
        }
    }
    @Test
    void shareCodeRequiresActiveQuestionAndAllowedEnvironment() {
        when(gateway.shareCode(10, "trial")).thenReturn(new byte[] {(byte) 137, 80, 78, 71, 13, 10, 26, 10, 1});
        Map<String, Object> code = service.shareCode(10, "trial");
        assertEquals("image/png", code.get("mime_type"));
        assertEquals("trial", code.get("env_version"));
        assertNotNull(code.get("image_base64"));
        assertThrows(RuntimeException.class, () -> service.shareCode(10, "other"));
        assertThrows(RuntimeException.class, () -> service.shareCode(30, "release"));
        assertThrows(RuntimeException.class, () -> service.shareCode(999, "release"));
        verify(gateway, times(1)).shareCode(anyInt(), anyString());
    }
    @Test
    void wechatJsonErrorIsNotReturnedAsImage() {
        when(gateway.shareCode(10, "release")).thenReturn("{\"errcode\":40001}".getBytes(StandardCharsets.UTF_8));
        assertThrows(RuntimeException.class, () -> service.shareCode(10, "release"));
    }
    @Test
    void failedCreditCanBeUsedOnLaterDateAndSuccessCannotBeReused() {
        accept();
        when(gateway.send(any())).thenReturn(response(40037), response(0));
        service = atEight();
        service.sendToday();
        service = new DailyChallengeService(
            store, gateway, Clock.fixed(Instant.parse("2026-11-02T00:00:00Z"), ZoneOffset.UTC), "release");
        service.sendToday();
        assertEquals(0, service.reminder(1).get("credits"));
        service = new DailyChallengeService(
            store, gateway, Clock.fixed(Instant.parse("2026-11-03T00:00:00Z"), ZoneOffset.UTC), "release");
        service.sendToday();
        verify(gateway, times(2)).send(any());
    }
    @Test
    void replayingAcceptDoesNotReenablePreferenceAfterDisable() {
        accept();
        service.setReminder(1, false);
        accept();
        assertEquals(false, service.reminder(1).get("enabled"));
        assertEquals(1, service.reminder(1).get("credits"));
    }
    @Test
    void nullWechatResponseDoesNotReleaseCreditOrRetry() {
        accept();
        service = atEight();
        service.sendToday();
        service.sendToday();
        verify(gateway, times(1)).send(any());
        assertEquals("UNKNOWN", jdbc.queryForObject("SELECT status FROM daily_reminder_delivery", String.class));
        assertEquals(0, service.reminder(1).get("credits"));
    }
    @Test
    void schedulerUsesEightOclockShanghaiTime() throws Exception {
        org.springframework.scheduling.annotation.Scheduled scheduled =
            DailyChallengeSchedule.class.getMethod("sendAtEight")
                .getAnnotation(org.springframework.scheduling.annotation.Scheduled.class);
        assertEquals("0 0 8 * * *", scheduled.cron());
        assertEquals("Asia/Shanghai", scheduled.zone());
    }
    @Test
    void tokenPreparationFailureDoesNotReserveAnUnsentCredit() {
        accept();
        site.haihui.challenge.utils.WeixinProxy proxy = mock(site.haihui.challenge.utils.WeixinProxy.class);
        when(proxy.getAccessToken(any(), any(), any())).thenReturn("");
        service = new DailyChallengeService(store, new DailyWechatGateway(proxy),
            Clock.fixed(Instant.parse("2026-11-01T00:00:00Z"), ZoneOffset.UTC), "release");
        service.sendToday();
        assertEquals("AVAILABLE", jdbc.queryForObject("SELECT state FROM daily_reminder_credit", String.class));
        verify(proxy, never()).sendMiniMessage(any(), any());
    }

    void accept() {
        service.accept(1, "request-00000001", DailyChallengeService.TEMPLATE_ID, "accept");
    }
    DailyChallengeService atEight() {
        return new DailyChallengeService(
            store, gateway, Clock.fixed(Instant.parse("2026-11-01T00:00:00Z"), ZoneOffset.UTC), "release");
    }
    MiniResponse response(int code) {
        MiniResponse response = new MiniResponse();
        response.setErrCode(code);
        return response;
    }
}
