package site.haihui.challenge.daily;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import site.haihui.challenge.common.exception.BaseException;

@Slf4j
@Service
public class DailyActivityService {
    private final DailyActivityStore store;
    private final Clock clock;

    public DailyActivityService(DailyActivityStore store, @Qualifier("dailyChallengeClock") Clock clock) {
        this.store = store;
        this.clock = clock;
    }

    public Map<String, Object> activity(int uid, Integer dayCount) {
        if (dayCount == null || dayCount < 1 || dayCount > 366) {
            throw new BaseException(400, "查询天数必须为1至366天");
        }
        LocalDate end = clock.instant().atZone(DailyChallengeService.ZONE).toLocalDate();
        LocalDate start = end.minusDays(dayCount - 1L);
        try {
            Map<LocalDate, Long> counts = store.counts(uid, start, end);
            ZonedDateTime boundary =
                Instant.ofEpochSecond(store.trackingStartedAt()).atZone(DailyChallengeService.ZONE);
            List<Map<String, Object>> days = new ArrayList<>();
            long total = 0;
            int active = 0;
            for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                long count = counts.getOrDefault(date, 0L);
                Map<String, Object> day = new LinkedHashMap<>();
                day.put("date", date.toString());
                day.put("count", count);
                days.add(day);
                total += count;
                if (count > 0)
                    active++;
            }
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("start_date", start.toString());
            result.put("end_date", end.toString());
            result.put("days", days);
            result.put("total_count", total);
            result.put("active_days", active);
            result.put("history_start_date", boundary.toLocalDate().toString());
            result.put("tracking_started_at", boundary.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            return result;
        } catch (RuntimeException e) {
            throw new BaseException(400, "暂时无法读取做题记录，请稍后重试");
        }
    }

    public void recordChallenge(
        Integer uid, Integer detailId, Integer questionId, Integer answer, Integer timeout, String options) {
        if (!Integer.valueOf(0).equals(timeout) || detailId == null || detailId <= 0 || !validAnswer(answer, options))
            return;
        record(uid, "challenge", String.valueOf(detailId), questionId);
    }

    public void recordPractice(Integer uid, Integer questionId, Integer answer, String options) {
        if (!validAnswer(answer, options))
            return;
        record(uid, "practice", null, questionId);
    }

    private boolean validAnswer(Integer answer, String options) {
        if (answer == null || answer <= 0 || options == null)
            return false;
        String[] choices = options.split("\\|", -1);
        return answer <= choices.length && !choices[answer - 1].trim().isEmpty();
    }

    private void record(Integer uid, String source, String attempt, Integer questionId) {
        if (uid == null || uid <= 0 || questionId == null || questionId <= 0)
            return;
        try {
            Instant answered = clock.instant();
            LocalDate date = answered.atZone(DailyChallengeService.ZONE).toLocalDate();
            String attemptKey = attempt == null ? date + ":" + questionId : attempt;
            Runnable persist = () -> {
                try {
                    store.record(uid, source, attemptKey, questionId, date, answered.getEpochSecond());
                } catch (RuntimeException e) {
                    log.warn(
                        "Answer activity could not be recorded for user {}; accepted answer remains committed", uid);
                }
            };
            if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        persist.run();
                    }
                });
            } else {
                persist.run();
            }
        } catch (RuntimeException e) {
            log.warn("Answer activity hook unavailable for user {}; answer behavior is unchanged", uid);
        }
    }
}
