CREATE TABLE `ai_chat_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `user_id` bigint NOT NULL COMMENT 'User id',
  `role` varchar(16) NOT NULL COMMENT 'Role (user/assistant)',
  `content_text` text COMMENT 'Plain text',
  `content_json` text COMMENT 'Raw content json',
  `created_at` datetime NOT NULL COMMENT 'Created time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=59 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI chat messages';

CREATE TABLE `ai_vision_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `type` varchar(32) NOT NULL,
  `image_url` varchar(512) DEFAULT NULL,
  `payload_json` text,
  `created_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_ai_vision_user` (`user_id`),
  KEY `idx_ai_vision_created` (`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `diet_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `user_id` bigint NOT NULL COMMENT 'User id',
  `meal_type` varchar(32) NOT NULL COMMENT 'Meal type',
  `food_name` varchar(64) NOT NULL COMMENT 'Food name',
  `calories` int NOT NULL COMMENT 'Calories',
  `protein` double DEFAULT NULL COMMENT 'Protein (g)',
  `carbs` double DEFAULT NULL COMMENT 'Carbs (g)',
  `fat` double DEFAULT NULL COMMENT 'Fat (g)',
  `note` varchar(255) DEFAULT NULL COMMENT 'Notes',
  `date` date NOT NULL COMMENT 'Record date',
  `photo_url` varchar(255) DEFAULT NULL COMMENT '饮食图片URL',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=133 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Diet records';

CREATE TABLE `exercise_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `user_id` bigint NOT NULL COMMENT 'User id',
  `type` varchar(32) NOT NULL COMMENT 'Exercise type',
  `steps` int DEFAULT NULL COMMENT 'Steps',
  `duration` int NOT NULL COMMENT 'Duration (minutes)',
  `calories` int NOT NULL COMMENT 'Calories',
  `date` date NOT NULL COMMENT 'Record date',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Exercise records';

CREATE TABLE `family_member` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `user_id` bigint NOT NULL COMMENT 'Owner user id',
  `name` varchar(64) NOT NULL COMMENT 'Name',
  `relation` varchar(32) DEFAULT NULL COMMENT 'Relation',
  `age` int DEFAULT NULL COMMENT 'Age',
  `condition_text` varchar(64) DEFAULT NULL COMMENT 'Condition/notes',
  `role` varchar(16) DEFAULT NULL COMMENT 'Role label',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT 'Status (0=未授权,1=已授权)',
  `avatar` varchar(255) DEFAULT NULL COMMENT 'Avatar url',
  `created_at` datetime DEFAULT NULL COMMENT 'Created time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Family members';

CREATE TABLE `file_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `user_id` bigint DEFAULT NULL COMMENT 'User id',
  `type` varchar(32) DEFAULT NULL COMMENT 'File type',
  `original_name` varchar(255) DEFAULT NULL COMMENT 'Original file name',
  `file_name` varchar(255) DEFAULT NULL COMMENT 'Stored file name',
  `file_path` varchar(512) DEFAULT NULL COMMENT 'Local file path',
  `file_url` varchar(512) DEFAULT NULL COMMENT 'Public URL',
  `file_size` bigint DEFAULT NULL COMMENT 'File size',
  `content_type` varchar(128) DEFAULT NULL COMMENT 'Content type',
  `created_at` datetime NOT NULL COMMENT 'Created time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='File records';

CREATE TABLE `goal` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `user_id` bigint NOT NULL COMMENT 'User id',
  `goal_type` int NOT NULL COMMENT 'Goal type (int)',
  `target_value` int NOT NULL COMMENT 'Target value',
  `current_value` int NOT NULL COMMENT 'Current value',
  `period` varchar(16) NOT NULL COMMENT 'Period (day/week)',
  `start_date` date DEFAULT NULL COMMENT '开始日期',
  `end_date` date DEFAULT NULL COMMENT '结束日期',
  `status` tinyint DEFAULT '1' COMMENT '状态(1:进行中, 2:已达成, 3:放弃)',
  `ai_strategy` text COMMENT 'AI生成的执行策略/建议描述',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Goals';

CREATE TABLE `health_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `user_id` bigint NOT NULL COMMENT 'User id',
  `systolic` int DEFAULT NULL COMMENT 'Systolic blood pressure',
  `diastolic` int DEFAULT NULL COMMENT 'Diastolic blood pressure',
  `heart_rate` int DEFAULT NULL COMMENT 'Heart rate',
  `date` date NOT NULL COMMENT 'Record date',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=68 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Blood pressure/heart rate records';

CREATE TABLE `medication` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `user_id` bigint NOT NULL COMMENT 'User id',
  `drug_name` varchar(64) NOT NULL COMMENT 'Drug name',
  `dosage` varchar(32) NOT NULL COMMENT 'Dosage',
  `frequency` varchar(32) NOT NULL COMMENT 'Frequency',
  `remind_time` varchar(16) DEFAULT NULL COMMENT 'Remind time (yyyy-MM-dd HH:mm)',
  `stock` int DEFAULT NULL COMMENT 'Stock',
  `stock_threshold` int DEFAULT NULL COMMENT 'Low stock threshold',
  `start_date` date NOT NULL COMMENT 'Start date',
  `end_date` date DEFAULT NULL COMMENT 'End date',
  `notes` varchar(255) DEFAULT NULL COMMENT 'Notes',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Medication list';

CREATE TABLE `medication_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `user_id` bigint NOT NULL COMMENT 'User id',
  `medication_id` bigint NOT NULL COMMENT 'Medication id',
  `date` date NOT NULL COMMENT 'Date',
  `time` time NOT NULL COMMENT 'Time',
  `status` tinyint NOT NULL COMMENT 'Status (0=未服,1=已服,2=漏服)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Medication intake records';

CREATE TABLE `period_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `user_id` bigint NOT NULL COMMENT 'User id',
  `start_date` date NOT NULL COMMENT 'Period start date',
  `end_date` date NOT NULL COMMENT 'Period end date',
  `flow` int DEFAULT NULL COMMENT 'Flow level (int)',
  `note` varchar(255) DEFAULT NULL COMMENT 'Notes',
  `created_at` datetime DEFAULT NULL COMMENT 'Created time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Period records';

CREATE TABLE `privacy_settings` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `user_id` bigint NOT NULL COMMENT 'User id',
  `allow_subscribe` tinyint NOT NULL DEFAULT '1' COMMENT 'Allow subscribe messages',
  `allow_cloud_sync` tinyint NOT NULL DEFAULT '1' COMMENT 'Allow cloud sync',
  `allow_family_share` tinyint NOT NULL DEFAULT '1' COMMENT 'Allow family share',
  `created_at` datetime DEFAULT NULL COMMENT 'Created time',
  `updated_at` datetime DEFAULT NULL COMMENT 'Updated time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Privacy settings';

CREATE TABLE `reminder` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `user_id` bigint NOT NULL COMMENT 'User id',
  `title` varchar(64) NOT NULL COMMENT 'Title',
  `type` int NOT NULL COMMENT 'Type (1=运动,2=饮食,3=睡眠,4=用药)',
  `content` varchar(255) DEFAULT NULL COMMENT 'Content',
  `remind_time` datetime DEFAULT NULL COMMENT 'Remind time',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT 'Status (0=未提醒,1=已提醒)',
  `created_at` datetime DEFAULT NULL COMMENT 'Created time',
  `source_type` tinyint DEFAULT '0' COMMENT '来源(0:手动设置, 1:AI补救自动生成)',
  `related_record_id` bigint DEFAULT NULL COMMENT '关联的数据记录ID(如饮食记录ID)',
  `finish_time` datetime DEFAULT NULL COMMENT '实际完成时间',
  `priority` tinyint DEFAULT '1' COMMENT '优先级(1:普通, 2:重要, 3:紧急)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Reminders';

CREATE TABLE `sleep_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `user_id` bigint NOT NULL COMMENT 'User id',
  `start_time` datetime NOT NULL COMMENT 'Sleep start time',
  `end_time` datetime NOT NULL COMMENT 'Sleep end time',
  `deep_sleep_minutes` int DEFAULT NULL COMMENT 'Deep sleep minutes',
  `light_sleep_minutes` int DEFAULT NULL COMMENT 'Light sleep minutes',
  `quality` varchar(16) DEFAULT NULL COMMENT 'Sleep quality',
  `routine` varchar(32) DEFAULT NULL COMMENT 'Routine label',
  `record_date` date NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_sleep_record_user_date` (`user_id`,`record_date`)
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Sleep records';

CREATE TABLE `sms_code` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `phone` varchar(20) NOT NULL COMMENT 'Phone',
  `code` varchar(8) NOT NULL COMMENT 'SMS code',
  `created_at` datetime NOT NULL COMMENT 'Created time',
  `expires_at` datetime NOT NULL COMMENT 'Expires time',
  `used` tinyint NOT NULL DEFAULT '0' COMMENT 'Used flag',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='SMS codes';

CREATE TABLE `subscribe_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `user_id` bigint NOT NULL COMMENT 'User id',
  `openid` varchar(64) NOT NULL COMMENT 'WeChat openid',
  `template_id` varchar(128) NOT NULL COMMENT 'Template id',
  `page` varchar(128) DEFAULT NULL COMMENT 'Target page',
  `data_json` text COMMENT 'Template data json',
  `send_time` datetime NOT NULL COMMENT 'Send time',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT 'Status (0=待发送,1=已发送,2=失败)',
  `response` text COMMENT 'Send response',
  `created_at` datetime NOT NULL COMMENT 'Created time',
  `sent_at` datetime DEFAULT NULL COMMENT 'Sent time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Subscribe message tasks';

CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `username` varchar(32) DEFAULT NULL COMMENT 'Login username',
  `password` varchar(128) DEFAULT NULL COMMENT 'Password hash',
  `phone` varchar(20) DEFAULT NULL COMMENT 'Phone number',
  `wx_openid` varchar(64) DEFAULT NULL COMMENT 'WeChat openid',
  `wx_unionid` varchar(64) DEFAULT NULL COMMENT 'WeChat unionid',
  `wx_phone` varchar(20) DEFAULT NULL COMMENT 'WeChat bound phone',
  `wx_nickname` varchar(64) DEFAULT NULL COMMENT 'WeChat nickname',
  `wx_avatar` varchar(255) DEFAULT NULL COMMENT 'WeChat avatar url',
  `sex` varchar(8) DEFAULT NULL COMMENT 'Gender',
  `age` int DEFAULT NULL COMMENT 'Age',
  `height` int DEFAULT NULL COMMENT 'Height (cm)',
  `weight` double DEFAULT NULL COMMENT 'Weight (kg)',
  `systolic` int DEFAULT NULL COMMENT 'Systolic blood pressure',
  `diastolic` int DEFAULT NULL COMMENT 'Diastolic blood pressure',
  `heart_rate` int DEFAULT NULL COMMENT 'Heart rate',
  `created_at` datetime DEFAULT NULL COMMENT 'Created time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `phone` (`phone`),
  UNIQUE KEY `wx_openid` (`wx_openid`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='User profile';

CREATE TABLE `water_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `ml` int NOT NULL COMMENT '摄入毫升数',
  `drink_time` datetime NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='饮水记录';

CREATE TABLE `we_run_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `user_id` bigint NOT NULL COMMENT 'User id',
  `record_date` date NOT NULL COMMENT 'Date',
  `steps` int NOT NULL COMMENT 'Steps',
  `created_at` datetime NOT NULL COMMENT 'Created time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`,`record_date`)
) ENGINE=InnoDB AUTO_INCREMENT=74 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='WeRun steps';

CREATE TABLE `weight_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `user_id` bigint NOT NULL COMMENT 'User id',
  `weight` double NOT NULL COMMENT 'Weight (kg)',
  `bmi` double DEFAULT NULL COMMENT 'BMI',
  `date` date NOT NULL COMMENT 'Record date',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Weight records';