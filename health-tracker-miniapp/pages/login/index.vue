<template>
  <view class="page">
    <!-- 装饰背景 -->
    <view class="bg-decor">
      <view class="bg-circle bg-circle-1"></view>
      <view class="bg-circle bg-circle-2"></view>
      <view class="bg-circle bg-circle-3"></view>
    </view>

    <view class="hero">
      <view class="logo-wrap">
        <view class="logo">
          <text class="logo-text">智康</text>
        </view>
        <view class="logo-glow"></view>
      </view>
      <text class="app-name">智康 AI</text>
      <text class="slogan">懂你的健康，一问即得</text>
    </view>

    <view class="bottom-wrap">
      <!-- 协议勾选 -->
      <view class="agreement-wrap">
        <view class="checkbox" :class="{ checked: agreed }" @tap="toggleAgree">
          <text v-if="agreed" class="check-icon">✓</text>
        </view>
        <text class="agreement-text">我已阅读并同意</text>
        <text class="agreement-link" @tap="openAgreement('user')">《用户协议》</text>
        <text class="agreement-text">与</text>
        <text class="agreement-link" @tap="openAgreement('privacy')">《隐私政策》</text>
      </view>

      <!-- #ifdef MP-WEIXIN -->
        <button class="btn-wx" @tap="loginWeChat" :disabled="loading || !agreed" :class="{ disabled: !agreed }">
          <text>{{ loading ? "登录中..." : "微信一键登录" }}</text>
        </button>
      <!-- #endif -->
      <!-- #ifndef MP-WEIXIN -->
      <view class="btn-wx disabled">
        <text>请使用微信小程序打开</text>
      </view>
      <!-- #endif -->
      <text v-if="message" class="message">{{ message }}</text>
    </view>
  </view>
</template>

<script>
import { request } from "../../utils/api";

export default {
  data() {
    return {
      loading: false,
      message: "",
      agreed: false
    };
  },
  onShow() {
    const token = uni.getStorageSync("token");
    const userId = uni.getStorageSync("userId");
    if (token && userId) {
      uni.switchTab({ url: "/pages/index/index" });
    }
  },
  methods: {
    toggleAgree() {
      this.agreed = !this.agreed;
    },
    openAgreement(type) {
      const path = type === "user" ? "/pages/agreement/user" : "/pages/agreement/privacy";
      uni.navigateTo({ url: path });
    },
    loginWeChat() {
      if (!this.agreed) {
        uni.showToast({ title: "请先同意用户协议", icon: "none" });
        return;
      }
      this.loading = true;
      this.message = "";
      uni.login({
        provider: "weixin",
        success: (res) => {
          this.loginWithCode(res.code);
        },
        fail: () => {
          this.message = "微信登录失败";
          uni.showToast({ title: this.message, icon: "none" });
          this.loading = false;
        }
      });
    },
    loginWithCode(code) {
      request("/api/auth/mini/login", "POST", { code })
        .then((data) => {
          if (data?.token) {
            uni.setStorageSync("token", data.token);
            if (data.userId) uni.setStorageSync("userId", data.userId);
            const userId = data.userId;
            if (userId) {
              request("/api/user/profile", "GET", { userId })
                .then((profile) => {
                  const hasProfile =
                    profile &&
                    (profile.wxNickname || profile.username) &&
                    (profile.sex || profile.age || profile.height || profile.weight);
                  uni.setStorageSync("needBodyProfile", !hasProfile);
                })
                .catch(() => uni.setStorageSync("needBodyProfile", true))
                .finally(() => {
                  uni.switchTab({ url: "/pages/index/index" });
                  uni.showToast({ title: "登录成功", icon: "success" });
                });
            } else {
              uni.setStorageSync("needBodyProfile", true);
              uni.switchTab({ url: "/pages/index/index" });
              uni.showToast({ title: "登录成功", icon: "success" });
            }
          }
        })
        .catch((err) => {
          this.message = err.message || "登录失败";
          uni.showToast({ title: this.message, icon: "none" });
        })
        .finally(() => {
          this.loading = false;
        });
    }
  }
};
</script>

