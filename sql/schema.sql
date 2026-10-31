-- Reconstructed from Java entities, mapper SQL and service writes.
-- Select the destination database before running this file. No existing data is removed.
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `user` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `nickname` varchar(100) DEFAULT NULL,
  `avatar` varchar(1000) DEFAULT NULL,
  `sex` tinyint DEFAULT NULL,
  `openid` varchar(512) DEFAULT NULL,
  `app_type` tinyint NOT NULL DEFAULT 1,
  `country` varchar(100) DEFAULT NULL,
  `province` varchar(100) DEFAULT NULL,
  `city` varchar(100) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_open_id` (`openid`, `app_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `question` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `content` varchar(1000) NOT NULL,
  `options` varchar(2000) NOT NULL DEFAULT '' COMMENT 'Options separated by |',
  `answer` tinyint unsigned NOT NULL COMMENT 'One-based option index',
  `level` tinyint unsigned NOT NULL DEFAULT 1,
  `category` tinyint unsigned NOT NULL DEFAULT 2 COMMENT '1 Finance,2 General,3 History,4 Geography,5 Poetry,6 Driving1,7 Driving4,8 Traffic,9 Funds,10 WorldCup',
  `status` tinyint unsigned NOT NULL DEFAULT 1 COMMENT '1 Active,0 Hidden',
  `analysis` mediumtext,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_level` (`level`),
  KEY `idx_category_status` (`category`, `status`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `round` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `uid` int unsigned NOT NULL,
  `total_question` int NOT NULL DEFAULT 0,
  `correct_question` int NOT NULL DEFAULT 0,
  `score` int NOT NULL DEFAULT 0,
  `is_over` tinyint NOT NULL DEFAULT 0,
  `timeout` int NOT NULL DEFAULT 30,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_uid_score` (`uid`, `score`),
  KEY `idx_create_time_score` (`create_time`, `score`),
  KEY `idx_score` (`score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `round_detail` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `round_id` int unsigned NOT NULL,
  `uid` int unsigned NOT NULL,
  `question_id` int unsigned NOT NULL,
  `cost_second` int NOT NULL DEFAULT 0,
  `score` int NOT NULL DEFAULT 0,
  `answer` tinyint NOT NULL DEFAULT 0 COMMENT '0 Unanswered',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_round_uid_question` (`round_id`, `uid`, `question_id`),
  KEY `idx_uid_question` (`uid`, `question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `wrong_question_book` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `uid` int unsigned NOT NULL,
  `question_id` int unsigned NOT NULL,
  `answer` tinyint NOT NULL DEFAULT 0,
  `status` tinyint NOT NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_uid_status_id` (`uid`, `status`, `id`),
  KEY `idx_question_id` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `coin_record` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `uid` int unsigned NOT NULL,
  `amount` int NOT NULL DEFAULT 0 COMMENT 'Signed coin change',
  `source` int NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_uid_id` (`uid`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `feedback` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `uid` int unsigned NOT NULL,
  `nickname` varchar(100) DEFAULT NULL,
  `question_id` int unsigned NOT NULL DEFAULT 0,
  `type` int NOT NULL,
  `remark` text,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_uid` (`uid`),
  KEY `idx_question_id` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `quotes` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `quote` text NOT NULL,
  `author` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_author` (`author`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
