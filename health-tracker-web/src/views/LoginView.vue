<template>
  <div class="login-page bg-surface font-body text-on-surface min-h-screen flex items-center justify-center p-6 selection:bg-primary-fixed">
    <main class="w-full max-w-[1120px] grid md:grid-cols-2 bg-surface-container-lowest rounded-lg overflow-hidden amber-care-shadow min-h-[700px]">
      <!-- Left Section: Lifestyle Illustration -->
      <div class="relative hidden md:flex flex-col justify-between p-12 overflow-hidden bg-surface-container">
        <div class="absolute inset-0 z-0">
          <div class="absolute inset-0 bg-primary/5 mix-blend-multiply"></div>
          <img 
            alt="Lifestyle workspace with soft lighting" 
            class="w-full h-full object-cover grayscale opacity-30" 
            src="https://lh3.googleusercontent.com/aida-public/AB6AXuCRgdExs9QniuMgs1hcIjgNi_15jh1AmGgl9w5QwVn9PQtoK9HHiIr1NbBmncUY7pk3r-A4TfcUvZBGlvtdbB_u9HEBR1feu0tke21wGArOST5KZipO3OOlWYYJ16hFEqTzG-keqvT5KHXzrGCJbjZ8DRCcI9tvfacpEbccfTQir63141gUcEKxiBYVa5OhaSF7ewtamGhWOJJOCWV0WRtpiSI4dIB7qZglvCJuK0yeE4qALKDq3BFZbmvAh1yLOiTLAVOWO1OSHb8"
          />
        </div>
        <div class="relative z-10">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 amber-gradient rounded-full flex items-center justify-center shadow-lg shadow-primary/20">
              <span class="material-symbols-outlined text-white text-xl">auto_awesome</span>
            </div>
            <span class="font-headline font-extrabold text-2xl tracking-tight text-on-surface">智康AI</span>
          </div>
        </div>
        <div class="relative z-10 mt-auto">
          <h1 class="font-headline text-4xl font-extrabold text-on-surface leading-tight tracking-tight mb-4">
            开启您的<br/><span class="text-primary">智能健康伴侣</span>
          </h1>
          <p class="text-on-surface-variant text-lg max-w-sm leading-relaxed">
            用 AI 开启您的智慧健康生活，为您提供温馨的全方位守护。
          </p>
          <div class="mt-8 flex gap-2">
            <div class="w-12 h-1.5 bg-primary rounded-full"></div>
            <div class="w-3 h-1.5 bg-outline-variant/30 rounded-full"></div>
            <div class="w-3 h-1.5 bg-outline-variant/30 rounded-full"></div>
          </div>
        </div>
      </div>

      <!-- Right Section: Login Form -->
      <div class="flex flex-col items-center justify-center p-8 md:p-16">
        <div class="w-full max-w-[360px]">
          <div class="mb-10 text-center md:text-left">
            <h2 class="font-headline text-3xl font-bold text-on-surface mb-2">欢迎回来</h2>
            <p class="text-on-surface-variant">请登录您的智康AI账号</p>
          </div>

          <form class="space-y-6" @submit.prevent="handleLogin">
            <div class="space-y-2">
              <label class="block text-sm font-semibold text-on-surface-variant ml-1" for="username">用户名</label>
              <div class="relative group">
                <div class="absolute inset-y-0 left-5 flex items-center pointer-events-none text-on-surface-variant group-focus-within:text-primary transition-colors">
                  <span class="material-symbols-outlined text-xl">person</span>
                </div>
                <input 
                  v-model="form.username"
                  class="w-full pl-14 pr-6 py-4 bg-surface-container-low border-0 rounded-full focus:ring-2 focus:ring-primary/20 focus:bg-white transition-all text-on-surface placeholder:text-outline-variant" 
                  id="username" 
                  name="username" 
                  placeholder="请输入您的账号" 
                  type="text"
                  :disabled="loading"
                  required
                />
              </div>
            </div>

            <div class="space-y-2">
              <label class="block text-sm font-semibold text-on-surface-variant ml-1" for="password">密码</label>
              <div class="relative group">
                <div class="absolute inset-y-0 left-5 flex items-center pointer-events-none text-on-surface-variant group-focus-within:text-primary transition-colors">
                  <span class="material-symbols-outlined text-xl">lock</span>
                </div>
                <input 
                  v-model="form.password"
                  class="w-full pl-14 pr-14 py-4 bg-surface-container-low border-0 rounded-full focus:ring-2 focus:ring-primary/20 focus:bg-white transition-all text-on-surface placeholder:text-outline-variant" 
                  id="password" 
                  name="password" 
                  placeholder="请输入您的密码" 
                  :type="showPassword ? 'text' : 'password'"
                  :disabled="loading"
                  required
                />
                <button 
                  type="button"
                  class="absolute inset-y-0 right-5 flex items-center text-on-surface-variant hover:text-primary transition-colors"
                  @click="showPassword = !showPassword"
                >
                  <span class="material-symbols-outlined text-xl">
                    {{ showPassword ? 'visibility_off' : 'visibility' }}
                  </span>
                </button>
              </div>
            </div>

            <div v-if="errorMsg" class="pt-2">
              <p class="text-sm text-error text-center">{{ errorMsg }}</p>
            </div>

            <div class="pt-4">
              <button 
                class="w-full amber-gradient text-white font-headline font-bold py-5 rounded-full shadow-lg shadow-primary/25 hover:shadow-xl hover:scale-[1.02] active:scale-95 transition-all flex items-center justify-center gap-2 group disabled:opacity-70 disabled:cursor-not-allowed" 
                type="submit"
                :disabled="loading"
              >
                <span>{{ loading ? '登录中...' : '立即登录' }}</span>
                <span v-if="!loading" class="material-symbols-outlined text-xl group-hover:translate-x-1 transition-transform">arrow_forward</span>
              </button>
            </div>

            <!-- Alternative login options -->
            <div class="pt-6 text-center space-y-3">
              <p class="text-sm text-on-surface-variant">其他登录方式</p>
              <div class="flex justify-center gap-4">
                <button 
                  type="button"
                  class="text-sm text-primary hover:text-primary-fixed-dim transition-colors"
                  @click="switchToWechat"
                >
                  微信扫码登录
                </button>
                <span class="text-outline-variant">|</span>
                <button 
                  type="button"
                  class="text-sm text-secondary hover:text-secondary-fixed-dim transition-colors"
                  @click="switchToAdmin"
                >
                  后台管理登录
                </button>
              </div>
            </div>
          </form>

          <div class="mt-16 text-center">
            <p class="text-[10px] text-outline font-bold tracking-[0.2em] uppercase opacity-70">
              ZIKING AI • SECURE PERSONAL HEALTH GATEWAY
            </p>
          </div>
        </div>
      </div>
    </main>

    <footer class="fixed bottom-8 left-1/2 -translate-x-1/2 flex items-center gap-6 opacity-40 pointer-events-none">
      <span class="text-xs font-semibold text-on-surface-variant">隐私政策</span>
      <span class="w-1 h-1 bg-outline-variant rounded-full"></span>
      <span class="text-xs font-semibold text-on-surface-variant">服务条款</span>
      <span class="w-1 h-1 bg-outline-variant rounded-full"></span>
      <span class="text-xs font-semibold text-on-surface-variant">© 2024 智康AI</span>
    </footer>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { userLogin, setToken, setUserId, setUserInfo } from "../api";

