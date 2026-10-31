package site.haihui.challenge.daily;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import site.haihui.challenge.common.exception.BaseException;
import site.haihui.challenge.dto.weixin.MiniMessageBody;
import site.haihui.challenge.dto.weixin.MiniResponse;
import site.haihui.challenge.dto.weixin.MiniTemplateData;
import site.haihui.challenge.utils.MiniAppCode;

@Slf4j
@Service
public class DailyChallengeService {
    public static final String TEMPLATE_ID = "faTHcOm8TebcnbsK92I5DyJ-bJxo7XQfR5wiwJdh24A";
    public static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final DailyChallengeStore store;
    private final DailyWechatGateway gateway;
    private final Clock clock;
    private final String environment;

    public DailyChallengeService(DailyChallengeStore store, DailyWechatGateway gateway,
        @Qualifier("dailyChallengeClock") Clock clock,
        @Value("${daily-challenge.env-version:release}") String environment) {
        requireEnvironment(environment);
        this.store = store;
        this.gateway = gateway;
        this.clock = clock;
        this.environment = environment;
    }

    private ZonedDateTime now() {
        return clock.instant().atZone(ZONE);
    }

    private static BaseException fail(String message) {
        return new BaseException(400, message);
    }

    public Map<String, Object> today() {
        LocalDate date = now().toLocalDate();
        Integer id = store.questionForDate(date);
        if (id == null)
            throw fail("暂无可用的每日挑战题目");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("date", date.toString());
        result.put("question_id", id);
        result.put("title", "每日挑战");
        result.put("start_time", date.atTime(8, 0).format(FORMAT));
        result.put("page", page(id));
        return result;
    }

    public Map<String, Object> reminder(int uid) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("enabled", store.enabled(uid));
        result.put("credits", store.availableCredits(uid));
        result.put("template_id", TEMPLATE_ID);
        ZonedDateTime next = now().toLocalDate().atTime(8, 0).atZone(ZONE);
        if (!now().isBefore(next))
            next = next.plusDays(1);
        result.put("next_send_time", next.format(FORMAT));
        return result;
    }

    public Map<String, Object> setReminder(int uid, boolean enabled) {
        store.setEnabled(uid, enabled);
        return reminder(uid);
    }

    public Map<String, Object> accept(int uid, String requestId, String templateId, String result) {
        if (!TEMPLATE_ID.equals(templateId) || !"accept".equals(result))
            throw fail("请先同意微信的一次订阅提醒");
        if (requestId == null || !requestId.matches("[A-Za-z0-9_-]{8,64}"))
            throw fail("订阅请求标识无效");
        store.accept(uid, requestId);
        return reminder(uid);
    }

    public Map<String, Object> shareCode(int questionId, String env) {
        requireEnvironment(env);
        if (!store.validQuestion(questionId))
            throw fail("题目不存在或已下架");
        byte[] image;
        try {
            image = gateway.shareCode(questionId, env);
        } catch (RuntimeException e) {
            throw fail("小程序码生成失败，请稍后重试");
        }
        Map<String, Object> result = MiniAppCode.imagePayload(image);
        result.put("question_id", questionId);
        result.put("env_version", env);
        return result;
    }

    public static void requireEnvironment(String environment) {
        MiniAppCode.requireEnvironment(environment);
    }

    private static String page(int id) {
        return "pages/answer/answer?question_id=" + id;
    }

    public void sendToday() {
        ZonedDateTime now = now();
        if (now.toLocalTime().isBefore(LocalTime.of(8, 0)))
            return;
        LocalDate date = now.toLocalDate();
        Integer question = store.questionForDate(date);
        if (question == null)
            return;
        int after = 0;
        while (true) {
            List<Integer> users = store.candidates(after, 200);
            if (users.isEmpty())
                return;
            for (Integer uid : users) {
                after = uid;
                try {
                    sendOne(uid, date, question);
                } catch (RuntimeException e) {
                    // A process/DB failure must leave the durable SENDING claim in place.
                    log.warn(
                        "Daily reminder dispatch incomplete for user {} on {}; claim will not be retried", uid, date);
                }
            }
        }
    }

    private void sendOne(int uid, LocalDate date, int question) {
        DailyChallengeStore.Claim claim = store.claim(uid, date, question);
        if (claim == null)
            return;
        MiniMessageBody body = new MiniMessageBody();
        body.setTouser(claim.openid);
        body.setTemplateId(TEMPLATE_ID);
        body.setPage(page(question));
        body.setMiniprogramState(
            "release".equals(environment) ? "formal" : ("trial".equals(environment) ? "trial" : "developer"));
        Map<String, MiniTemplateData> data = new LinkedHashMap<>();
        MiniTemplateData title = new MiniTemplateData();
        title.setValue("每日挑战");
        data.put("thing1", title);
        MiniTemplateData start = new MiniTemplateData();
        start.setValue(date.atTime(8, 0).format(FORMAT));
        data.put("time7", start);
        body.setData(data);
        MiniResponse response;
        try {
            response = gateway.send(body);
        } catch (DailyWechatGateway.SendNotStartedException e) {
            store.finish(claim, date, "FAILED", "AVAILABLE", null);
            return;
        } catch (RuntimeException e) {
            store.finish(claim, date, "UNKNOWN", "RESERVED", null);
            return;
        }
        if (response == null || response.getErrCode() == null) {
            store.finish(claim, date, "UNKNOWN", "RESERVED", null);
            return;
        }
        int code = response.getErrCode();
        if (code == 0)
            store.finish(claim, date, "SENT", "CONSUMED", code);
        else if (code == 43101)
            store.finish(claim, date, "FAILED", "INVALID", code);
        else
            store.finish(claim, date, "FAILED", "AVAILABLE", code);
    }
}
