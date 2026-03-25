<template>
  <div class="text-on-surface bg-background min-h-screen">
    <!-- SideNavBar Shell -->
    <aside class="fixed left-0 top-0 h-full flex flex-col p-4 w-64 bg-stone-50/70 dark:bg-stone-900/70 backdrop-blur-xl shadow-[20px_0_40px_rgba(86,67,55,0.04)] z-50">
      <div class="flex items-center gap-3 px-2 mb-8">
        <div class="w-10 h-10 rounded-full bg-primary-container flex items-center justify-center">
          <span class="material-symbols-outlined text-white">health_and_safety</span>
        </div>
        <div>
          <h1 class="text-xl font-bold text-orange-800 dark:text-orange-400 tracking-wider headline-font">智康AI</h1>
          <p class="text-[10px] text-on-surface-variant/70 uppercase tracking-widest">智能健康管理助手</p>
        </div>
      </div>
      <nav class="flex-1 space-y-1 overflow-y-auto pr-2">
        <!-- Active Item: 首页 -->
        <router-link 
          to="/" 
          class="flex items-center gap-3 px-4 py-3 bg-orange-100/50 dark:bg-orange-900/30 text-orange-800 dark:text-orange-200 rounded-full font-bold font-manrope text-sm tracking-tight transition-all duration-300"
          active-class="bg-orange-100/50 dark:bg-orange-900/30 text-orange-800 dark:text-orange-200"
          exact
        >
          <span class="material-symbols-outlined">home</span>
          <span>首页</span>
        </router-link>
        <router-link 
          to="/analysis" 
          class="flex items-center gap-3 px-4 py-3 text-stone-600 dark:text-stone-400 hover:bg-stone-100 dark:hover:bg-stone-800 transition-all duration-300 font-manrope text-sm font-medium tracking-tight rounded-full"
          active-class="bg-orange-100/50 dark:bg-orange-900/30 text-orange-800 dark:text-orange-200"
        >
          <span class="material-symbols-outlined">insights</span>
          <span>数据分析</span>
        </router-link>
        <router-link 
          to="/goals" 
          class="flex items-center gap-3 px-4 py-3 text-stone-600 dark:text-stone-400 hover:bg-stone-100 dark:hover:bg-stone-800 transition-all duration-300 font-manrope text-sm font-medium tracking-tight rounded-full"
          active-class="bg-orange-100/50 dark:bg-orange-900/30 text-orange-800 dark:text-orange-200"
        >
          <span class="material-symbols-outlined">track_changes</span>
          <span>目标管理</span>
        </router-link>
        <router-link 
          to="/records" 
          class="flex items-center gap-3 px-4 py-3 text-stone-600 dark:text-stone-400 hover:bg-stone-100 dark:hover:bg-stone-800 transition-all duration-300 font-manrope text-sm font-medium tracking-tight rounded-full"
          active-class="bg-orange-100/50 dark:bg-orange-900/30 text-orange-800 dark:text-orange-200"
        >
          <span class="material-symbols-outlined">restaurant</span>
          <span>饮食记录</span>
        </router-link>
        <router-link 
          to="/medications" 
          class="flex items-center gap-3 px-4 py-3 text-stone-600 dark:text-stone-400 hover:bg-stone-100 dark:hover:bg-stone-800 transition-all duration-300 font-manrope text-sm font-medium tracking-tight rounded-full"
          active-class="bg-orange-100/50 dark:bg-orange-900/30 text-orange-800 dark:text-orange-200"
        >
          <span class="material-symbols-outlined">medication</span>
          <span>用药管理</span>
        </router-link>
        <router-link 
          to="/settings" 
          class="flex items-center gap-3 px-4 py-3 text-stone-600 dark:text-stone-400 hover:bg-stone-100 dark:hover:bg-stone-800 transition-all duration-300 font-manrope text-sm font-medium tracking-tight rounded-full"
          active-class="bg-orange-100/50 dark:bg-orange-900/30 text-orange-800 dark:text-orange-200"
        >
          <span class="material-symbols-outlined">settings</span>
          <span>设置</span>
        </router-link>
      </nav>
      <!-- 个人中心菜单已移至设置 -->

    </aside>

    <!-- Main Content Wrapper -->
    <div class="ml-64 flex flex-col min-h-screen">
      <!-- TopNavBar Shell -->
      <header class="fixed top-0 right-0 left-64 h-16 bg-white/70 dark:bg-stone-950/70 backdrop-blur-md z-40 flex justify-between items-center px-8">
        <div class="flex items-center gap-4">
          <h2 class="text-lg font-black text-orange-900 dark:text-orange-100 headline-font">概览</h2>
          <div class="h-4 w-px bg-stone-200 mx-2"></div>
          <nav class="flex gap-6">
            <button type="button" class="text-orange-700 font-bold border-b-2 border-orange-700 py-1 font-manrope text-base transition-all">今日统计</button>
            <button type="button" class="text-stone-500 hover:text-orange-600 py-1 font-manrope text-base transition-all">近期趋势</button>
          </nav>
        </div>
        <div class="flex items-center gap-6">
          <div class="relative group">
            <input class="bg-surface-container-low border-none rounded-full py-2 px-4 pl-10 text-sm focus:ring-2 focus:ring-primary-container w-64 transition-all" placeholder="搜索健康记录..." type="text"/>
            <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-on-surface-variant text-sm">search</span>
          </div>
          <div class="flex items-center gap-2">
            <button class="p-2 text-stone-600 hover:text-primary transition-all rounded-full hover:bg-surface-container">
              <span class="material-symbols-outlined">notifications</span>
            </button>
            <button class="p-2 text-stone-600 hover:text-primary transition-all rounded-full hover:bg-surface-container">
              <span class="material-symbols-outlined">settings</span>
            </button>
            <div class="w-8 h-8 rounded-full bg-stone-200 overflow-hidden ml-2 border-2 border-primary-container/20">
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
        <!-- AI Hint Section (Bento Style) -->
        <section class="grid grid-cols-12 gap-6">
          <div class="col-span-12 lg:col-span-8 bg-gradient-to-br from-primary to-primary-container p-8 rounded-xl relative overflow-hidden text-white shadow-xl shadow-orange-900/10">
            <div class="relative z-10 flex flex-col justify-center h-full">
              <div class="flex items-center gap-2 mb-4">
                <span class="material-symbols-outlined text-amber-200">magic_button</span>
                <span class="text-xs font-bold tracking-widest uppercase opacity-80">AI 智能分析</span>
              </div>
              <h3 class="text-2xl font-bold headline-font mb-2">{{ aiTitle }}</h3>
              <p class="text-lg opacity-90 max-w-xl">“{{ aiSummary }}”</p>
            </div>
            <div class="absolute -right-12 -bottom-12 w-64 h-64 bg-white/10 rounded-full blur-3xl"></div>
            <div class="absolute right-8 top-1/2 -translate-y-1/2 opacity-20">
              <span class="material-symbols-outlined text-[120px]">auto_awesome</span>
            </div>
          </div>
          
          <!-- Today Goal Progress (Circular) -->
          <div class="col-span-12 lg:col-span-4 bg-surface-container-lowest p-8 rounded-xl flex flex-col items-center justify-center relative shadow-sm border border-orange-100/10">
            <div class="relative w-32 h-32 flex items-center justify-center">
              <!-- Progress Ring -->
              <svg class="w-full h-full -rotate-90">
                <circle class="text-surface-container-high" cx="64" cy="64" fill="transparent" r="56" stroke="currentColor" stroke-width="8"></circle>
                <circle class="text-primary" cx="64" cy="64" fill="transparent" r="56" stroke="currentColor" stroke-dasharray="351.85" :stroke-dashoffset="goalStrokeOffset" stroke-linecap="round" stroke-width="8"></circle>
              </svg>
              <div class="absolute inset-0 flex flex-col items-center justify-center">
                <span class="text-2xl font-black headline-font text-on-surface">{{ goalProgress }}%</span>
                <span class="text-[10px] text-on-surface-variant font-medium">总目标</span>
              </div>
            </div>
            <div class="mt-6 text-center">
              <p class="text-sm font-bold text-on-surface mb-1">今日目标完成进度</p>
              <p class="text-xs text-on-surface-variant">{{ goalHint }}</p>
            </div>
          </div>
        </section>

        <!-- Today Overview Cards Grid -->
        <section>
          <div class="flex items-baseline justify-between mb-6">
            <h3 class="text-xl font-bold headline-font flex items-center gap-2">
              今日概览
              <span class="text-xs font-medium text-on-surface-variant/60 bg-surface-container-high px-2 py-0.5 rounded-full">实时同步</span>
            </h3>
          </div>
          <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-6">
            <!-- Steps Card -->
            <div class="bg-surface-container-lowest p-6 rounded-xl hover:shadow-lg transition-all duration-300 border border-orange-100/10 group">
              <div class="flex justify-between items-start mb-4">
                <div class="p-3 bg-orange-50 rounded-lg group-hover:bg-primary group-hover:text-white transition-colors duration-300">
                  <span class="material-symbols-outlined">directions_walk</span>
                </div>
                <span class="text-xs font-bold text-tertiary flex items-center gap-1">
                  <span class="material-symbols-outlined text-sm">arrow_upward</span> 12%
                </span>
              </div>
              <div class="space-y-1">
                <p class="text-on-surface-variant text-xs font-medium uppercase tracking-wider">今日步数</p>
                <div class="flex items-baseline gap-2">
                  <h4 class="text-3xl font-black headline-font">{{ stepsValue.toLocaleString() }}</h4>
                  <span class="text-xs text-on-surface-variant">/ {{ stepsTarget.toLocaleString() }}</span>
                </div>
              </div>
              <div class="mt-6 h-1 w-full bg-surface-container-high rounded-full overflow-hidden">
                <div class="h-full bg-primary rounded-full" :style="{ width: stepsPercent + '%' }"></div>
              </div>
            </div>
            
            <!-- Sleep Card -->
            <div class="bg-surface-container-lowest p-6 rounded-xl hover:shadow-lg transition-all duration-300 border border-orange-100/10 group">
              <div class="flex justify-between items-start mb-4">
                <div class="p-3 bg-orange-50 rounded-lg group-hover:bg-secondary group-hover:text-white transition-colors duration-300">
                  <span class="material-symbols-outlined">bedtime</span>
                </div>
                <span class="text-xs font-bold text-tertiary bg-tertiary/10 px-2 py-0.5 rounded-full">优</span>
              </div>
              <div class="space-y-1">
                <p class="text-on-surface-variant text-xs font-medium uppercase tracking-wider">睡眠时长</p>
                <div class="flex items-baseline gap-2">
                  <h4 class="text-3xl font-black headline-font">{{ sleepText }}</h4>
                </div>
              </div>
              <div class="mt-6 flex items-center gap-2">
                <div class="flex -space-x-1">
                  <div class="w-1 h-3 bg-secondary/30 rounded-full"></div>
                  <div class="w-1 h-4 bg-secondary/50 rounded-full"></div>
                  <div class="w-1 h-6 bg-secondary rounded-full"></div>
                  <div class="w-1 h-5 bg-secondary/70 rounded-full"></div>
                  <div class="w-1 h-3 bg-secondary/30 rounded-full"></div>
                </div>
                <p class="text-[10px] text-on-surface-variant">深睡比例 {{ sleepDeepPercent }}% ({{ sleepDeepText }})</p>
              </div>
            </div>
            
            <!-- Weight/BMI Card -->
            <div class="bg-surface-container-lowest p-6 rounded-xl hover:shadow-lg transition-all duration-300 border border-orange-100/10 group">
              <div class="flex justify-between items-start mb-4">
                <div class="p-3 bg-orange-50 rounded-lg group-hover:bg-orange-800 group-hover:text-white transition-colors duration-300">
                  <span class="material-symbols-outlined">monitor_weight</span>
                </div>
                <div class="text-right">
                  <p class="text-[10px] text-on-surface-variant uppercase font-bold">BMI</p>
                  <p class="text-xs font-bold text-on-surface">{{ bmiValue }}</p>
                </div>
              </div>
              <div class="space-y-1">
                <p class="text-on-surface-variant text-xs font-medium uppercase tracking-wider">当前体重</p>
                <div class="flex items-baseline gap-2">
                  <h4 class="text-3xl font-black headline-font">{{ weightValue }}</h4>
                  <span class="text-xs text-on-surface-variant">kg</span>
                </div>
              </div>
              <div class="mt-6 flex justify-between items-center text-[10px] text-on-surface-variant font-medium">
                <span>较上次: {{ weightDeltaText }}</span>
                <span class="text-tertiary">{{ weightStatus }}</span>
              </div>
            </div>
            
            <!-- Diet Card -->
            <div class="bg-surface-container-lowest p-6 rounded-xl hover:shadow-lg transition-all duration-300 border border-orange-100/10 group">
              <div class="flex justify-between items-start mb-4">
                <div class="p-3 bg-orange-50 rounded-lg group-hover:bg-secondary-container group-hover:text-white transition-colors duration-300">
                  <span class="material-symbols-outlined">restaurant</span>
                </div>
              </div>
              <div class="space-y-1">
                <p class="text-on-surface-variant text-xs font-medium uppercase tracking-wider">今日摄入</p>
                <div class="flex items-baseline gap-2">
                  <h4 class="text-3xl font-black headline-font">{{ caloriesValue.toLocaleString() }}</h4>
                  <span class="text-xs text-on-surface-variant">kcal</span>
                </div>
              </div>
              <div class="mt-6 flex flex-col gap-1">
                <div class="flex justify-between text-[10px] font-bold">
                  <span>剩余配额</span>
                  <span>{{ caloriesRemaining }} kcal</span>
                </div>
                <div class="h-1 w-full bg-surface-container-high rounded-full overflow-hidden">
                  <div class="h-full bg-secondary-container rounded-full" :style="{ width: caloriesPercent + '%' }"></div>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- Advanced Bento Section -->
        <section class="grid grid-cols-12 gap-6">
          <!-- Activity Chart Placeholder -->
          <div class="col-span-12 lg:col-span-8 bg-surface-container-lowest p-8 rounded-xl shadow-sm border border-orange-100/10">
            <div class="flex items-center justify-between mb-8">
              <h3 class="text-lg font-bold headline-font">活动强度分布</h3>
              <div class="flex gap-2">
                <button class="text-[10px] font-bold px-3 py-1 rounded-full bg-surface-container-high text-on-surface">日</button>
                <button class="text-[10px] font-bold px-3 py-1 rounded-full text-on-surface-variant hover:bg-surface-container">周</button>
                <button class="text-[10px] font-bold px-3 py-1 rounded-full text-on-surface-variant hover:bg-surface-container">月</button>
              </div>
            </div>
            <!-- Mock Chart -->
            <div class="h-48 flex items-end justify-between px-4 gap-2">
              <div v-for="item in activitySeries" :key="item.label" class="w-full bg-surface-container-low rounded-t-lg relative group"
                   :style="{ height: item.height + '%' }">
                <div class="absolute bottom-full mb-2 left-1/2 -translate-x-1/2 bg-on-surface text-white text-[10px] px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition-opacity">{{ item.label }}</div>
              </div>
            </div>
            <div class="flex justify-between mt-4 text-[10px] text-on-surface-variant font-medium px-2">
              <span>06:00</span>
              <span>12:00</span>
              <span>18:00</span>
              <span>24:00</span>
            </div>
          </div>
          
          <!-- Health Badges/Status -->
          <div class="col-span-12 lg:col-span-4 space-y-6" v-if="false"></div>
        </section>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getStatisticsOverview, getStatisticsTrend, getRecordSummary, getGoalList, getAIAnalysis, getUserId } from '../api'

