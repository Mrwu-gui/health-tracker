CREATE TABLE IF NOT EXISTS `user` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
  username VARCHAR(32) UNIQUE COMMENT 'Login username',
  password VARCHAR(128) COMMENT 'Password hash',
  phone VARCHAR(20) UNIQUE COMMENT 'Phone number',
  wx_openid VARCHAR(64) UNIQUE COMMENT 'WeChat openid',
  wx_unionid VARCHAR(64) COMMENT 'WeChat unionid',
  wx_phone VARCHAR(20) COMMENT 'WeChat bound phone',
  wx_nickname VARCHAR(64) COMMENT 'WeChat nickname',
  wx_avatar VARCHAR(255) COMMENT 'WeChat avatar url',
  sex VARCHAR(8) COMMENT 'Gender',
  age INT COMMENT 'Age',
  height INT COMMENT 'Height (cm)',
  weight DOUBLE COMMENT 'Weight (kg)',
  systolic INT COMMENT 'Systolic blood pressure',
  diastolic INT COMMENT 'Diastolic blood pressure',
  heart_rate INT COMMENT 'Heart rate',
  created_at DATETIME COMMENT 'Created time'
) COMMENT='User profile';

CREATE TABLE IF NOT EXISTS privacy_settings (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
  user_id BIGINT NOT NULL UNIQUE COMMENT 'User id',
  allow_subscribe TINYINT NOT NULL DEFAULT 1 COMMENT 'Allow subscribe messages',
  allow_cloud_sync TINYINT NOT NULL DEFAULT 1 COMMENT 'Allow cloud sync',
  allow_family_share TINYINT NOT NULL DEFAULT 1 COMMENT 'Allow family share',
  created_at DATETIME COMMENT 'Created time',
  updated_at DATETIME COMMENT 'Updated time'
) COMMENT='Privacy settings';

CREATE TABLE IF NOT EXISTS family_member (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
  user_id BIGINT NOT NULL COMMENT 'Owner user id',
  name VARCHAR(64) NOT NULL COMMENT 'Name',
  relation VARCHAR(32) COMMENT 'Relation',
  age INT COMMENT 'Age',
  condition_text VARCHAR(64) COMMENT 'Condition/notes',
  role VARCHAR(16) COMMENT 'Role label',
  status TINYINT NOT NULL DEFAULT 1 COMMENT 'Status (0=未授权,1=已授权)',
  avatar VARCHAR(255) COMMENT 'Avatar url',
  created_at DATETIME COMMENT 'Created time'
) COMMENT='Family members';

CREATE TABLE IF NOT EXISTS exercise_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
  user_id BIGINT NOT NULL COMMENT 'User id',
  type VARCHAR(32) NOT NULL COMMENT 'Exercise type',
  steps INT COMMENT 'Steps',
  duration INT NOT NULL COMMENT 'Duration (minutes)',
  calories INT NOT NULL COMMENT 'Calories',
  date DATE NOT NULL COMMENT 'Record date'
) COMMENT='Exercise records';

CREATE TABLE IF NOT EXISTS diet_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
  user_id BIGINT NOT NULL COMMENT 'User id',
  meal_type VARCHAR(32) NOT NULL COMMENT 'Meal type',
  food_name VARCHAR(64) NOT NULL COMMENT 'Food name',
  calories INT NOT NULL COMMENT 'Calories',
  protein DOUBLE COMMENT 'Protein (g)',
  carbs DOUBLE COMMENT 'Carbs (g)',
  fat DOUBLE COMMENT 'Fat (g)',
  note VARCHAR(255) COMMENT 'Notes',
  date DATE NOT NULL COMMENT 'Record date'
) COMMENT='Diet records';

CREATE TABLE IF NOT EXISTS sleep_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
  user_id BIGINT NOT NULL COMMENT 'User id',
  start_time DATETIME NOT NULL COMMENT 'Sleep start time',
  end_time DATETIME NOT NULL COMMENT 'Sleep end time',
  deep_sleep_minutes INT COMMENT 'Deep sleep minutes',
  light_sleep_minutes INT COMMENT 'Light sleep minutes',
  quality VARCHAR(16) COMMENT 'Sleep quality',
  routine VARCHAR(32) COMMENT 'Routine label'
) COMMENT='Sleep records';

CREATE TABLE IF NOT EXISTS weight_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
  user_id BIGINT NOT NULL COMMENT 'User id',
  weight DOUBLE NOT NULL COMMENT 'Weight (kg)',
  bmi DOUBLE COMMENT 'BMI',
  date DATE NOT NULL COMMENT 'Record date'
) COMMENT='Weight records';

