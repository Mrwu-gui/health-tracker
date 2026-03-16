<template>
  <view class="page">
    <view class="header">
      <text class="title">智康健康分析</text>
      <navigator class="btn-link" url="/pages/ai/index">去对话</navigator>
    </view>

    <view class="tabs">
      <view
        v-for="item in periods"
        :key="item.value"
        class="tab"
        :class="{ active: period === item.value }"
        @tap="switchPeriod(item.value)"
      >
        {{ item.label }}
      </view>
    </view>

    <view class="section-card">
      <view class="section-head">
        <view class="section-title-wrap">
          <view class="section-icon section-icon-data"><text class="section-icon-txt">数</text></view>
          <text class="section-title">本{{ periodLabel }}数据</text>
        </view>
      </view>
      <view class="data-grid">
        <view class="data-item">
          <text class="data-label">步数</text>
          <text class="data-value">{{ overview.steps }} 步</text>
        </view>
        <view class="data-item">
          <text class="data-label">睡眠</text>
          <text class="data-value">{{ overview.sleep }}</text>
        </view>
        <view class="data-item">
          <text class="data-label">体重/BMI</text>
          <text class="data-value">{{ overview.weightBmi }}</text>
        </view>
        <view class="data-item">
          <text class="data-label">运动</text>
          <text class="data-value">{{ overview.exerciseMinutes || '0' }} 分钟</text>
        </view>
        <view class="data-item">
          <text class="data-label">饮食</text>
          <text class="data-value">{{ overview.dietCount || '--' }}</text>
        </view>
        <view class="data-item data-item-period">
          <text class="data-label">经期</text>
          <text class="data-value data-value-period">{{ overview.periodSummary || '--' }}</text>
        </view>
      </view>
    </view>

    <view class="section-card ai-card">
      <view class="section-head">
        <view class="section-title-wrap">
          <view class="section-icon section-icon-ai"><text class="section-icon-txt">智</text></view>
          <text class="section-title">智康 为你解读</text>
        </view>
        <text v-if="!aiLoading && aiContent" class="btn-text" @tap="refreshAnalysis">重新分析</text>
      </view>
      <view class="ai-body">
        <view v-if="aiLoading" class="ai-loading">
          <text class="ai-loading-dot">·</text>
          <text class="ai-loading-text">分析中…</text>
        </view>
        <view v-else-if="aiError" class="ai-error">
          <text>{{ aiError }}</text>
          <text class="ai-retry" @tap="refreshAnalysis">点击重试</text>
        </view>
        <view v-else-if="aiContent" class="ai-content">
          <text class="ai-text">{{ aiContent }}</text>
        </view>
        <view v-else class="ai-empty">
          <text>暂无解读</text>
          <text class="ai-retry" @tap="refreshAnalysis">点击生成</text>
        </view>
      </view>
    </view>

    <view class="tips-section">
      <view class="tips-head">
        <view class="section-icon section-icon-tips"><text class="section-icon-txt">贴</text></view>
        <text class="tips-title">健康小贴士</text>
      </view>
      <scroll-view class="tips-scroll" scroll-y :show-scrollbar="true">
        <view v-for="(item, i) in healthTips" :key="i" class="tip-card">
          <text class="tip-tag">{{ item.tag }}</text>
          <text class="tip-headline">{{ item.title }}</text>
          <text class="tip-body">{{ item.content }}</text>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script>
import { request } from "../../utils/api";

