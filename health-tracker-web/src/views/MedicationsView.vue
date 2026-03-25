<template>
  <!-- SideNavBar Shell -->
  <aside class="fixed left-0 top-0 h-full flex flex-col p-4 h-screen w-64 bg-stone-50/70 dark:bg-stone-900/70 backdrop-blur-xl shadow-[20px_0_40px_rgba(86,67,55,0.04)] z-50">
    <div class="mb-10 px-4">
      <h1 class="text-xl font-bold text-orange-800 dark:text-orange-400 tracking-wider font-headline">智康AI</h1>
      <p class="text-[10px] text-on-surface-variant uppercase tracking-[0.2em] mt-1">智能健康管理助手</p>
    </div>
    <nav class="flex-1 space-y-1">
      <router-link to="/" class="flex items-center gap-3 px-4 py-3 text-stone-600 dark:text-stone-400 hover:bg-stone-100 dark:hover:bg-stone-800 transition-all duration-300 font-manrope text-sm font-medium tracking-tight rounded-full">
        <span class="material-symbols-outlined">home</span>
        <span>首页</span>
      </router-link>
      <router-link to="/analysis" class="flex items-center gap-3 px-4 py-3 text-stone-600 dark:text-stone-400 hover:bg-stone-100 dark:hover:bg-stone-800 transition-all duration-300 font-manrope text-sm font-medium tracking-tight rounded-full">
        <span class="material-symbols-outlined">insights</span>
        <span>数据分析</span>
      </router-link>
      <router-link to="/goals" class="flex items-center gap-3 px-4 py-3 text-stone-600 dark:text-stone-400 hover:bg-stone-100 dark:hover:bg-stone-800 transition-all duration-300 font-manrope text-sm font-medium tracking-tight rounded-full">
        <span class="material-symbols-outlined">track_changes</span>
        <span>目标管理</span>
      </router-link>
      <router-link to="/records" class="flex items-center gap-3 px-4 py-3 text-stone-600 dark:text-stone-400 hover:bg-stone-100 dark:hover:bg-stone-800 transition-all duration-300 font-manrope text-sm font-medium tracking-tight rounded-full">
        <span class="material-symbols-outlined">restaurant</span>
        <span>饮食记录</span>
      </router-link>
      <router-link to="/medications" class="flex items-center gap-3 px-4 py-3 bg-orange-100/50 dark:bg-orange-900/30 text-orange-800 dark:text-orange-200 rounded-full font-bold font-manrope text-sm tracking-tight">
        <span class="material-symbols-outlined" style="font-variation-settings: 'FILL' 1;">medication</span>
        <span>用药管理</span>
      </router-link>
      <router-link to="/settings" class="flex items-center gap-3 px-4 py-3 text-stone-600 dark:text-stone-400 hover:bg-stone-100 dark:hover:bg-stone-800 transition-all duration-300 font-manrope text-sm font-medium tracking-tight rounded-full">
        <span class="material-symbols-outlined">settings</span>
        <span>设置</span>
      </router-link>
    </nav>
    <!-- 个人中心菜单已移至设置 -->

  </aside>

  <!-- TopNavBar Shell -->
  <header class="fixed top-0 right-0 left-0 h-16 bg-white/70 dark:bg-stone-950/70 backdrop-blur-md flex justify-between items-center pl-72 pr-8 py-2 z-40">
    <div class="flex items-center gap-2">
      <span class="text-lg font-black text-orange-900 dark:text-orange-100 font-headline uppercase tracking-tighter">智康AI (Zhiking AI)</span>
    </div>
    <div class="flex items-center gap-6">
      <div class="relative flex items-center bg-surface-container-low rounded-full px-4 py-1.5 w-64">
        <span class="material-symbols-outlined text-on-surface-variant text-sm" data-icon="search">search</span>
        <input 
          v-model="searchQuery"
          class="bg-transparent border-none focus:ring-0 text-sm w-full placeholder:text-on-surface-variant/50" 
          placeholder="搜索健康记录..." 
          type="text"
        />
      </div>
      <div class="flex items-center gap-4 text-orange-700 dark:text-orange-500">
        <button @click="toggleNotifications" class="material-symbols-outlined hover:text-orange-600 transition-colors" data-icon="notifications">notifications</button>
        <router-link to="/settings" class="material-symbols-outlined hover:text-orange-600 transition-colors" data-icon="settings">settings</router-link>
        <div class="w-8 h-8 rounded-full bg-primary-container overflow-hidden border-2 border-primary-fixed cursor-pointer" @click="goToProfile">
          <img 
            alt="User Avatar" 
            class="w-full h-full object-cover" 
            :src="userAvatar" 
          />
        </div>
      </div>
    </div>
  </header>

  <!-- Main Content Canvas -->
  <main class="ml-64 pt-24 px-10 pb-12 min-h-screen">
    <div class="max-w-7xl mx-auto space-y-10">
      <!-- Section 1: Medication List Header -->
      <header class="flex flex-col gap-2">
        <div class="flex items-center gap-3">
          <span class="w-1.5 h-8 bg-primary rounded-full"></span>
          <h2 class="text-3xl font-bold font-headline tracking-tight text-on-surface">用药与经期管理</h2>
        </div>
        <p class="text-on-surface-variant body-lg pl-5">今日健康计划执行状态及未来生理周期预测</p>
      </header>

      <div class="bento-grid grid grid-cols-12 gap-6">
        <!-- Medication Overview Card -->
        <section class="col-span-12 lg:col-span-7 bg-surface-container-lowest p-8 rounded-xl shadow-[0_20px_40px_rgba(86,67,55,0.04)] relative overflow-hidden">
          <div class="absolute top-0 right-0 p-8 opacity-5">
            <span class="material-symbols-outlined text-9xl" data-icon="pill">pill</span>
          </div>
          <div class="flex justify-between items-end mb-8 relative z-10">
            <div>
              <h3 class="text-xl font-bold font-headline mb-1">今日用药记录</h3>
              <p class="text-sm text-on-surface-variant">{{ todayDate }}</p>
            </div>
            <div class="bg-tertiary/10 text-tertiary px-4 py-2 rounded-full flex items-center gap-2">
              <span class="material-symbols-outlined text-sm" data-icon="check_circle">check_circle</span>
              <span class="text-xs font-bold">已完成 {{ completedMedications }}/{{ totalMedications }}</span>
            </div>
          </div>
          <div class="space-y-4 relative z-10">
            <div v-for="item in medicationItems" :key="item.id" class="flex items-center justify-between p-5 rounded-lg group transition-all duration-300"
                 :class="item.status === 'pending' ? 'bg-surface-container-lowest border-2 border-orange-100/50' : 'bg-surface-container-low'">
              <div class="flex items-center gap-5">
                <div class="w-12 h-12 rounded-xl flex items-center justify-center"
                     :class="item.status === 'pending' ? 'bg-orange-100 text-primary' : 'bg-primary/10 text-primary'">
                  <span class="material-symbols-outlined" data-icon="medication">medication</span>
                </div>
                <div>
                  <p class="font-bold text-on-surface">{{ item.name }}</p>
                  <p class="text-xs text-on-surface-variant mt-0.5">{{ item.desc }}</p>
                </div>
              </div>
              <div class="flex flex-col items-end gap-1">
                <span class="text-xs font-bold" :class="item.status === 'completed' ? 'text-tertiary' : 'text-primary'">{{ item.time }} {{ item.statusLabel }}</span>
                <span class="text-[10px] text-on-surface-variant font-medium">{{ item.note }}</span>
              </div>
            </div>
          </div>
        </section>

        <!-- Menstrual Summary Card -->
        <section class="col-span-12 lg:col-span-5 bg-secondary-container p-8 rounded-xl relative overflow-hidden group">
          <div class="absolute inset-0 bg-gradient-to-br from-primary/20 to-transparent"></div>
          <div class="absolute -right-12 -bottom-12 w-48 h-48 bg-white/10 rounded-full blur-3xl group-hover:bg-white/20 transition-all duration-700"></div>
          <div class="relative z-10 flex flex-col h-full">
            <div class="flex justify-between items-start mb-10">
              <div class="bg-white/20 backdrop-blur-md p-3 rounded-xl">
                <span class="material-symbols-outlined text-white" data-icon="calendar_month">calendar_month</span>
              </div>
              <div class="text-right">
                <span class="text-[10px] text-white/70 uppercase tracking-widest font-bold">Current Phase</span>
                <p class="text-white text-xl font-bold font-headline">{{ menstrualPhase }}</p>
              </div>
            </div>
            <div class="mt-auto">
              <p class="text-white/80 text-sm font-medium">距离下次经期还有</p>
              <div class="flex items-baseline gap-2 mt-2">
                <span class="text-6xl font-black text-white font-headline">{{ daysToNextPeriod }}</span>
                <span class="text-2xl font-bold text-white">天</span>
              </div>
              <div class="mt-8 space-y-4">
                <div class="h-2 w-full bg-white/20 rounded-full overflow-hidden">
                  <div class="h-full bg-white rounded-full" :style="{ width: cycleProgress + '%' }"></div>
                </div>
                <div class="flex justify-between text-[10px] text-white/80 font-bold uppercase">
                  <span>上次开始: {{ lastPeriodStart }}</span>
                  <span>预计开始: {{ nextPeriodStart }}</span>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- Menstrual Timeline View -->
        <section class="col-span-12 bg-surface-container-low p-8 rounded-xl">
          <div class="flex items-center justify-between mb-10">
            <h3 class="text-xl font-bold font-headline">生理周期时间轴</h3>
            <div class="flex items-center gap-6">
              <div class="flex items-center gap-2">
                <span class="w-3 h-3 rounded-full bg-primary-container"></span>
                <span class="text-xs text-on-surface-variant font-medium">月经期</span>
              </div>
              <div class="flex items-center gap-2">
                <span class="w-3 h-3 rounded-full bg-tertiary-container"></span>
                <span class="text-xs text-on-surface-variant font-medium">排卵期</span>
              </div>
              <div class="flex items-center gap-2">
                <span class="w-3 h-3 rounded-full bg-surface-container-high"></span>
                <span class="text-xs text-on-surface-variant font-medium">安全期</span>
              </div>
            </div>
          </div>
          <!-- Horizontal Timeline Scroll -->
          <div class="relative flex items-center pb-6">
            <div class="flex w-full justify-between items-start relative px-4">
              <!-- Timeline Line -->
              <div class="absolute top-7 left-0 right-0 h-0.5 bg-outline-variant/30 z-0"></div>
              
              <!-- Timeline Days -->
              <div v-for="day in timelineDays" :key="day.date" class="flex flex-col items-center gap-4 relative z-10" :class="{ 'opacity-40': !day.isCurrent && !day.isToday, 'scale-110': day.isToday }">
                <span class="text-xs font-bold" :class="day.isToday ? 'text-primary font-black' : 'text-on-surface-variant'">{{ day.displayDate }}</span>
                <div 
                  class="flex items-center justify-center text-xs" 
                  :class="getDayCellClasses(day)"
                  :style="day.isToday ? 'width: 4rem; height: 4rem;' : 'width: 3.5rem; height: 3.5rem;'"
                >
                  {{ day.phase }}
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- Future Predictions Bento -->
        <section class="col-span-12 lg:col-span-4 bg-surface-container-lowest p-6 rounded-xl border border-outline-variant/10">
          <div class="flex items-center gap-3 mb-6">
            <span class="material-symbols-outlined text-primary" data-icon="query_stats">query_stats</span>
            <h4 class="font-bold">预测洞察</h4>
          </div>
          <div class="space-y-4">
            <div class="p-4 bg-surface-container-low rounded-lg">
              <p class="text-xs text-on-surface-variant font-medium">易孕期起始</p>
              <p class="text-lg font-black font-headline mt-1">{{ fertileWindowStart }}</p>
            </div>
            <div class="p-4 bg-surface-container-low rounded-lg">
              <p class="text-xs text-on-surface-variant font-medium">下次经期起始</p>
              <p class="text-lg font-black font-headline mt-1">{{ nextPeriodStart }}</p>
            </div>
          </div>
        </section>

        <!-- Health Suggestions Card -->
        <section class="col-span-12 lg:col-span-8 bg-surface-container-lowest p-6 rounded-xl border border-outline-variant/10">
          <div class="flex items-center gap-3 mb-6">
            <span class="material-symbols-outlined text-secondary" data-icon="lightbulb">lightbulb</span>
            <h4 class="font-bold">健康建议</h4>
          </div>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="flex gap-4 p-4 hover:bg-surface-container-low transition-colors rounded-xl cursor-default">
              <div class="w-10 h-10 shrink-0 rounded-full bg-tertiary/10 text-tertiary flex items-center justify-center">
                <span class="material-symbols-outlined text-sm" data-icon="spa">spa</span>
              </div>
              <div>
                <p class="text-sm font-bold text-on-surface">即将进入排卵期</p>
                <p class="text-xs text-on-surface-variant mt-1 leading-relaxed">体温可能会有轻微升高，建议保持规律作息，增加水分摄入。</p>
              </div>
            </div>
            <div class="flex gap-4 p-4 hover:bg-surface-container-low transition-colors rounded-xl cursor-default">
              <div class="w-10 h-10 shrink-0 rounded-full bg-primary/10 text-primary flex items-center justify-center">
                <span class="material-symbols-outlined text-sm" data-icon="restaurant_menu">restaurant_menu</span>
              </div>
              <div>
                <p class="text-sm font-bold text-on-surface">用药与营养</p>
                <p class="text-xs text-on-surface-variant mt-1 leading-relaxed">阿托伐他汀对血脂管理至关重要，请确保晚餐后准时服用。</p>
              </div>
            </div>
          </div>
        </section>
      </div>
    </div>
  </main>

  <!-- Bottom Toast (Read-only indication) -->
  <div class="fixed bottom-8 left-1/2 -translate-x-1/2 z-50">
    <div class="bg-inverse-surface text-inverse-on-surface px-6 py-3 rounded-full shadow-2xl flex items-center gap-3">
      <span class="material-symbols-outlined text-sm" data-icon="visibility">visibility</span>
      <span class="text-xs font-medium tracking-wide">当前为预览模式 · 记录已自动同步</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMedicationList, getMedicationRecordList, getPeriodList, getUserId } from '../api'

