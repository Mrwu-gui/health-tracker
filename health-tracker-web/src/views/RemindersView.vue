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
      <router-link to="/records" class="flex items-center gap-3 px-4 py-3 text-stone-600 dark:text-stone-400 hover:bg-stone-100 dark:hover:bg-stone-800 transition-all duration-300 font-manrope text-sm font-medium tracking-tight rounded-full">
        <span class="material-symbols-outlined">list_alt</span>综合记录
      </router-link>
      <!-- Active Tab: 提醒列表 -->
      <router-link to="/reminders" class="flex items-center gap-3 px-4 py-3 bg-orange-100/50 dark:bg-orange-900/30 text-orange-800 dark:text-orange-200 rounded-full font-bold font-manrope text-sm tracking-tight">
        <span class="material-symbols-outlined" style="font-variation-settings: 'FILL' 1;">notifications</span>提醒列表
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
      <h2 class="text-lg font-black text-orange-900 dark:text-orange-100">提醒管理</h2>
    </div>
    <div class="flex items-center gap-4">
      <div class="relative group">
        <input 
          v-model="searchQuery"
          class="bg-surface-container-low border-none rounded-full px-5 py-1.5 text-sm w-64 focus:ring-2 focus:ring-primary/20 transition-all" 
          placeholder="搜索提醒..." 
          type="text"
        />
        <span class="material-symbols-outlined absolute right-3 top-1/2 -translate-y-1/2 text-on-surface-variant text-lg">search</span>
      </div>
      <button @click="toggleNotifications" class="w-10 h-10 flex items-center justify-center rounded-full hover:bg-surface-container-high transition-colors text-on-surface-variant">
        <span class="material-symbols-outlined">notifications_active</span>
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
    <div class="max-w-6xl mx-auto">
      <!-- Header with Add Button -->
      <div class="flex justify-between items-center mb-10">
        <div>
          <h1 class="text-3xl font-extrabold text-on-surface tracking-tight mb-2">我的提醒</h1>
          <p class="text-on-surface-variant text-lg font-light">管理您的用药、运动、饮食等健康提醒</p>
        </div>
        <button 
          @click="showAddModal = true"
          class="amber-gradient text-white font-headline font-bold py-4 px-8 rounded-full shadow-lg shadow-primary/25 hover:shadow-xl hover:scale-[1.02] active:scale-95 transition-all flex items-center justify-center gap-2 group"
        >
          <span class="material-symbols-outlined">add</span>新建提醒
        </button>
      </div>

      <!-- Stats Summary -->
      <div class="grid grid-cols-4 gap-6 mb-10">
        <div class="bg-surface p-6 rounded-lg border border-outline-variant/20">
          <p class="text-on-surface-variant text-xs font-bold uppercase tracking-widest mb-2">今日提醒</p>
          <div class="flex items-end justify-between">
            <span class="text-2xl font-black">{{ todayReminders }} <small class="text-sm font-medium">个</small></span>
            <span class="material-symbols-outlined text-primary">today</span>
          </div>
        </div>
        <div class="bg-surface p-6 rounded-lg border border-outline-variant/20">
          <p class="text-on-surface-variant text-xs font-bold uppercase tracking-widest mb-2">已完成</p>
          <div class="flex items-end justify-between">
            <span class="text-2xl font-black">{{ completedReminders }} <small class="text-sm font-medium">个</small></span>
            <span class="material-symbols-outlined text-tertiary">check_circle</span>
          </div>
        </div>
        <div class="bg-surface p-6 rounded-lg border border-outline-variant/20">
          <p class="text-on-surface-variant text-xs font-bold uppercase tracking-widest mb-2">待执行</p>
          <div class="flex items-end justify-between">
            <span class="text-2xl font-black">{{ pendingReminders }} <small class="text-sm font-medium">个</small></span>
            <span class="material-symbols-outlined text-secondary">schedule</span>
          </div>
        </div>
        <div class="bg-surface p-6 rounded-lg border border-outline-variant/20">
          <p class="text-on-surface-variant text-xs font-bold uppercase tracking-widest mb-2">已错过</p>
          <div class="flex items-end justify-between">
            <span class="text-2xl font-black">{{ missedReminders }} <small class="text-sm font-medium">个</small></span>
            <span class="material-symbols-outlined text-error">warning</span>
          </div>
        </div>
      </div>

      <!-- Reminders List -->
      <div class="space-y-6">
        <div v-for="reminder in filteredReminders" :key="reminder.id" 
             class="bg-surface-container-lowest p-6 rounded-xl shadow-[0_20px_40px_rgba(86,67,55,0.04)] border border-orange-100/10 hover:border-primary/20 transition-all group"
             :class="{ 'opacity-60': reminder.status === 'completed' }">
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-4">
              <div class="w-12 h-12 rounded-full flex items-center justify-center" 
                   :class="typeColorClass(reminder.type)">
                <span class="material-symbols-outlined text-xl">{{ typeIcon(reminder.type) }}</span>
              </div>
              <div>
                <h3 class="font-bold text-lg text-on-surface">{{ reminder.content }}</h3>
                <div class="flex items-center gap-4 mt-1">
                  <span class="text-sm text-on-surface-variant flex items-center gap-1">
                    <span class="material-symbols-outlined text-sm">schedule</span>
                    {{ reminder.time }}
                  </span>
                  <span class="text-xs font-bold px-3 py-1 rounded-full uppercase tracking-tighter" 
                        :class="typeLabelClass(reminder.type)">
                    {{ typeLabel(reminder.type) }}
                  </span>
                </div>
              </div>
            </div>
            <div class="flex items-center gap-4">
              <span class="text-sm font-bold px-4 py-2 rounded-full" 
                    :class="statusClass(reminder.status)">
                {{ statusLabel(reminder.status) }}
              </span>
              <div class="flex items-center gap-2">
                <button @click="editReminder(reminder)" class="w-10 h-10 flex items-center justify-center rounded-full hover:bg-surface-container-high transition-colors text-on-surface-variant">
                  <span class="material-symbols-outlined">edit</span>
                </button>
                <button @click="deleteReminder(reminder.id)" class="w-10 h-10 flex items-center justify-center rounded-full hover:bg-surface-container-high transition-colors text-error">
                  <span class="material-symbols-outlined">delete</span>
                </button>
                <button v-if="reminder.status !== 'completed'" @click="toggleStatus(reminder)" 
                        class="w-10 h-10 flex items-center justify-center rounded-full hover:bg-surface-container-high transition-colors text-tertiary">
                  <span class="material-symbols-outlined">check_circle</span>
                </button>
              </div>
            </div>
          </div>
          <div v-if="reminder.notes" class="mt-4 pt-4 border-t border-outline-variant/10">
            <p class="text-sm text-on-surface-variant">{{ reminder.notes }}</p>
          </div>
        </div>

        <!-- Empty State -->
        <div v-if="filteredReminders.length === 0" class="text-center py-20">
          <span class="material-symbols-outlined text-6xl text-on-surface-variant/30 mb-4">notifications_off</span>
          <h3 class="text-xl font-bold text-on-surface-variant mb-2">暂无提醒</h3>
          <p class="text-on-surface-variant">点击“新建提醒”按钮添加您的第一个健康提醒</p>
        </div>
      </div>
    </div>
  </main>

  <!-- Add/Edit Modal -->
  <div v-if="showAddModal || showEditModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm">
    <div class="bg-surface-container-lowest rounded-2xl shadow-2xl w-full max-w-lg p-8">
      <div class="flex justify-between items-center mb-8">
        <h3 class="text-2xl font-bold text-on-surface">{{ editingReminder ? '编辑提醒' : '新建提醒' }}</h3>
        <button @click="closeModal" class="w-10 h-10 flex items-center justify-center rounded-full hover:bg-surface-container-high transition-colors text-on-surface-variant">
          <span class="material-symbols-outlined">close</span>
        </button>
      </div>
      <form @submit.prevent="saveReminder">
        <div class="space-y-6">
          <div>
            <label class="block text-sm font-semibold text-on-surface-variant mb-2">提醒内容 *</label>
            <input 
              v-model="form.content"
              class="w-full px-5 py-4 bg-surface-container-low border-0 rounded-full focus:ring-2 focus:ring-primary/20 focus:bg-white transition-all text-on-surface placeholder:text-outline-variant" 
              placeholder="例如：服用降压药"
              required
            />
          </div>
          <div>
            <label class="block text-sm font-semibold text-on-surface-variant mb-2">提醒时间 *</label>
            <input 
              v-model="form.time"
              class="w-full px-5 py-4 bg-surface-container-low border-0 rounded-full focus:ring-2 focus:ring-primary/20 focus:bg-white transition-all text-on-surface placeholder:text-outline-variant" 
              placeholder="例如：09:00"
              required
            />
          </div>
          <div>
            <label class="block text-sm font-semibold text-on-surface-variant mb-2">提醒类型</label>
            <div class="grid grid-cols-3 gap-3">
              <button 
                type="button"
                v-for="type in reminderTypes" 
                :key="type.value"
                @click="form.type = type.value"
                class="py-3 rounded-xl flex flex-col items-center justify-center gap-2 transition-all"
                :class="form.type === type.value ? 'bg-primary/10 border-2 border-primary' : 'bg-surface-container-low border-2 border-transparent hover:border-outline-variant'"
              >
                <span class="material-symbols-outlined" :class="form.type === type.value ? 'text-primary' : 'text-on-surface-variant'">{{ type.icon }}</span>
                <span class="text-xs font-bold" :class="form.type === type.value ? 'text-primary' : 'text-on-surface-variant'">{{ type.label }}</span>
              </button>
            </div>
          </div>
          <div>
            <label class="block text-sm font-semibold text-on-surface-variant mb-2">备注 (可选)</label>
            <textarea 
              v-model="form.notes"
              class="w-full px-5 py-4 bg-surface-container-low border-0 rounded-2xl focus:ring-2 focus:ring-primary/20 focus:bg-white transition-all text-on-surface placeholder:text-outline-variant resize-none"
              rows="3"
              placeholder="添加额外说明..."
            ></textarea>
          </div>
        </div>
        <div class="flex justify-end gap-4 mt-10">
          <button type="button" @click="closeModal" class="px-8 py-3 text-on-surface-variant font-bold rounded-full hover:bg-surface-container-high transition-colors">
            取消
          </button>
          <button type="submit" class="amber-gradient text-white font-bold py-3 px-8 rounded-full shadow-lg shadow-primary/25 hover:shadow-xl transition-all">
            {{ editingReminder ? '保存更改' : '创建提醒' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getReminderList, addReminder, updateReminder, updateReminderStatus, getUserId } from '../api'

const router = useRouter()

// 搜索查询
const searchQuery = ref('')

// 用户头像
const userAvatar = ref('https://lh3.googleusercontent.com/aida-public/AB6AXuChA2B-Y1C1HI34DXQRX14vYR2trYn-dippxcXdHFtW7Ji797Bk9mcgp_tZDwmyMXgPDQWuvyqvIqZmDQ6HDr5aitQab1ynYIkFKXZhO5N6GAkWcUXgEIoG1attkAQTCar7lXBGK9lwyTM1rv1FA2ie0kKVk3ap7Q2aUfQm2xMhhxItnRR1HXvux6Brn2mMMGzMt9F5EsaZG8nT_YDQ6MNW-z9eOha9UPr9MKLIu8A9G84It6XxRz9PqK8e6d7UGtPxNbSGliSxqfc')

// 提醒数据
const reminders = ref([])

// 表单状态
const showAddModal = ref(false)
const showEditModal = ref(false)
const editingReminder = ref(null)
const form = reactive({
  content: '',
  time: '',
  type: 'medication',
  notes: ''
})

// 提醒类型配置
const reminderTypes = ref([
  { value: 'medication', label: '用药', icon: 'medication' },
  { value: 'exercise', label: '运动', icon: 'fitness_center' },
  { value: 'diet', label: '饮食', icon: 'restaurant' },
  { value: 'habit', label: '习惯', icon: 'self_improvement' },
  { value: 'health', label: '健康', icon: 'monitor_heart' },
  { value: 'sleep', label: '睡眠', icon: 'bedtime' }
])

// 计算属性
const todayReminders = computed(() => reminders.value.length)
const completedReminders = computed(() => reminders.value.filter(r => r.status === 'completed').length)
const pendingReminders = computed(() => reminders.value.filter(r => r.status === 'pending').length)
const missedReminders = computed(() => reminders.value.filter(r => r.status === 'missed').length)

const filteredReminders = computed(() => {
  if (!searchQuery.value) return reminders.value
  const query = searchQuery.value.toLowerCase()
  return reminders.value.filter(r => 
    r.content.toLowerCase().includes(query) ||
    r.notes?.toLowerCase().includes(query) ||
    r.type.includes(query)
  )
})

// 类型和状态样式
function typeIcon(type) {
  const map = {
    medication: 'medication',
    exercise: 'fitness_center',
    diet: 'restaurant',
    habit: 'self_improvement',
    health: 'monitor_heart',
    sleep: 'bedtime'
  }
  return map[type] || 'notifications'
}

function typeColorClass(type) {
  const map = {
    medication: 'bg-primary/10 text-primary',
    exercise: 'bg-secondary/10 text-secondary',
    diet: 'bg-tertiary/10 text-tertiary',
    habit: 'bg-blue-500/10 text-blue-500',
    health: 'bg-purple-500/10 text-purple-500',
    sleep: 'bg-indigo-500/10 text-indigo-500'
  }
  return map[type] || 'bg-surface-container-high text-on-surface-variant'
}

function typeLabelClass(type) {
  const map = {
    medication: 'bg-primary/20 text-primary',
    exercise: 'bg-secondary/20 text-secondary',
    diet: 'bg-tertiary/20 text-tertiary',
    habit: 'bg-blue-500/20 text-blue-500',
    health: 'bg-purple-500/20 text-purple-500',
    sleep: 'bg-indigo-500/20 text-indigo-500'
  }
  return map[type] || 'bg-surface-container-high text-on-surface-variant'
}

function typeLabel(type) {
  const map = {
    medication: '用药',
    exercise: '运动',
    diet: '饮食',
    habit: '习惯',
    health: '健康',
    sleep: '睡眠'
  }
  return map[type] || '其他'
}

function statusClass(status) {
  const map = {
    pending: 'bg-orange-100 text-orange-800',
    completed: 'bg-tertiary/20 text-tertiary',
    missed: 'bg-error/10 text-error'
  }
  return map[status] || 'bg-surface-container-high text-on-surface-variant'
}

function statusLabel(status) {
  const map = {
    pending: '待执行',
    completed: '已完成',
    missed: '已错过'
  }
  return map[status] || '未知'
}

// 方法
function toggleNotifications() {
  console.log('切换通知')
}

function goToProfile() {
  router.push('/settings')
}

function editReminder(reminder) {
  editingReminder.value = reminder
  form.content = reminder.content
  form.time = reminder.time
  form.type = reminder.type
  form.notes = reminder.notes || ''
  showEditModal.value = true
}

function deleteReminder(id) {
  if (confirm('确定删除这条提醒吗？')) {
    reminders.value = reminders.value.filter(r => r.id !== id)
  }
}

async function toggleStatus(reminder) {
  const nextStatus = reminder.status === 'completed' ? 'pending' : 'completed'
  reminder.status = nextStatus
  try {
    const statusValue = nextStatus === 'completed' ? 1 : 0
    await updateReminderStatus({
      id: reminder.id,
      status: statusValue
    })
  } catch (error) {
    console.error('更新提醒状态失败', error)
  }
}

function closeModal() {
  showAddModal.value = false
  showEditModal.value = false
  editingReminder.value = null
  form.content = ''
  form.time = ''
  form.type = 'medication'
  form.notes = ''
}

async function saveReminder() {
  if (editingReminder.value) {
    const index = reminders.value.findIndex(r => r.id === editingReminder.value.id)
    if (index !== -1) {
      reminders.value[index] = { ...reminders.value[index], ...form }
    }
    try {
      await updateReminder({
        id: editingReminder.value.id,
        content: form.content,
        type: toReminderType(form.type),
        remindTime: buildRemindTime(form.time),
        title: form.notes || form.content
      })
    } catch (error) {
      console.error('更新提醒失败', error)
    }
  } else {
    const newReminder = {
      id: Date.now(),
      content: form.content,
      time: form.time,
      type: form.type,
      status: 'pending',
      notes: form.notes
    }
    reminders.value.unshift(newReminder)
    try {
      await addReminder({
        content: form.content,
        type: toReminderType(form.type),
        remindTime: buildRemindTime(form.time),
        title: form.notes || form.content
      })
    } catch (error) {
      console.error('创建提醒失败', error)
    }
  }
  closeModal()
}

function toReminderType(type) {
  const map = {
    exercise: 1,
    diet: 2,
    sleep: 3,
    medication: 4
  }
  return map[type] || 0
}

function mapReminderType(value) {
  const map = {
    1: 'exercise',
    2: 'diet',
    3: 'sleep',
    4: 'medication'
  }
  return map[value] || 'health'
}

function formatTime(value) {
  if (!value) return '--'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    const parts = value.toString()
    return parts.length >= 16 ? parts.slice(11, 16) : parts
  }
  return date.toTimeString().slice(0, 5)
}

function buildRemindTime(timeText) {
  if (!timeText) return null
  const now = new Date()
  const [hour, minute] = timeText.split(':')
  const date = new Date(now.getFullYear(), now.getMonth(), now.getDate(), Number(hour || 0), Number(minute || 0), 0)
  const pad = (v) => String(v).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

async function loadReminders() {
  try {
    const userId = getUserId()
    const list = await getReminderList(userId)
    const now = new Date()
    reminders.value = (list || []).map(item => {
      const remindTime = item.remindTime
      const remindDate = remindTime ? new Date(remindTime) : null
      const isMissed = item.status === 0 && remindDate && remindDate < now
      return {
        id: item.id,
        content: item.content || item.title || '提醒',
        time: formatTime(remindTime),
        type: mapReminderType(item.type),
        status: item.status === 1 ? 'completed' : (isMissed ? 'missed' : 'pending'),
        notes: item.title || item.content
      }
    })
  } catch (error) {
    console.error('加载提醒失败', error)
  }
}

onMounted(loadReminders)
</script>

<style scoped>
/* 自定义样式已在Tailwind中定义 */
</style>
