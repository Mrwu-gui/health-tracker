<template>
  <div class="login">
    <div class="login__card">
      <div class="login__brand">
        <span class="logo">HT</span>
        <div>
          <div class="title">健康管理</div>
          <div class="subtitle">欢迎回来</div>
        </div>
      </div>
      <a-tabs v-model:activeKey="activeTab">
        <a-tab-pane key="account" tab="账号登录">
          <a-form layout="vertical">
            <a-form-item label="用户名">
              <a-input v-model:value="accountForm.username" placeholder="输入用户名" />
            </a-form-item>
            <a-form-item label="密码">
              <a-input-password v-model:value="accountForm.password" placeholder="输入密码" />
            </a-form-item>
            <a-button type="primary" block :loading="loading" @click="submitAccount">
              登录
            </a-button>
          </a-form>
        </a-tab-pane>
        <a-tab-pane key="phone" tab="手机号登录">
          <a-form layout="vertical">
            <a-form-item label="手机号">
              <a-input v-model:value="phoneForm.phone" placeholder="输入手机号" />
            </a-form-item>
            <a-form-item label="验证码">
              <div class="code-row">
                <a-input v-model:value="phoneForm.code" placeholder="输入验证码" />
                <a-button :loading="smsLoading" :disabled="smsCountdown > 0" @click="sendSms">
                  {{ smsCountdown > 0 ? `${smsCountdown}s` : "获取验证码" }}
                </a-button>
              </div>
            </a-form-item>
            <a-button type="primary" block :loading="loading" @click="submitPhone">
              登录 / 自动注册
            </a-button>
          </a-form>
        </a-tab-pane>
        <a-tab-pane key="wechat" tab="微信扫码">
          <div class="wechat">
            <div class="wechat__title">微信扫码快速登录</div>
            <div class="wechat__box">
              <img v-if="qrUrl" :src="qrUrl" alt="微信登录二维码" />
              <div v-else class="wechat__placeholder">未获取二维码</div>
            </div>
            <a-button :loading="qrLoading" @click="loadQr">获取二维码</a-button>
            <div class="wechat__hint">如未配置微信开放平台，请先完善后台配置</div>
          </div>
        </a-tab-pane>
        <a-tab-pane key="register" tab="注册账号">
          <a-form layout="vertical">
            <a-form-item label="用户名">
              <a-input v-model:value="registerForm.username" placeholder="设置用户名" />
            </a-form-item>
            <a-form-item label="密码">
              <a-input-password v-model:value="registerForm.password" placeholder="设置密码" />
            </a-form-item>
            <a-form-item label="确认密码">
              <a-input-password v-model:value="registerForm.confirm" placeholder="再次输入密码" />
            </a-form-item>
            <a-button type="primary" block :loading="loading" @click="submitRegister">
              注册
            </a-button>
          </a-form>
        </a-tab-pane>
      </a-tabs>
      <div class="hint">{{ message }}</div>
    </div>
    <div class="login__aside">
      <h2>你的专属健康助手</h2>
      <p>用一个面板管理运动、睡眠、饮食与用药。</p>
      <div class="pill">统一记录</div>
      <div class="pill">智能提醒</div>
      <div class="pill">周报汇总</div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { api, setToken } from "../api/http";

const router = useRouter();
const accountForm = reactive({ username: "demo", password: "demo" });
const phoneForm = reactive({ phone: "", code: "" });
const registerForm = reactive({ username: "", password: "", confirm: "" });
const activeTab = ref("account");
const loading = ref(false);
const smsLoading = ref(false);
const smsCountdown = ref(0);
const qrUrl = ref("");
const qrLoading = ref(false);
const message = ref("");
let countdownTimer = null;

async function submitAccount() {
  loading.value = true;
  message.value = "";
  try {
    const resp = await api.login(accountForm);
    setToken(resp.token);
    localStorage.setItem("userId", String(resp.userId));
    localStorage.setItem("username", accountForm.username);
    router.push("/dashboard");
  } catch (err) {
    message.value = err.message || "登录失败";
  } finally {
    loading.value = false;
  }
}

