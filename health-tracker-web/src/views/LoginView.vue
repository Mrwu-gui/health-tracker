<template>
  <div class="login-page bg-surface font-body text-on-surface min-h-screen flex items-center justify-center p-6 selection:bg-primary-fixed relative">
    <main class="w-full max-w-[1120px] grid md:grid-cols-2 bg-surface-container-lowest rounded-lg overflow-hidden amber-care-shadow min-h-[700px]">
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
            开启您的<br /><span class="text-primary">智能健康伴侣</span>
          </h1>
          <p class="text-on-surface-variant text-lg max-w-sm leading-relaxed">
            用 AI 开启您的智慧健康生活，为您提供温馨的全方位守护。
          </p>
        </div>
      </div>

      <div class="flex flex-col items-center justify-center p-8 md:p-16">
        <div class="w-full max-w-[360px]">
          <div class="mb-10 text-center md:text-left">
            <h2 class="font-headline text-3xl font-bold text-on-surface mb-2">欢迎使用</h2>
            <p class="text-on-surface-variant">请使用账号密码登录智康AI</p>
          </div>

          <div class="space-y-5">
            <div class="space-y-2">
              <label class="text-sm text-on-surface-variant">用户名</label>
              <input
                v-model="username"
                class="w-full rounded-xl border border-outline-variant/60 bg-white px-4 py-3 text-on-surface focus:outline-none focus:ring-2 focus:ring-primary/40"
                placeholder="请输入用户名"
                autocomplete="username"
              />
            </div>
            <div class="space-y-2">
              <label class="text-sm text-on-surface-variant">密码</label>
              <input
                v-model="password"
                type="password"
                class="w-full rounded-xl border border-outline-variant/60 bg-white px-4 py-3 text-on-surface focus:outline-none focus:ring-2 focus:ring-primary/40"
                placeholder="请输入密码"
                autocomplete="current-password"
              />
            </div>
            <button
              type="button"
              class="w-full bg-primary text-white font-semibold py-3 rounded-full hover:bg-primary/90 transition-colors disabled:opacity-60"
              @click="handleLogin"
              :disabled="loginLoading"
            >
              {{ loginLoading ? "登录中..." : "登录" }}
            </button>
            <p v-if="loginError" class="text-xs text-error text-center">{{ loginError }}</p>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { userLogin, setToken, setUserId, setUserInfo } from "../api";

const router = useRouter();
const username = ref("");
const password = ref("");
const loginLoading = ref(false);
const loginError = ref("");

async function handleLogin() {
  if (username.value === "" || password.value === "") {
    loginError.value = "请输入用户名和密码";
    return;
  }
  loginLoading.value = true;
  loginError.value = "";
  try {
    const data = await userLogin(username.value.trim(), password.value);
    if (!data?.token) throw new Error("登录失败");
    setToken(data.token);
    setUserId(data.userId);
    setUserInfo(data.user || { username: username.value.trim() });
    router.replace("/");
  } catch (error) {
    loginError.value = error.message || "登录失败，请重试";
  } finally {
    loginLoading.value = false;
  }
}
</script>

<style scoped>
</style>
