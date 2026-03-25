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
      <router-link to="/analysis" class="flex items-center gap-3 px-4 py-3 text-stone-600 dark:text-stone-400 hover:bg-stone-100 dark:hover:bg-stone-800 transition-all duration-300 font-manrope text-sm font-medium tracking-tight rounded-full">
        <span class="material-symbols-outlined">insights</span>数据分析
      </router-link>
      <router-link to="/goals" class="flex items-center gap-3 px-4 py-3 text-stone-600 dark:text-stone-400 hover:bg-stone-100 dark:hover:bg-stone-800 transition-all duration-300 font-manrope text-sm font-medium tracking-tight rounded-full">
        <span class="material-symbols-outlined">track_changes</span>目标管理
      </router-link>
      <!-- Active Tab: 综合记录 -->
      <router-link to="/records" class="flex items-center gap-3 px-4 py-3 bg-orange-100/50 dark:bg-orange-900/30 text-orange-800 dark:text-orange-200 rounded-full font-bold font-manrope text-sm tracking-tight">
        <span class="material-symbols-outlined" style="font-variation-settings: 'FILL' 1;">list_alt</span>综合记录
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
      <h2 class="text-lg font-black text-orange-900 dark:text-orange-100">综合健康记录</h2>
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

  <!-- Main Content Area -->
  <main class="pl-72 pr-8 pt-24 pb-12 min-h-screen">
    <div class="max-w-7xl mx-auto space-y-10">
      <!-- Hero Stats Bento -->
      <section class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <!-- Diet Summary -->
        <div class="bg-surface-container-lowest p-8 rounded-xl shadow-[0_20px_40px_rgba(86,67,55,0.04)] border-none relative overflow-hidden col-span-1">
          <div class="absolute top-0 right-0 w-32 h-32 bg-orange-50 rounded-full -mr-16 -mt-16 opacity-50"></div>
          <div class="relative z-10">
            <div class="flex items-center justify-between mb-6">
              <span class="p-3 bg-orange-100 rounded-2xl text-orange-700 material-symbols-outlined" style="font-variation-settings: 'FILL' 1;">restaurant</span>
              <span class="text-xs font-bold text-orange-800 bg-orange-50 px-3 py-1 rounded-full uppercase tracking-tighter">今日饮食</span>
            </div>
            <h3 class="text-4xl font-black font-headline text-on-surface mb-2">{{ dietSummaryCalories.toLocaleString() }} <span class="text-base font-normal text-on-surface-variant">kcal</span></h3>
            <p class="text-sm text-on-surface-variant mb-6 font-medium">{{ dietSummaryGapText }}</p>
            <div class="space-y-3">
              <div class="flex items-center justify-between text-xs">
                <span class="text-on-surface-variant">蛋白质</span>
                <span class="font-bold">{{ dietSummaryProtein }}g / 120g</span>
              </div>
              <div class="w-full h-2 bg-surface-container-high rounded-full overflow-hidden">
                <div class="h-full bg-primary" :style="{ width: proteinPercent + '%' }"></div>
              </div>
            </div>
          </div>
        </div>
        <!-- Exercise Summary -->
        <div class="bg-surface-container-lowest p-8 rounded-xl shadow-[0_20px_40px_rgba(86,67,55,0.04)] border-none relative overflow-hidden col-span-1">
          <div class="absolute top-0 right-0 w-32 h-32 bg-secondary-fixed rounded-full -mr-16 -mt-16 opacity-30"></div>
          <div class="relative z-10">
            <div class="flex items-center justify-between mb-6">
              <span class="p-3 bg-secondary-fixed text-secondary material-symbols-outlined" style="font-variation-settings: 'FILL' 1;">fitness_center</span>
              <span class="text-xs font-bold text-secondary bg-secondary-fixed px-3 py-1 rounded-full uppercase tracking-tighter">今日步数</span>
            </div>
            <h3 class="text-4xl font-black font-headline text-on-surface mb-2">{{ exerciseSummarySteps.toLocaleString() }} <span class="text-base font-normal text-on-surface-variant">步</span></h3>
            <p class="text-sm text-on-surface-variant mb-6 font-medium">距离 10,000 步还差 {{ exerciseGapPercent }}%</p>
            <div class="flex gap-4">
              <div class="flex-1">
                <p class="text-[10px] text-on-surface-variant uppercase font-bold mb-1">活跃时间</p>
                <p class="text-lg font-bold font-headline">{{ exerciseActiveMinutes }} <span class="text-xs">min</span></p>
              </div>
              <div class="flex-1">
                <p class="text-[10px] text-on-surface-variant uppercase font-bold mb-1">运动消耗</p>
                <p class="text-lg font-bold font-headline">{{ exerciseCalories }} <span class="text-xs">kcal</span></p>
              </div>
            </div>
          </div>
        </div>
        <!-- Sleep Summary -->
        <div class="bg-surface-container-lowest p-8 rounded-xl shadow-[0_20px_40px_rgba(86,67,55,0.04)] border-none relative overflow-hidden col-span-1">
          <div class="absolute top-0 right-0 w-32 h-32 bg-tertiary-fixed rounded-full -mr-16 -mt-16 opacity-30"></div>
          <div class="relative z-10">
            <div class="flex items-center justify-between mb-6">
              <span class="p-3 bg-tertiary-fixed text-tertiary material-symbols-outlined" style="font-variation-settings: 'FILL' 1;">bedtime</span>
              <span class="text-xs font-bold text-tertiary bg-tertiary-fixed px-3 py-1 rounded-full uppercase tracking-tighter">昨晚睡眠</span>
            </div>
            <h3 class="text-4xl font-black font-headline text-on-surface mb-2">{{ sleepSummaryHours }} <span class="text-base font-normal text-on-surface-variant">h</span></h3>
            <p class="text-sm text-on-surface-variant mb-6 font-medium">睡眠质量：{{ sleepSummaryQuality }}</p>
            <div class="flex items-end gap-2 h-12">
              <div class="w-3 bg-tertiary rounded-full h-full opacity-40"></div>
              <div class="w-3 bg-tertiary rounded-full h-[60%] opacity-40"></div>
              <div class="w-3 bg-tertiary rounded-full h-[80%] opacity-40"></div>
              <div class="w-3 bg-tertiary rounded-full h-full"></div>
              <div class="w-3 bg-tertiary rounded-full h-[90%] opacity-40"></div>
              <div class="w-3 bg-tertiary rounded-full h-[40%] opacity-40"></div>
              <div class="w-3 bg-tertiary rounded-full h-[70%] opacity-40"></div>
            </div>
          </div>
        </div>
      </section>

      <!-- Detailed Records Grid -->
      <section class="grid grid-cols-1 xl:grid-cols-2 gap-8">
        <!-- Diet Log Section -->
        <div class="space-y-6">
          <div class="flex items-center justify-between px-2">
            <h4 class="text-xl font-bold font-headline">饮食明细</h4>
            <span class="text-xs text-on-surface-variant font-medium">今日 {{ dietData.length }} 笔记录</span>
          </div>
          <div class="space-y-4 max-h-[500px] overflow-y-auto no-scrollbar">
            <div v-for="item in dietData" :key="item.meal + item.time" class="bg-white p-6 rounded-xl flex items-center justify-between group transition-all hover:translate-x-1">
              <div class="flex items-center gap-4">
                <div class="w-12 h-12 rounded-full bg-orange-50 flex items-center justify-center">
                  <span class="material-symbols-outlined text-orange-700">{{ mealIcon(item.meal) }}</span>
                </div>
                <div>
                  <p class="font-bold text-on-surface">{{ item.meal }}</p>
                  <p class="text-xs text-on-surface-variant">{{ item.time }} · {{ item.items }}</p>
                </div>
              </div>
              <div class="text-right">
                <p class="font-bold text-on-surface">{{ item.calories }} kcal</p>
                <p class="text-[10px] text-orange-700 font-bold tracking-widest uppercase">{{ item.quality }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Exercise & Sleep Logs Combined -->
        <div class="space-y-6">
          <div class="flex items-center justify-between px-2">
            <h4 class="text-xl font-bold font-headline">活跃与休息</h4>
            <span class="text-xs text-on-surface-variant font-medium">详情分析</span>
          </div>
          <!-- Exercise Details Card -->
          <div class="bg-surface-container-low p-6 rounded-xl">
            <div class="flex items-center gap-3 mb-6">
              <span class="material-symbols-outlined text-secondary">directions_run</span>
              <span class="font-bold">运动轨迹记录</span>
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div v-for="item in exerciseData" :key="item.type + item.value" class="bg-white p-4 rounded-xl">
                <p class="text-[10px] text-on-surface-variant uppercase font-bold mb-1">{{ item.type }}</p>
                <div class="flex items-baseline gap-1">
                  <span class="text-2xl font-black font-headline text-on-surface">{{ item.value }}</span>
                  <span class="text-xs text-on-surface-variant">{{ item.unit }}</span>
                </div>
                <p class="text-[10px] text-secondary mt-2">{{ item.status }}</p>
              </div>
            </div>
          </div>
          <!-- Sleep Details Card -->
          <div class="bg-surface-container-low p-6 rounded-xl">
            <div class="flex items-center gap-3 mb-6">
              <span class="material-symbols-outlined text-tertiary">auto_awesome</span>
              <span class="font-bold">睡眠质量分析</span>
            </div>
            <div class="space-y-6">
              <div class="flex justify-between items-end h-32 gap-3 px-4">
                <div v-for="item in sleepData" :key="item.date" class="flex-1 flex flex-col justify-end gap-2 group">
                  <div class="bg-tertiary-fixed rounded-t-lg group-hover:bg-tertiary transition-colors"
                       :style="{ height: (item.deepSleep / sleepMaxMinutes * 100) + '%' }"></div>
                  <p class="text-[8px] text-center font-bold text-on-surface-variant">{{ item.date }}</p>
                </div>
              </div>
              <div class="grid grid-cols-2 gap-6 pt-4 border-t border-white">
                <div class="flex items-center gap-3">
                  <div class="w-2 h-2 rounded-full bg-tertiary"></div>
                  <div>
                    <p class="text-[10px] text-on-surface-variant font-bold uppercase">深度睡眠</p>
                    <p class="text-sm font-bold">{{ sleepDeepSummary }}</p>
                  </div>
                </div>
                <div class="flex items-center gap-3">
                  <div class="w-2 h-2 rounded-full bg-tertiary-fixed-dim"></div>
                  <div>
                    <p class="text-[10px] text-on-surface-variant font-bold uppercase">浅度睡眠</p>
                    <p class="text-sm font-bold">{{ sleepLightSummary }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Bottom Note -->
      <footer class="pt-8 pb-12 flex flex-col items-center text-center">
        <div class="w-16 h-1 bg-orange-100 rounded-full mb-6"></div>
        <p class="text-sm text-on-surface-variant max-w-lg">
          所有数据均由智能穿戴设备与用户手动输入同步，最后更新于 5 分钟前。
          <br/>保持关注，智康AI 将持续为您提供专业健康建议。
        </p>
      </footer>
    </div>
  </main>

  <!-- Visual Decor Elements -->
  <div class="fixed -bottom-24 -left-24 w-96 h-96 bg-orange-100 rounded-full blur-[100px] -z-10 opacity-30"></div>
  <div class="fixed top-24 right-0 w-64 h-64 bg-secondary-fixed rounded-full blur-[80px] -z-10 opacity-20"></div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getRecordSummary, getDietList, getExerciseList, getSleepList, getUserId } from '../api'

const router = useRouter()

// 搜索查询
const searchQuery = ref('')

// 用户头像
const userAvatar = ref('https://lh3.googleusercontent.com/aida-public/AB6AXuChA2B-Y1C1HI34DXQRX14vYR2trYn-dippxcXdHFtW7Ji797Bk9mcgp_tZDwmyMXgPDQWuvyqvIqZmDQ6HDr5aitQab1ynYIkFKXZhO5N6GAkWcUXgEIoG1attkAQTCar7lXBGK9lwyTM1rv1FA2ie0kKVk3ap7Q2aUfQm2xMhhxItnRR1HXvux6Brn2mMMGzMt9F5EsaZG8nT_YDQ6MNW-z9eOha9UPr9MKLIu8A9G84It6XxRz9PqK8e6d7UGtPxNbSGliSxqfc')

// 饮食数据
const dietData = ref([])

// 运动数据
const exerciseData = ref([])

// 睡眠数据
const sleepData = ref([])
const sleepMaxMinutes = computed(() => {
  const values = sleepData.value.map(item => Number(item.deepSleep || 0))
  return Math.max(...values, 1)
})

const dietSummaryCalories = ref(0)
const dietSummaryProtein = ref(0)
const dietSummaryGapText = ref('暂无目标')
const proteinPercent = ref(0)

const exerciseSummarySteps = ref(0)
const exerciseGapPercent = ref(0)
const exerciseActiveMinutes = ref(0)
const exerciseCalories = ref(0)

const sleepSummaryHours = ref(0)
const sleepSummaryQuality = ref('--')
const sleepDeepSummary = ref('0h')
const sleepLightSummary = ref('0h')

function formatTime(value) {
  if (!value) return '--'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    const text = value.toString()
    return text.length >= 16 ? text.slice(11, 16) : text
  }
  return date.toTimeString().slice(0, 5)
}

