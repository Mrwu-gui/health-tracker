<template>
  <view class="page">
    <view class="header">
      <text class="title">AI 健康分析</text>
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

    <!-- 本周期数据摘要（供 AI 分析用） -->
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
          <text class="section-title">AI 为你解读</text>
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
      <navigator class="footer-link" url="/pages/ai/index">去和 AI 健康助手对话</navigator>
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
      aiError: ""
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
  padding: 18px;
  padding-bottom: calc(60px + env(safe-area-inset-bottom));
  min-height: 100vh;
  background: #f4f5f7;
  color: #0f172a;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: #0f172a;
}

.btn-link {
  font-size: 13px;
  color: #2563eb;
}

.tabs {
  display: inline-flex;
  background: #e2e8f0;
  border-radius: 999px;
  padding: 2px;
  gap: 4px;
}

.tab {
  font-size: 12px;
  padding: 6px 14px;
  border-radius: 999px;
  color: #64748b;
}

.tab.active {
  background: #ffffff;
  color: #0f172a;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.06);
}

.section-card {
  background: #ffffff;
  border-radius: 18px;
  padding: 0;
  border: 1px solid #e2e8f0;
  overflow: hidden;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid #f1f5f9;
}

.section-title-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-icon {
  width: 28px;
  height: 28px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.section-icon-data {
  background: #e0e7ff;
  color: #4f46e5;
}

.section-icon-ai {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  color: #b45309;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #0f172a;
}

.btn-text {
  font-size: 12px;
  color: #64748b;
}

.data-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  padding: 14px 16px;
}

.data-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.data-label {
  font-size: 11px;
  color: #94a3b8;
}

.data-value {
  font-size: 13px;
  font-weight: 600;
  color: #0f172a;
}

.ai-card .section-head {
  border-bottom-color: #fef3c7;
}

.ai-body {
  padding: 16px;
  min-height: 100px;
}

.ai-loading {
  display: flex;
  align-items: center;
  gap: 8px;
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
  font-size: 13px;
}

.ai-error,
.ai-empty {
  font-size: 13px;
  color: #64748b;
  text-align: center;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.ai-retry {
  font-size: 12px;
  color: #2563eb;
}

.ai-content {
  width: 100%;
}

.ai-text {
  font-size: 14px;
  line-height: 1.65;
  color: #334155;
  white-space: pre-wrap;
  word-break: break-word;
  display: block;
}

.footer-tip {
  font-size: 12px;
  color: #94a3b8;
  text-align: center;
  padding: 8px 0;
}

.footer-link {
  color: #2563eb;
  margin-left: 4px;
}
</style>
