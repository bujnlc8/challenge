package site.haihui.challenge.daily;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.sql.Connection;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.support.TransactionTemplate;
import site.haihui.challenge.common.auth.LoginInterceptor;
import site.haihui.challenge.common.exception.BaseException;
import site.haihui.challenge.common.exception.GlobalExceptionHandler;
import site.haihui.challenge.controller.DailyChallengeController;
import site.haihui.challenge.entity.User;
import site.haihui.challenge.service.IRedisService;
import site.haihui.challenge.service.IShareService;

class DailyActivityTest {
    JdbcTemplate jdbc;
    DailyActivityStore store;
    DailyActivityService service;
    TransactionTemplate transactions;
    Clock clock = Clock.fixed(Instant.parse("2026-11-01T16:00:00Z"), ZoneOffset.UTC);
    @BeforeEach
    void setup() throws Exception {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE");
        jdbc = new JdbcTemplate(ds);
        try (Connection connection = ds.getConnection()) {
            ScriptUtils.executeSqlScript(connection, new FileSystemResource("sql/daily-activity.sql"));
        }
        DataSourceTransactionManager manager = new DataSourceTransactionManager(ds);
        store = new DailyActivityStore(jdbc, manager);
        service = new DailyActivityService(store, clock);
        transactions = new TransactionTemplate(manager);
        jdbc.update("UPDATE daily_activity_tracking SET started_at_epoch=? WHERE id=1",
            Instant.parse("2026-11-01T00:00:00Z").getEpochSecond());
    }
    @Test
    void shanghaiMidnightRangeIsZeroFilledAndIncludesOnlyOwner() {
        DailyActivityService before =
            new DailyActivityService(store, Clock.fixed(Instant.parse("2026-11-01T15:59:59Z"), ZoneOffset.UTC));
        before.recordChallenge(1, 501, 20, 1, 0, "甲|乙");
        service.recordPractice(1, 21, 2, "甲|乙");
        service.recordPractice(2, 21, 2, "甲|乙");
        Map<String, Object> activity = service.activity(1, 3);
        assertEquals("2026-10-31", activity.get("start_date"));
        assertEquals("2026-11-02", activity.get("end_date"));
        assertEquals(
            Arrays.asList(day("2026-10-31", 0), day("2026-11-01", 1), day("2026-11-02", 1)), activity.get("days"));
        assertEquals(2L, activity.get("total_count"));
        assertEquals(2, activity.get("active_days"));
    }
    @Test
    void validatesRangeAndNeverBackfillsIncompleteLegacyHistory() {
        jdbc.execute("CREATE TABLE round_detail(id INT,uid INT,answer INT,cost_second INT)");
        jdbc.update("INSERT INTO round_detail VALUES(1,1,1,20)");
        assertEquals(0L, service.activity(1, 365).get("total_count"));
        assertEquals("2026-11-01", service.activity(1, 365).get("history_start_date"));
        assertEquals("2026-11-01 08:00:00", service.activity(1, 365).get("tracking_started_at"));
        assertThrows(BaseException.class, () -> service.activity(1, 0));
        assertThrows(BaseException.class, () -> service.activity(1, 367));
        assertEquals(366, ((List<?>) service.activity(1, 366).get("days")).size());
    }
    @Test
    void validAnswersDeduplicateChallengeAttemptsAndPracticeQuestionPerDay() {
        service.recordChallenge(1, 501, 20, 1, 0, "甲|乙");
        service.recordChallenge(1, 501, 20, 2, 0, "甲|乙");
        service.recordPractice(1, 20, 1, "甲|乙");
        service.recordPractice(1, 20, 2, "甲|乙");
        assertEquals(2L, service.activity(1, 1).get("total_count"));
        service.recordChallenge(1, 502, 20, 2, 0, "甲|乙");
        assertEquals(3L, service.activity(1, 1).get("total_count"));
    }
    @Test
    void timeoutMissingAndInvalidOptionsAreNotActivity() {
        service.recordChallenge(1, 501, 20, 1, 1, "甲|乙");
        service.recordChallenge(1, 502, 20, 0, 0, "甲|乙");
        service.recordChallenge(1, 503, 20, 3, 0, "甲|乙");
        service.recordChallenge(1, null, 20, 1, 0, "甲|乙");
        service.recordPractice(1, 20, 0, "甲|乙");
        service.recordPractice(1, 20, 3, "甲|乙");
        service.recordPractice(1, 20, 1, null);
        assertEquals(0L, service.activity(1, 1).get("total_count"));
    }
    @Test
    void practiceQuestionCanCountAgainOnNextShanghaiDate() {
        service.recordPractice(1, 20, 1, "甲|乙");
        DailyActivityService tomorrow = new DailyActivityService(store, Clock.offset(clock, Duration.ofDays(1)));
        tomorrow.recordPractice(1, 20, 1, "甲|乙");
        assertEquals(2L, tomorrow.activity(1, 2).get("total_count"));
        assertEquals(2, tomorrow.activity(1, 2).get("active_days"));
    }
    @Test
    void waitsForOriginalCommitAndDiscardsRolledBackAnswers() {
        transactions.execute(status -> {
            service.recordPractice(1, 20, 1, "甲|乙");
            assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM daily_activity", Integer.class));
            status.setRollbackOnly();
            return null;
        });
        assertEquals(0L, service.activity(1, 1).get("total_count"));
        transactions.execute(status -> {
            service.recordPractice(1, 20, 1, "甲|乙");
            return null;
        });
        assertEquals(1L, service.activity(1, 1).get("total_count"));
    }
    @Test
    void analyticsOutageCannotRollBackCommittedAnswer() {
        jdbc.execute("CREATE TABLE accepted_answer(id INT PRIMARY KEY)");
        jdbc.execute("DROP TABLE daily_activity");
        assertDoesNotThrow(() -> transactions.execute(status -> {
            jdbc.update("INSERT INTO accepted_answer VALUES(1)");
            service.recordPractice(1, 20, 1, "甲|乙");
            return null;
        }));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM accepted_answer", Integer.class));
        assertThrows(BaseException.class, () -> service.activity(1, 365));
    }
    @Test
    void concurrentDuplicateEventsStayOneContribution() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            List<Callable<Void>> tasks = new ArrayList<>();
            for (int i = 0; i < 12; i++)
                tasks.add(() -> {
                    service.recordPractice(1, 20, 1, "甲|乙");
                    return null;
                });
            for (Future<Void> result : pool.invokeAll(tasks)) result.get();
            assertEquals(1L, service.activity(1, 1).get("total_count"));
        } finally {
            pool.shutdownNow();
        }
    }
    @Test
    @SuppressWarnings("unchecked")
    void endpointRequiresLoginAndCannotReadAnotherUsersCounts() throws Exception {
        service.recordPractice(1, 20, 1, "甲|乙");
        service.recordPractice(2, 20, 1, "甲|乙");
        service.recordPractice(2, 21, 1, "甲|乙");
        LoginInterceptor interceptor = new LoginInterceptor();
        IRedisService<String> redis = mock(IRedisService.class);
        IShareService users = mock(IShareService.class);
        User owner = new User();
        owner.setId(1);
        owner.setAppType(1);
        when(redis.get("challenge:1:token")).thenReturn("session-token");
        when(users.getUser(1)).thenReturn(owner);
        ReflectionTestUtils.setField(interceptor, "redisService", redis);
        ReflectionTestUtils.setField(interceptor, "shareService", users);
        MockMvc mvc =
            MockMvcBuilders.standaloneSetup(new DailyChallengeController(mock(DailyChallengeService.class), service))
                .addInterceptors(interceptor)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        mvc.perform(get("/daily-challenge/activity")).andExpect(status().isUnauthorized());
        mvc.perform(get("/daily-challenge/activity")
                        .param("days", "1")
                        .param("uid", "2")
                        .header("Authorization", "Bearer 1_session-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.total_count").value(1))
            .andExpect(jsonPath("$.data.days[0].count").value(1));
    }
    Map<String, Object> day(String date, long count) {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("date", date);
        d.put("count", count);
        return d;
    }
}
