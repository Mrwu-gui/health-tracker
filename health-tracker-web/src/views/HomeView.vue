<template>
  <div class="home">
    <div class="hero">
      <div class="hero__content">
        <span class="badge">健康管理工具</span>
        <h1>把运动、饮食、睡眠与用药放进一个清爽面板</h1>
        <p>跨端同步，随时记录，数据可视化，支持微信登录与手机登录。</p>
        <div class="actions">
          <a-button type="primary" @click="goLogin">登录 / 注册</a-button>
          <a-button ghost @click="goDemo">体验演示</a-button>
        </div>
        <div class="tips">
          <span>微信运动步数自动同步</span>
          <span>睡眠支持手动录入</span>
          <span>短信验证码登录</span>
        </div>
      </div>
      <div class="hero__card">
        <div class="preview">
          <div class="preview__title">今日概览</div>
          <div class="preview__stats">
            <div class="stat">
              <span class="label">步数</span>
              <strong>{{ preview.steps }}</strong>
            </div>
            <div class="stat">
              <span class="label">睡眠</span>
              <strong>{{ preview.sleep }}</strong>
            </div>
            <div class="stat">
              <span class="label">热量</span>
              <strong>{{ preview.calories }}</strong>
            </div>
          </div>
          <div class="progress">
            <span>今日目标完成度</span>
            <a-progress :percent="preview.percent" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from "vue-router";
import { onMounted, reactive } from "vue";
import { api } from "../api/http";

const router = useRouter();
const preview = reactive({
  steps: "--",
  sleep: "--",
  calories: "--",
  percent: 0
});

function goLogin() {
  router.push("/login");
}

function goDemo() {
  router.push("/login");
}

onMounted(async () => {
  try {
    const data = await api.dashboard(1);
    if (data?.steps) preview.steps = data.steps;
    if (data?.sleep) preview.sleep = data.sleep;
    if (data?.calories) preview.calories = data.calories;
    const goal = await api.goalList(1);
    if (Array.isArray(goal) && goal.length > 0) {
      const stepGoal = goal.find((item) => item.goalType === "步数" || item.goalType === "steps");
      if (stepGoal?.targetValue) {
        preview.percent = Math.round((stepGoal.currentValue / stepGoal.targetValue) * 100);
      }
    }
  } catch (_) {
  }
});
</script>

<style scoped>
.home {
  min-height: 100vh;
  padding: 60px;
  color: #e2e8f0;
}

.hero {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 32px;
  align-items: center;
}

.hero__content h1 {
  font-size: 40px;
  margin: 16px 0;
  color: #f8fafc;
}

.hero__content p {
  color: #cbd5f5;
  max-width: 520px;
}

.badge {
  display: inline-flex;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(56, 189, 248, 0.25);
  border: 1px solid rgba(56, 189, 248, 0.45);
  color: #e0f2fe;
}

.actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

.tips {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 20px;
  color: #cbd5f5;
  font-size: 13px;
}

.hero__card {
  background: rgba(255, 255, 255, 0.12);
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.25);
  padding: 24px;
  backdrop-filter: blur(18px);
}

.preview {
  background: rgba(15, 23, 42, 0.6);
  border-radius: 16px;
  padding: 18px;
  color: #e2e8f0;
}

.preview__title {
  font-weight: 600;
  margin-bottom: 12px;
}

.preview__stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.stat {
  background: rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  padding: 10px;
  display: grid;
  gap: 6px;
}

.stat .label {
  font-size: 12px;
  color: #cbd5f5;
}

.progress {
  margin-top: 16px;
}

@media (max-width: 1024px) {
  .home {
    padding: 32px;
  }

  .hero {
    grid-template-columns: 1fr;
  }
}
</style>
