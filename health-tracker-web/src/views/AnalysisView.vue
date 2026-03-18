<template>
  <div class="analysis">
    <div class="page-header">
      <h1 class="page-title">数据分析</h1>
      <div class="period-selector">
        <button 
          v-for="p in periods" 
          :key="p.value"
          :class="{ active: period === p.value }"
          @click="period = p.value"
        >
          {{ p.label }}
        </button>
      </div>
    </div>

    <!-- 步数趋势 -->
    <div class="chart-card" id="chart-steps">
      <h3>步数趋势</h3>
      <div class="bar-chart">
        <div class="bar" v-for="item in trend.steps" :key="item.date">
          <div class="bar-fill" :style="{ height: (item.value / maxSteps * 100) + '%' }"></div>
          <span class="bar-label">{{ item.date }}</span>
          <span class="bar-value">{{ item.value.toLocaleString() }}</span>
        </div>
      </div>
    </div>

    <!-- 睡眠趋势 -->
    <div class="chart-card" id="chart-sleep">
      <h3>睡眠时长</h3>
      <div class="bar-chart">
        <div class="bar" v-for="item in trend.sleep" :key="item.date">
          <div class="bar-fill sleep" :style="{ height: (item.value / 10 * 100) + '%' }"></div>
          <span class="bar-label">{{ item.date }}</span>
          <span class="bar-value">{{ item.value }}h</span>
        </div>
      </div>
    </div>

    <!-- 饮食 -->
    <div class="chart-card" id="chart-diet">
      <h3>饮食热量（堆叠柱状图）</h3>
      <div class="stacked-chart">
        <div class="stacked-bar" v-for="item in trend.diet" :key="item.date">
          <div class="segment breakfast" :style="{ height: item.breakfast / (item.breakfast + item.lunch + item.dinner + item.snack) * 100 + '%' }"></div>
          <div class="segment lunch" :style="{ height: item.lunch / (item.breakfast + item.lunch + item.dinner + item.snack) * 100 + '%' }"></div>
          <div class="segment dinner" :style="{ height: item.dinner / (item.breakfast + item.lunch + item.dinner + item.snack) * 100 + '%' }"></div>
          <div class="segment snack" :style="{ height: item.snack / (item.breakfast + item.lunch + item.dinner + item.snack) * 100 + '%' }"></div>
          <span class="bar-label">{{ item.date }}</span>
        </div>
      </div>
      <div class="chart-legend">
        <span class="legend-item"><i class="dot breakfast"></i>早餐</span>
        <span class="legend-item"><i class="dot lunch"></i>午餐</span>
        <span class="legend-item"><i class="dot dinner"></i>晚餐</span>
        <span class="legend-item"><i class="dot snack"></i>加餐</span>
      </div>
    </div>

    <!-- 体重趋势 -->
    <div class="chart-card" id="chart-weight">
      <h3>体重趋势</h3>
      <div class="line-chart">
        <svg viewBox="0 0 700 200" class="chart-svg">
          <polyline 
            :points="weightPoints" 
            fill="none" 
            stroke="#ff7a45" 
            stroke-width="3"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
          <circle 
            v-for="(item, index) in trend.weight" 
            :key="index"
            :cx="index * 100 + 50" 
            :cy="180 - (item.value - 60) * 40" 
            r="5" 
            fill="#ff7a45"
          />
        </svg>
        <div class="line-labels">
          <span v-for="item in trend.weight" :key="item.date">{{ item.date }}</span>
        </div>
      </div>
    </div>

    <!-- 经期热力图 -->
    <div class="chart-card">
      <h3>经期记录</h3>
      <div class="heatmap">
        <div class="heatmap-row">
          <span 
            v-for="day in 31" 
            :key="day" 
            class="heatmap-cell"
            :class="getMenstrualClass(day)"
          ></span>
        </div>
        <div class="heatmap-legend">
          <span><i class="dot heavy"></i>经期</span>
          <span><i class="dot light"></i>轻微</span>
          <span><i class="dot none"></i>无</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from "vue";
import { useRoute } from "vue-router";
import { getUserId, getStatisticsTrend } from "../api";

const route = useRoute();
const period = ref("week");
const periods = [
  { label: "周", value: "week" },
  { label: "月", value: "month" },
  { label: "半年", value: "half" }
];

// 从首页跳转过来的类型
const activeType = ref("");

onMounted(async () => {
  // 检查URL参数，跳转到对应图表
  const type = route.query.type;
  if (type) {
    activeType.value = type;
    await nextTick();
    const chartMap = {
      steps: "chart-steps",
      sleep: "chart-sleep", 
      weight: "chart-weight",
      diet: "chart-diet"
    };
    const elementId = chartMap[type];
    if (elementId) {
      document.getElementById(elementId)?.scrollIntoView({ behavior: "smooth", block: "start" });
    }
  }
  
  loadData();
});

const trend = ref({
  steps: [],
  sleep: [],
  sleepDeep: [],
  sleepLight: [],
  diet: [],
  dietBreakfast: [],
  dietLunch: [],
  dietDinner: [],
  dietSnack: [],
  weight: [],
  menstrual: []
});

const maxSteps = computed(() => {
  const values = trend.value.steps.map(s => Number(s.value || 0));
  return Math.max(1, ...values);
});

