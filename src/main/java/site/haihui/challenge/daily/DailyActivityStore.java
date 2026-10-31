package site.haihui.challenge.daily;

import java.sql.Date;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Repository
public class DailyActivityStore {
    private final JdbcTemplate jdbc;
    private final TransactionTemplate writes;

    public DailyActivityStore(JdbcTemplate jdbc, PlatformTransactionManager manager) {
        this.jdbc = jdbc;
        this.writes = new TransactionTemplate(manager);
        this.writes.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public void record(int uid, String source, String attemptKey, int questionId, LocalDate date, long epoch) {
        writes.execute(status -> {
            jdbc.update(
                "INSERT IGNORE INTO daily_activity(uid,source,attempt_key,question_id,activity_date,answered_at_epoch) "
                + "VALUES(?,?,?,?,?,?)",
                uid, source, attemptKey, questionId, Date.valueOf(date), epoch);
            return null;
        });
    }

    public Map<LocalDate, Long> counts(int uid, LocalDate start, LocalDate end) {
        Map<LocalDate, Long> result = new LinkedHashMap<>();
        jdbc.query("SELECT activity_date,COUNT(*) AS answer_count FROM daily_activity WHERE uid=? AND activity_date>=? "
                   + "AND activity_date<=? GROUP BY activity_date",
            row
            -> { result.put(row.getDate("activity_date").toLocalDate(), row.getLong("answer_count")); },
            uid, Date.valueOf(start), Date.valueOf(end));
        return result;
    }

    public long trackingStartedAt() {
        return jdbc.queryForObject("SELECT started_at_epoch FROM daily_activity_tracking WHERE id=1", Long.class);
    }
}
