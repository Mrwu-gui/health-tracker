<template>
  <div class="records">
    <a-card title="每日记录" class="panel">
      <a-tabs default-active-key="exercise">
        <a-tab-pane key="exercise" tab="运动">
          <a-table :columns="exerciseColumns" :data-source="exerciseData" size="small" />
        </a-tab-pane>
        <a-tab-pane key="diet" tab="饮食">
          <a-table :columns="dietColumns" :data-source="dietData" size="small" />
        </a-tab-pane>
        <a-tab-pane key="sleep" tab="睡眠">
          <a-table :columns="sleepColumns" :data-source="sleepData" size="small" />
        </a-tab-pane>
        <a-tab-pane key="weight" tab="体重">
          <a-table :columns="weightColumns" :data-source="weightData" size="small" />
        </a-tab-pane>
        <a-tab-pane key="health" tab="血压">
          <a-table :columns="healthColumns" :data-source="healthData" size="small" />
        </a-tab-pane>
      </a-tabs>
      <div class="status-row" v-if="loading">加载中...</div>
      <div class="status-row" v-if="message">{{ message }}</div>
    </a-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { api } from "../api/http";

const exerciseColumns = [
  { title: "类型", dataIndex: "type" },
  { title: "时长", dataIndex: "duration" },
  { title: "热量", dataIndex: "calories" },
  { title: "日期", dataIndex: "date" }
];
const dietColumns = [
  { title: "餐次", dataIndex: "mealType" },
  { title: "食物", dataIndex: "foodName" },
  { title: "热量", dataIndex: "calories" },
  { title: "日期", dataIndex: "date" }
];
const sleepColumns = [
  { title: "开始", dataIndex: "startTime" },
  { title: "结束", dataIndex: "endTime" },
  { title: "深睡", dataIndex: "deepSleepMinutes" },
  { title: "浅睡", dataIndex: "lightSleepMinutes" }
];
const weightColumns = [
  { title: "体重", dataIndex: "weight" },
  { title: "体脂指数", dataIndex: "bmi" },
  { title: "日期", dataIndex: "date" }
];
const healthColumns = [
  { title: "收缩压", dataIndex: "systolic" },
  { title: "舒张压", dataIndex: "diastolic" },
  { title: "心率", dataIndex: "heartRate" },
  { title: "日期", dataIndex: "date" }
];

const exerciseData = ref([]);
const dietData = ref([]);
const sleepData = ref([]);
const weightData = ref([]);
const healthData = ref([]);
const message = ref("");
const loading = ref(false);

onMounted(async () => {
  const userId = localStorage.getItem("userId");
  if (!userId) return;
  const date = new Date().toISOString().slice(0, 10);
  loading.value = true;
  message.value = "";
  try {
    exerciseData.value = await api.exerciseList(userId, date);
    dietData.value = await api.dietList(userId, date);
    sleepData.value = await api.sleepList(userId, date);
    weightData.value = await api.weightList(userId, date);
    healthData.value = await api.healthList(userId, date);
  } catch (err) {
    message.value = err.message || "获取记录失败";
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped>
.records {
  display: grid;
  gap: 16px;
}

.panel {
  border-radius: 16px;
}
</style>