CREATE TABLE IF NOT EXISTS period_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
  user_id BIGINT NOT NULL COMMENT 'User id',
  start_date DATE NOT NULL COMMENT 'Period start date',
  end_date DATE NOT NULL COMMENT 'Period end date',
  flow INT COMMENT 'Flow level (int)',
  note VARCHAR(255) COMMENT 'Notes',
  created_at DATETIME COMMENT 'Created time'
) COMMENT='Period records';

CREATE TABLE IF NOT EXISTS health_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
  user_id BIGINT NOT NULL COMMENT 'User id',
  systolic INT COMMENT 'Systolic blood pressure',
  diastolic INT COMMENT 'Diastolic blood pressure',
  heart_rate INT COMMENT 'Heart rate',
  date DATE NOT NULL COMMENT 'Record date'
) COMMENT='Blood pressure/heart rate records';

CREATE TABLE IF NOT EXISTS goal (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
  user_id BIGINT NOT NULL COMMENT 'User id',
  goal_type INT NOT NULL COMMENT 'Goal type (int)',
  target_value INT NOT NULL COMMENT 'Target value',
  current_value INT NOT NULL COMMENT 'Current value',
  period VARCHAR(16) NOT NULL COMMENT 'Period (day/week)'
) COMMENT='Goals';

CREATE TABLE IF NOT EXISTS medication (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
  user_id BIGINT NOT NULL COMMENT 'User id',
  drug_name VARCHAR(64) NOT NULL COMMENT 'Drug name',
  dosage VARCHAR(32) NOT NULL COMMENT 'Dosage',
  frequency VARCHAR(32) NOT NULL COMMENT 'Frequency',
  remind_time VARCHAR(16) COMMENT 'Remind time (yyyy-MM-dd HH:mm)',
  stock INT COMMENT 'Stock',
  stock_threshold INT COMMENT 'Low stock threshold',
  start_date DATE NOT NULL COMMENT 'Start date',
  end_date DATE COMMENT 'End date',
  notes VARCHAR(255) COMMENT 'Notes'
) COMMENT='Medication list';

CREATE TABLE IF NOT EXISTS medication_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
  user_id BIGINT NOT NULL COMMENT 'User id',
  medication_id BIGINT NOT NULL COMMENT 'Medication id',
  date DATE NOT NULL COMMENT 'Date',
  time TIME NOT NULL COMMENT 'Time',
  status TINYINT NOT NULL COMMENT 'Status (0=未服,1=已服,2=漏服)'
) COMMENT='Medication intake records';

CREATE TABLE IF NOT EXISTS sms_code (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
  phone VARCHAR(20) NOT NULL COMMENT 'Phone',
  code VARCHAR(8) NOT NULL COMMENT 'SMS code',
  created_at DATETIME NOT NULL COMMENT 'Created time',
  expires_at DATETIME NOT NULL COMMENT 'Expires time',
  used TINYINT NOT NULL DEFAULT 0 COMMENT 'Used flag'
) COMMENT='SMS codes';

CREATE TABLE IF NOT EXISTS reminder (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
  user_id BIGINT NOT NULL COMMENT 'User id',
  title VARCHAR(64) NOT NULL COMMENT 'Title',
  type INT NOT NULL COMMENT 'Type (1=运动,2=饮食,3=睡眠,4=用药)',
  content VARCHAR(255) COMMENT 'Content',
  remind_time DATETIME COMMENT 'Remind time',
  status TINYINT NOT NULL DEFAULT 0 COMMENT 'Status (0=未提醒,1=已提醒)',
  created_at DATETIME COMMENT 'Created time'
) COMMENT='Reminders';

CREATE TABLE IF NOT EXISTS we_run_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
  user_id BIGINT NOT NULL COMMENT 'User id',
  record_date DATE NOT NULL COMMENT 'Date',
  steps INT NOT NULL COMMENT 'Steps',
  created_at DATETIME NOT NULL COMMENT 'Created time',
  UNIQUE KEY uk_user_date (user_id, record_date)
) COMMENT='WeRun steps';

CREATE TABLE IF NOT EXISTS ai_chat_message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
  user_id BIGINT NOT NULL COMMENT 'User id',
  role VARCHAR(16) NOT NULL COMMENT 'Role (user/assistant)',
  content_text TEXT COMMENT 'Plain text',
  content_json TEXT COMMENT 'Raw content json',
  created_at DATETIME NOT NULL COMMENT 'Created time'
) COMMENT='AI chat messages';
