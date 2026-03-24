<template>
  <div class="text-on-surface bg-background min-h-screen">
    <!-- SideNavBar Shell -->
    <aside class="fixed left-0 top-0 h-full flex flex-col p-4 w-64 bg-surface-container/70 backdrop-blur-xl shadow-[20px_0_40px_rgba(86,67,55,0.04)] z-50">
      <div class="flex items-center gap-3 px-2 mb-8">
        <div class="w-10 h-10 rounded-full bg-primary-container flex items-center justify-center">
          <span class="material-symbols-outlined text-white">health_and_safety</span>
        </div>
        <div>
          <h1 class="text-xl font-bold text-primary font-headline tracking-wider">智康AI</h1>
          <p class="text-[10px] text-on-surface-variant/70 uppercase tracking-widest">智能健康管理助手</p>
        </div>
      </div>
      <nav class="flex-1 space-y-1 overflow-y-auto pr-2">
        <!-- Active Item: 首页 -->
        <router-link 
          to="/" 
          class="flex items-center gap-3 px-4 py-3 bg-primary-container/20 text-primary font-bold font-headline text-sm tracking-tight rounded-full transition-all duration-300"
          active-class="bg-primary-container/20 text-primary"
          exact
        >
          <span class="material-symbols-outlined">home</span>
          <span>首页</span>
        </router-link>
        <router-link 
          to="/analysis" 
          class="flex items-center gap-3 px-4 py-3 text-on-surface-variant hover:bg-surface-container transition-all duration-300 font-body text-sm font-medium tracking-tight rounded-full"
          active-class="bg-primary-container/20 text-primary"
        >
          <span class="material-symbols-outlined">insights</span>
          <span>数据分析</span>
        </router-link>
        <router-link 
          to="/goals" 
          class="flex items-center gap-3 px-4 py-3 text-on-surface-variant hover:bg-surface-container transition-all duration-300 font-body text-sm font-medium tracking-tight rounded-full"
          active-class="bg-primary-container/20 text-primary"
        >
          <span class="material-symbols-outlined">track_changes</span>
          <span>目标管理</span>
        </router-link>
        <router-link 
          to="/records" 
          class="flex items-center gap-3 px-4 py-3 text-on-surface-variant hover:bg-surface-container transition-all duration-300 font-body text-sm font-medium tracking-tight rounded-full"
          active-class="bg-primary-container/20 text-primary"
        >
          <span class="material-symbols-outlined">restaurant</span>
          <span>饮食记录</span>
        </router-link>
        <router-link 
          to="/medications" 
          class="flex items-center gap-3 px-4 py-3 text-on-surface-variant hover:bg-surface-container transition-all duration-300 font-body text-sm font-medium tracking-tight rounded-full"
          active-class="bg-primary-container/20 text-primary"
        >
          <span class="material-symbols-outlined">medication</span>
          <span>用药管理</span>
        </router-link>
      </nav>
      <div class="pt-4 mt-4 border-t border-surface-container">
        <router-link 
          to="/settings" 
          class="flex items-center gap-3 px-4 py-3 text-on-surface-variant hover:bg-surface-container transition-all duration-300 font-body text-sm font-medium tracking-tight rounded-full"
          active-class="bg-primary-container/20 text-primary"
        >
          <span class="material-symbols-outlined">account_circle</span>
          <span>个人中心</span>
        </router-link>
      </div>
    </aside>

    <!-- Main Content Wrapper -->
    <div class="ml-64 flex flex-col min-h-screen">
      <!-- TopNavBar Shell -->
      <header class="fixed top-0 right-0 left-64 h-16 bg-surface/70 backdrop-blur-md z-40 flex justify-between items-center px-8">
        <div class="flex items-center gap-4">
          <h2 class="text-lg font-black text-primary font-headline">今日健康概览</h2>
          <div class="h-4 w-px bg-outline-variant/30 mx-2"></div>
          <nav class="flex gap-6">
            <a class="text-primary font-bold border-b-2 border-primary py-1 font-body text-base transition-all" href="#">今日统计</a>
            <a class="text-on-surface-variant hover:text-primary py-1 font-body text-base transition-all" href="#">近期趋势</a>
          </nav>
        </div>
        <div class="flex items-center gap-6">
          <div class="relative group">
            <input class="bg-surface-container-low border-none rounded-full py-2 px-4 pl-10 text-sm focus:ring-2 focus:ring-primary-container w-64 transition-all" placeholder="搜索健康记录..." type="text"/>
            <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-on-surface-variant text-sm">search</span>
          </div>
          <div class="flex items-center gap-2">
            <button class="p-2 text-on-surface-variant hover:text-primary transition-all rounded-full hover:bg-surface-container">
              <span class="material-symbols-outlined">notifications</span>
            </button>
            <button class="p-2 text-on-surface-variant hover:text-primary transition-all rounded-full hover:bg-surface-container">
              <span class="material-symbols-outlined">settings</span>
            </button>
            <div class="w-8 h-8 rounded-full bg-surface-container overflow-hidden ml-2 border-2 border-primary-container/20">
              <img 
                alt="User Avatar" 
                class="w-full h-full object-cover" 
                src="https://lh3.googleusercontent.com/aida-public/AB6AXuDmOvqFJaOYRBgohIV6rJjqFslRNMSfeSLgN7ymuoJ9_UNk3zdSAhIKYmpxuU8YnAyHLEXNQSaTKFnT7Pq5ggPNn4ssQWacKaM3H2HsUaoRIVuXRnDlCVbijMs_C_FlH7Y25YooGCqgPkzk5KlX1jdh7nGwVwNjPeHe8JnXIf5Un5P7ZfV0iINpQ5PW-bpupGQM-PD--I1ZI6hJxmq_f4yuz6-so6-eS2Ymau_ObSykat3Gqtxi-t-wXLXcOg6q4budHT1-5WaQVkk"
              />
            </div>
          </div>
        </div>
      </header>

      <!-- Content Canvas -->
      <main class="mt-16 p-8 pb-20 space-y-8 max-w-[1400px]">
        <!-- 健康评分 -->
        <div class="bg-surface rounded-2xl p-8 shadow-amber-care">
          <div class="flex items-center gap-8">
            <div class="relative w-40 h-40">
              <svg viewBox="0 0 100 100" class="w-full h-full">
                <circle cx="50" cy="50" r="45" fill="none" stroke="rgba(230, 126, 34, 0.1)" stroke-width="8" />
                <circle cx="50" cy="50" r="45" fill="none" stroke="#A23F00" stroke-width="8" 
                  :stroke-dasharray="`${score.score * 2.83} 283`" stroke-linecap="round" 
                  transform="rotate(-90 50 50)" />
              </svg>
              <div class="absolute inset-0 flex flex-col items-center justify-center">
                <span class="text-4xl font-bold text-primary font-headline">{{ score.score }}</span>
                <span class="text-sm text-on-surface-variant mt-1">健康评分</span>
              </div>
            </div>
            <div class="flex-1">
              <div class="mb-4">
                <h3 class="text-xl font-bold text-primary font-headline">{{ score.level }}</h3>
                <p class="text-on-surface-variant mt-1">根据您的健康数据评估</p>
              </div>
              <div class="space-y-3">
                <div class="factor" v-for="item in score.factors" :key="item.name">
                  <div class="flex items-center justify-between mb-1">
                    <span class="text-sm text-on-surface-variant">{{ item.name }}</span>
                    <span class="text-sm font-medium text-on-surface">{{ item.value }}%</span>
                  </div>
                  <div class="h-2 bg-surface-container rounded-full overflow-hidden">
                    <div class="h-full bg-primary rounded-full" :style="{ width: item.value + '%' }"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 核心指标 -->
        <div class="grid grid-cols-4 gap-6">
          <div class="bg-surface rounded-2xl p-6 shadow-amber-care hover:shadow-xl transition-shadow duration-300 cursor-pointer" @click="goToAnalysis('steps')">
            <div class="flex items-start justify-between">
              <div>
                <div class="w-12 h-12 bg-primary-container/20 rounded-xl flex items-center justify-center mb-4">
                  <span class="material-symbols-outlined text-primary">directions_walk</span>
                </div>
                <p class="text-sm text-on-surface-variant mb-1">步数</p>
                <p class="text-2xl font-bold text-on-surface font-headline">{{ overview.steps.toLocaleString() }}</p>
                <p class="text-xs text-on-surface-variant mt-2">目标: {{ overview.stepsTarget.toLocaleString() }}</p>
              </div>
              <div class="text-right">
                <div class="text-lg font-bold text-primary">{{ overview.stepsPercent }}%</div>
                <div class="h-2 w-16 bg-surface-container rounded-full overflow-hidden mt-2">
                  <div class="h-full bg-primary rounded-full" :style="{ width: overview.stepsPercent + '%' }"></div>
                </div>
              </div>
            </div>
          </div>

          <div class="bg-surface rounded-2xl p-6 shadow-amber-care hover:shadow-xl transition-shadow duration-300 cursor-pointer" @click="goToAnalysis('sleep')">
            <div class="flex items-start justify-between">
              <div>
                <div class="w-12 h-12 bg-secondary-container/20 rounded-xl flex items-center justify-center mb-4">
                  <span class="material-symbols-outlined text-secondary">bedtime</span>
                </div>
                <p class="text-sm text-on-surface-variant mb-1">睡眠</p>
                <p class="text-2xl font-bold text-on-surface font-headline">{{ overview.sleep }}小时</p>
                <p class="text-xs text-on-surface-variant mt-2">目标: {{ overview.sleepTarget }}小时</p>
              </div>
              <div class="text-right">
                <div class="text-lg font-bold text-secondary">{{ overview.sleepPercent }}%</div>
                <div class="h-2 w-16 bg-surface-container rounded-full overflow-hidden mt-2">
                  <div class="h-full bg-secondary rounded-full" :style="{ width: overview.sleepPercent + '%' }"></div>
                </div>
              </div>
            </div>
          </div>

          <div class="bg-surface rounded-2xl p-6 shadow-amber-care hover:shadow-xl transition-shadow duration-300 cursor-pointer" @click="goToAnalysis('weight')">
            <div class="flex items-start justify-between">
              <div>
                <div class="w-12 h-12 bg-tertiary-container/20 rounded-xl flex items-center justify-center mb-4">
                  <span class="material-symbols-outlined text-tertiary">monitor_weight</span>
                </div>
                <p class="text-sm text-on-surface-variant mb-1">体重</p>
                <p class="text-2xl font-bold text-on-surface font-headline">{{ overview.weight }}kg</p>
                <p class="text-xs text-on-surface-variant mt-2">健康范围</p>
              </div>
            </div>
          </div>

          <div class="bg-surface rounded-2xl p-6 shadow-amber-care hover:shadow-xl transition-shadow duration-300 cursor-pointer" @click="goToAnalysis('diet')">
            <div class="flex items-start justify-between">
              <div>
                <div class="w-12 h-12 bg-error-container/20 rounded-xl flex items-center justify-center mb-4">
                  <span class="material-symbols-outlined text-error">local_fire_department</span>
                </div>
                <p class="text-sm text-on-surface-variant mb-1">饮食热量</p>
                <p class="text-2xl font-bold text-on-surface font-headline">{{ overview.calories.toLocaleString() }}</p>
                <p class="text-xs text-on-surface-variant mt-2">千卡</p>
              </div>
            </div>
          </div>
        </div>

        <!-- AI 解读 -->
        <div class="bg-surface rounded-2xl p-8 shadow-amber-care">
          <div class="flex items-center gap-3 mb-6">
            <div class="w-10 h-10 bg-primary-container/20 rounded-full flex items-center justify-center">
              <span class="material-symbols-outlined text-primary">smart_toy</span>
            </div>
            <h3 class="text-xl font-bold text-on-surface font-headline">AI 健康解读</h3>
          </div>
          <div class="bg-surface-container rounded-xl p-6">
            <p class="text-on-surface leading-relaxed">{{ aiAnalysis }}</p>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { getUserId, getStatisticsOverview, getAIAnalysis, getGoalList } from "../api";

