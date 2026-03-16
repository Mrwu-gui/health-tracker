INSERT INTO `user` (username, password, sex, age, height, weight, created_at)
VALUES
  ('demo', '2a97516c354b68848cdbd8f54a226a0a55b21ed138e207ad6c5cbb9c00aa5aea', '男', 30, 175, 70.5, NOW());

INSERT INTO exercise_record (user_id, type, steps, duration, calories, date)
VALUES
  (1, '跑步', 6800, 30, 260, CURDATE());

INSERT INTO diet_record (user_id, meal_type, food_name, calories, protein, carbs, fat, note, date)
VALUES
  (1, '午餐', '鸡胸沙拉', 420, 35, 42, 18, '少油少盐', CURDATE());

INSERT INTO sleep_record (user_id, start_time, end_time, deep_sleep_minutes, light_sleep_minutes, quality, routine)
VALUES
  (1, DATE_SUB(NOW(), INTERVAL 8 HOUR), NOW(), 120, 280, '良', '23:30-07:05');

INSERT INTO weight_record (user_id, weight, bmi, date)
VALUES
  (1, 70.5, 23.0, CURDATE());

INSERT INTO health_record (user_id, systolic, diastolic, heart_rate, date)
VALUES
  (1, 118, 76, 68, CURDATE());

INSERT INTO goal (user_id, goal_type, target_value, current_value, period)
VALUES
  (1, 1, 10000, 8420, '每日');

INSERT INTO medication (user_id, drug_name, dosage, frequency, start_date, end_date, notes)
VALUES
  (1, '维生素D', '1 粒', '每日', CURDATE(), NULL, '早餐后');

INSERT INTO we_run_record (user_id, record_date, steps, created_at)
VALUES
  (1, CURDATE(), 8420, NOW());

INSERT INTO reminder (user_id, title, type, content, remind_time, status, created_at)
VALUES
  (1, '晚间服药', 4, '维生素D 1粒', DATE_ADD(NOW(), INTERVAL 2 HOUR), 0, NOW());

INSERT INTO privacy_settings (user_id, allow_subscribe, allow_cloud_sync, allow_family_share, created_at, updated_at)
VALUES
  (1, 1, 1, 1, NOW(), NOW());

INSERT INTO family_member (user_id, name, relation, age, condition_text, role, status, created_at)
VALUES
  (1, '李阿姨', '家人', 68, '高血压', '成员', 1, NOW()),
  (1, '张先生', '儿子', 35, '健康', '管理员', 1, NOW());
