-- Additive forward-only answer activity. No legacy answer history is guessed or backfilled.
CREATE TABLE IF NOT EXISTS daily_activity (
  uid int unsigned NOT NULL,
  source varchar(16) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  attempt_key varchar(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  question_id int unsigned NOT NULL,
  activity_date date NOT NULL,
  answered_at_epoch bigint unsigned NOT NULL,
  PRIMARY KEY (uid, source, attempt_key),
  KEY idx_uid_activity_date (uid, activity_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS daily_activity_tracking (
  id tinyint unsigned NOT NULL,
  started_at_epoch bigint unsigned NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- UNIX_TIMESTAMP is timezone-independent; preserve the original launch boundary on reruns.
INSERT IGNORE INTO daily_activity_tracking(id, started_at_epoch) VALUES(1, UNIX_TIMESTAMP());