export default {
  data() {
    return {
      period: "day",
      periods: [
        { label: "今日", value: "day" },
        { label: "本周", value: "week" },
        { label: "本月", value: "month" }
      ],
      overview: {
        steps: "0",
        sleep: "0小时0分",
        weight: "",
        bmi: "",
        weightBmi: "--",
        exerciseMinutes: "0",
        dietCount: "",
        periodSummary: "",
        periodLastDate: "",
        periodNextDate: ""
      },
      aiContent: "",
      aiLoading: false,
      aiError: "",
      healthTips: [
        {
          tag: "减脂饮食",
          title: "减脂期该怎么吃？",
          content: "优先保证蛋白质（蛋奶、瘦肉、豆类）和足量蔬菜，主食用粗粮替代部分精米白面，每餐七分饱。少油少糖、戒掉含糖饮料，晚餐可适当提前、减少碳水。"
        },
        {
          tag: "睡眠",
          title: "睡不好会影响减脂吗？",
          content: "会。睡眠不足会升高皮质醇、增加饥饿感，更容易囤脂。建议固定作息、睡前少看屏幕，保证 7～8 小时睡眠，有助稳定代谢。"
        },
        {
          tag: "运动",
          title: "每天动多久合适？",
          content: "WHO 建议成人每周至少 150 分钟中等强度有氧（如快走）或 75 分钟高强度，外加每周 2 次力量训练。从每天 6000 步开始，再逐步加量。"
        },
        {
          tag: "饮水",
          title: "一天要喝多少水？",
          content: "一般建议每日 1.5～2 升（约 8 杯）。运动多、天热可适当增加。晨起一杯水、餐前少量饮水有助控制食欲，少喝含糖饮料。"
        },
        {
          tag: "加餐",
          title: "下午饿可以吃什么？",
          content: "可选无糖酸奶、一小把坚果、水果（如苹果、蓝莓）、煮蛋或全麦面包。避免高糖零食和油炸食品，控制分量更利于控热量。"
        },
        {
          tag: "心态",
          title: "健康习惯坚持不下来怎么办？",
          content: "从小目标开始：先养成一个习惯（如每天多走 1000 步或固定睡觉时间），再叠加下一个。记录进度、找同伴互相督促，比一次改很多更容易坚持。"
        },
        {
          tag: "早餐",
          title: "早餐怎么吃更健康？",
          content: "建议有优质蛋白（蛋、奶、豆）和适量碳水，搭配一点蔬果。避免只吃精制主食或高糖糕点，早餐吃好有助稳定上午血糖和精力。"
        },
        {
          tag: "久坐",
          title: "久坐族怎么动？",
          content: "每 30～45 分钟起身活动 2～3 分钟，做做伸展、走动。下班后可步行一段、爬楼梯，或在家做 10～15 分钟徒手训练，减少久坐带来的风险。"
        },
        {
          tag: "压力",
          title: "压力大时怎么调节？",
          content: "试试深呼吸、短时散步或听音乐；保证睡眠、少熬夜。规律运动有助释放压力，必要时可与人倾诉或寻求专业支持。"
        },
        {
          tag: "护眼",
          title: "长时间用眼怎么护眼？",
          content: "遵循 20-20-20 法则：每 20 分钟看 20 英尺外约 20 秒。屏幕亮度适中、保持距离，多眨眼；户外活动也有益眼健康。"
        },
        {
          tag: "体重",
          title: "体重波动正常吗？",
          content: "日内或几天内 1～2 斤波动多与水分、饮食有关，属正常。更关注长期趋势：每周固定时间、空腹称重并记录，比单次数值更有参考意义。"
        },
        {
          tag: "经期",
          title: "经期可以运动吗？",
          content: "可以。经期适度运动有助缓解不适、改善情绪。建议选择快走、瑜伽、拉伸等中低强度，避免剧烈跑跳和腹部挤压。量力而行，不适时休息。"
        },
        {
          tag: "经期",
          title: "经期饮食要注意什么？",
          content: "多吃含铁食物（红肉、动物血、深色蔬菜）和维生素 C 助吸收；少喝冷饮、少吃生冷，可喝温水、红糖姜茶。保证睡眠、少熬夜，有助周期稳定。"
        },
        {
          tag: "经期",
          title: "经期周期多少天算正常？",
          content: "一般 21～35 天为常见范围，经期 3～7 天。偶尔提前或推后几天多与压力、作息有关。建议记录周期，若长期不规律或异常出血，可咨询医生。"
        }
      ]
    };
  },
  computed: {
    periodLabel() {
      const map = { day: "日", week: "周", month: "月" };
      return map[this.period] || "期";
    }
  },
  onShow() {
    const pages = getCurrentPages();
    const page = pages[pages.length - 1];
    if (page && typeof page.getTabBar === "function") {
      const tabBar = page.getTabBar();
      if (tabBar && typeof tabBar.setData === "function") tabBar.setData({ selected: 1 });
    }
    this.fetchOverview();
  },
  methods: {
    switchPeriod(value) {
      this.period = value;
      this.fetchOverview();
    },
    async fetchOverview() {
      const userId = uni.getStorageSync("userId") || 1;
      try {
        const data = await request("/api/statistics/overview", "GET", { userId, period: this.period });
        this.overview.steps = data.steps != null ? String(data.steps) : "0";
        this.overview.sleep = data.sleep || "0小时0分";
        this.overview.weight = data.weight != null ? String(data.weight) : "";
        this.overview.bmi = data.bmi != null ? String(data.bmi) : "";
        if (this.overview.weight || this.overview.bmi) {
          this.overview.weightBmi = this.overview.weight ? `${this.overview.weight} kg` : "";
          if (this.overview.bmi) this.overview.weightBmi += (this.overview.weightBmi ? " · " : "") + this.overview.bmi;
        } else {
          this.overview.weightBmi = "--";
        }
        this.overview.exerciseMinutes = data.exerciseMinutes != null ? String(data.exerciseMinutes) : "0";
        this.overview.dietCount = data.dietCount != null ? `已记录 ${data.dietCount} 餐` : "";
        await this.loadPeriodSummary();
        this.fetchAiAnalysis();
      } catch (err) {
        this.aiError = "获取数据失败，请稍后重试";
        this.aiContent = "";
      }
    },
    async loadPeriodSummary() {
      const STORAGE_KEY = "periodRecords";
      let list = [];
      try {
        const data = await request("/api/period/list", "GET", { userId: uni.getStorageSync("userId") || 1 });
        if (Array.isArray(data)) list = data;
      } catch (e) {
        try {
          const raw = uni.getStorageSync(STORAGE_KEY);
          if (raw) list = JSON.parse(raw);
        } catch (_) {}
      }
      if (list.length === 0) {
        this.overview.periodSummary = "";
        this.overview.periodLastDate = "";
        this.overview.periodNextDate = "";
        return;
      }
      const sorted = list
        .map((item) => ({ start: item.startDate || item.start_date, end: item.endDate || item.end_date }))
        .filter((item) => item.start)
        .sort((a, b) => (b.start || "").localeCompare(a.start || ""));
      const last = sorted[0];
      if (!last) {
        this.overview.periodSummary = "";
        this.overview.periodLastDate = "";
        this.overview.periodNextDate = "";
        return;
      }
      const lastStr = last.start;
      const d = new Date(lastStr.replace(/-/g, "/"));
      d.setDate(d.getDate() + 28);
      const nextStr = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
      this.overview.periodLastDate = lastStr;
      this.overview.periodNextDate = nextStr;
      const shortLast = lastStr.length >= 5 ? lastStr.slice(-5) : lastStr;
      const shortNext = nextStr.length >= 5 ? nextStr.slice(-5) : nextStr;
      this.overview.periodSummary = `${shortLast} → ${shortNext}`;
    },
    getCacheKey() {
      const today = new Date();
      return `ai_analysis_${this.period}_${today.getFullYear()}-${today.getMonth() + 1}-${today.getDate()}`;
    },
    async fetchAiAnalysis(forceRefresh) {
      if (this.aiLoading) return;
      const key = this.getCacheKey();
      if (!forceRefresh) {
        const cached = uni.getStorageSync(key);
        if (cached && typeof cached === "string" && cached.length > 0) {
          this.aiContent = cached;
          this.aiError = "";
          return;
        }
      }
      this.aiLoading = true;
      this.aiError = "";
      this.aiContent = "";
      const periodLabel = this.period === "day" ? "今日" : this.period === "week" ? "本周" : "本月";
      let dataLine = `步数 ${this.overview.steps} 步，睡眠 ${this.overview.sleep}，体重/BMI ${this.overview.weightBmi || "未记录"}，运动 ${this.overview.exerciseMinutes} 分钟，饮食 ${this.overview.dietCount || "未记录"}`;
      let periodInstruction = "";
      if (this.overview.periodLastDate && this.overview.periodNextDate) {
        dataLine += `，经期：最近一次 ${this.overview.periodLastDate}，预计下次 ${this.overview.periodNextDate}`;
        periodInstruction = "上述数据中包含经期信息，请在总结或建议中至少包含一句与经期相关的健康提醒或注意事项。";
      }
      const prompt = `你是一位贴心的健康顾问。请根据以下${periodLabel}的健康数据，用 2～4 句话简要总结健康状况，然后给出 2～3 条具体、可执行的改进建议。每条建议一行，简洁明了，每条不超过 25 字。不要寒暄，直接输出分析和建议。${periodInstruction ? "\n\n" + periodInstruction : ""}

数据：${dataLine}。`;
      try {
        const userId = uni.getStorageSync("userId") || 1;
        const data = await request("/api/ai/chat", "POST", { userId, message: prompt, store: false });
        const content = (data && data.content) ? String(data.content).trim() : "";
        if (content) {
          this.aiContent = content;
          uni.setStorageSync(key, content);
        } else {
          this.aiError = "AI 暂未返回解读";
        }
      } catch (err) {
        this.aiError = err.message || "分析失败，请稍后重试";
      } finally {
        this.aiLoading = false;
      }
    },
    refreshAnalysis() {
      const key = this.getCacheKey();
      uni.removeStorageSync(key);
      this.fetchAiAnalysis(true);
    }
  }
};
</script>

