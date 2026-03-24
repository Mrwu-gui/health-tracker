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
      <router-link to="/reminders" class="flex items-center gap-3 px-4 py-3 text-stone-600 dark:text-stone-400 hover:bg-stone-100 dark:hover:bg-stone-800 transition-all duration-300 font-manrope text-sm font-medium tracking-tight rounded-full">
        <span class="material-symbols-outlined">notifications</span>提醒列表
      </router-link>
      <router-link to="/medications" class="flex items-center gap-3 px-4 py-3 text-stone-600 dark:text-stone-400 hover:bg-stone-100 dark:hover:bg-stone-800 transition-all duration-300 font-manrope text-sm font-medium tracking-tight rounded-full">
        <span class="material-symbols-outlined">medication</span>用药管理
      </router-link>
      <!-- Active Tab: 个人中心/设置 -->
      <router-link to="/settings" class="flex items-center gap-3 px-4 py-3 bg-orange-100/50 dark:bg-orange-900/30 text-orange-800 dark:text-orange-200 rounded-full font-bold font-manrope text-sm tracking-tight">
        <span class="material-symbols-outlined" style="font-variation-settings: 'FILL' 1;">settings</span>个人设置
      </router-link>
    </nav>
    <!-- 个人中心菜单已移至设置 -->

  </aside>

  <!-- TopNavBar Shell -->
  <header class="fixed top-0 right-0 left-0 h-16 bg-white/70 dark:bg-stone-950/70 backdrop-blur-md flex justify-between items-center w-full pl-72 pr-8 py-2 z-40">
    <div class="flex items-center gap-8">
      <h2 class="text-lg font-black text-orange-900 dark:text-orange-100">个人设置</h2>
    </div>
    <div class="flex items-center gap-4">
      <div class="relative group">
        <input 
          v-model="searchQuery"
          class="bg-surface-container-low border-none rounded-full px-5 py-1.5 text-sm w-64 focus:ring-2 focus:ring-primary/20 transition-all" 
          placeholder="搜索设置..." 
          type="text"
        />
        <span class="material-symbols-outlined absolute right-3 top-1/2 -translate-y-1/2 text-on-surface-variant text-lg">search</span>
      </div>
      <button @click="toggleNotifications" class="w-10 h-10 flex items-center justify-center rounded-full hover:bg-surface-container-high transition-colors text-on-surface-variant">
        <span class="material-symbols-outlined">notifications</span>
      </button>
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
      <!-- Page Header -->
      <div class="mb-10">
        <h1 class="text-3xl font-extrabold text-on-surface tracking-tight mb-2">账户设置</h1>
        <p class="text-on-surface-variant text-lg font-light">管理您的个人资料、偏好和隐私设置</p>
      </div>

      <!-- Settings Grid -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <!-- Left Column: Profile & Account -->
        <div class="lg:col-span-2 space-y-8">
          <!-- Personal Profile Card -->
          <div class="bg-surface-container-lowest p-8 rounded-xl shadow-[0_20px_40px_rgba(86,67,55,0.04)] border border-orange-100/10">
            <div class="flex items-center gap-4 mb-8">
              <span class="material-symbols-outlined text-primary text-2xl">person</span>
              <h3 class="text-xl font-bold text-on-surface">个人资料</h3>
            </div>
            <div class="space-y-6">
              <div class="grid grid-cols-2 gap-6">
                <div>
                  <label class="block text-sm font-semibold text-on-surface-variant mb-2">姓名</label>
                  <input 
                    v-model="profile.name"
                    class="w-full px-5 py-4 bg-surface-container-low border-0 rounded-full focus:ring-2 focus:ring-primary/20 focus:bg-white transition-all text-on-surface placeholder:text-outline-variant" 
                    placeholder="李娜"
                  />
                </div>
                <div>
                  <label class="block text-sm font-semibold text-on-surface-variant mb-2">年龄</label>
                  <input 
                    v-model="profile.age"
                    class="w-full px-5 py-4 bg-surface-container-low border-0 rounded-full focus:ring-2 focus:ring-primary/20 focus:bg-white transition-all text-on-surface placeholder:text-outline-variant" 
                    placeholder="30"
                    type="number"
                  />
                </div>
              </div>
              <div class="grid grid-cols-2 gap-6">
                <div>
                  <label class="block text-sm font-semibold text-on-surface-variant mb-2">身高 (厘米)</label>
                  <input 
                    v-model="profile.height"
                    class="w-full px-5 py-4 bg-surface-container-low border-0 rounded-full focus:ring-2 focus:ring-primary/20 focus:bg-white transition-all text-on-surface placeholder:text-outline-variant" 
                    placeholder="175"
                    type="number"
                  />
                </div>
                <div>
                  <label class="block text-sm font-semibold text-on-surface-variant mb-2">体重 (千克)</label>
                  <input 
                    v-model="profile.weight"
                    class="w-full px-5 py-4 bg-surface-container-low border-0 rounded-full focus:ring-2 focus:ring-primary/20 focus:bg-white transition-all text-on-surface placeholder:text-outline-variant" 
                    placeholder="70.5"
                    type="number"
                  />
                </div>
              </div>
              <div class="pt-4">
                <button @click="saveProfile" class="amber-gradient text-white font-bold py-4 px-8 rounded-full shadow-lg shadow-primary/25 hover:shadow-xl hover:scale-[1.02] active:scale-95 transition-all flex items-center justify-center gap-2 group">
                  <span>保存更改</span>
                  <span class="material-symbols-outlined text-xl group-hover:translate-x-1 transition-transform">arrow_forward</span>
                </button>
              </div>
            </div>
          </div>
        </div>
        
        <!-- Right Column: Quick Actions & App Settings -->
        <div class="space-y-8">
          <!-- Account Security Card -->
          <div class="bg-surface-container-lowest p-8 rounded-xl shadow-[0_20px_40px_rgba(86,67,55,0.04)] border border-orange-100/10">
            <div class="flex items-center gap-4 mb-8">
              <span class="material-symbols-outlined text-tertiary text-2xl">security</span>
              <h3 class="text-xl font-bold text-on-surface">账户安全</h3>
            </div>
            <div class="space-y-4">
              <button @click="changePassword" class="w-full flex items-center justify-between p-4 rounded-xl hover:bg-surface-container-low transition-colors text-left group">
                <div class="flex items-center gap-3">
                  <span class="material-symbols-outlined text-on-surface-variant">lock</span>
                  <span class="font-bold text-on-surface">修改密码</span>
                </div>
                <span class="material-symbols-outlined text-on-surface-variant group-hover:translate-x-1 transition-transform">chevron_right</span>
              </button>
              <button @click="twoFactorAuth" class="w-full flex items-center justify-between p-4 rounded-xl hover:bg-surface-container-low transition-colors text-left group">
                <div class="flex items-center gap-3">
                  <span class="material-symbols-outlined text-on-surface-variant">enhanced_encryption</span>
                  <span class="font-bold text-on-surface">双重验证</span>
                </div>
                <span class="material-symbols-outlined text-on-surface-variant group-hover:translate-x-1 transition-transform">chevron_right</span>
              </button>
              <button @click="manageDevices" class="w-full flex items-center justify-between p-4 rounded-xl hover:bg-surface-container-low transition-colors text-left group">
                <div class="flex items-center gap-3">
                  <span class="material-symbols-outlined text-on-surface-variant">devices</span>
                  <span class="font-bold text-on-surface">设备管理</span>
                </div>
                <span class="material-symbols-outlined text-on-surface-variant group-hover:translate-x-1 transition-transform">chevron_right</span>
              </button>
            </div>
          </div>
          
          <!-- Danger Zone Card -->
          <div class="bg-surface-container-lowest p-8 rounded-xl shadow-[0_20px_40px_rgba(86,67,55,0.04)] border border-error/10">
            <div class="flex items-center gap-4 mb-8">
              <span class="material-symbols-outlined text-error text-2xl">warning</span>
              <h3 class="text-xl font-bold text-error">危险操作</h3>
            </div>
            <div class="space-y-4">
              <p class="text-sm text-on-surface-variant">这些操作将永久影响您的账户，请谨慎操作。</p>
              <button @click="exportData" class="w-full flex items-center justify-between p-4 rounded-xl hover:bg-surface-container-low transition-colors text-left group border border-outline-variant/20">
                <div class="flex items-center gap-3">
                  <span class="material-symbols-outlined text-on-surface-variant">download</span>
                  <span class="font-bold text-on-surface">导出所有数据</span>
                </div>
                <span class="material-symbols-outlined text-on-surface-variant group-hover:translate-x-1 transition-transform">chevron_right</span>
              </button>
              <button @click="deleteAccount" class="w-full flex items-center justify-between p-4 rounded-xl hover:bg-error/10 transition-colors text-left group border border-error/20">
                <div class="flex items-center gap-3">
                  <span class="material-symbols-outlined text-error">delete</span>
                  <span class="font-bold text-error">删除账户</span>
                </div>
                <span class="material-symbols-outlined text-error group-hover:translate-x-1 transition-transform">chevron_right</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getUserProfile, updateUserProfile, getUserId } from '../api'