const router = useRouter()

// 搜索查询
const searchQuery = ref('')

// 用户头像
const userAvatar = ref('https://lh3.googleusercontent.com/aida-public/AB6AXuDoaZ7A9822Xc9T_xjvP-G_ZEPOlrjaCKU9ta2oPYg_ItH1dRL7bFp94eQyvcIL6-Kl3LR6ElF6Ceql1-bp6-rrM7r8lrisA0bWF0FtMNAFbo8aJBagJo9fgWtv_beKMput-JnnaYvsy5UvoYt3HV8tT79QmfNLWLkBIiZXEPoPupEADvrg9uRfE2-7fg3q8azCq8aKLOr9mA88cFbS3_dFbIbOeTKmc61BNel7lxTcFA4YBuJsyT8vxa60QsyVN8aGh4SgGcovT_k')

// 用药数据
const todayDate = ref('')
const completedMedications = ref(0)
const totalMedications = ref(0)
const medicationItems = ref([])

// 经期数据
const menstrualPhase = ref('暂无')
const daysToNextPeriod = ref(0)
const lastPeriodStart = ref('--')
const nextPeriodStart = ref('--')
const fertileWindowStart = ref('--')
const cycleLength = ref(28)
const periodLength = ref(5)

// 周期进度（百分比）
const cycleProgress = computed(() => {
  if (daysToNextPeriod.value === 0) return 0
  const passed = Math.max(0, cycleLength.value - daysToNextPeriod.value)
  return Math.round((passed / cycleLength.value) * 100)
})

