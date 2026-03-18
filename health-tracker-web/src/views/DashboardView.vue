<template>
  <div class="dashboard">
    <section class="hero">
      <div class="hero__card">
        <h2>今日概览</h2>
        <p class="hero__subtitle">保持稳定的节奏与状态。</p>
        <div class="hero__stats">
          <div class="stat">
            <span class="stat__label">步数</span>
            <span class="stat__value">{{ overview.steps }}</span>
            <span class="stat__hint">{{ overview.stepsHint }}</span>
          </div>
          <div class="stat">
            <span class="stat__label">睡眠</span>
            <span class="stat__value">{{ overview.sleep }}</span>
            <span class="stat__hint">{{ overview.sleepHint }}</span>
          </div>
          <div class="stat">
            <span class="stat__label">热量</span>
            <span class="stat__value">{{ overview.calories }}</span>
            <span class="stat__hint">{{ overview.caloriesHint }}</span>
          </div>
        </div>
      </div>
      <div class="hero__side">
        <h3>运动结构</h3>
        <div class="meter" v-for="item in activityMix" :key="item.label">
          <span>{{ item.label }}</span>
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: item.value + '%' }"></div>
          </div>
          <span class="progress-text">{{ item.value }}%</span>
        </div>
      </div>
    </section>

    <section class="grid">
      <div class="panel">
        <h3>目标进度</h3>
        <div class="goal" v-for="item in goals" :key="item.label">
          <div class="goal__info">
            <strong>{{ item.label }}</strong>
            <span>{{ item.value }}</span>
          </div>
          <div class="progress-bar large">
            <div class="progress-fill" :style="{ width: item.percent + '%' }"></div>
          </div>
        </div>
      </div>

      <div class="panel">
        <h3>今日提醒</h3>
        <div class="tags">
          <span class="tag" v-for="item in reminders" :key="item.label" :class="item.color">
            {{ item.label }}
          </span>
        </div>
      </div>

      <div class="panel">
        <h3>健康趋势</h3>
        <div class="trend">
          <div class="trend-item">
            <span class="trend-label">本周平均步数</span>
            <span class="trend-value">{{ trendStats.avgSteps }}</span>
          </div>
          <div class="trend-item">
            <span class="trend-label">本周平均睡眠</span>
            <span class="trend-value">{{ trendStats.avgSleep }}小时</span>
          </div>
          <div class="trend-item">
            <span class="trend-label">本周平均热量</span>
            <span class="trend-value">{{ trendStats.avgDiet }} kcal</span>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { getUserId, getStatisticsOverview, getStatisticsTrend, getReminderList, getGoalList } from "../api";

const overview = ref({
  steps: 0,
  sleep: "0小时0分",
  calories: 0,
  stepsHint: "暂无数据",
  sleepHint: "暂无数据",
  caloriesHint: "暂无数据"
});
const activityMix = ref([
  { label: "步数", value: 0 },
  { label: "睡眠", value: 0 },
  { label: "饮食", value: 0 }
]);
const trendStats = ref({
  avgSteps: 0,
  avgSleep: 0,
  avgDiet: 0
});
const goals = ref([]);
const reminders = ref([]);

const formatDelta = (value, unit) => {
  if (value === 0 || value === null || value === undefined) return `与昨日持平`;
  const sign = value > 0 ? "↑" : "↓";
  const abs = Math.abs(value);
  return `${sign} ${abs}${unit}`;
};

const goalLabel = (goal) => {
  const map = {
    1: "步数目标",
    2: "睡眠目标",
    3: "热量目标",
    4: "体重目标"
  };
  return map[goal.goalType] || `目标 ${goal.goalType || ""}`.trim();
};