<style>
.page {
  height: 100vh;
  height: 100dvh;
  overflow: hidden;
  padding: 0 24px;
  padding-top: calc(env(safe-area-inset-top) + 24px);
  padding-bottom: calc(env(safe-area-inset-bottom) + 32px);
  box-sizing: border-box;
  background: linear-gradient(165deg, #fef9f3 0%, #fff7ed 40%, #ffedd5 100%);
  color: #1c1917;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

/* 装饰背景 */
.bg-decor {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  pointer-events: none;
}

.bg-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.4;
}

.bg-circle-1 {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, #fed7aa 0%, transparent 70%);
  top: -100px;
  right: -80px;
}

.bg-circle-2 {
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, #fecaca 0%, transparent 70%);
  bottom: 20%;
  left: -60px;
}

.bg-circle-3 {
  width: 150px;
  height: 150px;
  background: radial-gradient(circle, #fef08a 0%, transparent 70%);
  bottom: 40%;
  right: -30px;
}

.hero {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px 0 16px;
  position: relative;
  z-index: 1;
}

.logo-wrap {
  margin-bottom: 16px;
  position: relative;
}

.logo {
  width: 72px;
  height: 72px;
  border-radius: 22px;
  background: linear-gradient(145deg, #f97316 0%, #ea580c 100%);
  color: #fff;
  font-size: 22px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 12px 32px rgba(249, 115, 22, 0.4);
  position: relative;
  z-index: 2;
}

.logo-text {
  letter-spacing: 2px;
}

.logo-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100px;
  height: 100px;
  background: radial-gradient(circle, rgba(249, 115, 22, 0.25) 0%, transparent 70%);
  border-radius: 50%;
  z-index: 1;
}

.app-name {
  font-size: 26px;
  font-weight: 800;
  color: #1c1917;
  letter-spacing: 1px;
  margin-bottom: 8px;
}

.slogan {
  font-size: 15px;
  color: #78716c;
  font-weight: 500;
}

.bottom-wrap {
  flex-shrink: 0;
  width: 100%;
  max-width: 320px;
  padding-top: 16px;
  position: relative;
  z-index: 1;
}

/* 协议勾选 */
.agreement-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 4px;
  margin-bottom: 20px;
}

.checkbox {
  width: 18px;
  height: 18px;
  border-radius: 4px;
  border: 1.5px solid #d6d3d1;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 4px;
  transition: all 0.2s;
  flex-shrink: 0;
}

.checkbox.checked {
  background: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
  border-color: #f97316;
}

.check-icon {
  color: #fff;
  font-size: 12px;
  font-weight: bold;
  line-height: 1;
}

.agreement-text {
  font-size: 12px;
  color: #78716c;
}

.agreement-link {
  font-size: 12px;
  color: #f97316;
  font-weight: 500;
}

.btn-wx {
  width: 100%;
  height: 52px;
  border-radius: 26px;
  background: linear-gradient(135deg, #07c160 0%, #06ad56 100%);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  box-shadow: 0 8px 24px rgba(7, 193, 96, 0.35);
  letter-spacing: 0.5px;
}

.btn-wx::after {
  border: none;
}

.btn-wx.disabled {
  background: linear-gradient(135deg, #94a3b8 0%, #64748b 100%);
  box-shadow: none;
  opacity: 0.7;
}

.wx-icon {
  width: 22px;
  height: 22px;
  background: #fff;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.wx-icon-svg {
  font-size: 10px;
  font-weight: bold;
  color: #07c160;
  letter-spacing: 0;
}

.policy-wrap {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  align-items: center;
  gap: 2px;
  line-height: 1.6;
  margin-top: 16px;
}

.policy {
  font-size: 11px;
  color: #a8a29e;
}

.policy-link {
  font-size: 11px;
  color: #f97316;
  font-weight: 500;
  text-decoration: underline;
}

.message {
  font-size: 13px;
  color: #ef4444;
  text-align: center;
  display: block;
  margin-top: 12px;
}
</style>
