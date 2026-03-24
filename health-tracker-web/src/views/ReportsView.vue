<template>
  <div class="reports">
    <h2>健康报告</h2>
    
    <div class="summary-cards">
      <div class="summary-card">
        <div class="icon">👟</div>
        <div class="info">
          <span class="label">本周总步数</span>
          <span class="value">{{ summary.steps }}</span>
        </div>
      </div>
      <div class="summary-card">
        <div class="icon">😴</div>
        <div class="info">
          <span class="label">平均睡眠</span>
          <span class="value">{{ summary.avgSleep }}</span>
        </div>
      </div>
      <div class="summary-card">
        <div class="icon">🔥</div>
        <div class="info">
          <span class="label">平均热量</span>
          <span class="value">{{ summary.avgCalories }} kcal</span>
        </div>
      </div>
    </div>

    <div class="charts">
      <div class="chart-card">
        <h3>本周步数趋势</h3>
        <div class="bar-chart">
          <div 
            class="bar" 
            v-for="(value, index) in weeklySteps" 
            :key="index"
            :style="{ height: (value / 10000 * 100) + '%' }"
          >
            <span class="bar-value">{{ value.toLocaleString() }}</span>
          </div>
        </div>
        <div class="bar-labels">
          <span v-for="(day, index) in ['周一', '周二', '周三', '周四', '周五', '周六', '周日']" :key="index">{{ day }}</span>
        </div>
      </div>

      <div class="chart-card">
        <h3>睡眠质量</h3>
        <div class="bar-chart">
          <div 
            class="bar sleep" 
            v-for="(value, index) in weeklySleep" 
            :key="index"
            :style="{ height: (value / 10 * 100) + '%' }"
          >
            <span class="bar-value">{{ value }}h</span>
          </div>
        </div>
        <div class="bar-labels">
          <span v-for="(day, index) in ['周一', '周二', '周三', '周四', '周五', '周六', '周日']" :key="index">{{ day }}</span>
        </div>
      </div>
    </div>

    <div class="analysis">
      <h3>健康分析</h3>
      <div class="analysis-grid">
        <div class="analysis-item">
          <span class="analysis-label">运动活跃度</span>
          <span class="analysis-value good">良好</span>
          <span class="analysis-desc">本周运动量达标，继续保持</span>
        </div>
        <div class="analysis-item">
          <span class="analysis-label">睡眠质量</span>
          <span class="analysis-value good">良好</span>
          <span class="analysis-desc">睡眠时长和深度睡眠比例正常</span>
        </div>
        <div class="analysis-item">
          <span class="analysis-label">体重趋势</span>
          <span class="analysis-value normal">正常</span>
          <span class="analysis-desc">体重在健康范围内波动</span>
        </div>
        <div class="analysis-item">
          <span class="analysis-label">心血管健康</span>
          <span class="analysis-value good">优秀</span>
          <span class="analysis-desc">血压和心率保持稳定</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { getStatisticsOverview, getStatisticsTrend, getUserId } from "../api";

const weeklySteps = ref([]);
const weeklySleep = ref([]);
const summary = ref({
  steps: "0",
  avgSleep: "0小时",
  avgCalories: 0,
  avgHeartRate: "--"
});

function average(values) {
  if (!values || values.length === 0) return 0;
  const total = values.reduce((acc, item) => acc + (Number(item) || 0), 0);
  return total / values.length;
}

function toNumber(value) {
  const num = Number(value);
  return Number.isFinite(num) ? num : 0;
}

async function loadReport() {
  const userId = getUserId();
  try {
    const [overview, trend] = await Promise.all([
      getStatisticsOverview(userId, "week"),
      getStatisticsTrend(userId, "week")
    ]);

    const stepsSeries = trend?.series?.steps || [];
    const sleepSeries = trend?.series?.sleep || [];
    const dietSeries = trend?.series?.diet || [];

    weeklySteps.value = stepsSeries.map(item => toNumber(item.value));
    weeklySleep.value = sleepSeries.map(item => toNumber(item.value));

    const avgSleepHours = average(weeklySleep.value);
    const avgCalories = Math.round(average(dietSeries.map(item => toNumber(item.value))));

    summary.value = {
      steps: overview?.steps ? Number(overview.steps).toLocaleString() : "0",
      avgSleep: avgSleepHours ? `${avgSleepHours.toFixed(1)}小时` : "0小时",
      avgCalories: avgCalories || 0
    };
  } catch (error) {
    console.error("加载健康报告失败", error);
  }
}

onMounted(loadReport);
</script>

<style scoped>
.reports h2 {
  margin-bottom: 32px;
  color: #fff;
}

.summary-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 32px;
}

.summary-card {
  background: rgba(255, 255, 255, 0.08);
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.summary-card .icon {
  font-size: 2rem;
}

.summary-card .info {
  display: grid;
  gap: 4px;
}

.summary-card .label {
  font-size: 0.85rem;
  color: #888;
}

.summary-card .value {
  font-size: 1.4rem;
  font-weight: 600;
  color: #00d9ff;
}

.charts {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
  margin-bottom: 32px;
}

.chart-card {
  background: rgba(255, 255, 255, 0.08);
  border-radius: 20px;
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.chart-card h3 {
  margin-bottom: 24px;
  color: #fff;
}

.bar-chart {
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  height: 180px;
  padding-bottom: 30px;
}

.bar {
  width: 40px;
  background: linear-gradient(180deg, #00d9ff, #00ff88);
  border-radius: 6px 6px 0 0;
  position: relative;
  min-height: 20px;
  transition: height 0.5s ease;
}

.bar.sleep {
  background: linear-gradient(180deg, #8b5cf6, #6366f1);
}

.bar-value {
  position: absolute;
  top: -25px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 0.75rem;
  color: #aaa;
  white-space: nowrap;
}

.bar-labels {
  display: flex;
  justify-content: space-around;
  color: #666;
  font-size: 0.8rem;
  margin-top: 10px;
}

.analysis {
  background: rgba(255, 255, 255, 0.08);
  border-radius: 20px;
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.analysis h3 {
  margin-bottom: 20px;
  color: #fff;
}

.analysis-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.analysis-item {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 12px;
  padding: 16px;
  display: grid;
  gap: 8px;
}

.analysis-label {
  color: #888;
  font-size: 0.85rem;
}

.analysis-value {
  font-size: 1.1rem;
  font-weight: 600;
}

.analysis-value.good {
  color: #10b981;
}

.analysis-value.normal {
  color: #3b82f6;
}

.analysis-desc {
  font-size: 0.8rem;
  color: #666;
}

@media (max-width: 1100px) {
  .summary-cards, .charts, .analysis-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .summary-cards, .charts, .analysis-grid {
    grid-template-columns: 1fr;
  }
}
</style>
