<template>
  <div class="goals">
    <h2>健康目标</h2>
    <div class="goals-grid">
      <div class="goal-card" v-for="item in goals" :key="item.label">
        <div class="goal-header">
          <span class="goal-title">{{ item.label }}</span>
          <span class="goal-value">{{ item.value }}</span>
        </div>
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: item.percent + '%' }"></div>
        </div>
        <div class="goal-status">
          <span>完成度</span>
          <span :class="item.percent >= 100 ? 'complete' : 'pending'">
            {{ item.percent }}%
          </span>
        </div>
      </div>
    </div>

    <div class="tips">
      <h3>健康小贴士</h3>
      <ul>
        <li>建议每天保持 8,000-10,000 步的运动量</li>
        <li>成年人每天需要 7-9 小时的睡眠</li>
        <li>保持规律的运动习惯对健康至关重要</li>
        <li>合理的饮食搭配有助于维持健康体重</li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { mockData } from "../mock/data";

const goals = mockData.goals;
</script>

<style scoped>
.goals h2 {
  margin-bottom: 32px;
  color: #fff;
}

.goals-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
  margin-bottom: 40px;
}

.goal-card {
  background: rgba(255, 255, 255, 0.08);
  border-radius: 20px;
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.goal-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
}

.goal-title {
  font-size: 1.1rem;
  color: #fff;
}

.goal-value {
  color: #00d9ff;
}

.progress-bar {
  height: 12px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 6px;
  overflow: hidden;
  margin-bottom: 12px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #00d9ff, #00ff88);
  border-radius: 6px;
  transition: width 0.5s ease;
}

.goal-status {
  display: flex;
  justify-content: space-between;
  font-size: 0.9rem;
  color: #888;
}

.complete {
  color: #10b981;
}

.pending {
  color: #f59e0b;
}

.tips {
  background: rgba(255, 255, 255, 0.08);
  border-radius: 20px;
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.tips h3 {
  margin-bottom: 16px;
  color: #fff;
}

.tips ul {
  list-style: none;
  padding: 0;
}

.tips li {
  padding: 10px 0;
  color: #aaa;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.tips li:last-child {
  border-bottom: none;
}

.tips li::before {
  content: "•";
  color: #00d9ff;
  margin-right: 10px;
}

@media (max-width: 900px) {
  .goals-grid {
    grid-template-columns: 1fr;
  }
}
</style>
