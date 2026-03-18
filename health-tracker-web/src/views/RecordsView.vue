<template>
  <div class="records">
    <h2>健康记录</h2>
    <div class="tabs">
      <button 
        v-for="tab in tabs" 
        :key="tab.key" 
        :class="{ active: activeTab === tab.key }"
        @click="activeTab = tab.key"
      >
        {{ tab.name }}
      </button>
    </div>

    <div class="table-container">
      <table v-if="activeTab === 'exercise'">
        <thead>
          <tr>
            <th>类型</th>
            <th>时长</th>
            <th>热量</th>
            <th>日期</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in exerciseData" :key="item.key">
            <td>{{ item.type }}</td>
            <td>{{ item.duration }}</td>
            <td>{{ item.calories }} kcal</td>
            <td>{{ item.date }}</td>
          </tr>
        </tbody>
      </table>

      <table v-if="activeTab === 'diet'">
        <thead>
          <tr>
            <th>餐次</th>
            <th>食物</th>
            <th>热量</th>
            <th>日期</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in dietData" :key="item.key">
            <td>{{ item.mealType }}</td>
            <td>{{ item.foodName }}</td>
            <td>{{ item.calories }} kcal</td>
            <td>{{ item.date }}</td>
          </tr>
        </tbody>
      </table>

      <table v-if="activeTab === 'sleep'">
        <thead>
          <tr>
            <th>入睡时间</th>
            <th>起床时间</th>
            <th>深睡</th>
            <th>浅睡</th>
            <th>日期</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in sleepData" :key="item.key">
            <td>{{ item.startTime }}</td>
            <td>{{ item.endTime }}</td>
            <td>{{ item.deepSleep }}</td>
            <td>{{ item.lightSleep }}</td>
            <td>{{ item.date }}</td>
          </tr>
        </tbody>
      </table>

      <table v-if="activeTab === 'weight'">
        <thead>
          <tr>
            <th>体重</th>
            <th>BMI</th>
            <th>日期</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in weightData" :key="item.key">
            <td>{{ item.weight }}</td>
            <td>{{ item.bmi }}</td>
            <td>{{ item.date }}</td>
          </tr>
        </tbody>
      </table>

      <table v-if="activeTab === 'health'">
        <thead>
          <tr>
            <th>收缩压</th>
            <th>舒张压</th>
            <th>心率</th>
            <th>日期</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in healthData" :key="item.key">
            <td>{{ item.systolic }} mmHg</td>
            <td>{{ item.diastolic }} mmHg</td>
            <td>{{ item.heartRate }} bpm</td>
            <td>{{ item.date }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { mockData } from "../mock/data";

const activeTab = ref("exercise");

const tabs = [
  { key: "exercise", name: "运动" },
  { key: "diet", name: "饮食" },
  { key: "sleep", name: "睡眠" },
  { key: "weight", name: "体重" },
  { key: "health", name: "血压" }
];

const exerciseData = mockData.exerciseData;
const dietData = mockData.dietData;
const sleepData = mockData.sleepData;
const weightData = mockData.weightData;
const healthData = mockData.healthData;
</script>

<style scoped>
.records h2 {
  margin-bottom: 24px;
  color: #fff;
}

.tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
}

.tabs button {
  padding: 12px 24px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  color: #aaa;
  cursor: pointer;
  transition: all 0.3s;
}

.tabs button:hover {
  background: rgba(255, 255, 255, 0.15);
}

.tabs button.active {
  background: rgba(0, 217, 255, 0.2);
  color: #00d9ff;
  border-color: rgba(0, 217, 255, 0.3);
}

.table-container {
  background: rgba(255, 255, 255, 0.08);
  border-radius: 16px;
  padding: 20px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 14px 16px;
  text-align: left;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

th {
  color: #888;
  font-weight: 500;
  font-size: 0.85rem;
}

td {
  color: #ddd;
}

tr:hover td {
  background: rgba(255, 255, 255, 0.05);
}
</style>
