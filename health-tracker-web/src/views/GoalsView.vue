<template>
  <!-- SideNavBar (Desktop Shell) -->
  <aside class="fixed left-0 top-0 h-full w-64 bg-stone-50/70 dark:bg-stone-900/70 backdrop-blur-xl flex flex-col p-4 border-r border-orange-100/20 dark:border-stone-800 z-50 shadow-[20px_0_40px_rgba(86,67,55,0.04)]">
    <div class="mb-10 px-4">
      <div class="flex items-center gap-3">
        <div class="w-10 h-10 rounded-xl bg-primary flex items-center justify-center text-white shadow-lg">
          <span class="material-symbols-outlined" style="font-variation-settings: 'FILL' 1;">health_metrics</span>
        </div>
        <div>
          <h1 class="text-xl font-bold text-orange-800 dark:text-orange-400 tracking-wider font-headline">智康AI</h1>
          <p class="text-[10px] text-on-surface-variant uppercase tracking-widest font-medium opacity-70">智能健康管理助手</p>
        </div>
      </div>
    </div>
    <nav class="flex-1 space-y-2 overflow-y-auto">
      <router-link to="/" class="flex items-center gap-3 px-4 py-3 text-stone-600 dark:text-stone-400 font-manrope text-sm font-medium tracking-tight hover:bg-stone-100 dark:hover:bg-stone-800 transition-all duration-300 rounded-full">
        <span class="material-symbols-outlined">home</span>
        <span>首页</span>
      </router-link>
      <router-link to="/analysis" class="flex items-center gap-3 px-4 py-3 text-stone-600 dark:text-stone-400 font-manrope text-sm font-medium tracking-tight hover:bg-stone-100 dark:hover:bg-stone-800 transition-all duration-300 rounded-full">
        <span class="material-symbols-outlined">insights</span>
        <span>数据分析</span>
      </router-link>
      <router-link to="/goals" class="flex items-center gap-3 px-4 py-3 bg-orange-100/50 dark:bg-orange-900/30 text-orange-800 dark:text-orange-200 rounded-full font-bold font-manrope text-sm tracking-tight">
        <span class="material-symbols-outlined" style="font-variation-settings: 'FILL' 1;">track_changes</span>
        <span>目标管理</span>
      </router-link>
      <router-link to="/records" class="flex items-center gap-3 px-4 py-3 text-stone-600 dark:text-stone-400 font-manrope text-sm font-medium tracking-tight hover:bg-stone-100 dark:hover:bg-stone-800 transition-all duration-300 rounded-full">
        <span class="material-symbols-outlined">restaurant</span>
        <span>饮食记录</span>
      </router-link>
      <router-link to="/medications" class="flex items-center gap-3 px-4 py-3 text-stone-600 dark:text-stone-400 font-manrope text-sm font-medium tracking-tight hover:bg-stone-100 dark:hover:bg-stone-800 transition-all duration-300 rounded-full">
        <span class="material-symbols-outlined">medication</span>
        <span>用药管理</span>
      </router-link>
      <router-link to="/settings" class="flex items-center gap-3 px-4 py-3 text-stone-600 dark:text-stone-400 font-manrope text-sm font-medium tracking-tight hover:bg-stone-100 dark:hover:bg-stone-800 transition-all duration-300 rounded-full">
        <span class="material-symbols-outlined">settings</span>
        <span>设置</span>
      </router-link>
    </nav>
    <!-- 个人中心菜单已移至设置 -->

  </aside>

  <!-- Main Content Area -->
  <main class="pl-64 min-h-screen">
    <!-- TopNavBar -->
    <header class="sticky top-0 h-16 bg-white/70 dark:bg-stone-950/70 backdrop-blur-md flex justify-between items-center w-full pl-8 pr-8 py-2 z-40 border-b border-surface-container-low">
      <div class="flex items-center gap-2">
        <span class="text-lg font-black text-orange-900 dark:text-orange-100 font-headline">目标管理</span>
        <span class="text-xs text-on-surface-variant bg-surface-container-low px-2 py-0.5 rounded-full font-medium">Read Only</span>
      </div>
      <div class="flex items-center gap-6">
        <div class="relative w-64">
          <input 
            v-model="searchQuery"
            class="w-full bg-surface-container-low border-none rounded-full py-2 pl-10 pr-4 text-sm focus:ring-2 focus:ring-primary/20 placeholder-on-surface-variant/50" 
            placeholder="搜索目标..." 
            type="text"
          />
          <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-on-surface-variant text-lg">search</span>
        </div>
        <div class="flex items-center gap-4 text-on-surface-variant">
          <button @click="toggleNotifications" class="hover:text-primary transition-colors">
            <span class="material-symbols-outlined" data-icon="notifications">notifications</span>
          </button>
          <router-link to="/settings" class="hover:text-primary transition-colors">
            <span class="material-symbols-outlined" data-icon="settings">settings</span>
          </router-link>
          <div class="h-8 w-8 rounded-full bg-surface-container-high overflow-hidden border border-outline-variant/30 cursor-pointer" @click="goToProfile">
            <img 
              alt="User Avatar" 
              class="w-full h-full object-cover" 
              :src="userAvatar" 
            />
          </div>
        </div>
      </div>
    </header>

    <!-- Content Canvas -->
    <section class="p-8 max-w-7xl mx-auto">
      <!-- Hero Section / Statistics -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-10">
        <div class="md:col-span-2 relative overflow-hidden bg-primary rounded-xl p-8 text-white shadow-2xl">
          <div class="relative z-10">
            <h2 class="text-3xl font-bold font-headline mb-2">坚持就是胜利</h2>
            <p class="opacity-80 max-w-md mb-6">您目前有 {{ activeGoalsCount }} 个正在进行的健康目标。目前的平均完成率为 {{ averageCompletionRate }}%。继续保持，向更健康的自己迈进！</p>
            <div class="flex gap-4">
              <div class="px-4 py-2 bg-white/20 backdrop-blur-md rounded-full text-xs font-bold flex items-center gap-2">
                <span class="material-symbols-outlined text-sm">auto_awesome</span>
                智能建议已开启
              </div>
            </div>
          </div>
          <!-- Abstract Background Decor -->
          <div class="absolute right-[-5%] top-[-10%] w-64 h-64 bg-primary-container rounded-full blur-[80px] opacity-40"></div>
          <div class="absolute right-[10%] bottom-[-20%] w-48 h-48 bg-secondary-container rounded-full blur-[60px] opacity-30"></div>
        </div>
        <div class="bg-surface-container-lowest rounded-xl p-8 flex flex-col justify-between shadow-[0_20px_40px_rgba(86,67,55,0.04)] border border-surface-container-low">
          <div>
            <p class="text-on-surface-variant text-sm font-medium mb-1">本周健康指数</p>
            <h3 class="text-4xl font-black text-primary font-headline tracking-tighter">{{ healthScore }}<span class="text-lg opacity-50 ml-1">/100</span></h3>
          </div>
          <div class="mt-4 pt-4 border-t border-surface-container-low flex items-center justify-between">
            <span class="text-xs text-tertiary font-bold flex items-center gap-1">
              <span class="material-symbols-outlined text-sm">trending_up</span>
              较上周提升 {{ weekImprovement }}%
            </span>
            <div class="flex -space-x-2">
              <div class="w-6 h-6 rounded-full border-2 border-white bg-stone-200"></div>
              <div class="w-6 h-6 rounded-full border-2 border-white bg-stone-300"></div>
              <div class="w-6 h-6 rounded-full border-2 border-white bg-stone-400"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- Goals Bento Grid -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <!-- Weight Goal Card -->
        <div class="bg-surface-container-lowest rounded-xl p-6 shadow-[0_20px_40px_rgba(86,67,55,0.04)] hover:shadow-[0_25px_50px_rgba(86,67,55,0.08)] transition-all duration-500 group border border-transparent hover:border-orange-100/50">
          <div class="flex justify-between items-start mb-6">
            <div class="w-12 h-12 rounded-xl bg-orange-50 text-primary flex items-center justify-center group-hover:scale-110 transition-transform duration-500">
              <span class="material-symbols-outlined" style="font-variation-settings: 'FILL' 1;">monitor_weight</span>
            </div>
            <span class="text-[10px] font-bold uppercase tracking-widest text-on-surface-variant/40">WEIGHT LOSS</span>
          </div>
          <h3 class="text-xl font-bold font-headline text-on-surface mb-1">体重管理目标</h3>
          <p class="text-sm text-on-surface-variant mb-6">目标是在本季度末减重 5 公斤。</p>
          <div class="space-y-4">
            <div class="flex justify-between items-end">
              <span class="text-2xl font-black text-on-surface">{{ weightGoal.current }} <span class="text-xs font-normal opacity-50">/ {{ weightGoal.target }} kg</span></span>
              <span class="text-sm font-bold text-primary">{{ weightGoal.percentage }}%</span>
            </div>
            <div class="w-full h-3 bg-surface-container-low rounded-full overflow-hidden">
              <div class="h-full bg-primary rounded-full" :style="{ width: weightGoal.percentage + '%' }"></div>
            </div>
            <div class="flex justify-between text-[10px] font-bold text-on-surface-variant/60 uppercase">
              <span>起始: {{ weightGoal.start }}kg</span>
              <span>剩余: {{ weightGoal.remaining }}kg</span>
            </div>
          </div>
        </div>

        <!-- Steps Goal Card -->
        <div class="bg-surface-container-lowest rounded-xl p-6 shadow-[0_20px_40px_rgba(86,67,55,0.04)] hover:shadow-[0_25px_50px_rgba(86,67,55,0.08)] transition-all duration-500 group border border-transparent hover:border-orange-100/50">
          <div class="flex justify-between items-start mb-6">
            <div class="w-12 h-12 rounded-xl bg-green-50 text-tertiary flex items-center justify-center group-hover:scale-110 transition-transform duration-500">
              <span class="material-symbols-outlined" style="font-variation-settings: 'FILL' 1;">directions_walk</span>
            </div>
            <span class="text-[10px] font-bold uppercase tracking-widest text-on-surface-variant/40">ACTIVITY</span>
          </div>
          <h3 class="text-xl font-bold font-headline text-on-surface mb-1">每日步数目标</h3>
          <p class="text-sm text-on-surface-variant mb-6">维持健康的日常活动水平。</p>
          <div class="space-y-4">
            <div class="flex justify-between items-end">
              <span class="text-2xl font-black text-on-surface">{{ stepsGoal.current.toLocaleString() }} <span class="text-xs font-normal opacity-50">/ {{ stepsGoal.target.toLocaleString() }} steps</span></span>
              <span class="text-sm font-bold text-tertiary">{{ stepsGoal.percentage }}%</span>
            </div>
            <div class="w-full h-3 bg-surface-container-low rounded-full overflow-hidden">
              <div class="h-full bg-tertiary rounded-full" :style="{ width: stepsGoal.percentage + '%' }"></div>
            </div>
            <div class="flex justify-between text-[10px] font-bold text-on-surface-variant/60 uppercase">
              <span>今日已达成</span>
              <span>还差: {{ stepsGoal.remaining.toLocaleString() }}</span>
            </div>
          </div>
        </div>

        <!-- Sleep Goal Card -->
        <div class="bg-surface-container-lowest rounded-xl p-6 shadow-[0_20px_40px_rgba(86,67,55,0.04)] hover:shadow-[0_25px_50px_rgba(86,67,55,0.08)] transition-all duration-500 group border border-transparent hover:border-orange-100/50">
          <div class="flex justify-between items-start mb-6">
            <div class="w-12 h-12 rounded-xl bg-blue-50 text-blue-700 flex items-center justify-center group-hover:scale-110 transition-transform duration-500">
              <span class="material-symbols-outlined" style="font-variation-settings: 'FILL' 1;">bedtime</span>
            </div>
            <span class="text-[10px] font-bold uppercase tracking-widest text-on-surface-variant/40">RECOVERY</span>
          </div>
          <h3 class="text-xl font-bold font-headline text-on-surface mb-1">睡眠质量目标</h3>
          <p class="text-sm text-on-surface-variant mb-6">提高深度睡眠时间，保障精神状态。</p>
          <div class="space-y-4">
            <div class="flex justify-between items-end">
              <span class="text-2xl font-black text-on-surface">{{ sleepGoal.current }} <span class="text-xs font-normal opacity-50">/ {{ sleepGoal.target }}</span></span>
              <span class="text-sm font-bold text-blue-700">{{ sleepGoal.percentage }}%</span>
            </div>
            <div class="w-full h-3 bg-surface-container-low rounded-full overflow-hidden">
              <div class="h-full bg-blue-600 rounded-full" :style="{ width: sleepGoal.percentage + '%' }"></div>
            </div>
            <div class="flex justify-between text-[10px] font-bold text-on-surface-variant/60 uppercase">
              <span>昨晚时长</span>
              <span>目标缺口: {{ sleepGoal.gap }}</span>
            </div>
          </div>
        </div>

        <!-- Water Goal Card -->
        <div class="bg-surface-container-lowest rounded-xl p-6 shadow-[0_20px_40px_rgba(86,67,55,0.04)] hover:shadow-[0_25px_50px_rgba(86,67,55,0.08)] transition-all duration-500 group border border-transparent hover:border-orange-100/50">
          <div class="flex justify-between items-start mb-6">
            <div class="w-12 h-12 rounded-xl bg-cyan-50 text-cyan-700 flex items-center justify-center group-hover:scale-110 transition-transform duration-500">
              <span class="material-symbols-outlined" style="font-variation-settings: 'FILL' 1;">water_drop</span>
            </div>
            <span class="text-[10px] font-bold uppercase tracking-widest text-on-surface-variant/40">HYDRATION</span>
          </div>
          <h3 class="text-xl font-bold font-headline text-on-surface mb-1">每日饮水目标</h3>
          <p class="text-sm text-on-surface-variant mb-6">保持身体水分，促进新陈代谢。</p>
          <div class="space-y-4">
            <div class="flex justify-between items-end">
              <span class="text-2xl font-black text-on-surface">{{ waterGoal.current.toLocaleString() }} <span class="text-xs font-normal opacity-50">/ {{ waterGoal.target.toLocaleString() }} ml</span></span>
              <span class="text-sm font-bold text-cyan-700">{{ waterGoal.percentage }}%</span>
            </div>
            <div class="w-full h-3 bg-surface-container-low rounded-full overflow-hidden">
              <div class="h-full bg-cyan-500 rounded-full" :style="{ width: waterGoal.percentage + '%' }"></div>
            </div>
            <div class="flex justify-between text-[10px] font-bold text-on-surface-variant/60 uppercase">
              <span>今日摄入</span>
              <span>剩余: {{ waterGoal.remaining.toLocaleString() }} ml</span>
            </div>
          </div>
        </div>

        <!-- Medication Goal Card (Continuous) -->
        <div class="bg-surface-container-lowest rounded-xl p-6 shadow-[0_20px_40px_rgba(86,67,55,0.04)] hover:shadow-[0_25px_50px_rgba(86,67,55,0.08)] transition-all duration-500 group border border-transparent hover:border-orange-100/50 lg:col-span-2">
          <div class="flex flex-col md:flex-row gap-8">
            <div class="flex-1">
              <div class="flex justify-between items-start mb-6">
                <div class="w-12 h-12 rounded-xl bg-purple-50 text-purple-700 flex items-center justify-center group-hover:scale-110 transition-transform duration-500">
                  <span class="material-symbols-outlined" style="font-variation-settings: 'FILL' 1;">medication</span>
                </div>
                <span class="text-[10px] font-bold uppercase tracking-widest text-on-surface-variant/40">COMPLIANCE</span>
              </div>
              <h3 class="text-xl font-bold font-headline text-on-surface mb-1">用药依从性目标</h3>
              <p class="text-sm text-on-surface-variant mb-6">严格按照医生叮嘱，准时记录用药状态。</p>
              <div class="space-y-4">
                <div class="flex justify-between items-end">
                  <span class="text-2xl font-black text-on-surface">{{ medicationGoal.current }} <span class="text-xs font-normal opacity-50">/ {{ medicationGoal.target }} days streak</span></span>
                  <span class="text-sm font-bold text-purple-700">{{ medicationGoal.percentage }}%</span>
                </div>
                <div class="w-full h-3 bg-surface-container-low rounded-full overflow-hidden">
                  <div class="h-full bg-purple-600 rounded-full" :style="{ width: medicationGoal.percentage + '%' }"></div>
                </div>
              </div>
            </div>
            <div class="flex-shrink-0 flex items-center justify-center">
              <div class="relative w-32 h-32 flex items-center justify-center">
                <svg class="w-full h-full transform -rotate-90">
                  <circle class="text-surface-container-low" cx="64" cy="64" fill="transparent" r="56" stroke="currentColor" stroke-width="8"></circle>
                  <circle class="text-purple-600" cx="64" cy="64" fill="transparent" r="56" stroke="currentColor" :stroke-dasharray="circumference" :stroke-dashoffset="medicationGoalStrokeOffset" stroke-width="8"></circle>
                </svg>
                <div class="absolute inset-0 flex flex-col items-center justify-center">
                  <span class="text-2xl font-black font-headline">{{ medicationGoal.percentage }}%</span>
                  <span class="text-[8px] font-bold opacity-50">TOTAL</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Footer Section -->
      <footer class="mt-16 text-center text-on-surface-variant/40 pb-12">
        <p class="text-xs font-medium uppercase tracking-[0.2em] mb-4">目标状态由系统根据您的健康记录自动更新</p>
        <div class="flex justify-center gap-8">
          <div class="flex items-center gap-2">
            <div class="w-2 h-2 rounded-full bg-primary"></div>
            <span class="text-[10px] font-bold">正在进行</span>
          </div>
          <div class="flex items-center gap-2">
            <div class="w-2 h-2 rounded-full bg-tertiary"></div>
            <span class="text-[10px] font-bold">已达成</span>
          </div>
          <div class="flex items-center gap-2">
            <div class="w-2 h-2 rounded-full bg-stone-300"></div>
            <span class="text-[10px] font-bold">已归档</span>
          </div>
        </div>
      </footer>
    </section>
  </main>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getGoalList, getStatisticsOverview, getUserId } from '../api'