<style>
.page {
  padding: 20px 18px;
  padding-bottom: calc(56px + env(safe-area-inset-bottom));
  min-height: 100vh;
  background: linear-gradient(180deg, #f5f1eb 0%, #e8e2db 50%, #fefcf9 100%);
  color: #0f172a;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.title {
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: 0.3px;
}

.btn-link {
  font-size: 13px;
  color: #4f46e5;
  font-weight: 500;
}

.tabs {
  display: inline-flex;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 12px;
  padding: 4px;
  gap: 4px;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.06);
}

.tab {
  font-size: 13px;
  padding: 8px 18px;
  border-radius: 10px;
  color: #64748b;
  transition: all 0.2s ease;
}

.tab.active {
  background: #ffffff;
  color: #334155;
  font-weight: 600;
  box-shadow: 0 2px 6px rgba(15, 23, 42, 0.08);
}

.section-card {
  background: #ffffff;
  border-radius: 20px;
  padding: 0;
  border: none;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(15, 23, 42, 0.06);
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 18px;
  border-bottom: 1px solid #f2ede8;
}

.section-title-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
}

.section-icon {
  width: 32px;
  height: 32px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 700;
}

.section-icon-txt {
  color: inherit;
}

.section-icon-data {
  background: linear-gradient(135deg, #e0e7ff 0%, #c7d2fe 100%);
  color: #4f46e5;
}

.section-icon-ai {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  color: #b45309;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #0f172a;
}

.btn-text {
  font-size: 12px;
  color: #6366f1;
  font-weight: 500;
}

.data-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
  padding: 18px;
}

