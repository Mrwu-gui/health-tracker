<template>
  <view class="page">
    <view class="header">
      <text class="title">数据统计</text>
      <navigator class="btn-dark btn-export" url="/pages/data/export">导出周报</navigator>
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

    <view class="summary">
      <text class="summary-title">
        <text class="fa-solid fa-lightbulb summary-icon">💡</text>
        AI 健康建议
      </text>
      <text class="summary-note">基于你的运动、睡眠与体重数据生成</text>
      <text v-for="(item, idx) in aiSummary" :key="idx" class="summary-item">· {{ item }}</text>
      <text v-if="aiSummary.length === 0" class="summary-item">· 暂无智能解读。</text>
    </view>

    <view class="grid">
      <view class="card">
        <text class="label">体重</text>
        <text class="value">{{ overview.weight || "--" }} kg</text>
        <text class="sub">较上周 {{ weightDelta }}</text>
      </view>
      <view class="card">
        <text class="label">步数</text>
        <text class="value">{{ overview.steps }} 步</text>
        <text class="sub">完成 {{ overview.progress }}%</text>
      </view>
      <view class="card">
        <text class="label">睡眠</text>
        <text class="value">{{ overview.sleep }}</text>
        <text class="sub">评分 {{ sleepScore }}</text>
      </view>
      <view class="card">
        <text class="label">用药完成率</text>
        <text class="value">{{ medRate }}</text>
        <text class="sub">本周漏服 {{ medMiss }} 次</text>
      </view>
    </view>

    <view class="card large">
      <view class="card-head">
        <text class="card-title">体重变化</text>
        <text class="card-sub">最近 7 天</text>
      </view>
      <view class="line">
        <view class="line-row" v-for="n in 3" :key="n"></view>
        <view class="line-path"></view>
      </view>
    </view>

    <view class="card large">
      <view class="card-head">
        <text class="card-title">
          <text class="fa-solid fa-person-running card-icon">🏃</text>
          运动数据
        </text>
        <view class="pill">
          <text class="pill-item active">步数</text>
          <text class="pill-item">时长</text>
        </view>
      </view>
      <view class="bars">
        <view v-for="(v, idx) in stepBars" :key="idx" class="bar">
          <view class="bar-fill" :style="{ height: `${v}%` }"></view>
        </view>
      </view>
    </view>

    <view class="grid two">
      <view class="card">
        <text class="label">睡眠结构</text>
        <view class="progress">
          <view class="progress-fill" :style="{ width: `${sleepDeep}%` }"></view>
        </view>
        <text class="sub">深睡 {{ sleepDeep }}% · 浅睡 {{ 100 - sleepDeep }}%</text>
      </view>
      <view class="card">
        <text class="label">用药完成率</text>
        <view class="ring">
          <text class="ring-value">{{ medRate }}</text>
        </view>
        <text class="sub">本周漏服 {{ medMiss }} 次</text>
      </view>
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
        { label: "日", value: "day" },
        { label: "周", value: "week" },
        { label: "月", value: "month" }
      ],
      overview: {
        steps: "0",
        progress: "0",
        sleep: "0小时0分",
        weight: "",
        bmi: ""
      },
      stepBars: [0, 0, 0, 0, 0, 0, 0],
      sleepDeep: 0,
      weightDelta: "--",
      sleepScore: "--",
      medRate: "--",
      medMiss: "0",
      aiSummary: []
    };
  },
  onLoad() {
  },
  onShow() {
    this.fetchOverview();
  },
  methods: {
    switchPeriod(value) {
      this.period = value;
      this.fetchOverview();
    },
    fetchOverview() {
      const userId = uni.getStorageSync("userId") || 1;
      request("/api/statistics/overview", "GET", { userId, period: this.period })
        .then((data) => {
          this.overview.steps = data.steps || "0";
          this.overview.sleep = data.sleep || "0小时0分";
          this.overview.weight = data.weight || "";
          this.overview.bmi = data.bmi || "";
          const steps = parseInt(this.overview.steps, 10);
          const progress = Math.min(100, Math.round((steps / 10000) * 100));
          this.overview.progress = String(isNaN(progress) ? 0 : progress);
          this.fetchAiSummary();
        })
        .catch(() => {});
    },
    applyDefaultData() {
      this.overview.steps = "0";
      this.overview.progress = "0";
      this.overview.sleep = "0小时0分";
      this.overview.weight = "";
      this.overview.bmi = "";
      this.stepBars = [0, 0, 0, 0, 0, 0, 0];
      this.sleepDeep = 0;
      this.weightDelta = "--";
      this.sleepScore = "--";
      this.medRate = "--";
      this.medMiss = "0";
    },
    exportReport() {
      uni.showToast({ title: "已生成周报", icon: "success" });
    },
    async fetchAiSummary() {
      const today = new Date();
      const key = `ai_summary_${this.period}_${today.getFullYear()}-${today.getMonth() + 1}-${today.getDate()}`;
      const cached = uni.getStorageSync(key);
      if (cached && Array.isArray(cached) && cached.length) {
        this.aiSummary = cached;
        return;
      }
      try {
        const prompt = `请根据以下健康数据生成3条可执行建议，每条不超过20字：步数${this.overview.steps}，睡眠${this.overview.sleep}，体重${this.overview.weight}，BMI${this.overview.bmi}。`;
        const data = await request("/api/ai/chat", "POST", { message: prompt, store: false });
        if (data && data.content) {
          const items = data.content
            .split(/\n|。|；|;/)
            .map((item) => item.replace(/^·\s*/, "").trim())
            .filter((item) => item.length > 0)
            .slice(0, 3);
          this.aiSummary = items;
          if (items.length) {
            uni.setStorageSync(key, items);
          }
        }
      } catch (err) {
        this.aiSummary = [];
      }
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
  width: 100%;
  margin-left: -18px;
  margin-right: -18px;
  padding-left: 18px;
  padding-right: 18px;
  box-sizing: border-box;
}

.title {
  font-size: 18px;
  font-weight: 600;
}

.btn-dark {
  background: #2563eb;
  color: #ffffff;
  border-radius: 12px;
  font-size: 11px;
  padding: 6px 12px;
}

.btn-export {
  flex-shrink: 0;
  margin-right: 0;
}

.tabs {
  display: inline-flex;
  background: #e2e8f0;
  border-radius: 999px;
  padding: 2px;
  gap: 4px;
  align-self: flex-start;
}

.tab {
  font-size: 11px;
  padding: 4px 10px;
  border-radius: 999px;
  color: #64748b;
}

.tab.active {
  background: #ffffff;
  color: #0f172a;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.08);
}

.grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.grid.two {
  grid-template-columns: repeat(2, 1fr);
}

.card {
  background: #ffffff;
  border-radius: 16px;
  padding: 12px;
  border: 1px solid #e2e8f0;
}

.card.large {
  grid-column: span 2;
}

.label {
  font-size: 11px;
  color: #94a3b8;
  display: block;
}

.value {
  font-size: 14px;
  font-weight: 600;
  margin-top: 6px;
  display: block;
}

.sub {
  font-size: 10px;
  color: #64748b;
  margin-top: 4px;
  display: block;
}

.card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.card-title {
  font-size: 12px;
  font-weight: 600;
}

.card-sub {
  font-size: 10px;
  color: #94a3b8;
}

.line {
  height: 100px;
  position: relative;
}

.line-row {
  height: 33%;
  border-top: 1px dashed #e2e8f0;
}

.line-path {
  position: absolute;
  left: 10px;
  right: 10px;
  top: 30px;
  height: 40px;
  border-bottom: 2px solid #2563eb;
  border-left: 2px solid #2563eb;
  border-top-right-radius: 12px;
}

.pill {
  display: inline-flex;
  background: #e2e8f0;
  border-radius: 999px;
  padding: 2px;
  gap: 4px;
}

.pill-item {
  font-size: 10px;
  padding: 4px 8px;
  border-radius: 999px;
  color: #64748b;
}

.pill-item.active {
  background: #ffffff;
  color: #0f172a;
}

.bars {
  display: flex;
  align-items: flex-end;
  gap: 6px;
  height: 80px;
}

.bar {
  flex: 1;
  background: #dbeafe;
  border-radius: 999px;
  overflow: hidden;
  display: flex;
  align-items: flex-end;
}

.bar-fill {
  width: 100%;
  background: #2563eb;
}

.progress {
  width: 100%;
  height: 8px;
  background: #e2e8f0;
  border-radius: 999px;
  overflow: hidden;
  margin-top: 8px;
}

.progress-fill {
  height: 100%;
  background: #6366f1;
}

.ring {
  width: 60px;
  height: 60px;
  border-radius: 30px;
  border: 6px solid #e2e8f0;
  display: grid;
  place-items: center;
  margin: 8px auto;
}

.ring-value {
  font-size: 12px;
  font-weight: 600;
}

.summary {
  background: #0f172a;
  color: #e2e8f0;
  border-radius: 16px;
  padding: 12px;
  display: grid;
  gap: 6px;
}

.summary-title {
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
}

.summary-note {
  font-size: 11px;
  color: #94a3b8;
  margin-top: 2px;
}

.summary-item {
  font-size: 11px;
  color: #cbd5f5;
}

.summary-icon {
  color: #fbbf24;
  font-size: 12px;
}

.card-icon {
  font-size: 12px;
  color: #2563eb;
  margin-right: 6px;
}
</style>
