# 智康AI 意图识别模板（带 userId）

以下内容用于元器意图识别配置，**必须只返回 JSON**，不输出解释或多余文字。  
所有返回 JSON 必须包含 `userId`、`intent`、`payload`。

---

## 1. 提醒（reminder）
**意图描述：** 用户需要设置未来的健康相关提醒，如吃药、运动、睡眠、测量等。  
**意图示例：** 提醒我明天早上7点吃药；晚上9点提醒我睡觉；周一早上8点提醒运动

**输出格式（仅 JSON）：**
```json
{
  "userId": 1,
  "intent": "reminder",
  "payload": {
    "title": "吃药提醒",
    "type": 4,
    "content": "吃药",
    "remind_time": "2026-03-16 07:00:00",
    "status": 0
  }
}
```

---

## 2. 用药新增（medication）
**意图描述：** 用户新增或调整长期用药计划。  
**意图示例：** 我每天饭后吃氨氯地平 5mg；新增用药：阿司匹林100mg，早晚各一次；每天早上8点吃维生素D 1粒

**输出格式（仅 JSON）：**
```json
{
  "userId": 1,
  "intent": "medication",
  "payload": {
    "drug_name": "氨氯地平",
    "dosage": "5mg",
    "frequency": "每天饭后",
    "remind_time": "08:00",
    "start_date": "2026-03-16",
    "end_date": null,
    "notes": null
  }
}
```

---

## 3. 用药打卡（medication_record）
**意图描述：** 用户记录单次用药行为。  
**意图示例：** 我刚吃了维生素D；今天早上吃了降压药；中午忘记吃药了

**输出格式（仅 JSON）：**
```json
{
  "userId": 1,
  "intent": "medication_record",
  "payload": {
    "medication_id": null,
    "date": "2026-03-16",
    "time": "08:00:00",
    "status": 1
  }
}
```

---

## 4. 运动记录（exercise_record）
**意图描述：** 用户记录已完成的运动行为。  
**意图示例：** 今天走了6000步；跑步30分钟，消耗200卡；骑行45分钟

**输出格式（仅 JSON）：**
```json
{
  "userId": 1,
  "intent": "exercise_record",
  "payload": {
    "type": "步行",
    "steps": 6000,
    "duration": null,
    "calories": null,
    "date": "2026-03-16"
  }
}
```

---

## 5. 饮食记录（diet_record）
**意图描述：** 用户记录已完成的饮食行为。  
**意图示例：** 早餐吃了牛奶和面包；午饭米饭青菜，大概650卡；晚餐吃了鱼和蔬菜，少油

**输出格式（仅 JSON）：**
```json
{
  "userId": 1,
  "intent": "diet_record",
  "payload": {
    "meal_type": "晚餐",
    "food_name": "鱼、蔬菜",
    "calories": null,
    "note": "少油",
    "date": "2026-03-16"
  }
}
```

---

## 6. 睡眠记录（sleep_record）
**意图描述：** 用户记录睡眠行为。  
**意图示例：** 昨晚11点睡，早上6点起；我睡了7小时，深睡2小时；今天睡眠很浅

**输出格式（仅 JSON）：**
```json
{
  "userId": 1,
  "intent": "sleep_record",
  "payload": {
    "start_time": "2026-03-15 23:00:00",
    "end_time": "2026-03-16 06:00:00",
    "deep_sleep_minutes": 120,
    "light_sleep_minutes": null,
    "quality": "浅"
  }
}
```

---

## 7. 体重记录（weight_record）
**意图描述：** 用户记录体重数据。  
**意图示例：** 体重62.5；今天体重63公斤；体重61

**输出格式（仅 JSON）：**
```json
{
  "userId": 1,
  "intent": "weight_record",
  "payload": {
    "weight": 63,
    "bmi": null,
    "date": "2026-03-16"
  }
}
```

---

## 8. 血压/心率（health_record）
**意图描述：** 用户记录血压或心率数据。  
**意图示例：** 今天血压135/88，心率76；血压120/80；心率70

**输出格式（仅 JSON）：**
```json
{
  "userId": 1,
  "intent": "health_record",
  "payload": {
    "systolic": 135,
    "diastolic": 88,
    "heart_rate": 76,
    "date": "2026-03-16"
  }
}
```

---

## 9. 目标设置（goal）
**意图描述：** 用户设置健康目标。  
**意图示例：** 我想每天走一万步；一周睡眠目标7小时；目标是每天2000卡以内

**输出格式（仅 JSON）：**
```json
{
  "userId": 1,
  "intent": "goal",
  "payload": {
    "goal_type": 1,
    "target_value": 10000,
    "current_value": 0,
    "period": "day"
  }
}
```

---

## 10. 经期记录（period_record）
**意图描述：** 用户记录经期相关数据。  
**意图示例：** 今天来月经，经量中等；经期从3月1号到3月6号；最近经量偏多，腹痛明显

**输出格式（仅 JSON）：**
```json
{
  "userId": 1,
  "intent": "period_record",
  "payload": {
    "start_date": "2026-03-16",
    "end_date": "2026-03-16",
    "flow": 2,
    "note": null
  }
}
```

---

## 11. 添加家人（family_member）
**意图描述：** 用户添加家人健康档案。  
**意图示例：** 添加我爸，65岁，高血压；新增家人：妈妈，60岁；添加紧急联系人张三

**输出格式（仅 JSON）：**
```json
{
  "userId": 1,
  "intent": "family_member",
  "payload": {
    "name": "爸爸",
    "relation": "父亲",
    "age": 65,
    "condition_text": "高血压",
    "role": null,
    "status": 1
  }
}
```