const router = useRouter();

const form = reactive({
  username: "",
  password: ""
});

const showPassword = ref(false);
const loading = ref(false);
const errorMsg = ref("");

// 处理普通用户登录
async function handleLogin() {
  if (!form.username || !form.password) {
    errorMsg.value = "请输入用户名和密码";
    return;
  }

  loading.value = true;
  errorMsg.value = "";

  try {
    const data = await userLogin(form.username, form.password);
    
    if (data.token) {
      setToken(data.token);
      setUserId(data.userId);
      setUserInfo(data.user || { username: form.username });
      router.push("/");
    }
  } catch (error) {
    errorMsg.value = error.message || "登录失败，请检查用户名和密码";
  } finally {
    loading.value = false;
  }
}

// 切换到微信扫码登录
function switchToWechat() {
  // 这里可以跳转到微信扫码登录页面或显示二维码
  // 暂时保持当前页面，可以添加状态切换
  console.log("切换到微信扫码登录");
  // 可以在此处添加微信登录逻辑
}

// 切换到后台管理登录
function switchToAdmin() {
  // 可以跳转到专门的管理员登录页面或显示管理员登录表单
  console.log("切换到后台管理登录");
  // 可以在此处添加管理员登录逻辑
}
</script>

<style scoped>
/* 组件特定样式已在Tailwind中定义 */
</style>
