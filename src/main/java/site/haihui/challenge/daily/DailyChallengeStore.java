package site.haihui.challenge.daily;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** Durable selection, subscription credits and at-most-once daily dispatch claims. */
@Repository
public class DailyChallengeStore {
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactions;

    public DailyChallengeStore(JdbcTemplate jdbc, PlatformTransactionManager transactionManager) {
        this.jdbc = jdbc;
        this.transactions = new TransactionTemplate(transactionManager);
    }

    public Integer questionForDate(LocalDate date) {
        List<Integer> existing = jdbc.queryForList(
            "SELECT question_id FROM daily_challenge WHERE challenge_date=?", Integer.class, Date.valueOf(date));
        if (!existing.isEmpty()) {
            Integer pinned = existing.get(0);
            return validQuestion(pinned) ? pinned : null;
        }
        List<Integer> ids = jdbc.queryForList("SELECT id FROM question WHERE status=1 ORDER BY id", Integer.class);
        if (ids.isEmpty())
            return null;
        Integer selected = ids.get((int) Math.floorMod(date.toEpochDay(), (long) ids.size()));
        jdbc.update(
            "INSERT IGNORE INTO daily_challenge(challenge_date,question_id) VALUES(?,?)", Date.valueOf(date), selected);
        Integer pinned = jdbc.queryForObject(
            "SELECT question_id FROM daily_challenge WHERE challenge_date=?", Integer.class, Date.valueOf(date));
        return validQuestion(pinned) ? pinned : null;
    }

    public boolean validQuestion(Integer id) {
        return id != null
            && jdbc.queryForObject("SELECT COUNT(*) FROM question WHERE id=? AND status=1", Integer.class, id) > 0;
    }

    private void ensurePreference(int uid) {
        jdbc.update("INSERT IGNORE INTO daily_reminder_preference(uid,enabled) VALUES(?,0)", uid);
    }

    public void setEnabled(int uid, boolean enabled) {
        transactions.execute(status -> {
            ensurePreference(uid);
            jdbc.update("UPDATE daily_reminder_preference SET enabled=?,update_time=CURRENT_TIMESTAMP WHERE uid=?",
                enabled ? 1 : 0, uid);
            return null;
        });
    }

    public boolean enabled(int uid) {
        List<Integer> values =
            jdbc.queryForList("SELECT enabled FROM daily_reminder_preference WHERE uid=?", Integer.class, uid);
        return !values.isEmpty() && values.get(0) == 1;
    }

    public int availableCredits(int uid) {
        return jdbc.queryForObject(
            "SELECT COUNT(*) FROM daily_reminder_credit WHERE uid=? AND state='AVAILABLE'", Integer.class, uid);
    }

    public void accept(int uid, String requestId) {
        transactions.execute(status -> {
            ensurePreference(uid);
            jdbc.queryForObject(
                "SELECT enabled FROM daily_reminder_preference WHERE uid=? FOR UPDATE", Integer.class, uid);
            int inserted =
                jdbc.update("INSERT IGNORE INTO daily_reminder_credit(uid,request_id,state) VALUES(?,?,'AVAILABLE')",
                    uid, requestId);
            if (inserted == 1)
                jdbc.update(
                    "UPDATE daily_reminder_preference SET enabled=1,update_time=CURRENT_TIMESTAMP WHERE uid=?", uid);
            return null;
        });
    }

    public List<Integer> candidates(int after, int limit) {
        return jdbc.queryForList("SELECT p.uid FROM daily_reminder_preference p JOIN `user` u ON u.id=p.uid "
                + "WHERE p.enabled=1 AND p.uid>? AND u.app_type=1 AND u.openid IS NOT NULL AND u.openid<>'' "
                + "AND EXISTS(SELECT 1 FROM daily_reminder_credit c WHERE c.uid=p.uid AND c.state='AVAILABLE') ORDER "
                  + "BY p.uid LIMIT ?",
            Integer.class, after, limit);
    }

    public Claim claim(int uid, LocalDate date, int questionId) {
        return transactions.execute(status -> {
            List<Integer> preferences = jdbc.queryForList(
                "SELECT enabled FROM daily_reminder_preference WHERE uid=? FOR UPDATE", Integer.class, uid);
            if (preferences.isEmpty() || preferences.get(0) != 1)
                return null;
            List<String> openids = jdbc.queryForList(
                "SELECT openid FROM `user` WHERE id=? AND app_type=1 AND openid IS NOT NULL AND openid<>''",
                String.class, uid);
            if (openids.isEmpty())
                return null;
            List<Long> ids = jdbc.queryForList(
                "SELECT id FROM daily_reminder_credit WHERE uid=? AND state='AVAILABLE' ORDER BY id LIMIT 1 FOR UPDATE",
                Long.class, uid);
            if (ids.isEmpty())
                return null;
            long credit = ids.get(0);
            int inserted = jdbc.update(
                "INSERT IGNORE INTO daily_reminder_delivery(uid,challenge_date,question_id,credit_id,status) "
                + "VALUES(?,?,?,?,'SENDING')",
                uid, Date.valueOf(date), questionId, credit);
            if (inserted != 1)
                return null;
            jdbc.update("UPDATE daily_reminder_credit SET state='RESERVED',update_time=CURRENT_TIMESTAMP WHERE id=? "
                        + "AND state='AVAILABLE'",
                credit);
            return new Claim(uid, openids.get(0), credit);
        });
    }

    public void finish(Claim claim, LocalDate date, String deliveryStatus, String creditState, Integer errorCode) {
        transactions.execute(status -> {
            int changed = jdbc.update(
                "UPDATE daily_reminder_delivery SET status=?,wechat_errcode=?,update_time=CURRENT_TIMESTAMP WHERE "
                + "uid=? AND challenge_date=? AND status='SENDING'",
                deliveryStatus, errorCode, claim.uid, Date.valueOf(date));
            if (changed == 1)
                jdbc.update("UPDATE daily_reminder_credit SET state=?,update_time=CURRENT_TIMESTAMP WHERE id=? AND "
                            + "state='RESERVED'",
                    creditState, claim.creditId);
            return null;
        });
    }

    public static final class Claim {
        public final int uid;
        public final String openid;
        public final long creditId;
        Claim(int uid, String openid, long creditId) {
            this.uid = uid;
            this.openid = openid;
            this.creditId = creditId;
        }
    }
}
