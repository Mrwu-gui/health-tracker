// Mock 数据用于开发演示
export const mockData = {
  // 今日健康评分
  todayScore: {
    score: 85,
    level: "良好",
    factors: [
      { name: "运动", value: 90, status: "good" },
      { name: "睡眠", value: 78, status: "normal" },
      { name: "饮食", value: 88, status: "good" },
      { name: "体重", value: 82, status: "good" }
    ]
  },

  // 统计数据概览
  overview: {
    steps: 8432,
    sleep: "7小时30分",
    weight: "68.5kg",
    calories: 1850,
    stepsTarget: 10000,
    sleepTarget: 8,
    stepsPercent: 84,
    sleepPercent: 94
  },

  // AI 解读
  aiAnalysis: "今日健康状态整体良好。步数达到目标的84%，建议继续保持每日的运动习惯。睡眠质量不错，深睡比例适中。饮食摄入热量在合理范围内。",

  // 趋势数据
  trend: {
    steps: [
      { date: "03-12", value: 6500 },
      { date: "03-13", value: 7200 },
      { date: "03-14", value: 8100 },
      { date: "03-15", value: 7800 },
      { date: "03-16", value: 8900 },
      { date: "03-17", value: 9200 },
      { date: "03-18", value: 8432 }
    ],
    sleep: [
      { date: "03-12", value: 6.5 },
      { date: "03-13", value: 7.0 },
      { date: "03-14", value: 7.2 },
      { date: "03-15", value: 6.8 },
      { date: "03-16", value: 7.5 },
      { date: "03-17", value: 8.0 },
      { date: "03-18", value: 7.5 }
    ],
    diet: [
      { date: "03-12", breakfast: 450, lunch: 620, dinner: 380, snack: 150 },
      { date: "03-13", breakfast: 420, lunch: 650, dinner: 400, snack: 180 },
      { date: "03-14", breakfast: 480, lunch: 580, dinner: 350, snack: 200 },
      { date: "03-15", breakfast: 400, lunch: 700, dinner: 420, snack: 100 },
      { date: "03-16", breakfast: 450, lunch: 620, dinner: 380, snack: 150 },
      { date: "03-17", breakfast: 500, lunch: 580, dinner: 400, snack: 180 },
      { date: "03-18", breakfast: 450, lunch: 620, dinner: 380, snack: 200 }
    ],
    weight: [
      { date: "03-12", value: 69.0 },
      { date: "03-13", value: 68.8 },
      { date: "03-14", value: 68.5 },
      { date: "03-15", value: 68.7 },
      { date: "03-16", value: 68.4 },
      { date: "03-17", value: 68.6 },
      { date: "03-18", value: 68.5 }
    ],
    menstrual: [
      { date: "03-01", value: 1 },
      { date: "03-02", value: 2 },
      { date: "03-03", value: 3 },
      { date: "03-04", value: 2 },
      { date: "03-05", value: 1 },
      { date: "03-15", value: 1 },
      { date: "03-16", value: 2 },
      { date: "03-17", value: 3 },
      { date: "03-18", value: 2 }
    ]
  },

  // 提醒列表
  reminders: [
    { id: 1, content: "早上8点服药", time: "08:00", status: "pending", type: "medication" },
    { id: 2, content: "上午10点喝水", time: "10:00", status: "completed", type: "habit" },
    { id: 3, content: "下午6点运动", time: "18:00", status: "pending", type: "exercise" },
    { id: 4, content: "晚上10点半睡觉", time: "22:30", status: "pending", type: "sleep" },
    { id: 5, content: "午餐营养摄入", time: "12:00", status: "completed", type: "diet" }
  ],

  // 用药列表
  medications: [
    { id: 1, name: "维生素D", dosage: "1片", frequency: "每日1次", time: "08:00", status: "active" },
    { id: 2, name: "钙片", dosage: "2片", frequency: "每日2次", time: "08:00,20:00", status: "active" },
    { id: 3, name: "感冒灵", dosage: "1包", frequency: "按需", time: "", status: "paused" }
  ],

  // 用药记录
  medicationRecords: [
    { id: 1, medicationName: "维生素D", takeTime: "2026-03-18 08:05", status: "taken" },
    { id: 2, medicationName: "钙片", takeTime: "2026-03-18 08:05", status: "taken" },
    { id: 3, medicationName: "钙片", takeTime: "2026-03-18 20:00", status: "taken" },
    { id: 4, medicationName: "维生素D", takeTime: "2026-03-17 08:00", status: "taken" },
    { id: 5, medicationName: "感冒灵", takeTime: "2026-03-15 14:00", status: "missed" }
  ],

  // 家人列表
  families: [
    { id: 1, name: "爸爸", relation: "父亲", age: 55, phone: "138****1234", healthStatus: "良好" },
    { id: 2, name: "妈妈", relation: "母亲", age: 52, phone: "139****5678", healthStatus: "一般" },
    { id: 3, name: "妻子", relation: "配偶", age: 28, phone: "137****9012", healthStatus: "良好" }
  ],

  // ========== 管理端 Mock 数据 ==========

  // 用户列表
  adminUsers: [
    { id: 1, nickname: "张三", phone: "13812345678", openid: "oXXXX1", createTime: "2026-01-15 10:30", status: "active" },
    { id: 2, nickname: "李四", phone: "13912345678", openid: "oXXXX2", createTime: "2026-02-01 14:20", status: "active" },
    { id: 3, nickname: "王五", phone: "", openid: "oXXXX3", createTime: "2026-02-10 09:15", status: "active" },
    { id: 4, nickname: "赵六", phone: "13712345678", openid: "oXXXX4", createTime: "2026-02-20 16:45", status: "inactive" },
    { id: 5, nickname: "孙七", phone: "13612345678", openid: "oXXXX5", createTime: "2026-03-01 11:00", status: "active" }
  ],

  // 系统日志
  systemLogs: [
    { id: 1, level: "info", module: "user", path: "/api/user/login", message: "用户登录成功", time: "2026-03-18 10:30:15" },
    { id: 2, level: "warn", module: "medication", path: "/api/medication/list", message: "用药记录查询超时", time: "2026-03-18 10:28:45" },
    { id: 3, level: "error", module: "statistics", path: "/api/statistics/trend", message: "数据计算异常", time: "2026-03-18 10:25:30" },
    { id: 4, level: "info", module: "admin", path: "/api/admin/users", message: "管理员查询用户列表", time: "2026-03-18 10:20:00" },
    { id: 5, level: "info", module: "ai", path: "/api/ai/chat", message: "AI分析请求", time: "2026-03-18 10:15:22" }
  ],

  // AI 调用日志
  aiLogs: [
    { id: 1, userId: 1, openid: "oXXXX1", request: "今日健康分析", response: "良好...", status: "success", error: "", time: "2026-03-18 10:30:15" },
    { id: 2, userId: 2, openid: "oXXXX2", request: "睡眠质量评估", response: "您的睡眠...", status: "success", error: "", time: "2026-03-18 10:25:30" },
    { id: 3, userId: 1, openid: "oXXXX1", request: "运动建议", response: "", status: "error", error: "API调用超时", time: "2026-03-18 10:20:00" },
    { id: 4, userId: 3, openid: "oXXXX3", request: "饮食分析", response: "营养摄入...", status: "success", error: "", time: "2026-03-18 10:15:45" }
  ],

  // 订阅任务队列
  subscribeTasks: [
    { id: 1, userId: 1, openid: "oXXXX1", templateId: "TPL_HEALTH_REMIND", content: "该喝水了", status: "pending", createTime: "2026-03-18 10:00:00", sendTime: "" },
    { id: 2, userId: 2, openid: "oXXXX2", templateId: "TPL_MEDICATION", content: "该服药了", status: "sent", createTime: "2026-03-18 08:00:00", sendTime: "2026-03-18 08:00:05" },
    { id: 3, userId: 1, openid: "oXXXX1", templateId: "TPL_EXERCISE", content: "运动时间到了", status: "sent", createTime: "2026-03-18 18:00:00", sendTime: "2026-03-18 18:00:03" },
    { id: 4, userId: 3, openid: "oXXXX3", templateId: "TPL_SLEEP", content: "该休息了", status: "failed", createTime: "2026-03-18 22:30:00", sendTime: "", error: "用户拒收" },
    { id: 5, userId: 2, openid: "oXXXX2", templateId: "TPL_HEALTH_REPORT", content: "周报已生成", status: "pending", createTime: "2026-03-18 20:00:00", sendTime: "" }
  ]
};
