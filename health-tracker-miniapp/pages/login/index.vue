<template>
  <view class="page">
    <view class="topbar">
      <view class="brand">
        <view class="logo">HT</view>
        <text class="brand-cn">健康管家</text>
      </view>
      <text class="version">v1.0</text>
    </view>

    <view class="banner">
      <view class="banner-bg" />
      <view class="banner-glow" />
      <view class="banner-content">
        <view class="banner-icon-wrap">
          <text class="banner-icon">✨</text>
        </view>
        <text class="banner-tag">AI 健康助手</text>
        <text class="banner-title">懂你的健康，随时为你解答</text>
        <text class="banner-sub">饮食、运动、睡眠，一问即得</text>
      </view>
    </view>

    <view class="bottom-wrap">
      <view class="login-card">
        <!-- #ifdef MP-WEIXIN -->
        <button class="btn-wx" @tap="loginWeChat" :disabled="loading">
          <text>{{ loading ? "登录中..." : "微信一键登录" }}</text>
        </button>
        <!-- #endif -->
        <!-- #ifndef MP-WEIXIN -->
        <view class="btn-wx disabled">
          <text class="btn-wx-icon">微</text>
          <text>请使用微信小程序打开</text>
        </view>
        <!-- #endif -->
      </view>
      <text class="policy">登录即表示同意《用户协议》与《隐私政策》</text>
      <text v-if="message" class="message">{{ message }}</text>
    </view>
  </view>
</template>

<script>
import { request } from "../../utils/api";
import { ensureDevLogin } from "../../utils/dev-auth";

export default {
  data() {
    return {
      loading: false,
      message: ""
    };
  },
  onShow() {
    ensureDevLogin();
  },
  methods: {
    loginWeChat() {
      this.loading = true;
      this.message = "";
      uni.removeStorageSync("__devLoginTriedAt");
      uni.removeStorageSync("loginSource");
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
            uni.setStorageSync("loginSource", "wechat");
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
  min-height: 100vh;
  padding: 24px 20px 28px;
  padding-top: calc(24px + env(safe-area-inset-top));
  padding-bottom: calc(28px + env(safe-area-inset-bottom));
  background: linear-gradient(180deg, #eef2ff 0%, #e0e7ff 30%, #f5f3ff 100%);
  color: #0f172a;
  display: flex;
  flex-direction: column;
}

.topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo {
  width: 38px;
  height: 38px;
  border-radius: 14px;
  background: linear-gradient(135deg, #4338ca 0%, #6366f1 50%, #818cf8 100%);
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6px 20px rgba(99, 102, 241, 0.45);
}

.brand-cn {
  font-size: 18px;
  font-weight: 600;
  color: #1e1b4b;
}

.version {
  font-size: 11px;
  color: #6366f1;
  opacity: 0.8;
}

.banner {
  position: relative;
  border-radius: 24px;
  overflow: hidden;
  height: 220px;
  margin-bottom: 20px;
  box-shadow: 0 12px 40px rgba(67, 56, 202, 0.25);
}

.banner-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #312e81 0%, #4338ca 30%, #4f46e5 60%, #6366f1 100%);
}

.banner-glow {
  position: absolute;
  top: -50%;
  right: -30%;
  width: 90%;
  height: 120%;
  background: radial-gradient(circle, rgba(255,255,255,0.22) 0%, transparent 60%);
  border-radius: 50%;
}

.banner-content {
  position: relative;
  height: 100%;
  padding: 24px 24px 32px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  color: #fff;
}

.banner-icon-wrap {
  width: 50px;
  height: 50px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.28);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
}

.banner-icon {
  font-size: 28px;
}

.banner-tag {
  font-size: 12px;
  opacity: 0.98;
  letter-spacing: 0.5px;
  margin-bottom: 6px;
}

.banner-title {
  font-size: 18px;
  font-weight: 700;
  line-height: 1.35;
  margin-bottom: 6px;
}

.banner-sub {
  font-size: 13px;
  opacity: 0.92;
}

.bottom-wrap {
  margin-top: auto;
  padding-top: 8px;
}

.login-card {
  background: #fff;
  border-radius: 22px;
  padding: 22px;
  box-shadow: 0 10px 40px rgba(67, 56, 202, 0.12);
  margin-bottom: 14px;
  border: 1px solid rgba(99, 102, 241, 0.12);
}

.btn-wx {
  width: 100%;
  height: 54px;
  border-radius: 16px;
  background: linear-gradient(180deg, #06c755 0%, #05b04d 100%);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  box-shadow: 0 6px 20px rgba(5, 176, 77, 0.35);
}

.btn-wx::after {
  border: none;
}

.btn-wx.disabled {
  background: #cbd5e1;
  color: #64748b;
  box-shadow: none;
}

.btn-wx-icon {
  font-size: 20px;
  font-weight: 600;
}

.policy {
  font-size: 11px;
  color: #6366f1;
  opacity: 0.85;
  text-align: center;
  display: block;
  line-height: 1.5;
}

.message {
  font-size: 13px;
  color: #dc2626;
  text-align: center;
  display: block;
  margin-top: 10px;
}
</style>
