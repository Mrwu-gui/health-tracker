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

    <!-- 本周期数据摘要（供智康分析用） -->
    <view class="section-card">
      <view class="section-head">
        <view class="section-title-wrap">
          <view class="section-icon section-icon-data"><text>📋</text></view>
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
          <text class="data-label">体重</text>
          <text class="data-value">{{ overview.weight ? overview.weight + ' kg' : '--' }}</text>
        </view>
        <view class="data-item">
          <text class="data-label">BMI</text>
          <text class="data-value">{{ overview.bmi || '--' }}</text>
        </view>
        <view class="data-item">
          <text class="data-label">运动</text>
          <text class="data-value">{{ overview.exerciseMinutes || '0' }} 分钟</text>
        </view>
        <view class="data-item">
          <text class="data-label">饮食</text>
          <text class="data-value">{{ overview.dietCount || '--' }}</text>
        </view>
      </view>
    </view>

    <!-- AI 解读主卡 -->
    <view class="section-card ai-card">
      <view class="section-head">
        <view class="section-title-wrap">
          <view class="section-icon section-icon-ai"><text>✨</text></view>
          <text class="section-title">智康 为你解读</text>
        </view>
        <text v-if="!aiLoading && aiContent" class="btn-text" @tap="refreshAnalysis">重新分析</text>
      </view>
      <view class="ai-body">
        <view v-if="aiLoading" class="ai-loading">
          <text class="ai-loading-dot">·</text>
          <text class="ai-loading-text">正在分析你的数据...</text>
        </view>
        <view v-else-if="aiError" class="ai-error">
          <text>{{ aiError }}</text>
          <text class="ai-retry" @tap="refreshAnalysis">点击重试</text>
        </view>
        <view v-else-if="aiContent" class="ai-content">
          <text class="ai-text">{{ aiContent }}</text>
        </view>
        <view v-else class="ai-empty">
          <text>暂无解读，请确保已记录步数、睡眠等数据后重试。</text>
          <text class="ai-retry" @tap="refreshAnalysis">点击生成</text>
        </view>
      </view>
    </view>

    <view class="footer-tip">
      <text>想追问细节？</text>
      <navigator class="footer-link" url="/pages/ai/index">去和智康对话</navigator>
    </view>

    <!-- 健康小贴士：可滚动半屏 -->
    <view class="tips-section">
      <view class="tips-head">
        <view class="section-icon section-icon-tips"><text>💡</text></view>
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
        exerciseMinutes: "0",
        dietCount: ""
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
        this.overview.exerciseMinutes = data.exerciseMinutes != null ? String(data.exerciseMinutes) : "0";
        this.overview.dietCount = data.dietCount != null ? `已记录 ${data.dietCount} 餐` : "";
        this.fetchAiAnalysis();
      } catch (err) {
        this.aiError = "获取数据失败，请稍后重试";
        this.aiContent = "";
      }
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
      const prompt = `你是一位贴心的健康顾问。请根据以下${periodLabel}的健康数据，用 2～4 句话简要总结健康状况，然后给出 2～3 条具体、可执行的改进建议。每条建议一行，简洁明了，每条不超过 25 字。不要寒暄，直接输出分析和建议。

数据：步数 ${this.overview.steps} 步，睡眠 ${this.overview.sleep}，体重 ${this.overview.weight || "未记录"} kg，BMI ${this.overview.bmi || "未记录"}，运动 ${this.overview.exerciseMinutes} 分钟，饮食 ${this.overview.dietCount || "未记录"}。`;
      try {
        const data = await request("/api/ai/chat", "POST", { message: prompt, store: false });
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
  padding-bottom: calc(72px + env(safe-area-inset-bottom));
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
  font-size: 17px;
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

.footer-tip {
  font-size: 13px;
  color: #64748b;
  text-align: center;
  padding: 10px 14px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 14px;
  box-shadow: 0 2px 10px rgba(15, 23, 42, 0.04);
}

.footer-link {
  color: #4f46e5;
  margin-left: 4px;
  font-weight: 500;
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
  height: 42vh;
  max-height: 340px;
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