const router = useRouter()

// 搜索查询
const searchQuery = ref('')

// 用户头像
const userAvatar = ref('https://lh3.googleusercontent.com/aida-public/AB6AXuCGgb3kCEuhO3Mwv_ILw1vNmA-MKepVFDeuhD4_oOBq5qazUYcXrBNGmTl_-5Q2c6dhSmlP_Te4JVRLM_yTJYzNRfCIArir9QAoIb8_Aw01-B8TFMuQbzFGdzEyNy7B3OMDKjdTraXVE6pbFSF60ZG6sXM8e0hAqTkb6-pKHhUeSVb5jIWc7VAC5-PWO4C2AbItTDVJhQoloLu19T_dG77DW6bzqHrI3bex3ob1D6usZea3Lm-sPsC8-GWZTfAe4NZ2CJT2wN-QumE')

// 目标数据
const activeGoalsCount = ref(0)
const averageCompletionRate = ref(0)
const healthScore = ref(0)
const weekImprovement = ref(0)

// 各个目标的数据
const weightGoal = ref({
  current: 0,
  target: 0,
  start: 0,
  percentage: 0,
  get remaining() {
    return (this.current - this.target).toFixed(1)
  }
})

const stepsGoal = ref({
  current: 0,
  target: 0,
  percentage: 0,
  get remaining() {
    return this.target - this.current
  }
})