.data-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 12px 10px;
  background: #fefcf9;
  border-radius: 12px;
  text-align: center;
}

.data-label {
  font-size: 11px;
  color: #94a3b8;
}

.data-value {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}

.data-item-period .data-value-period {
  font-size: 12px;
  font-weight: 500;
  line-height: 1.4;
  word-break: break-all;
  white-space: normal;
  text-align: center;
  display: block;
}

.ai-card {
  box-shadow: 0 4px 20px rgba(79, 70, 229, 0.08);
}

.ai-card .section-head {
  border-bottom-color: #fef9c3;
  background: linear-gradient(180deg, #fffbeb 0%, #ffffff 100%);
}

.ai-body {
  padding: 18px;
  min-height: 100px;
  background: #ffffff;
}

.ai-loading {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #64748b;
}

.ai-loading-dot {
  font-size: 18px;
  animation: blink 0.8s ease-in-out infinite;
}

@keyframes blink {
  50% { opacity: 0.3; }
}

.ai-loading-text {
  font-size: 14px;
}

.ai-error,
.ai-empty {
  font-size: 14px;
  color: #64748b;
  text-align: center;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.ai-retry {
  font-size: 13px;
  color: #4f46e5;
  font-weight: 500;
}

.ai-content {
  width: 100%;
}

.ai-text {
  font-size: 14px;
  line-height: 1.75;
  color: #334155;
  white-space: pre-wrap;
  word-break: break-word;
  display: block;
}

.tips-section {
  background: #ffffff;
  border-radius: 20px;
  overflow: hidden;
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  box-shadow: 0 4px 16px rgba(15, 23, 42, 0.06);
}

.tips-head {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 18px;
  border-bottom: 1px solid #f2ede8;
  background: linear-gradient(180deg, #fefcf9 0%, #ffffff 100%);
}

.section-icon-tips {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  color: #2563eb;
}

.tips-title {
  font-size: 16px;
  font-weight: 600;
  color: #0f172a;
}

.tips-scroll {
  height: 56vh;
  max-height: 480px;
  padding: 16px 18px 24px;
  box-sizing: border-box;
}

.tip-card {
  background: #fefcf9;
  border-radius: 16px;
  padding: 16px 18px;
  margin-bottom: 14px;
  border: 1px solid #f2ede8;
  position: relative;
  overflow: hidden;
}

.tip-card::before {
  content: "";
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: linear-gradient(180deg, #6366f1 0%, #818cf8 100%);
  border-radius: 4px 0 0 4px;
}

.tip-card:last-child {
  margin-bottom: 0;
}

.tip-tag {
  display: inline-block;
  font-size: 11px;
  color: #4f46e5;
  background: #e0e7ff;
  padding: 4px 10px;
  border-radius: 8px;
  margin-bottom: 10px;
  font-weight: 500;
}

.tip-headline {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  display: block;
  margin-bottom: 8px;
  line-height: 1.4;
}

.tip-body {
  font-size: 13px;
  color: #64748b;
  line-height: 1.6;
  display: block;
}
</style>
