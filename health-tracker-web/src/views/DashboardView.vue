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
      <a-card class="hero__side" bordered>
        <div class="hero__side-title">运动结构</div>
        <div v-if="activityMix.length === 0" class="empty">暂无运动数据</div>
        <div class="meter" v-else v-for="item in activityMix" :key="item.label">
          <span>{{ item.label }}</span>
          <a-progress :percent="item.value" :show-info="false" :status="item.status" />
        </div>
      </a-card>
    </section>

    <section class="grid">
      <a-card class="panel" title="快速记录">
        <div class="quick">
          <a-button disabled>运动</a-button>
          <a-button disabled>饮食</a-button>
          <a-button disabled>睡眠</a-button>
          <a-button disabled>体重</a-button>
          <a-button disabled>用药</a-button>
        </div>
        <div class="quick-hint">快速记录暂不可用，请到“记录”页面维护。</div>
      </a-card>
      <a-card class="panel" title="目标">
        <div v-if="goalItems.length === 0" class="empty">暂无目标</div>
        <div class="goal" v-for="item in goalItems" :key="item.label">
          <div class="goal__info">
            <strong>{{ item.label }}</strong>
            <span>{{ item.value }}</span>
          </div>
          <a-progress :percent="item.percent" />
        </div>
      </a-card>
      <a-card class="panel" title="提醒">
        <div v-if="reminders.length === 0" class="empty">暂无提醒</div>
        <div class="tags">
          <a-tag v-for="item in reminders" :key="item.label" :color="item.color">
            {{ item.label }}
          </a-tag>
        </div>
      </a-card>
    </section>
    <div class="status-row" v-if="loading">加载中...</div>
    <div class="status-row" v-if="message">{{ message }}</div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { api } from "../api/http";

const overview = reactive({
  steps: "--",
  stepsHint: "暂无目标",
  sleep: "--",
  sleepHint: "暂无记录",
  calories: "--",
  caloriesHint: "暂无记录"
});

const activityMix = ref([]);

const reminders = ref([]);
const goalItems = ref([]);
const loading = ref(false);
const message = ref("");

onMounted(async () => {
  const userId = localStorage.getItem("userId");
  if (!userId) return;
  loading.value = true;
  message.value = "";
  try {
    const today = new Date().toISOString().slice(0, 10);
    const [data, goals, meds, exercises] = await Promise.all([
      api.dashboard(userId),
      api.goalList(userId),
      api.medicationList(userId),
      api.exerciseList(userId, today)
    ]);
    if (data?.steps) overview.steps = data.steps;
    if (data?.sleep) overview.sleep = data.sleep;
    if (data?.calories) overview.calories = data.calories;
    overview.sleepHint = data?.sleep && data.sleep !== "0小时0分" ? "已记录" : "暂无记录";
    overview.caloriesHint = data?.calories && data.calories !== "0" ? "饮食 + 运动累计" : "暂无记录";
    if (data?.steps && Array.isArray(goals)) {
      const stepGoal = goals.find((item) => item.goalType === "步数" || item.goalType === "steps");
      if (stepGoal) {
        const pct = stepGoal.targetValue
          ? Math.round((stepGoal.currentValue / stepGoal.targetValue) * 100)
          : 0;
        overview.stepsHint = `完成 ${pct}%`;
      }
    }
    goalItems.value = Array.isArray(goals)
      ? goals.map((item) => {
          const percent = item.targetValue
            ? Math.round((item.currentValue / item.targetValue) * 100)
            : 0;
          return {
            label: item.goalType,
            value: `${item.currentValue} / ${item.targetValue}`,
            percent
          };
        })
      : [];
    reminders.value = Array.isArray(meds)
      ? meds.map((item) => ({
          label: `${item.drugName || "用药"} ${item.remindTime || item.frequency || ""}`.trim(),
          color: "green"
        }))
      : [];
    if (Array.isArray(exercises) && exercises.length > 0) {
      const totals = new Map();
      let sum = 0;
      exercises.forEach((item) => {
        const key = item.type || "其他";
        const value = Number(item.duration || 0);
        totals.set(key, (totals.get(key) || 0) + value);
        sum += value;
      });
      activityMix.value = Array.from(totals.entries()).map(([label, value]) => ({
        label,
        value: sum > 0 ? Math.round((value / sum) * 100) : 0,
        status: "normal"
      }));
    } else {
      activityMix.value = [];
    }
  } catch (err) {
    message.value = err.message || "获取概览失败";
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped>
.dashboard {
  display: grid;
  gap: 24px;
}

.hero {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 18px;
}

.hero__card {
  background: linear-gradient(135deg, rgba(56, 189, 248, 0.2), rgba(255, 255, 255, 0.85));
  border-radius: 18px;
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(12px);
}

.hero__subtitle {
  color: #64748b;
  margin-top: 6px;
}

.hero__stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin-top: 20px;
}

.stat {
  background: rgba(255, 255, 255, 0.85);
  border-radius: 14px;
  padding: 14px;
  display: grid;
  gap: 6px;
  border: 1px solid rgba(255, 255, 255, 0.6);
}

.stat__label {
  color: #64748b;
  font-size: 0.85rem;
}

.stat__value {
  font-size: 1.4rem;
  font-weight: 600;
}

.stat__hint {
  color: #10b981;
  font-size: 0.8rem;
}

.hero__side {
  border-radius: 18px;
}

.hero__side-title {
  font-weight: 600;
  margin-bottom: 12px;
}

.meter {
  display: grid;
  gap: 6px;
  margin-bottom: 12px;
  color: #475569;
}

.grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.panel {
  border-radius: 16px;
}

.quick {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.quick-hint {
  margin-top: 10px;
  color: #94a3b8;
  font-size: 12px;
}

.goal {
  margin-bottom: 16px;
}

.goal__info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.empty {
  color: #94a3b8;
  font-size: 0.9rem;
}

@media (max-width: 1100px) {
  .hero {
    grid-template-columns: 1fr;
  }

  .hero__stats {
    grid-template-columns: 1fr;
  }

  .grid {
    grid-template-columns: 1fr;
  }
}
</style>
