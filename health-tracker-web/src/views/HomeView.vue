<template>
  <div class="home">
    <h1 class="page-title">今日健康概览</h1>

    <!-- 健康评分 -->
    <div class="score-card">
      <div class="score-circle">
        <svg viewBox="0 0 100 100">
          <circle cx="50" cy="50" r="45" fill="none" stroke="#fff0eb" stroke-width="8" />
          <circle cx="50" cy="50" r="45" fill="none" stroke="#ff7a45" stroke-width="8" 
            :stroke-dasharray="`${score.score * 2.83} 283`" stroke-linecap="round" 
            transform="rotate(-90 50 50)" />
        </svg>
        <div class="score-value">
          <span class="number">{{ score.score }}</span>
          <span class="label">健康评分</span>
        </div>
      </div>
      <div class="score-level">{{ score.level }}</div>
      <div class="score-factors">
        <div class="factor" v-for="item in score.factors" :key="item.name">
          <span class="factor-name">{{ item.name }}</span>
          <div class="factor-bar">
            <div class="factor-fill" :style="{ width: item.value + '%' }"></div>
          </div>
          <span class="factor-value">{{ item.value }}</span>
        </div>
      </div>
    </div>

    <!-- 核心指标 -->
    <div class="stats-grid">
      <div class="stat-card clickable" @click="goToAnalysis('steps')">
        <div class="stat-icon steps">👟</div>
        <div class="stat-info">
          <span class="stat-label">步数</span>
          <span class="stat-value">{{ overview.steps.toLocaleString() }}</span>
          <span class="stat-target">目标: {{ overview.stepsTarget.toLocaleString() }}</span>
        </div>
        <div class="stat-progress">
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: overview.stepsPercent + '%' }"></div>
          </div>
          <span>{{ overview.stepsPercent }}%</span>
        </div>
      </div>

      <div class="stat-card clickable" @click="goToAnalysis('sleep')">
        <div class="stat-icon sleep">😴</div>
        <div class="stat-info">
          <span class="stat-label">睡眠</span>
          <span class="stat-value">{{ overview.sleep }}</span>
          <span class="stat-target">目标: {{ overview.sleepTarget }}小时</span>
        </div>
        <div class="stat-progress">
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: overview.sleepPercent + '%' }"></div>
          </div>
          <span>{{ overview.sleepPercent }}%</span>
        </div>
      </div>

      <div class="stat-card clickable" @click="goToAnalysis('weight')">
        <div class="stat-icon weight">⚖️</div>
        <div class="stat-info">
          <span class="stat-label">体重</span>
          <span class="stat-value">{{ overview.weight }}</span>
          <span class="stat-target">健康范围</span>
        </div>
      </div>

      <div class="stat-card clickable" @click="goToAnalysis('diet')">
        <div class="stat-icon calories">🔥</div>
        <div class="stat-info">
          <span class="stat-label">饮食热量</span>
          <span class="stat-value">{{ overview.calories.toLocaleString() }}</span>
          <span class="stat-target">千卡</span>
        </div>
      </div>
    </div>

    <!-- AI 解读 -->
    <div class="ai-card">
      <div class="ai-header">
        <span class="ai-icon">🤖</span>
        <span class="ai-title">AI 健康解读</span>
      </div>
      <div class="ai-content">
        {{ aiAnalysis }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { mockData } from "../mock/data";
import { getUserId, getTodayScore, getStatisticsOverview, getAIAnalysis } from "../api";

const router = useRouter();

const score = ref(mockData.todayScore);
const overview = ref(mockData.overview);
const aiAnalysis = ref(mockData.aiAnalysis);

const goToAnalysis = (type) => {
  window.location.href = `/analysis?type=${type}`;
};

onMounted(async () => {
  const userId = getUserId();
  try {
    const [scoreData, overviewData, aiData] = await Promise.all([
      getTodayScore(userId),
      getStatisticsOverview(userId, "day"),
      getAIAnalysis([{ role: "user", content: "分析今日健康状态" }])
    ]);
    if (scoreData) score.value = scoreData;
    if (overviewData) overview.value = overviewData;
    if (aiData) aiAnalysis.value = aiData;
  } catch (e) {
    console.log("使用mock数据");
  }
});
</script>

<style scoped>
.home {
  max-width: 1200px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 24px;
  color: #333;
}

/* 评分卡片 */
.score-card {
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  gap: 40px;
}

.score-circle {
  position: relative;
  width: 140px;
  height: 140px;
}

.score-circle svg {
  width: 100%;
  height: 100%;
}

.score-value {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
}

.score-value .number {
  display: block;
  font-size: 36px;
  font-weight: 700;
  color: #ff7a45;
}

.score-value .label {
  font-size: 12px;
  color: #999;
}

.score-level {
  font-size: 20px;
  font-weight: 600;
  color: #52c41a;
}

.score-factors {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.factor {
  display: flex;
  align-items: center;
  gap: 12px;
}

.factor-name {
  width: 40px;
  font-size: 14px;
  color: #666;
}

.factor-bar {
  flex: 1;
  height: 8px;
  background: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
}

.factor-fill {
  height: 100%;
  background: linear-gradient(90deg, #ff7a45, #ffa85c);
  border-radius: 4px;
}

.factor-value {
  width: 30px;
  text-align: right;
  font-size: 14px;
  color: #333;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.stat-card.clickable {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card.clickable:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(255, 122, 69, 0.15);
}

.stat-icon {
  font-size: 28px;
  margin-bottom: 12px;
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-size: 14px;
  color: #999;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #333;
}

.stat-target {
  font-size: 12px;
  color: #bbb;
}

.stat-progress {
  margin-top: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.progress-bar {
  flex: 1;
  height: 6px;
  background: #f0f0f0;
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: #ff7a45;
  border-radius: 3px;
}

.stat-progress span {
  font-size: 13px;
  color: #ff7a45;
  font-weight: 500;
}

/* AI 解读 */
.ai-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.ai-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.ai-icon {
  font-size: 24px;
}

.ai-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.ai-content {
  font-size: 15px;
  line-height: 1.8;
  color: #666;
}

@media (max-width: 1100px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .score-card {
    flex-direction: column;
    text-align: center;
  }
  
  .score-factors {
    width: 100%;
  }
}
</style>