const router = useRouter();

const score = ref({
  score: 0,
  level: "暂无",
  factors: [
    { name: "步数", value: 0 },
    { name: "睡眠", value: 0 },
    { name: "饮食", value: 0 },
    { name: "体重", value: 0 }
  ]
});
const overview = ref({
  steps: 0,
  stepsTarget: 10000,
  stepsPercent: 0,
  sleep: "0小时",
  sleepTarget: 8,
  sleepPercent: 0,
  weight: "--",
  calories: 0
});
const aiAnalysis = ref("暂无分析结果");

const goToAnalysis = (type) => {
  window.location.href = `/analysis?type=${type}`;
};

function buildScore(metrics) {
  const stepScore = metrics.stepsPercent || 0;
  const sleepScore = metrics.sleepPercent || 0;
  const dietScore = metrics.calories > 0 ? Math.min(100, Math.round(80 - Math.abs(metrics.calories - 2000) / 30)) : 0;
  const weightScore = metrics.weight !== "--" ? 80 : 0;
  const total = Math.round((stepScore + sleepScore + dietScore + weightScore) / 4);
  return {
    score: total,
    level: total >= 85 ? "优秀" : total >= 70 ? "良好" : total >= 50 ? "一般" : "需改善",
    factors: [
      { name: "步数", value: stepScore },
      { name: "睡眠", value: sleepScore },
      { name: "饮食", value: dietScore },
      { name: "体重", value: weightScore }
    ]
  };
}

