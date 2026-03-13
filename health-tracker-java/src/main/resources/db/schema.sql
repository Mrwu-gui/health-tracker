CREATE TABLE IF NOT EXISTS `user` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(32) UNIQUE,
  password VARCHAR(128),
  phone VARCHAR(20) UNIQUE,
  wx_openid VARCHAR(64) UNIQUE,
  wx_unionid VARCHAR(64),
  wx_phone VARCHAR(20),
  wx_nickname VARCHAR(64),
  wx_avatar VARCHAR(255),
  sex VARCHAR(8),
  age INT,
  height INT,
  weight DOUBLE,
  created_at DATETIME
);

CREATE TABLE IF NOT EXISTS exercise_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  type VARCHAR(32) NOT NULL,
  duration INT NOT NULL,
  calories INT NOT NULL,
  date DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS diet_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  meal_type VARCHAR(32) NOT NULL,
  food_name VARCHAR(64) NOT NULL,
  calories INT NOT NULL,
  protein DOUBLE,
  carbs DOUBLE,
  fat DOUBLE,
  date DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS sleep_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  start_time DATETIME NOT NULL,
  end_time DATETIME NOT NULL,
  deep_sleep_minutes INT,
  light_sleep_minutes INT,
  quality VARCHAR(16),
  routine VARCHAR(32)
);

CREATE TABLE IF NOT EXISTS weight_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  weight DOUBLE NOT NULL,
  bmi DOUBLE,
  date DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS health_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  systolic INT,
  diastolic INT,
  heart_rate INT,
  date DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS goal (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  goal_type VARCHAR(32) NOT NULL,
  target_value INT NOT NULL,
  current_value INT NOT NULL,
  period VARCHAR(16) NOT NULL
);

CREATE TABLE IF NOT EXISTS medication (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  drug_name VARCHAR(64) NOT NULL,
  dosage VARCHAR(32) NOT NULL,
  frequency VARCHAR(32) NOT NULL,
  remind_time VARCHAR(16),
  stock INT,
  stock_threshold INT,
  start_date DATE NOT NULL,
  end_date DATE,
  notes VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS medication_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  medication_id BIGINT NOT NULL,
  date DATE NOT NULL,
  time TIME NOT NULL,
  status VARCHAR(16) NOT NULL
);

CREATE TABLE IF NOT EXISTS sms_code (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  phone VARCHAR(20) NOT NULL,
  code VARCHAR(8) NOT NULL,
  created_at DATETIME NOT NULL,
  expires_at DATETIME NOT NULL,
  used TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS reminder (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  title VARCHAR(64) NOT NULL,
  type VARCHAR(32) NOT NULL,
  content VARCHAR(255),
  remind_time DATETIME,
  status VARCHAR(16),
  created_at DATETIME
);

CREATE TABLE IF NOT EXISTS we_run_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  record_date DATE NOT NULL,
  steps INT NOT NULL,
  created_at DATETIME NOT NULL,
  UNIQUE KEY uk_user_date (user_id, record_date)
);