const weightPoints = computed(() => {
  const values = trend.value.weight.map(item => Number(item.value || 0)).filter(v => v > 0);
  const minWeight = values.length ? Math.min(...values) : 60;
  const maxWeight = values.length ? Math.max(...values) : 75;
  const range = maxWeight - minWeight || 1;
  return trend.value.weight
    .map((item, index) => {
      const x = index * 100 + 50;
      const y = 180 - ((Number(item.value || 0) - minWeight) / range) * 160;
      return `${x},${y}`;
    })
    .join(" ");
});

function getMenstrualClass(day) {
  const record = trend.value.menstrual[day - 1];
  if (!record) return "";
  return Number(record.value || 0) >= 2 ? "heavy" : Number(record.value || 0) > 0 ? "light" : "";
}

const rebuildTrend = (data) => {
  if (!data || !data.series) return;
  const series = data.series;
  trend.value.steps = (series.steps || []).map(item => ({ date: item.label, value: Number(item.value || 0) }));
  trend.value.sleep = (series.sleep || []).map(item => ({ date: item.label, value: Number(item.value || 0) }));
  trend.value.sleepDeep = (series.sleepDeep || []).map(item => ({ date: item.label, value: Number(item.value || 0) }));
  trend.value.sleepLight = (series.sleepLight || []).map(item => ({ date: item.label, value: Number(item.value || 0) }));
  trend.value.weight = (series.weight || []).map(item => ({ date: item.label, value: Number(item.value || 0) }));
  trend.value.menstrual = (series.period || []).map(item => ({ date: item.label, value: Number(item.value || 0) }));
  const breakfast = series.dietBreakfast || [];
  const lunch = series.dietLunch || [];
  const dinner = series.dietDinner || [];
  const snack = series.dietSnack || [];
  trend.value.diet = breakfast.map((item, index) => ({
    date: item.label,
    breakfast: Number(item.value || 0),
    lunch: Number(lunch[index]?.value || 0),
    dinner: Number(dinner[index]?.value || 0),
    snack: Number(snack[index]?.value || 0)
  }));
};

const loadTrend = async () => {
  const userId = getUserId();
  try {
    const data = await getStatisticsTrend(userId, period.value);
    rebuildTrend(data);
  } catch (e) {
    console.log("趋势数据加载失败", e);
  }
};

watch(period, () => {
  loadTrend();
});

onMounted(loadTrend);
</script>

<style scoped>
.analysis {
  max-width: 1200px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.period-selector {
  display: flex;
  gap: 8px;
}

.period-selector button {
  padding: 8px 20px;
  border: 1px solid #d9d9d9;
  background: #fff;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.period-selector button:hover {
  border-color: #ff7a45;
  color: #ff7a45;
}

.period-selector button.active {
  background: #ff7a45;
  border-color: #ff7a45;
  color: #fff;
}

.chart-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.chart-card h3 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 24px;
}

/* 柱状图 */
.bar-chart {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  height: 220px;
  padding: 0 20px;
}

.bar {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  height: 100%;
}

.bar-fill {
  width: 40px;
  background: linear-gradient(180deg, #ff7a45, #ffa070);
  border-radius: 4px 4px 0 0;
  margin-top: auto;
  transition: height 0.3s;
}

.bar-fill.sleep {
  background: linear-gradient(180deg, #722ed1, #b37feb);
}

.bar-label {
  margin-top: 10px;
  font-size: 12px;
  color: #999;
}

.bar-value {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
}

/* 堆叠柱状图 */
.stacked-chart {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  height: 200px;
  padding: 0 20px;
}

.stacked-bar {
  display: flex;
  flex-direction: column;
  flex: 1;
  height: 100%;
  justify-content: flex-end;
}

.segment {
  width: 36px;
  transition: height 0.3s;
}

.segment.breakfast { background: #ff7a45; }
.segment.lunch { background: #52c41a; }
.segment.dinner { background: #faad14; }
.segment.snack { background: #ff7875; }

.chart-legend {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-top: 16px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #666;
}

.dot {
  width: 12px;
  height: 12px;
  border-radius: 3px;
}

.dot.breakfast { background: #ff7a45; }
.dot.lunch { background: #52c41a; }
.dot.dinner { background: #faad14; }
.dot.snack { background: #ff7875; }

/* 折线图 */
.line-chart {
  padding: 0 20px;
}

.chart-svg {
  width: 100%;
  height: 200px;
}

.line-labels {
  display: flex;
  justify-content: space-between;
  padding: 0 30px;
  font-size: 12px;
  color: #999;
}

/* 热力图 */
.heatmap {
  padding: 0 20px;
}

.heatmap-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.heatmap-cell {
  width: 28px;
  height: 28px;
  border-radius: 4px;
  background: #f0f0f0;
}

.heatmap-cell.heavy {
  background: #ff4d4f;
}

.heatmap-cell.light {
  background: #ffccc7;
}

.heatmap-legend {
  display: flex;
  gap: 20px;
  margin-top: 16px;
  font-size: 13px;
  color: #666;
}

.heatmap-legend .dot {
  display: inline-block;
  margin-right: 6px;
}

.dot.heavy { background: #ff4d4f; }
.dot.light { background: #ffccc7; }
.dot.none { background: #f0f0f0; }
</style>
