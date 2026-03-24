<template>
  <!-- SideNavBar Shell -->
  <aside class="fixed left-0 top-0 h-full flex flex-col p-4 h-screen w-64 bg-stone-50/70 dark:bg-stone-900/70 backdrop-blur-xl shadow-[20px_0_40px_rgba(86,67,55,0.04)] z-50">
    <div class="mb-10 px-4">
      <div class="flex items-center gap-3">
        <div class="w-10 h-10 rounded-xl bg-primary flex items-center justify-center text-white">
          <span class="material-symbols-outlined" style="font-variation-settings: 'FILL' 1;">health_and_safety</span>
        </div>
        <div>
          <div class="text-xl font-bold text-orange-800 dark:text-orange-400 tracking-wider">智康AI</div>
          <div class="text-[10px] text-on-surface-variant/60 uppercase tracking-widest font-bold">智能健康管理助手</div>
        </div>
      </div>
    </div>
    <nav class="flex-1 space-y-1">
      <!-- Navigation Items Mapping -->
      <router-link to="/" class="flex items-center gap-3 px-4 py-3 text-stone-600 dark:text-stone-400 hover:bg-stone-100 dark:hover:bg-stone-800 transition-all duration-300 font-manrope text-sm font-medium tracking-tight rounded-full">
        <span class="material-symbols-outlined">home</span>首页
      </router-link>
      <!-- Active Tab: 数据分析 -->
      <router-link to="/analysis" class="flex items-center gap-3 px-4 py-3 bg-orange-100/50 dark:bg-orange-900/30 text-orange-800 dark:text-orange-200 rounded-full font-bold font-manrope text-sm tracking-tight">
        <span class="material-symbols-outlined" style="font-variation-settings: 'FILL' 1;">insights</span>数据分析
      </router-link>
      <router-link to="/goals" class="flex items-center gap-3 px-4 py-3 text-stone-600 dark:text-stone-400 hover:bg-stone-100 dark:hover:bg-stone-800 transition-all duration-300 font-manrope text-sm font-medium tracking-tight rounded-full">
        <span class="material-symbols-outlined">track_changes</span>目标管理
      </router-link>
      <router-link to="/records" class="flex items-center gap-3 px-4 py-3 text-stone-600 dark:text-stone-400 hover:bg-stone-100 dark:hover:bg-stone-800 transition-all duration-300 font-manrope text-sm font-medium tracking-tight rounded-full">
        <span class="material-symbols-outlined">restaurant</span>饮食记录
      </router-link>
      <router-link to="/medications" class="flex items-center gap-3 px-4 py-3 text-stone-600 dark:text-stone-400 hover:bg-stone-100 dark:hover:bg-stone-800 transition-all duration-300 font-manrope text-sm font-medium tracking-tight rounded-full">
        <span class="material-symbols-outlined">medication</span>用药管理
      </router-link>
      <router-link to="/settings" class="flex items-center gap-3 px-4 py-3 text-stone-600 dark:text-stone-400 hover:bg-stone-100 dark:hover:bg-stone-800 transition-all duration-300 font-manrope text-sm font-medium tracking-tight rounded-full">
        <span class="material-symbols-outlined">settings</span>设置
      </router-link>
    </nav>
    <!-- 个人中心菜单已移至设置 -->

  </aside>

  <!-- TopNavBar Shell -->
  <header class="fixed top-0 right-0 left-0 h-16 bg-white/70 dark:bg-stone-950/70 backdrop-blur-md flex justify-between items-center w-full pl-72 pr-8 py-2 z-40">
    <div class="flex items-center gap-8">
      <h2 class="text-lg font-black text-orange-900 dark:text-orange-100">数据分析中心</h2>
    </div>
    <div class="flex items-center gap-4">
      <div class="relative group">
        <input 
          v-model="searchQuery"
          class="bg-surface-container-low border-none rounded-full px-5 py-1.5 text-sm w-64 focus:ring-2 focus:ring-primary/20 transition-all" 
          placeholder="搜索健康数据..." 
          type="text"
        />
        <span class="material-symbols-outlined absolute right-3 top-1/2 -translate-y-1/2 text-on-surface-variant text-lg">search</span>
      </div>
      <button @click="toggleNotifications" class="w-10 h-10 flex items-center justify-center rounded-full hover:bg-surface-container-high transition-colors text-on-surface-variant">
        <span class="material-symbols-outlined">notifications</span>
      </button>
      <router-link to="/settings" class="w-10 h-10 flex items-center justify-center rounded-full hover:bg-surface-container-high transition-colors text-on-surface-variant">
        <span class="material-symbols-outlined">settings</span>
      </router-link>
      <div class="h-8 w-8 rounded-full bg-orange-100 overflow-hidden ml-2 cursor-pointer" @click="goToProfile">
        <img 
          alt="User Avatar" 
          class="w-full h-full object-cover" 
          :src="userAvatar" 
        />
      </div>
    </div>
  </header>

  <!-- Main Content Canvas -->
  <main class="pl-72 pr-8 pt-24 pb-12 min-h-screen">
    <!-- Header Section & Time Dimensions -->
    <section class="mb-10 flex flex-col md:flex-row md:items-end justify-between gap-6">
      <div>
        <h1 class="text-3xl font-extrabold text-on-surface tracking-tight mb-2">深度健康洞察</h1>
        <p class="text-on-surface-variant text-lg font-light">基于您的生理指标与日常记录的智能分析</p>
      </div>
      <div class="bg-surface-container-low p-1.5 rounded-full flex gap-1">
        <button 
          v-for="timeRange in timeRanges" 
          :key="timeRange.value"
          @click="activeTimeRange = timeRange.value"
          class="px-6 py-2 rounded-full text-sm transition-all"
          :class="activeTimeRange === timeRange.value 
            ? 'bg-white shadow-sm text-primary font-bold' 
            : 'text-on-surface-variant font-medium hover:bg-white/50'"
        >
          {{ timeRange.label }}
        </button>
      </div>
    </section>

    <!-- Bento Grid Layout -->
    <div class="grid grid-cols-12 gap-6">
      <!-- Trend Summary (Wide Sidebar style within grid) -->
      <div class="col-span-12 lg:col-span-4 space-y-6">
        <div class="bg-surface-container-lowest p-8 rounded-xl shadow-[0_20px_40px_rgba(86,67,55,0.04)] h-full flex flex-col justify-between border border-orange-100/10">
          <div>
            <div class="flex items-center gap-2 mb-6">
              <span class="material-symbols-outlined text-secondary" style="font-variation-settings: 'FILL' 1;">auto_awesome</span>
              <h3 class="text-xl font-bold">趋势总结</h3>
            </div>
            <div class="space-y-6">
              <div class="relative pl-6 before:absolute before:left-0 before:top-1 before:bottom-1 before:w-1 before:bg-primary-container before:rounded-full">
                <p class="text-sm font-semibold text-primary uppercase tracking-wider mb-1">关键发现</p>
                <p class="text-on-surface text-base leading-relaxed">过去7天内，您的平均深度睡眠比例提高了12%，这可能归功于您近期增加的晚间冥想练习。</p>
              </div>
              <div class="relative pl-6 before:absolute before:left-0 before:top-1 before:bottom-1 before:w-1 before:bg-secondary before:rounded-full">
                <p class="text-sm font-semibold text-secondary uppercase tracking-wider mb-1">体重预警</p>
                <p class="text-on-surface text-base leading-relaxed">体重呈现轻微下降趋势（-0.8kg），建议关注肌肉量变化并适当补充蛋白质。</p>
              </div>
              <div class="bg-surface-container-low p-5 rounded-lg border-none italic text-on-surface-variant/80 text-sm leading-relaxed">
                "持之以恒的规律作息是维持健康基石的关键。建议下周继续保持当前的运动强度。"
              </div>
            </div>
          </div>
          <div class="mt-8">
            <button @click="generateReport" class="w-full py-4 bg-primary text-on-primary rounded-full font-bold flex items-center justify-center gap-2 shadow-lg shadow-primary/20">
              生成详细PDF报告 <span class="material-symbols-outlined">download</span>
            </button>
          </div>
        </div>
      </div>

      <!-- Charts Cluster -->
      <div class="col-span-12 lg:col-span-8 grid grid-cols-1 md:grid-cols-2 gap-6">
        <!-- Weight Trends (Line Chart Placeholder) -->
        <div class="bg-surface-container-lowest p-6 rounded-xl shadow-[0_20px_40px_rgba(86,67,55,0.04)] border border-orange-100/10">
          <div class="flex justify-between items-start mb-6">
            <div>
              <p class="text-on-surface-variant text-sm font-medium mb-1">体重趋势 (kg)</p>
              <h4 class="text-2xl font-bold">{{ currentWeight }} <span class="text-xs text-error font-medium">↓ {{ weightChange }}</span></h4>
            </div>
            <span class="material-symbols-outlined text-on-surface-variant/40">monitor_weight</span>
          </div>
          <!-- Visualization: Svg Line Chart -->
          <div class="h-48 w-full relative group">
            <svg class="w-full h-full" viewBox="0 0 400 150">
              <defs>
                <linearGradient id="line-grad" x1="0" x2="0" y1="0" y2="1">
                  <stop offset="0%" stop-color="#a23f00" stop-opacity="0.2"></stop>
                  <stop offset="100%" stop-color="#a23f00" stop-opacity="0"></stop>
                </linearGradient>
              </defs>
              <path d="M0,120 Q50,110 100,115 T200,80 T300,90 T400,60 L400,150 L0,150 Z" fill="url(#line-grad)"></path>
              <path d="M0,120 Q50,110 100,115 T200,80 T300,90 T400,60" fill="none" stroke="#a23f00" stroke-linecap="round" stroke-width="3"></path>
              <circle cx="200" cy="80" fill="#ffffff" r="4" stroke="#a23f00" stroke-width="2"></circle>
            </svg>
            <div class="absolute top-10 left-1/2 -translate-x-1/2 bg-on-surface text-white text-[10px] py-1 px-2 rounded opacity-0 group-hover:opacity-100 transition-opacity">{{ hoverTooltip }}</div>
          </div>
          <div class="flex justify-between mt-4 text-[10px] text-on-surface-variant/60 font-bold uppercase tracking-widest">
            <span>Mon</span><span>Wed</span><span>Fri</span><span>Sun</span>
          </div>
        </div>

        <!-- Sleep Duration (Stacked Bar Placeholder) -->
        <div class="bg-surface-container-lowest p-6 rounded-xl shadow-[0_20px_40px_rgba(86,67,55,0.04)] border border-orange-100/10">
          <div class="flex justify-between items-start mb-6">
            <div>
              <p class="text-on-surface-variant text-sm font-medium mb-1">平均睡眠时长</p>
              <h4 class="text-2xl font-bold">{{ averageSleep }} <span class="text-xs text-tertiary font-medium">↑ {{ sleepImprovement }}%</span></h4>
            </div>
            <span class="material-symbols-outlined text-on-surface-variant/40">bedtime</span>
          </div>
          <!-- Stacked Bar Visual -->
          <div class="h-48 flex items-end justify-between gap-3 px-2">
            <div v-for="day in sleepData" :key="day.day" class="w-full flex flex-col gap-1 items-center">
              <div class="w-full bg-secondary rounded-t-full" :style="{ height: day.deepSleep + '%' }"></div>
              <div class="w-full bg-secondary-container/30 rounded-b-full" :style="{ height: day.lightSleep + '%' }"></div>
              <span class="text-[9px] mt-2 font-bold" :class="day.isToday ? 'text-secondary opacity-100' : 'opacity-40'">{{ day.label }}</span>
            </div>
          </div>
          <div class="mt-4 flex gap-4 text-[10px] font-bold">
            <div class="flex items-center gap-1.5"><span class="w-2 h-2 rounded-full bg-secondary"></span> 深度睡眠</div>
            <div class="flex items-center gap-1.5"><span class="w-2 h-2 rounded-full bg-secondary-container/30"></span> 浅度睡眠</div>
          </div>
        </div>

        <!-- Steps/Movement (Bar Chart) -->
        <div class="col-span-1 md:col-span-2 bg-surface-container-lowest p-8 rounded-xl shadow-[0_20px_40px_rgba(86,67,55,0.04)] border border-orange-100/10">
          <div class="flex justify-between items-center mb-8">
            <div>
              <h3 class="text-xl font-bold">每日步数分析</h3>
              <p class="text-on-surface-variant text-sm">目标: 10,000 步/日</p>
            </div>
            <div class="text-right">
              <p class="text-on-surface-variant text-sm font-medium">周平均步数</p>
              <p class="text-3xl font-black text-primary">{{ averageSteps.toLocaleString() }}</p>
            </div>
          </div>
          <div class="h-40 flex items-end gap-2">
            <div v-for="day in stepsData" :key="day.day" class="flex-1 flex flex-col items-center gap-2 group cursor-pointer" @click="focusDay(day)">
              <div class="w-full bg-surface-container-low rounded-t-full relative overflow-hidden h-full">
                <div 
                  class="absolute bottom-0 w-full transition-all" 
                  :class="day.isToday ? 'bg-primary-container group-hover:bg-primary' : 'bg-primary-container/40 group-hover:bg-primary-container/60'"
                  :style="{ height: day.percentage + '%' }"
                ></div>
              </div>
              <span class="text-xs font-bold" :class="day.isToday ? 'text-primary' : 'text-on-surface-variant/40'">{{ day.label }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Supportive Quick Stats -->
    <section class="mt-12 grid grid-cols-2 md:grid-cols-4 gap-6">
      <div class="bg-surface p-6 rounded-lg border border-outline-variant/20">
        <p class="text-on-surface-variant text-xs font-bold uppercase tracking-widest mb-2">活跃分钟</p>
        <div class="flex items-end justify-between">
          <span class="text-2xl font-black">{{ activeMinutes }} <small class="text-sm font-medium">min</small></span>
          <span class="material-symbols-outlined text-tertiary">bolt</span>
        </div>
      </div>
      <div class="bg-surface p-6 rounded-lg border border-outline-variant/20">
        <p class="text-on-surface-variant text-xs font-bold uppercase tracking-widest mb-2">静息心率</p>
        <div class="flex items-end justify-between">
          <span class="text-2xl font-black">{{ restingHeartRate }} <small class="text-sm font-medium">bpm</small></span>
          <span class="material-symbols-outlined text-primary">favorite</span>
        </div>
      </div>
      <div class="bg-surface p-6 rounded-lg border border-outline-variant/20">
        <p class="text-on-surface-variant text-xs font-bold uppercase tracking-widest mb-2">饮水量</p>
        <div class="flex items-end justify-between">
          <span class="text-2xl font-black">{{ waterIntake }} <small class="text-sm font-medium">L</small></span>
          <span class="material-symbols-outlined text-blue-500">water_drop</span>
        </div>
      </div>
    </section>
  </main>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getStatisticsOverview, getStatisticsTrend, getRecordSummary, getWaterList, getUserId } from '../api'

