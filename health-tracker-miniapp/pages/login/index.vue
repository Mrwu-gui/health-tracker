<template>
  <view class="page">
    <view class="hero">
      <view class="logo-wrap">
        <view class="logo">智康</view>
      </view>
      <text class="app-name">智康AI</text>
      <text class="slogan">懂你的健康，一问即得</text>
      <text class="slogan-sub">饮食 · 运动 · 睡眠</text>
    </view>

    <view class="bottom-wrap">
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
      <view class="policy-wrap">
        <text class="policy">登录即表示同意</text>
        <text class="policy-link" @tap="openAgreement('user')">《用户协议》</text>
        <text class="policy">与</text>
        <text class="policy-link" @tap="openAgreement('privacy')">《隐私政策》</text>
      </view>
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
      message: ""
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
    openAgreement(type) {
      const path = type === "user" ? "/pages/agreement/user" : "/pages/agreement/privacy";
      uni.navigateTo({ url: path });
    },
    loginWeChat() {
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
  padding-bottom: calc(env(safe-area-inset-bottom) + 24px);
  box-sizing: border-box;
  background: linear-gradient(180deg, #fef9f3 0%, #fef5eb 35%, #fef3c7 100%);
  color: #1c1917;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.hero {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 12px 0 16px;
}

.logo-wrap {
  margin-bottom: 12px;
}

.logo {
  width: 56px;
  height: 56px;
  border-radius: 18px;
  background: linear-gradient(145deg, #f59e0b 0%, #ea580c 100%);
  color: #fff;
  font-size: 18px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(245, 158, 11, 0.35);
}

.app-name {
  font-size: 22px;
  font-weight: 700;
  color: #1c1917;
  letter-spacing: 0.5px;
  margin-bottom: 6px;
}

.slogan {
  font-size: 13px;
  color: #78716c;
  margin-bottom: 4px;
}

.slogan-sub {
  font-size: 12px;
  color: #a8a29e;
}

.bottom-wrap {
  flex-shrink: 0;
  width: 100%;
  max-width: 320px;
  padding-top: 12px;
}

.btn-wx {
  width: 100%;
  height: 48px;
  border-radius: 14px;
  background: linear-gradient(180deg, #22c55e 0%, #16a34a 100%);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  box-shadow: 0 6px 20px rgba(34, 197, 94, 0.3);
  margin-bottom: 14px;
}

.btn-test {
  width: 100%;
  height: 48px;
  border-radius: 14px;
  background: #fff7ed;
  color: #9a3412;
  font-size: 14px;
  font-weight: 600;
  border: 1px solid rgba(251, 146, 60, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 12px;
  box-shadow: 0 4px 16px rgba(251, 146, 60, 0.18);
}

.btn-test::after {
  border: none;
}

.btn-wx::after {
  border: none;
}

.btn-wx.disabled {
  background: #d6d3d1;
  color: #78716c;
  box-shadow: none;
}

.btn-wx-icon {
  font-size: 20px;
  font-weight: 600;
}

.policy-wrap {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  align-items: center;
  gap: 2px;
  line-height: 1.5;
}

.policy {
  font-size: 11px;
  color: #a8a29e;
}

.policy-link {
  font-size: 11px;
  color: #ea580c;
  font-weight: 500;
  text-decoration: underline;
}

.message {
  font-size: 13px;
  color: #dc2626;
  text-align: center;
  display: block;
  margin-top: 10px;
}
</style>
