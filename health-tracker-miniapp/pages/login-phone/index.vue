<template>
  <view class="page">
    <view class="nav">
      <button class="back" @tap="goBack">←</button>
      <text class="nav-title">手机号登录</text>
      <text class="nav-spacer"></text>
    </view>

    <view class="content">
      <text class="hint">未注册的手机号验证后将自动创建账号</text>

      <view class="field">
        <text class="label">手机号</text>
        <input
          class="input"
          type="text"
          inputmode="numeric"
          maxlength="11"
          placeholder="请输入手机号"
          v-model.trim="form.phone"
        />
      </view>

      <view class="field">
        <text class="label">验证码</text>
        <view class="code-row">
          <input
            class="input"
            type="text"
            inputmode="numeric"
            maxlength="6"
            placeholder="请输入验证码"
            v-model.trim="form.code"
          />
          <button class="code-btn" :disabled="cooldown > 0" @tap="sendCode">
            {{ cooldown > 0 ? `${cooldown}s` : "获取验证码" }}
          </button>
        </view>
        <text class="tip">点击获取验证码后，请留意短信（60 秒内有效）</text>
      </view>

      <button class="primary" @tap="submit">
        登录
      </button>

      <view class="policy-wrap">
        <text class="policy">登录即表示您已阅读并同意</text>
        <text class="policy-link" @tap="openAgreement('user')">《用户协议》</text>
        <text class="policy">和</text>
        <text class="policy-link" @tap="openAgreement('privacy')">《隐私政策》</text>
      </view>
    </view>
  </view>
</template>

<script>
import { request } from "../../utils/api";

export default {
  data() {
    return {
      form: {
        phone: "",
        code: ""
      },
      cooldown: 0,
      timer: null
    };
  },
  computed: {
    canSubmit() {
      const phone = String(this.form.phone || "");
      const code = String(this.form.code || "");
      return phone.length === 11 && code.length >= 4;
    }
  },
  methods: {
    openAgreement(type) {
      const path = type === "user" ? "/pages/agreement/user" : "/pages/agreement/privacy";
      uni.navigateTo({ url: path });
    },
    goBack() {
      uni.navigateBack();
    },
    sendCode() {
      if (!/^\d{11}$/.test(this.form.phone)) {
        uni.showToast({ title: "请输入正确手机号", icon: "none" });
        return;
      }
      request("/api/auth/sms/send", "POST", { phone: this.form.phone })
        .then(() => {
          this.startCooldown();
          uni.showToast({ title: "验证码已发送", icon: "success" });
        })
        .catch((err) => {
          uni.showToast({ title: err.message || "发送失败", icon: "none" });
        });
    },
    startCooldown() {
      this.cooldown = 60;
      if (this.timer) clearInterval(this.timer);
      this.timer = setInterval(() => {
        this.cooldown -= 1;
        if (this.cooldown <= 0) {
          clearInterval(this.timer);
          this.timer = null;
        }
      }, 1000);
    },
    submit() {
      if (!this.canSubmit) {
        uni.showToast({ title: "请填写完整手机号和验证码", icon: "none" });
        return;
      }
      if (!/^\d{11}$/.test(this.form.phone)) {
        uni.showToast({ title: "手机号格式不正确", icon: "none" });
        return;
      }
      if (!/^\d{4,6}$/.test(this.form.code)) {
        uni.showToast({ title: "验证码格式不正确", icon: "none" });
        return;
      }
      request("/api/auth/sms/login", "POST", {
        phone: this.form.phone,
        code: this.form.code
      })
        .then((data) => {
          if (data?.token) {
            uni.setStorageSync("token", data.token);
            if (data.userId) {
              uni.setStorageSync("userId", data.userId);
            }
            uni.switchTab({ url: "/pages/profile/index" });
            uni.showToast({ title: "登录成功", icon: "success" });
            uni.setStorageSync("needBodyProfile", true);
          }
        })
        .catch((err) => {
          uni.showToast({ title: err.message || "登录失败", icon: "none" });
        });
    }
  },
  onUnload() {
    if (this.timer) clearInterval(this.timer);
  }
};
</script>

<style>
.page {
  min-height: 100vh;
  background: #f5f7fa;
  color: #0f172a;
  display: flex;
  flex-direction: column;
}

.status-bar {
  height: 40px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 11px;
  color: #94a3b8;
}

.status-icons {
  display: flex;
  gap: 6px;
}

.nav {
  height: 48px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  border-bottom: 1px solid #e5e7eb;
  background: #ffffff;
}

.back {
  width: 36px;
  height: 36px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #475569;
}

.nav-title {
  flex: 1;
  text-align: center;
  font-size: 16px;
  font-weight: 600;
  color: #0f172a;
}

.nav-spacer {
  width: 36px;
}

.content {
  flex: 1;
  padding: 24px 16px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.hint {
  font-size: 12px;
  color: #94a3b8;
  text-align: center;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.label {
  font-size: 11px;
  color: #94a3b8;
}

.input {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 12px 14px;
  font-size: 15px;
  background: #ffffff;
}

.code-row {
  display: flex;
  gap: 8px;
  align-items: center;
}

.code-btn {
  padding: 10px 14px;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  background: #fefcf9;
  font-size: 12px;
  color: #334155;
}

.tip {
  font-size: 10px;
  color: #94a3b8;
}

.primary {
  background: #0f172a;
  color: #ffffff;
  border: none;
  border-radius: 16px;
  padding: 16px 0;
  font-size: 16px;
  font-weight: 600;
  width: 100%;  /* 确保按钮占满容器宽度 */
  max-width: 400px;  /* 限制最大宽度，避免在大屏幕上太宽 */
  margin: 16px auto 0;  /* 水平居中 */
  display: block;  /* 确保margin auto生效 */
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.primary:not(:disabled):active {
  background: #1e293b;
  transform: scale(0.98);
}

.primary:disabled {
  background: #94a3b8;
  opacity: 0.6;
  box-shadow: none;
}

.policy-wrap {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  align-items: center;
  gap: 2px;
  line-height: 1.6;
}

.policy {
  font-size: 11px;
  color: #94a3b8;
}

.policy-link {
  font-size: 11px;
  color: #f97316;
  font-weight: 600;
  text-decoration: underline;
}
</style>
