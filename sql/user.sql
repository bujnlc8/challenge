CREATE TABLE `user` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `nickname` varchar(100) DEFAULT NULL,
  `avatar` varchar(200) DEFAULT NULL,
  `sex` tinyint(1) DEFAULT NULL,
  `openid` varchar(512) DEFAULT NULL,
  `app_type` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1 微信小程序',
  `country` varchar(100) DEFAULT NULL,
  `province` varchar(100) DEFAULT NULL,
  `city` varchar(100) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_open_id` (`openid`,`app_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