const router = useRouter()

// 搜索查询
const searchQuery = ref('')

// 用户头像
const userAvatar = ref('https://lh3.googleusercontent.com/aida-public/AB6AXuChA2B-Y1C1HI34DXQRX14vYR2trYn-dippxcXdHFtW7Ji797Bk9mcgp_tZDwmyMXgPDQWuvyqvIqZmDQ6HDr5aitQab1ynYIkFKXZhO5N6GAkWcUXgEIoG1attkAQTCar7lXBGK9lwyTM1rv1FA2ie0kKVk3ap7Q2aUfQm2xMhhxItnRR1HXvux6Brn2mMMGzMt9F5EsaZG8nT_YDQ6MNW-z9eOha9UPr9MKLIu8A9G84It6XxRz9PqK8e6d7UGtPxNbSGliSxqfc')

// 时间范围
const activeTimeRange = ref('7day')
const timeRanges = ref([
  { label: '7天', value: '7day' },
  { label: '30天', value: '30day' },
  { label: '6个月', value: '6month' }
])

// 体重数据
const currentWeight = ref('0')
const weightChange = ref('0')
const hoverTooltip = ref('')

// 睡眠数据
const averageSleep = ref('0h 0m')
const sleepImprovement = ref(0)
const sleepData = ref([])