const loadDashboard = async () => {
  const userId = getUserId();
  try {
    const [overviewData, reminderData, goalData, trendData] = await Promise.all([
      getStatisticsOverview(userId, "day"),
      getReminderList(userId),
      getGoalList(userId, "day"),
      getStatisticsTrend(userId, "week")
    ]);

    if (overviewData) {
      overview.value.steps = Number(overviewData.stepsToday || overviewData.steps || 0);
      overview.value.sleep = overviewData.sleep || "0小时0分";
      overview.value.calories = Number(overviewData.dietCaloriesToday || overviewData.dietCalories || 0);
      overview.value.stepsHint = formatDelta(overviewData.stepsDelta || 0, "步");
      overview.value.sleepHint = formatDelta(overviewData.sleepDeltaMinutes || 0, "分");
      overview.value.caloriesHint = formatDelta(overviewData.dietDeltaCalories || 0, "kcal");

      const stepsValue = Number(overviewData.stepsToday || 0);
      const sleepValue = Number(overviewData.sleepMinutesToday || 0);
      const dietValue = Number(overviewData.dietCaloriesToday || 0);
      const total = stepsValue + sleepValue + dietValue;
      activityMix.value = [
        { label: "步数", value: total ? Math.round((stepsValue / total) * 100) : 0 },
        { label: "睡眠", value: total ? Math.round((sleepValue / total) * 100) : 0 },
        { label: "饮食", value: total ? Math.round((dietValue / total) * 100) : 0 }
      ];
    }

    if (Array.isArray(reminderData)) {
      reminders.value = reminderData.slice(0, 6).map((item) => ({
        label: item.title || item.content || item.remindTime || "提醒",
        color: item.status === 1 ? "green" : "orange"
      }));
    }

    if (Array.isArray(goalData)) {
      goals.value = goalData.map((item) => {
        const target = Number(item.targetValue || 0);
        const current = Number(item.currentValue || 0);
        const percent = target > 0 ? Math.min(100, Math.round((current / target) * 100)) : 0;
        return {
          label: goalLabel(item),
          value: target ? `${current}/${target}` : `${current}`,
          percent
        };
      });
    }

    if (trendData && trendData.series) {
      const stepsSeries = trendData.series.steps || [];
      const sleepSeries = trendData.series.sleep || [];
      const dietSeries = trendData.series.diet || [];
      const avgSteps = stepsSeries.length
        ? Math.round(stepsSeries.reduce((sum, item) => sum + Number(item.value || 0), 0) / stepsSeries.length)
        : 0;
      const avgSleep = sleepSeries.length
        ? (sleepSeries.reduce((sum, item) => sum + Number(item.value || 0), 0) / sleepSeries.length).toFixed(1)
        : 0;
      const avgDiet = dietSeries.length
        ? Math.round(dietSeries.reduce((sum, item) => sum + Number(item.value || 0), 0) / dietSeries.length)
        : 0;
      trendStats.value = {
        avgSteps,
        avgSleep,
        avgDiet
      };
    }
  } catch (e) {
    console.warn("Dashboard 使用默认数据", e);
  }
};

onMounted(loadDashboard);
</script>

<style scoped>
.dashboard {
  display: grid;
  gap: 24px;
}

.hero {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
}

.hero__card {
  background: linear-gradient(135deg, rgba(0, 217, 255, 0.15), rgba(255, 255, 255, 0.1));
  border-radius: 20px;
  padding: 32px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}

.hero__card h2 {
  margin-bottom: 8px;
  color: #fff;
}

.hero__subtitle {
  color: #888;
  margin-bottom: 24px;
}

.hero__stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.stat {
  background: rgba(255, 255, 255, 0.08);
  border-radius: 16px;
  padding: 20px;
  display: grid;
  gap: 8px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.stat__label {
  color: #888;
  font-size: 0.9rem;
}

.stat__value {
  font-size: 1.8rem;
  font-weight: 600;
  color: #00d9ff;
}

.stat__hint {
  color: #10b981;
  font-size: 0.85rem;
}

.hero__side {
  background: rgba(255, 255, 255, 0.08);
  border-radius: 20px;
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.hero__side h3 {
  margin-bottom: 20px;
  color: #fff;
}

.meter {
  display: grid;
  grid-template-columns: 60px 1fr 40px;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
  color: #aaa;
}

.progress-bar {
  height: 8px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  overflow: hidden;
}

.progress-bar.large {
  height: 12px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #00d9ff, #00ff88);
  border-radius: 4px;
  transition: width 0.5s ease;
}

.progress-text {
  color: #00d9ff;
  font-size: 0.85rem;
  text-align: right;
}

.grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.panel {
  background: rgba(255, 255, 255, 0.08);
  border-radius: 20px;
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.panel h3 {
  margin-bottom: 20px;
  color: #fff;
}

.goal {
  margin-bottom: 20px;
}

.goal__info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  color: #ccc;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.tag {
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 0.85rem;
  background: rgba(255, 255, 255, 0.1);
}

.tag.green { border-left: 3px solid #10b981; }
.tag.blue { border-left: 3px solid #3b82f6; }
.tag.orange { border-left: 3px solid #f59e0b; }
.tag.purple { border-left: 3px solid #8b5cf6; }

.trend {
  display: grid;
  gap: 16px;
}

.trend-item {
  display: flex;
  justify-content: space-between;
  padding: 12px;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 10px;
}

.trend-label {
  color: #888;
}

.trend-value {
  color: #00d9ff;
  font-weight: 600;
}

@media (max-width: 1100px) {
  .hero, .grid {
    grid-template-columns: 1fr;
  }
  
  .hero__stats {
    grid-template-columns: 1fr;
  }
}
</style>
