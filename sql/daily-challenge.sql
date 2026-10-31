-- Apply to the existing challenge database before deploying the daily challenge backend.
-- Additive and idempotent. No subscription credit is granted by this migration.
CREATE TABLE IF NOT EXISTS daily_challenge (
  challenge_date date NOT NULL,
  question_id int unsigned NOT NULL,
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (challenge_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS daily_reminder_preference (
  uid int unsigned NOT NULL,
  enabled tinyint NOT NULL DEFAULT 0,
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (uid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS daily_reminder_credit (
  id bigint unsigned NOT NULL AUTO_INCREMENT,
  uid int unsigned NOT NULL,
  request_id varchar(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  state varchar(16) NOT NULL DEFAULT 'AVAILABLE',
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_request (uid, request_id),
  KEY idx_user_state (uid, state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS daily_reminder_delivery (
  uid int unsigned NOT NULL,
  challenge_date date NOT NULL,
  question_id int unsigned NOT NULL,
  credit_id bigint unsigned NOT NULL,
  status varchar(16) NOT NULL DEFAULT 'SENDING',
  wechat_errcode int DEFAULT NULL,
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (uid, challenge_date),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
