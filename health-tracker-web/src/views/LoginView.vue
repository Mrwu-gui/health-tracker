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
      <a-form layout="vertical">
        <a-form-item label="手机号">
          <a-input v-model:value="phoneForm.phone" placeholder="输入手机号" />
        </a-form-item>
        <a-form-item label="图形验证码">
          <div class="captcha-row">
            <a-input v-model:value="phoneForm.captcha" placeholder="输入图形验证码" />
            <div class="captcha-image" @click="loadCaptcha">
              <img v-if="captchaImage" :src="captchaImage" alt="图形验证码" />
              <span v-else>点击刷新</span>
            </div>
          </div>
        </a-form-item>
        <a-form-item label="短信验证码">
          <div class="code-row">
            <a-input v-model:value="phoneForm.code" placeholder="输入短信验证码" />
            <a-button :loading="smsLoading" :disabled="smsCountdown > 0" @click="sendSms">
              {{ smsCountdown > 0 ? `${smsCountdown}s` : "获取验证码" }}
            </a-button>
          </div>
        </a-form-item>
        <a-button type="primary" block :loading="loading" @click="submitPhone">
          登录 / 自动注册
        </a-button>
      </a-form>
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
const phoneForm = reactive({ phone: "", code: "", captcha: "" });
const loading = ref(false);
const smsLoading = ref(false);
const smsCountdown = ref(0);
const captchaKey = ref("");
const captchaImage = ref("");
const message = ref("");
let countdownTimer = null;

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
  if (!phoneForm.captcha || !captchaKey.value) {
    message.value = "请先完成图形验证码";
    return;
  }
  smsLoading.value = true;
  message.value = "";
  try {
    const resp = await api.smsSend({
      phone: phoneForm.phone,
      captchaKey: captchaKey.value,
      captchaCode: phoneForm.captcha
    });
    startCountdown(resp?.cooldown || 60);
    if (resp?.devCode) {
      message.value = `验证码已发送（本地调试：${resp.devCode}）`;
    } else {
      message.value = "验证码已发送";
    }
  } catch (err) {
    message.value = err.message || "验证码发送失败";
    await loadCaptcha();
  } finally {
    smsLoading.value = false;
  }
}

function startCountdown(seconds) {
  smsCountdown.value = seconds;
  if (countdownTimer) clearInterval(countdownTimer);
  countdownTimer = setInterval(() => {
    smsCountdown.value -= 1;
    if (smsCountdown.value <= 0) {
      clearInterval(countdownTimer);
      countdownTimer = null;
    }
  }, 1000);
}

async function loadCaptcha() {
  try {
    const resp = await api.captcha();
    captchaKey.value = resp.key;
    captchaImage.value = resp.image;
  } catch (err) {
    message.value = err.message || "验证码获取失败";
  }
}

onMounted(async () => {
  await loadCaptcha();
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

.captcha-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.captcha-image {
  width: 110px;
  height: 40px;
  border-radius: 10px;
  background: #f1f5f9;
  display: grid;
  place-items: center;
  cursor: pointer;
  border: 1px solid #e2e8f0;
}

.captcha-image img {
  width: 100%;
  height: 100%;
  border-radius: 10px;
  object-fit: cover;
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