function mealIcon(meal) {
  if (meal.includes('早')) return 'breakfast_dining'
  if (meal.includes('午')) return 'lunch_dining'
  if (meal.includes('晚')) return 'dinner_dining'
  return 'cookie'
}

function mealTag(meal) {
  if (meal.includes('早')) return '优质摄入'
  if (meal.includes('午')) return '高蛋白'
  if (meal.includes('晚')) return '低碳'
  return '常规'
}

async function loadRecords() {
  try {
    const userId = getUserId()
    const today = new Date().toISOString().slice(0, 10)
    const [summary, diets, exercises, sleeps] = await Promise.all([
      getRecordSummary(userId),
      getDietList(userId, today),
      getExerciseList(userId, today),
      getSleepList(userId, today)
    ])

    const dietList = Array.isArray(diets) ? diets : []
    dietData.value = dietList.map(item => ({
      meal: item.mealType || '其他',
      calories: Number(item.calories || 0),
      time: item.date || today,
      items: item.foodName || '未填写',
      quality: mealTag(item.mealType || '')
    }))

    dietSummaryCalories.value = dietList.reduce((sum, item) => sum + (Number(item.calories) || 0), 0)
    dietSummaryProtein.value = dietList.reduce((sum, item) => sum + (Number(item.protein) || 0), 0)
    const proteinTarget = 120
    proteinPercent.value = proteinTarget ? Math.min(100, Math.round((dietSummaryProtein.value / proteinTarget) * 100)) : 0
    dietSummaryGapText.value = dietSummaryCalories.value
      ? `今日摄入 ${dietSummaryCalories.value} kcal`
      : '暂无饮食记录'

    const exerciseList = Array.isArray(exercises) ? exercises : []
    exerciseData.value = exerciseList.map(item => {
      const value = item.steps ? (item.steps / 1000).toFixed(1) : (item.duration || 0)
      const unit = item.steps ? 'km' : 'min'
      return {
        type: item.type || '运动',
        value,
        unit,
        status: item.calories ? `消耗 ${item.calories} kcal` : '已记录'
      }
    })
    exerciseSummarySteps.value = Number(summary?.exercise?.steps || 0)
    exerciseGapPercent.value = exerciseSummarySteps.value
      ? Math.max(0, Math.round((1 - exerciseSummarySteps.value / 10000) * 100))
      : 0
    exerciseActiveMinutes.value = Number(summary?.exercise?.duration || 0)
    exerciseCalories.value = Number(summary?.exercise?.calories || 0)

    const sleepList = Array.isArray(sleeps) ? sleeps : []
    sleepData.value = sleepList.map(item => {
      const deep = Number(item.deepSleepMinutes || 0)
      const light = Number(item.lightSleepMinutes || 0)
      return {
        date: item.recordDate || today,
        deepSleep: deep,
        lightSleep: light
      }
    })
    const lastSleep = sleepList.length ? sleepList[sleepList.length - 1] : null
    if (lastSleep?.startTime && lastSleep?.endTime) {
      const start = new Date(lastSleep.startTime)
      const end = new Date(lastSleep.endTime)
      const minutes = Math.max(0, (end - start) / 60000)
      sleepSummaryHours.value = (minutes / 60).toFixed(1)
    }
    sleepSummaryQuality.value = lastSleep?.quality || '--'
    if (lastSleep) {
      const deep = Number(lastSleep.deepSleepMinutes || 0)
      const light = Number(lastSleep.lightSleepMinutes || 0)
      sleepDeepSummary.value = deep ? `${Math.floor(deep / 60)}h ${deep % 60}m` : '0h'
      sleepLightSummary.value = light ? `${Math.floor(light / 60)}h ${light % 60}m` : '0h'
    }
  } catch (error) {
    console.error('加载健康记录失败', error)
  }
}

// 方法
function toggleNotifications() {
  console.log('切换通知')
  // 这里可以添加实际的通知切换逻辑
}

function goToProfile() {
  router.push('/settings')
}

onMounted(loadRecords)
</script>

<style scoped>
.no-scrollbar::-webkit-scrollbar {
  display: none;
}
.no-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>
