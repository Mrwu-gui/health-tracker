-- Run this in MySQL to add/update table and column comments on existing tables.

ALTER TABLE `user`
  MODIFY id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
  MODIFY username VARCHAR(32) UNIQUE COMMENT 'Login username',
  MODIFY password VARCHAR(128) COMMENT 'Password hash',
  MODIFY phone VARCHAR(20) UNIQUE COMMENT 'Phone number',
  MODIFY wx_openid VARCHAR(64) UNIQUE COMMENT 'WeChat openid',
  MODIFY wx_unionid VARCHAR(64) COMMENT 'WeChat unionid',
  MODIFY wx_phone VARCHAR(20) COMMENT 'WeChat bound phone',
  MODIFY wx_nickname VARCHAR(64) COMMENT 'WeChat nickname',
  MODIFY wx_avatar VARCHAR(255) COMMENT 'WeChat avatar url',
  MODIFY sex VARCHAR(8) COMMENT 'Gender',
  MODIFY age INT COMMENT 'Age',
  MODIFY height INT COMMENT 'Height (cm)',
  MODIFY weight DOUBLE COMMENT 'Weight (kg)',
  MODIFY systolic INT COMMENT 'Systolic blood pressure',
  MODIFY diastolic INT COMMENT 'Diastolic blood pressure',
  MODIFY heart_rate INT COMMENT 'Heart rate',
  MODIFY created_at DATETIME COMMENT 'Created time',
  COMMENT='User profile';

ALTER TABLE privacy_settings
  MODIFY id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
  MODIFY user_id BIGINT NOT NULL UNIQUE COMMENT 'User id',
  MODIFY allow_subscribe TINYINT NOT NULL DEFAULT 1 COMMENT 'Allow subscribe messages',
  MODIFY allow_cloud_sync TINYINT NOT NULL DEFAULT 1 COMMENT 'Allow cloud sync',
  MODIFY allow_family_share TINYINT NOT NULL DEFAULT 1 COMMENT 'Allow family share',
  MODIFY created_at DATETIME COMMENT 'Created time',
  MODIFY updated_at DATETIME COMMENT 'Updated time',
  COMMENT='Privacy settings';

ALTER TABLE family_member
  MODIFY id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
  MODIFY user_id BIGINT NOT NULL COMMENT 'Owner user id',
  MODIFY name VARCHAR(64) NOT NULL COMMENT 'Name',
  MODIFY relation VARCHAR(32) COMMENT 'Relation',
  MODIFY age INT COMMENT 'Age',
  MODIFY condition_text VARCHAR(64) COMMENT 'Condition/notes',
  MODIFY role VARCHAR(16) COMMENT 'Role label',
  MODIFY status TINYINT NOT NULL DEFAULT 1 COMMENT 'Status (0=未授权,1=已授权)',
  MODIFY avatar VARCHAR(255) COMMENT 'Avatar url',
  MODIFY created_at DATETIME COMMENT 'Created time',
  COMMENT='Family members';

ALTER TABLE exercise_record
  MODIFY id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
  MODIFY user_id BIGINT NOT NULL COMMENT 'User id',
  MODIFY type VARCHAR(32) NOT NULL COMMENT 'Exercise type',
  MODIFY steps INT COMMENT 'Steps',
  MODIFY duration INT NOT NULL COMMENT 'Duration (minutes)',
  MODIFY calories INT NOT NULL COMMENT 'Calories',
  MODIFY date DATE NOT NULL COMMENT 'Record date',
  COMMENT='Exercise records';

ALTER TABLE diet_record
  MODIFY id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
  MODIFY user_id BIGINT NOT NULL COMMENT 'User id',
  MODIFY meal_type VARCHAR(32) NOT NULL COMMENT 'Meal type',
  MODIFY food_name VARCHAR(64) NOT NULL COMMENT 'Food name',
  MODIFY calories INT NOT NULL COMMENT 'Calories',
  MODIFY protein DOUBLE COMMENT 'Protein (g)',
  MODIFY carbs DOUBLE COMMENT 'Carbs (g)',
  MODIFY fat DOUBLE COMMENT 'Fat (g)',
  MODIFY note VARCHAR(255) COMMENT 'Notes',
  MODIFY date DATE NOT NULL COMMENT 'Record date',
  COMMENT='Diet records';

ALTER TABLE sleep_record
  MODIFY id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
  MODIFY user_id BIGINT NOT NULL COMMENT 'User id',
  MODIFY start_time DATETIME NOT NULL COMMENT 'Sleep start time',
  MODIFY end_time DATETIME NOT NULL COMMENT 'Sleep end time',
  MODIFY deep_sleep_minutes INT COMMENT 'Deep sleep minutes',
  MODIFY light_sleep_minutes INT COMMENT 'Light sleep minutes',
  MODIFY quality VARCHAR(16) COMMENT 'Sleep quality',
  MODIFY routine VARCHAR(32) COMMENT 'Routine label',
  COMMENT='Sleep records';

ALTER TABLE weight_record
  MODIFY id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
  MODIFY user_id BIGINT NOT NULL COMMENT 'User id',
  MODIFY weight DOUBLE NOT NULL COMMENT 'Weight (kg)',
  MODIFY bmi DOUBLE COMMENT 'BMI',
  MODIFY date DATE NOT NULL COMMENT 'Record date',
  COMMENT='Weight records';

ALTER TABLE period_record
  MODIFY id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
  MODIFY user_id BIGINT NOT NULL COMMENT 'User id',
  MODIFY start_date DATE NOT NULL COMMENT 'Period start date',
  MODIFY end_date DATE NOT NULL COMMENT 'Period end date',
  MODIFY flow INT COMMENT 'Flow level (int)',
  MODIFY note VARCHAR(255) COMMENT 'Notes',
  MODIFY created_at DATETIME COMMENT 'Created time',
  COMMENT='Period records';