const router = useRouter()

// 搜索查询
const searchQuery = ref('')

// 用户头像
const userAvatar = ref('')

// 个人资料
const profile = reactive({
  name: '',
  age: null,
  height: null,
  weight: null
})

// 方法
function toggleNotifications() {
  console.log('切换通知')
}

function goToProfile() {
  router.push('/profile')
}

async function loadProfile() {
  try {
    const userId = getUserId()
    const data = await getUserProfile(userId)
    profile.name = data?.wxNickname || data?.username || ''
    profile.age = data?.age ?? null
    profile.height = data?.height ?? null
    profile.weight = data?.weight ?? null
    userAvatar.value = data?.wxAvatar || ''
  } catch (error) {
    console.error('加载个人资料失败', error)
  }
}

async function saveProfile() {
  try {
    const userId = getUserId()
    await updateUserProfile({
      userId,
      wxNickname: profile.name,
      age: profile.age,
      height: profile.height,
      weight: profile.weight
    })
  } catch (error) {
    console.error('保存个人资料失败', error)
  }
}

function changePassword() {
  console.log('修改密码')
}

function twoFactorAuth() {
  console.log('双重验证')
}

function manageDevices() {
  console.log('设备管理')
}

function exportData() {
  console.log('导出数据')
}

function deleteAccount() {
  if (confirm('确定要永久删除您的账户吗？此操作不可撤销。')) {
    console.log('删除账户')
  }
}

onMounted(loadProfile)
</script>

<style scoped>
/* 自定义样式已在Tailwind中定义 */
</style>
