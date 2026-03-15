INSERT INTO `user` (username, password, sex, age, height, weight, created_at)
VALUES
  ('demo', '2a97516c354b68848cdbd8f54a226a0a55b21ed138e207ad6c5cbb9c00aa5aea', '男', 30, 175, 70.5, TIMESTAMP '2026-03-14 08:00:00');

INSERT INTO exercise_record (user_id, type, steps, duration, calories, date)
VALUES
  (1, '跑步', 6800, 30, 260, DATE '2026-03-14');

INSERT INTO diet_record (user_id, meal_type, food_name, calories, protein, carbs, fat, note, date)
VALUES
  (1, '午餐', '鸡胸沙拉', 420, 35, 42, 18, '少油少盐', DATE '2026-03-14');

INSERT INTO sleep_record (user_id, start_time, end_time, deep_sleep_minutes, light_sleep_minutes, quality, routine)
VALUES
  (1, TIMESTAMP '2026-03-13 23:30:00', TIMESTAMP '2026-03-14 07:05:00', 120, 280, '良', '23:30-07:05');

INSERT INTO weight_record (user_id, weight, bmi, date)
VALUES
  (1, 70.5, 23.0, DATE '2026-03-14');

INSERT INTO health_record (user_id, systolic, diastolic, heart_rate, date)
VALUES
  (1, 118, 76, 68, DATE '2026-03-14');

INSERT INTO goal (user_id, goal_type, target_value, current_value, period)
VALUES
  (1, 1, 10000, 8420, '每日');

INSERT INTO medication (user_id, drug_name, dosage, frequency, start_date, end_date, notes)
VALUES
  (1, '维生素D', '1 粒', '每日', DATE '2026-03-14', NULL, '早餐后');

INSERT INTO we_run_record (user_id, record_date, steps, created_at)
VALUES
  (1, DATE '2026-03-14', 8420, TIMESTAMP '2026-03-14 08:00:00');

INSERT INTO reminder (user_id, title, type, content, remind_time, status, created_at)
VALUES
  (1, '晚间服药', 4, '维生素D 1粒', TIMESTAMP '2026-03-14 20:00:00', '待提醒', TIMESTAMP '2026-03-14 08:00:00');

INSERT INTO privacy_settings (user_id, allow_subscribe, allow_cloud_sync, allow_family_share, created_at, updated_at)
VALUES
  (1, 1, 1, 1, TIMESTAMP '2026-03-14 08:00:00', TIMESTAMP '2026-03-14 08:00:00');

INSERT INTO family_member (user_id, name, relation, age, condition_text, role, status, created_at)
VALUES
  (1, '李阿姨', '家人', 68, '高血压', '成员', '已授权', TIMESTAMP '2026-03-14 08:00:00'),
  (1, '张先生', '儿子', 35, '健康', '管理员', '已授权', TIMESTAMP '2026-03-14 08:00:00');