ALTER TABLE health_record
  MODIFY id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
  MODIFY user_id BIGINT NOT NULL COMMENT 'User id',
  MODIFY systolic INT COMMENT 'Systolic blood pressure',
  MODIFY diastolic INT COMMENT 'Diastolic blood pressure',
  MODIFY heart_rate INT COMMENT 'Heart rate',
  MODIFY date DATE NOT NULL COMMENT 'Record date',
  COMMENT='Blood pressure/heart rate records';

ALTER TABLE goal
  MODIFY id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
  MODIFY user_id BIGINT NOT NULL COMMENT 'User id',
  MODIFY goal_type INT NOT NULL COMMENT 'Goal type (int)',
  MODIFY target_value INT NOT NULL COMMENT 'Target value',
  MODIFY current_value INT NOT NULL COMMENT 'Current value',
  MODIFY period VARCHAR(16) NOT NULL COMMENT 'Period (day/week)',
  COMMENT='Goals';

ALTER TABLE medication
  MODIFY id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
  MODIFY user_id BIGINT NOT NULL COMMENT 'User id',
  MODIFY drug_name VARCHAR(64) NOT NULL COMMENT 'Drug name',
  MODIFY dosage VARCHAR(32) NOT NULL COMMENT 'Dosage',
  MODIFY frequency VARCHAR(32) NOT NULL COMMENT 'Frequency',
  MODIFY remind_time VARCHAR(16) COMMENT 'Remind time (yyyy-MM-dd HH:mm)',
  MODIFY stock INT COMMENT 'Stock',
  MODIFY stock_threshold INT COMMENT 'Low stock threshold',
  MODIFY start_date DATE NOT NULL COMMENT 'Start date',
  MODIFY end_date DATE COMMENT 'End date',
  MODIFY notes VARCHAR(255) COMMENT 'Notes',
  COMMENT='Medication list';

ALTER TABLE medication_record
  MODIFY id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
  MODIFY user_id BIGINT NOT NULL COMMENT 'User id',
  MODIFY medication_id BIGINT NOT NULL COMMENT 'Medication id',
  MODIFY date DATE NOT NULL COMMENT 'Date',
  MODIFY time TIME NOT NULL COMMENT 'Time',
  MODIFY status TINYINT NOT NULL COMMENT 'Status (0=未服,1=已服,2=漏服)',
  COMMENT='Medication intake records';

ALTER TABLE sms_code
  MODIFY id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
  MODIFY phone VARCHAR(20) NOT NULL COMMENT 'Phone',
  MODIFY code VARCHAR(8) NOT NULL COMMENT 'SMS code',
  MODIFY created_at DATETIME NOT NULL COMMENT 'Created time',
  MODIFY expires_at DATETIME NOT NULL COMMENT 'Expires time',
  MODIFY used TINYINT NOT NULL DEFAULT 0 COMMENT 'Used flag',
  COMMENT='SMS codes';

ALTER TABLE reminder
  MODIFY id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
  MODIFY user_id BIGINT NOT NULL COMMENT 'User id',
  MODIFY title VARCHAR(64) NOT NULL COMMENT 'Title',
  MODIFY type INT NOT NULL COMMENT 'Type (1=运动,2=饮食,3=睡眠,4=用药)',
  MODIFY content VARCHAR(255) COMMENT 'Content',
  MODIFY remind_time DATETIME COMMENT 'Remind time',
  MODIFY status TINYINT NOT NULL DEFAULT 0 COMMENT 'Status (0=未提醒,1=已提醒)',
  MODIFY created_at DATETIME COMMENT 'Created time',
  COMMENT='Reminders';

ALTER TABLE we_run_record
  MODIFY id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
  MODIFY user_id BIGINT NOT NULL COMMENT 'User id',
  MODIFY record_date DATE NOT NULL COMMENT 'Date',
  MODIFY steps INT NOT NULL COMMENT 'Steps',
  MODIFY created_at DATETIME NOT NULL COMMENT 'Created time',
  COMMENT='WeRun steps';

ALTER TABLE subscribe_task
  MODIFY id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
  MODIFY user_id BIGINT NOT NULL COMMENT 'User id',
  MODIFY openid VARCHAR(64) NOT NULL COMMENT 'WeChat openid',
  MODIFY template_id VARCHAR(128) NOT NULL COMMENT 'Template id',
  MODIFY page VARCHAR(128) COMMENT 'Target page',
  MODIFY data_json TEXT COMMENT 'Template data json',
  MODIFY send_time DATETIME NOT NULL COMMENT 'Send time',
  MODIFY status TINYINT NOT NULL DEFAULT 0 COMMENT 'Status (0=待发送,1=已发送,2=失败)',
  MODIFY response TEXT COMMENT 'Send response',
  MODIFY created_at DATETIME NOT NULL COMMENT 'Created time',
  MODIFY sent_at DATETIME COMMENT 'Sent time',
  COMMENT='Subscribe message tasks';

ALTER TABLE ai_chat_message
  MODIFY id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
  MODIFY user_id BIGINT NOT NULL COMMENT 'User id',
  MODIFY role VARCHAR(16) NOT NULL COMMENT 'Role (user/assistant)',
  MODIFY content_text TEXT COMMENT 'Plain text',
  MODIFY content_json TEXT COMMENT 'Raw content json',
  MODIFY created_at DATETIME NOT NULL COMMENT 'Created time',
  COMMENT='AI chat messages';