const sleepGoal = ref({
  current: '0h',
  target: '0h',
  percentage: 0,
  gap: '0h'
})

const waterGoal = ref({
  current: 0,
  target: 0,
  percentage: 0,
  get remaining() {
    return this.target - this.current
  }
})

const medicationGoal = ref({
  current: 0,
  target: 0,
  percentage: 0
})

// 计算圆形进度条的属性
const circumference = computed(() => 2 * Math.PI * 56)
const medicationGoalStrokeOffset = computed(() => {
  const progress = medicationGoal.value.percentage / 100
  return circumference.value * (1 - progress)
})

// 方法
function toggleNotifications() {
  console.log('切换通知')
  // 这里可以添加实际的通知切换逻辑
}

function goToProfile() {
  router.push('/profile')
}

function calcPercentage(current, target) {
  if (!target || target === 0) return 0
  return Math.min(100, Math.round((current / target) * 100))
}

function formatHours(value) {
  if (!value && value !== 0) return '0h'
  return `${value}h`
}

async function loadGoals() {
  try {
    const userId = getUserId()
    const [goals, overview] = await Promise.all([
      getGoalList(userId, 'day'),
      getStatisticsOverview(userId, 'week')
    ])

    const goalList = Array.isArray(goals) ? goals : []
    const activeGoals = goalList.filter(item => item.status === 1)
    activeGoalsCount.value = activeGoals.length

    const completionRates = goalList.map(item => calcPercentage(item.currentValue || 0, item.targetValue || 0))
    averageCompletionRate.value = completionRates.length
      ? Math.round(completionRates.reduce((sum, item) => sum + item, 0) / completionRates.length)
      : 0
    healthScore.value = averageCompletionRate.value
    weekImprovement.value = overview?.stepsDelta ? Math.abs(Math.round(overview.stepsDelta / 100)) : 0

    goalList.forEach(item => {
      if (item.goalType === 2) {
        const current = Number(item.currentValue || 0)
        const target = Number(item.targetValue || 0)
        weightGoal.value.current = current
        weightGoal.value.target = target
        weightGoal.value.start = current
        weightGoal.value.percentage = calcPercentage(current, target)
      }
      if (item.goalType === 1) {
        const current = Number(item.currentValue || 0)
        const target = Number(item.targetValue || 0)
        stepsGoal.value.current = current
        stepsGoal.value.target = target
        stepsGoal.value.percentage = calcPercentage(current, target)
      }
      if (item.goalType === 4) {
        const current = Number(item.currentValue || 0)
        const target = Number(item.targetValue || 0)
        sleepGoal.value.current = formatHours(current)
        sleepGoal.value.target = formatHours(target)
        sleepGoal.value.percentage = calcPercentage(current, target)
        sleepGoal.value.gap = formatHours(Math.max(target - current, 0))
      }
      if (item.goalType === 3) {
        const current = Number(item.currentValue || 0)
        const target = Number(item.targetValue || 0)
        waterGoal.value.current = current
        waterGoal.value.target = target
        waterGoal.value.percentage = calcPercentage(current, target)
      }
    })
  } catch (error) {
    console.error('加载目标数据失败', error)
  }
}

onMounted(loadGoals)
</script>

<style scoped>
/* 组件特定样式已在Tailwind中定义 */
</style>