onMounted(async () => {
  const userId = getUserId();
  try {
    const [overviewData, goals, aiData] = await Promise.all([
      getStatisticsOverview(userId, "day"),
      getGoalList(userId, "day"),
      getAIAnalysis([{ role: "user", content: "分析今日健康状态" }])
    ]);

    const goalList = Array.isArray(goals) ? goals : [];
    const stepsGoal = goalList.find(item => item.goalType === 1);
    const sleepGoal = goalList.find(item => item.goalType === 4);

    const stepsTarget = stepsGoal?.targetValue || 10000;
    const stepsValue = Number(overviewData?.stepsToday || overviewData?.steps || 0);
    const stepsPercent = stepsTarget ? Math.min(100, Math.round((stepsValue / stepsTarget) * 100)) : 0;

    const sleepMinutes = Number(overviewData?.sleepMinutesToday || 0);
    const sleepHours = sleepMinutes ? (sleepMinutes / 60).toFixed(1) : "0";
    const sleepTarget = sleepGoal?.targetValue || 8;
    const sleepPercent = sleepTarget ? Math.min(100, Math.round((sleepHours / sleepTarget) * 100)) : 0;

    const caloriesValue = Number(overviewData?.calories || 0);
    const weightValue = overviewData?.weight ?? "--";

    const mapped = {
      steps: stepsValue,
      stepsTarget,
      stepsPercent,
      sleep: `${sleepHours}小时`,
      sleepTarget,
      sleepPercent,
      weight: weightValue === "" ? "--" : weightValue,
      calories: caloriesValue
    };

    overview.value = mapped;
    score.value = buildScore(mapped);
    if (aiData) aiAnalysis.value = aiData;
  } catch (e) {
    console.log("加载首页数据失败", e);
  }
});
</script>

<style scoped>
/* 保留样式为空，因为使用Tailwind CSS类 */
</style>