async function submitPhone() {
  loading.value = true;
  message.value = "";
  try {
    const resp = await api.smsLogin({ phone: phoneForm.phone, code: phoneForm.code });
    setToken(resp.token);
    localStorage.setItem("userId", String(resp.userId));
    localStorage.setItem("username", phoneForm.phone);
    router.push("/dashboard");
  } catch (err) {
    message.value = err.message || "手机号登录失败";
  } finally {
    loading.value = false;
  }
}

async function sendSms() {
  if (!phoneForm.phone) {
    message.value = "请先输入手机号";
    return;
  }
  smsLoading.value = true;
  message.value = "";
  try {
    const resp = await api.smsSend({ phone: phoneForm.phone });
    startCountdown();
    if (resp?.devCode) {
      message.value = `验证码已发送（本地调试：${resp.devCode}）`;
    } else {
      message.value = "验证码已发送";
    }
  } catch (err) {
    message.value = err.message || "验证码发送失败";
  } finally {
    smsLoading.value = false;
  }
}

function startCountdown() {
  smsCountdown.value = 60;
  if (countdownTimer) clearInterval(countdownTimer);
  countdownTimer = setInterval(() => {
    smsCountdown.value -= 1;
    if (smsCountdown.value <= 0) {
      clearInterval(countdownTimer);
      countdownTimer = null;
    }
  }, 1000);
}

async function submitRegister() {
  if (registerForm.password !== registerForm.confirm) {
    message.value = "两次输入的密码不一致";
    return;
  }
  loading.value = true;
  message.value = "";
  try {
    await api.register({ username: registerForm.username, password: registerForm.password });
    message.value = "注册成功，请使用新账号登录";
    activeTab.value = "account";
  } catch (err) {
    message.value = err.message || "注册失败";
  } finally {
    loading.value = false;
  }
}

async function loadQr() {
  qrLoading.value = true;
  message.value = "";
  try {
    const resp = await api.wechatQr();
    qrUrl.value = resp.qrUrl;
  } catch (err) {
    message.value = err.message || "二维码获取失败";
  } finally {
    qrLoading.value = false;
  }
}

onMounted(async () => {
  const params = new URLSearchParams(window.location.search);
  const code = params.get("code");
  if (!code) return;
  try {
    loading.value = true;
    const resp = await api.wechatCallback(code);
    setToken(resp.token);
    localStorage.setItem("userId", String(resp.userId));
    router.push("/dashboard");
  } catch (err) {
    message.value = err.message || "微信登录失败";
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped>
.login {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1fr 1fr;
  background:
    radial-gradient(circle at 10% 20%, rgba(56, 189, 248, 0.35), transparent 40%),
    radial-gradient(circle at 80% 20%, rgba(16, 185, 129, 0.28), transparent 45%),
    linear-gradient(135deg, #0f172a 0%, #0f766e 100%);
}

.login__card {
  margin: auto;
  width: 360px;
  padding: 28px;
  background: rgba(255, 255, 255, 0.92);
  border-radius: 18px;
  box-shadow: 0 30px 60px rgba(15, 23, 42, 0.12);
}

.login__brand {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 20px;
}

.logo {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: linear-gradient(135deg, #38bdf8, #22c55e);
  display: grid;
  place-items: center;
  font-weight: 700;
}

.title {
  font-weight: 600;
}

.subtitle {
  color: #64748b;
  font-size: 0.85rem;
}

.login__aside {
  padding: 80px 60px;
  display: grid;
  gap: 16px;
  color: #e2e8f0;
}

.hint {
  margin-top: 12px;
  color: #ef4444;
  font-size: 0.85rem;
}

.code-row {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
}

.wechat {
  display: grid;
  gap: 12px;
  text-align: center;
}

.wechat__title {
  font-weight: 600;
}

.wechat__box {
  width: 200px;
  height: 200px;
  border-radius: 16px;
  border: 1px dashed #cbd5f5;
  margin: 0 auto;
  display: grid;
  place-items: center;
  overflow: hidden;
  background: #f8fafc;
}

.wechat__box img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.wechat__placeholder {
  color: #94a3b8;
}

.wechat__hint {
  color: #64748b;
  font-size: 12px;
}

.pill {
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 999px;
  padding: 10px 16px;
  width: fit-content;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.08);
}

@media (max-width: 900px) {
  .login {
    grid-template-columns: 1fr;
  }

  .login__aside {
    display: none;
  }
}
</style>