// 步数数据
const averageSteps = ref(0)
const stepsData = ref([])

// 健康指标
const activeMinutes = ref(0)
const restingHeartRate = ref('--')
const waterIntake = ref(0)

function mapPeriod(range) {
  if (range === '30day') return 'month'
  if (range === '6month') return 'half'
  return 'week'
}

function average(values) {
  if (!values || values.length === 0) return 0
  const total = values.reduce((sum, item) => sum + (Number(item) || 0), 0)
  return total / values.length
}

function formatSleepHours(hours) {
  const totalMinutes = Math.round(hours * 60)
  const h = Math.floor(totalMinutes / 60)
  const m = totalMinutes % 60
  return `${h}h ${m}m`
}

async function loadAnalysis() {
  const userId = getUserId()
  const period = mapPeriod(activeTimeRange.value)

  try {
    const [overview, trend, summary, waterList] = await Promise.all([
      getStatisticsOverview(userId, period),
      getStatisticsTrend(userId, period),
      getRecordSummary(userId),
      getWaterList(userId)
    ])

    const weightSeries = trend?.series?.weight || []
    const sleepSeries = trend?.series?.sleep || []
    const sleepDeepSeries = trend?.series?.sleepDeep || []
    const sleepLightSeries = trend?.series?.sleepLight || []
    const stepsSeries = trend?.series?.steps || []

    const lastWeight = weightSeries.length ? Number(weightSeries[weightSeries.length - 1].value || 0) : 0
    const prevWeight = weightSeries.length > 1 ? Number(weightSeries[weightSeries.length - 2].value || 0) : lastWeight
    const weightDiff = (lastWeight - prevWeight).toFixed(1)
    currentWeight.value = lastWeight ? lastWeight.toFixed(1) : '0'
    weightChange.value = Math.abs(weightDiff)
    hoverTooltip.value = lastWeight ? `${lastWeight.toFixed(1)}kg` : ''

    const avgSleepHours = average(sleepSeries.map(item => Number(item.value) || 0))
    averageSleep.value = formatSleepHours(avgSleepHours)
    const sleepDelta = Number(overview?.sleepDeltaMinutes || 0)
    const sleepBase = Number(overview?.sleepMinutesYesterday || 0)
    sleepImprovement.value = sleepBase ? Math.round((sleepDelta / sleepBase) * 100) : 0

    const maxSleepMinutes = Math.max(
      ...sleepDeepSeries.map((item, idx) => (Number(item.value) || 0) + (Number(sleepLightSeries[idx]?.value) || 0)),
      1
    )
    sleepData.value = sleepSeries.map((item, idx) => {
      const deep = Number(sleepDeepSeries[idx]?.value) || 0
      const light = Number(sleepLightSeries[idx]?.value) || 0
      const total = deep + light
      const deepPct = Math.round((deep / maxSleepMinutes) * 100)
      const lightPct = Math.round((light / maxSleepMinutes) * 100)
      const label = item.label?.toString().slice(0, 1) || ''
      return {
        day: item.label || '',
        deepSleep: deepPct,
        lightSleep: lightPct,
        isToday: idx === sleepSeries.length - 1,
        label
      }
    })

    const stepsValues = stepsSeries.map(item => Number(item.value) || 0)
    averageSteps.value = Math.round(average(stepsValues))
    stepsData.value = stepsSeries.map((item, idx) => {
      const steps = Number(item.value) || 0
      const percentage = Math.min(Math.round((steps / 10000) * 100), 100)
      return {
        day: item.label || '',
        steps,
        percentage,
        isToday: idx === stepsSeries.length - 1,
        label: item.label || ''
      }
    })

    activeMinutes.value = Number(summary?.exercise?.duration || overview?.exerciseMinutes || 0)
    restingHeartRate.value = summary?.health?.heartRate || '--'
    const totalMl = Array.isArray(waterList)
      ? waterList.reduce((sum, item) => sum + (Number(item.ml) || 0), 0)
      : 0
    waterIntake.value = totalMl ? (totalMl / 1000).toFixed(1) : 0
  } catch (error) {
    console.error('加载分析数据失败', error)
  }
}

// 方法
function toggleNotifications() {
  console.log('切换通知')
  // 这里可以添加实际的通知切换逻辑
}

function goToProfile() {
  router.push('/profile')
}

function generateReport() {
  console.log('生成PDF报告')
  // 这里可以添加生成报告的逻辑
}

function focusDay(day) {
  console.log('聚焦日期:', day)
  // 这里可以添加聚焦日期的逻辑，例如显示详细数据
}

watch(activeTimeRange, loadAnalysis)
onMounted(loadAnalysis)
</script>

<style scoped>
/* 自定义样式已在Tailwind中定义 */
</style>
