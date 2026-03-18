<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <h1>智康AI</h1>
        <p>选择登录方式</p>
      </div>

      <!-- 登录方式切换 -->
      <div class="login-tabs">
        <button 
          :class="{ active: loginType === 'wechat' }" 
          @click="loginType = 'wechat'"
        >
          微信扫码登录
        </button>
        <button 
          :class="{ active: loginType === 'admin' }" 
          @click="loginType = 'admin'"
        >
          后台管理登录
        </button>
      </div>

      <!-- 微信扫码登录 -->
      <div v-if="loginType === 'wechat'" class="wechat-login">
        <div class="qr-container">
          <img v-if="qrUrl" :src="qrUrl" alt="微信扫码登录" class="qr-code" />
          <div v-else class="qr-loading">加载中...</div>
        </div>
        <p class="qr-tip">请使用微信扫描二维码登录</p>
        <p v-if="scanStatus" class="scan-status" :class="scanStatus">
          {{ scanStatus === 'success' ? '登录成功，正在跳转...' : '扫码成功，请确认' }}
        </p>
      </div>

      <!-- 后台账号密码登录 -->
      <div v-if="loginType === 'admin'" class="admin-login">
        <div class="form-group">
          <label>用户名</label>
          <input 
            type="text" 
            v-model="username" 
            placeholder="请输入用户名" 
            @keyup.enter="handleAdminLogin"
          />
        </div>
        <div class="form-group">
          <label>密码</label>
          <input 
            type="password" 
            v-model="password" 
            placeholder="请输入密码" 
            @keyup.enter="handleAdminLogin"
          />
        </div>
        <button class="login-btn" @click="handleAdminLogin" :disabled="logging">
          {{ logging ? '登录中...' : '登录' }}
        </button>
        <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, onUnmounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { getWechatQR, wechatCallback, adminLogin, setToken, setAdminToken, setUserId, setUserInfo } from "../api";

const router = useRouter();
const route = useRoute();

const loginType = ref("wechat");
const qrUrl = ref("");
const scanStatus = ref("");
const pollingTimer = ref(null);

const username = ref("");
const password = ref("");
const logging = ref(false);
const errorMsg = ref("");

// 获取微信二维码
async function fetchQRCode() {
  try {
    const data = await getWechatQR();
    qrUrl.value = data.qrUrl || data.url || data;
    startPolling();
  } catch (e) {
    console.error("获取二维码失败:", e);
    // mock数据用于演示
    qrUrl.value = "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=wechat_login_demo";
  }
}

// 轮询检查扫码状态
function startPolling() {
  if (pollingTimer.value) clearInterval(pollingTimer.value);
  
  pollingTimer.value = setInterval(async () => {
    // 检查URL是否有code参数（微信回调）
    const urlParams = new URLSearchParams(window.location.search);
    const code = urlParams.get("code");
    
    if (code) {
      clearInterval(pollingTimer.value);
      await handleWechatCallback(code);
    }
  }, 2000);
}

// 处理微信回调
async function handleWechatCallback(code) {
  try {
    scanStatus.value = "success";
    const data = await wechatCallback(code);
    
    if (data.token) {
      setToken(data.token);
      setUserId(data.userId);
      setUserInfo(data.user);
      
      // 清除URL中的code参数
      window.history.replaceState({}, "", window.location.pathname);
      
      // 跳转首页
      setTimeout(() => router.push("/"), 1000);
    }
  } catch (e) {
    console.error("登录失败:", e);
    scanStatus.value = "error";
  }
}

// 后台登录
async function handleAdminLogin() {
  if (!username.value || !password.value) {
    errorMsg.value = "请输入用户名和密码";
    return;
  }
  
  logging.value = true;
  errorMsg.value = "";
  
  try {
    const data = await adminLogin(username.value, password.value);
    
    if (data.token) {
      setAdminToken(data.token);
      setUserId(data.userId);
      setUserInfo(data.user);
      
      // 跳转管理后台
      router.push("/admin/users");
    }
  } catch (e) {
    errorMsg.value = e.message || "登录失败，请检查用户名和密码";
  } finally {
    logging.value = false;
  }
}

// 监听登录类型切换
watch(loginType, (type) => {
  if (type === "wechat") {
    fetchQRCode();
  } else {
    if (pollingTimer.value) clearInterval(pollingTimer.value);
  }
});

onMounted(() => {
  // 检查是否有微信回调的code
  const urlParams = new URLSearchParams(window.location.search);
  const code = urlParams.get("code");
  
  if (code) {
    loginType.value = "wechat";
    handleWechatCallback(code);
  } else {
    fetchQRCode();
  }
});

onUnmounted(() => {
  if (pollingTimer.value) clearInterval(pollingTimer.value);
});
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
}

.login-card {
  width: 420px;
  background: #fff;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-header h1 {
  font-size: 28px;
  font-weight: 700;
  color: #ff7a45;
  margin-bottom: 8px;
}

.login-header p {
  color: #999;
  font-size: 14px;
}

.login-tabs {
  display: flex;
  margin-bottom: 32px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e8e8e8;
}

.login-tabs button {
  flex: 1;
  padding: 12px;
  border: none;
  background: #fafafa;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.login-tabs button.active {
  background: #ff7a45;
  color: #fff;
}

.login-tabs button:first-child {
  border-right: 1px solid #e8e8e8;
}

/* 微信扫码登录 */
.wechat-login {
  text-align: center;
}

.qr-container {
  width: 200px;
  height: 200px;
  margin: 0 auto 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  border-radius: 8px;
}

.qr-code {
  width: 180px;
  height: 180px;
}

.qr-loading {
  color: #999;
}

.qr-tip {
  color: #666;
  font-size: 14px;
}

.scan-status {
  margin-top: 16px;
  padding: 12px;
  border-radius: 8px;
  font-size: 14px;
}

.scan-status.success {
  background: #f6ffed;
  color: #52c41a;
}

/* 后台登录 */
.admin-login {
  padding: 0 20px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #333;
  font-size: 14px;
}

.form-group input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.form-group input:focus {
  outline: none;
  border-color: #ff7a45;
}

.login-btn {
  width: 100%;
  padding: 14px;
  background: #ff7a45;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.2s;
}

.login-btn:hover:not(:disabled) {
  background: #ff9a75;
}

.login-btn:disabled {
  background: #d9d9d9;
  cursor: not-allowed;
}

.error-msg {
  margin-top: 16px;
  color: #ff4d4f;
  font-size: 14px;
  text-align: center;
}
</style>
