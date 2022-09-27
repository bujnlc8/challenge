CREATE TABLE `question` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `content` varchar(1000) NOT NULL COMMENT '题目内容',
  `options` varchar(2000) NOT NULL DEFAULT '' COMMENT '题目选项，以|隔开',
  `answer` tinyint(1) NOT NULL COMMENT '答案1,2...',
  `level` tinyint(1) NOT NULL DEFAULT '1' COMMENT '难易度 1：简单，2：中等，3：困难',
  `category` tinyint(1) NOT NULL DEFAULT '2' COMMENT '题目分类 1 常识 2 百科 3 历史 4 地理 5 生活 6 体育 7 法规 8 诗词',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1 正常 0 删除',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_level` (`level`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