const aiTitle = ref('今日健康简报')
const aiSummary = ref('暂无数据')

const goalProgress = ref(0)
const goalHint = ref('暂无目标数据')
const goalStrokeOffset = computed(() => {
  const progress = goalProgress.value / 100
  return 351.85 * (1 - progress)
})

const stepsValue = ref(0)
const stepsTarget = ref(10000)
const stepsPercent = ref(0)

const sleepText = ref('0h 0m')
const sleepDeepPercent = ref(0)
const sleepDeepText = ref('0h 0m')

const weightValue = ref('--')
const bmiValue = ref('--')
const weightDeltaText = ref('--')
const weightStatus = ref('暂无')

const caloriesValue = ref(0)
const caloriesRemaining = ref(0)
const caloriesPercent = ref(0)

const activitySeries = ref([])

function formatSleep(minutes) {
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  return `${h}h ${m}m`
}

async function loadDashboard() {
  try {
    const userId = getUserId()
    const [overview, trend, summary, goals, aiData] = await Promise.all([
      getStatisticsOverview(userId, 'day'),
      getStatisticsTrend(userId, 'week'),
      getRecordSummary(userId),
      getGoalList(userId, 'day'),
      getAIAnalysis([{ role: 'user', content: '请用一句话总结今天的健康状态' }])
    ])

    aiSummary.value = aiData || '暂无分析结果'
    aiTitle.value = '今日健康简报'

    const goalList = Array.isArray(goals) ? goals : []
    if (goalList.length) {
      const progressValues = goalList.map(item => {
        const target = Number(item.targetValue || 0)
        if (!target) return 0
        return Math.min(100, Math.round((Number(item.currentValue || 0) / target) * 100))
      })
      goalProgress.value = Math.round(progressValues.reduce((a, b) => a + b, 0) / progressValues.length)
      const remaining = goalList.filter(item => Number(item.currentValue || 0) < Number(item.targetValue || 0)).length
      goalHint.value = remaining ? `您距离达成今日所有健康指标仅剩 ${remaining} 项` : '今日目标已达成'
    } else {
      goalProgress.value = 0
      goalHint.value = '暂无目标数据'
    }

    stepsValue.value = Number(overview?.stepsToday || overview?.steps || 0)
    const stepsGoal = goalList.find(item => item.goalType === 1)
    stepsTarget.value = Number(stepsGoal?.targetValue || 10000)
    stepsPercent.value = stepsTarget.value ? Math.min(100, Math.round((stepsValue.value / stepsTarget.value) * 100)) : 0

    const sleepMinutes = Number(overview?.sleepMinutesToday || 0)
    sleepText.value = formatSleep(sleepMinutes)

    const sleepDeepSeries = trend?.series?.sleepDeep || []
    const sleepLightSeries = trend?.series?.sleepLight || []
    if (sleepDeepSeries.length) {
      const deep = Number(sleepDeepSeries[sleepDeepSeries.length - 1].value || 0)
      const light = Number(sleepLightSeries[sleepLightSeries.length - 1]?.value || 0)
      const total = deep + light
      sleepDeepPercent.value = total ? Math.round((deep / total) * 100) : 0
      sleepDeepText.value = formatSleep(deep)
    }

    weightValue.value = overview?.weight || '--'
    bmiValue.value = overview?.bmi || '--'
    const weightDelta = overview?.weightDelta
    weightDeltaText.value = typeof weightDelta === 'number' ? `${weightDelta > 0 ? '+' : ''}${weightDelta.toFixed(1)}kg` : '--'
    weightStatus.value = '正常范围'

    caloriesValue.value = Number(overview?.dietCaloriesToday || overview?.calories || 0)
    const calorieTarget = 2000
    caloriesRemaining.value = Math.max(calorieTarget - caloriesValue.value, 0)
    caloriesPercent.value = Math.min(100, Math.round((caloriesValue.value / calorieTarget) * 100))

    const stepsSeries = trend?.series?.steps || []
    const maxSteps = Math.max(...stepsSeries.map(item => Number(item.value) || 0), 1)
    activitySeries.value = stepsSeries.map(item => ({
      label: item.label || '',
      height: Math.round((Number(item.value) || 0) / maxSteps * 100)
    }))
  } catch (error) {
    console.error('加载概览数据失败', error)
  }
}

onMounted(loadDashboard)
</script>

<style scoped>
/* 自定义滚动条样式 */
.pr-2::-webkit-scrollbar {
  width: 4px;
}

.pr-2::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05);
  border-radius: 4px;
}

.pr-2::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.1);
  border-radius: 4px;
}

.pr-2::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.2);
}
</style>