// 时间轴天数数据
const timelineDays = ref([])

// 方法
function toggleNotifications() {
  console.log('切换通知')
  // 这里可以添加实际的通知切换逻辑
}

function goToProfile() {
  router.push('/settings')
}

function getDayCellClasses(day) {
  const baseClasses = 'rounded-full flex items-center justify-center'
  
  if (day.isToday) {
    return `${baseClasses} bg-primary text-white shadow-xl ring-4 ring-primary/20`
  }
  
  if (day.phaseType === 'ovulation-high') {
    return `${baseClasses} bg-tertiary-container/10 border-2 border-tertiary-container font-bold text-tertiary-container`
  }
  
  if (day.phaseType === 'ovulation') {
    return `${baseClasses} bg-tertiary-container/10 border-2 border-tertiary-container font-bold text-tertiary-container`
  }
  
  return `${baseClasses} bg-white border border-outline-variant/30 ${day.isCurrent ? 'shadow-sm' : ''}`
}

function formatDateLabel(date) {
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'short' })
}

function formatTime(value) {
  if (!value) return '--'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    const text = value.toString()
    return text.length >= 16 ? text.slice(11, 16) : text
  }
  return date.toTimeString().slice(0, 5)
}

async function loadMedications() {
  try {
    const userId = getUserId()
    const [medications, records, periods] = await Promise.all([
      getMedicationList(userId),
      getMedicationRecordList(userId),
      getPeriodList(userId)
    ])

    const list = Array.isArray(medications) ? medications : []
    const recordList = Array.isArray(records) ? records : []
    const today = new Date()
    todayDate.value = formatDateLabel(today)
    totalMedications.value = list.length

    const todayKey = today.toISOString().slice(0, 10)
    const todayRecords = recordList.filter(item => item.date === todayKey)
    completedMedications.value = todayRecords.filter(item => item.status === 1).length

    const recordMap = new Map()
    todayRecords.forEach(item => {
      recordMap.set(item.medicationId, item)
    })

    medicationItems.value = list.map(item => {
      const record = recordMap.get(item.id)
      const status = record?.status === 1 ? 'completed' : record?.status === 2 ? 'missed' : 'pending'
      return {
        id: item.id,
        name: item.drugName || '未命名药物',
        desc: `${item.dosage || ''} · ${item.frequency || ''}`.replace(/·\s*·/g, '·'),
        time: record?.time ? record.time : formatTime(item.remindTime),
        status,
        statusLabel: status === 'completed' ? '已服用' : status === 'missed' ? '漏服' : '待服用',
        note: item.notes || '常规记录'
      }
    })

    if (Array.isArray(periods) && periods.length > 0) {
      const latest = periods[periods.length - 1]
      if (latest.startDate) {
        lastPeriodStart.value = latest.startDate.slice(5)
      }
      if (latest.endDate) {
        nextPeriodStart.value = latest.endDate.slice(5)
      }
    }
  } catch (error) {
    console.error('加载用药数据失败', error)
  }
}

onMounted(loadMedications)
</script>

<style scoped>
/* 自定义样式已在Tailwind中定义 */
</style>
