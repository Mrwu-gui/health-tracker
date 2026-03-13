<template>
  <view class="page">
    <view class="header">
      <text class="title">统计分析</text>
      <text class="subtitle">周报 / 月报 / 趋势对比</text>
    </view>
    <view class="card">
      <text class="card-title">本周步数趋势</text>
      <view class="chart">
        <view class="bar" v-for="(v, idx) in steps" :key="idx" :style="{ height: `${v}%` }"></view>
      </view>
      <text class="hint">均值 {{ avgSteps }} 步</text>
    </view>
    <view class="card">
      <text class="card-title">睡眠质量分布</text>
      <view class="chips">
        <view class="chip">优 {{ sleepQuality.excellent }} 天</view>
        <view class="chip">良 {{ sleepQuality.good }} 天</view>
        <view class="chip">一般 {{ sleepQuality.normal }} 天</view>
      </view>
    </view>
    <view class="card">
      <text class="card-title">体重趋势</text>
      <text class="value">{{ weightTrend.from }} kg → {{ weightTrend.to }} kg</text>
      <text class="hint">趋势：{{ weightTrend.trend }}</text>
    </view>
    <text v-if="loading" class="status">加载中...</text>
    <text v-if="error" class="status error">{{ error }}</text>
  </view>
</template>

<script>
import { request } from "../../utils/api";

export default {
  data() {
    return {
      steps: [0, 0, 0, 0, 0, 0, 0],
      avgSteps: 0,
      sleepQuality: {
        excellent: 0,
        good: 0,
        normal: 0
      },
      weightTrend: {
        from: "--",
        to: "--",
        trend: "暂无"
      },
      loading: false,
      error: ""
    };
  },
  onLoad() {
    this.fetchWeekly();
  },
  methods: {
    fetchWeekly() {
      this.loading = true;
      this.error = "";
      const userId = uni.getStorageSync("userId") || 1;
      request("/api/reports/weekly", "GET", { userId })
        .then((data) => {
          if (Array.isArray(data.steps)) {
            this.steps = data.steps.map((item) => Number(item) || 0);
          }
          this.avgSteps = data.avgSteps || 0;
          this.sleepQuality = data.sleepQuality || this.sleepQuality;
          this.weightTrend = data.weightTrend || this.weightTrend;
        })
        .catch(() => {
          this.error = "获取统计失败";
        })
        .finally(() => {
          this.loading = false;
        });
    }
  }
};
</script>

<style>
.page {
  padding: 22px;
  min-height: 100vh;
  background: #f7f4ef;
  color: #2d2a26;
}

.header {
  margin-bottom: 14px;
}

.title {
  font-size: 18px;
  font-weight: 600;
}

.subtitle {
  display: block;
  color: #7c736b;
  margin-top: 4px;
}

.card {
  background: #ffffff;
  border-radius: 16px;
  padding: 14px 16px;
  margin-bottom: 12px;
  border: 1px solid #efe7dd;
  box-shadow: 0 8px 18px rgba(30, 25, 18, 0.05);
}

.card-title {
  font-weight: 600;
}

.chart {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8px;
  align-items: end;
  height: 120px;
  margin: 12px 0;
}

.bar {
  background: linear-gradient(180deg, #e8dccf, #d7c8b8);
  border-radius: 8px;
}

.chips {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

.chip {
  padding: 6px 12px;
  border-radius: 999px;
  background: #f3ece4;
  color: #6a5f55;
  font-size: 12px;
}

.value {
  font-weight: 600;
  margin-top: 8px;
  display: block;
}

.hint {
  color: #8d847c;
  font-size: 12px;
  margin-top: 6px;
  display: block;
}

.status {
  display: block;
  color: #7c736b;
  margin-top: 8px;
}

.status.error {
  color: #ef4444;
}
</style>
