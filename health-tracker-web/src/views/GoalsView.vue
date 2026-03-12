<template>
  <div class="goals">
    <a-card title="本周目标" class="panel">
      <div class="goal" v-for="goal in goals" :key="goal.id">
        <div class="goal__info">
          <strong>{{ goalTypeLabel(goal.goalType) }}</strong>
          <span>{{ goal.currentValue }} / {{ goal.targetValue }}</span>
        </div>
        <a-progress :percent="goal.percent" />
      </div>
    </a-card>

    <a-card title="创建新目标" class="panel">
      <a-form layout="vertical">
        <a-form-item label="目标类型">
          <a-select v-model:value="form.goalType" placeholder="选择目标">
            <a-select-option value="steps">步数</a-select-option>
            <a-select-option value="sleep">睡眠</a-select-option>
            <a-select-option value="diet">饮食</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="目标值">
          <a-input v-model:value="form.targetValue" placeholder="例如 10000" />
        </a-form-item>
        <a-form-item label="周期">
          <a-select v-model:value="form.period" placeholder="请选择周期">
            <a-select-option value="daily">每日</a-select-option>
            <a-select-option value="weekly">每周</a-select-option>
          </a-select>
        </a-form-item>
        <a-button type="primary" :loading="loading" @click="saveGoal">保存目标</a-button>
        <div class="status-row" v-if="message">{{ message }}</div>
      </a-form>
    </a-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { api } from "../api/http";

const goals = ref([]);
const form = reactive({
  goalType: "",
  targetValue: "",
  period: ""
});
const loading = ref(false);
const message = ref("");

function normalize(list) {
  return list.map((item) => {
    const percent = item.targetValue
      ? Math.min(100, Math.round((item.currentValue / item.targetValue) * 100))
      : 0;
    return { ...item, percent };
  });
}

function goalTypeLabel(type) {
  if (type === "steps") return "步数";
  if (type === "sleep") return "睡眠";
  if (type === "diet") return "饮食";
  return type || "目标";
}

async function loadGoals() {
  const userId = localStorage.getItem("userId");
  if (!userId) return;
  try {
    const data = await api.goalList(userId);
    goals.value = normalize(data || []);
  } catch (err) {
    message.value = err.message || "获取目标失败";
  }
}

async function saveGoal() {
  loading.value = true;
  message.value = "";
  const userId = localStorage.getItem("userId");
  if (!userId) {
    message.value = "请先登录";
    loading.value = false;
    return;
  }
  try {
    await api.goalAdd({
      userId: Number(userId),
      goalType: form.goalType,
      targetValue: Number(form.targetValue),
      period: form.period
    });
    form.goalType = "";
    form.targetValue = "";
    form.period = "";
    await loadGoals();
  } catch (err) {
    message.value = err.message || "保存失败";
  } finally {
    loading.value = false;
  }
}

onMounted(loadGoals);
</script>

<style scoped>
.goals {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 18px;
}

.panel {
  border-radius: 16px;
}

.goal {
  margin-bottom: 16px;
}

.goal__info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

@media (max-width: 1000px) {
  .goals {
    grid-template-columns: 1fr;
  }
}
</style>
