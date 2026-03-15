<template>
  <view class="page">
    <view class="topbar">
      <view class="brand">
        <view class="logo">HT</view>
        <view>
          <text class="brand-en">Health Tracker</text>
          <text class="brand-cn">健康管理工具</text>
        </view>
      </view>
      <text class="version">v1.0</text>
    </view>

    <view class="banner">
      <image
        class="banner-img"
        src="https://images.unsplash.com/photo-1517836357463-d25dfeac3438?auto=format&fit=crop&w=900&q=80"
        mode="aspectFill"
      />
      <view class="banner-mask"></view>
      <view class="banner-content">
        <text class="banner-tag">今日关怀 · 健康每一步</text>
        <text class="banner-title">坚持记录，让未来的自己感谢现在的你。</text>
        <text class="banner-sub">小小的改变，会累积成看得见的进步。</text>
      </view>
    </view>

    <view class="intro">
      <text class="intro-title">一键开始你的健康旅程</text>
      <text class="intro-sub">仅在你授权的范围内，安全同步健康数据。</text>
    </view>

    <view class="login-card">
      <view class="login-row">
        <view class="login-icon wx">
          <text class="fa-brands fa-weixin">微</text>
        </view>
        <view class="login-meta">
          <text class="login-title">微信账号一键登录</text>
          <text class="login-desc">使用当前微信安全登录，方便又安心。</text>
        </view>
      </view>
      <!-- #ifdef MP-WEIXIN -->
      <button class="btn-primary" @tap="loginWeChat" :disabled="loading">
        {{ loading ? "登录中..." : "使用微信一键登录" }}
      </button>
      <!-- #endif -->
      <!-- #ifndef MP-WEIXIN -->
      <button class="btn-primary" @tap="loginDemo" :disabled="loading">
        {{ loading ? "登录中..." : "演示账号登录" }}
      </button>
      <!-- #endif -->
    </view>

    <view class="login-card slim">
      <text class="policy">
        手机号登录暂不开放
      </text>
      <text class="policy">
        登录即代表你已阅读并同意《用户协议》和《隐私政策》
      </text>
    </view>

    <text v-if="message" class="message">{{ message }}</text>
    <text class="footer-note">为中老年用户优化 · 大字号 · 高对比度</text>
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
          request("/api/auth/mini/login", "POST", { code: res.code })
            .then((data) => {
              if (data?.token) {
                uni.setStorageSync("token", data.token);
                if (data.userId) {
                  uni.setStorageSync("userId", data.userId);
                }
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
                    .catch(() => {
                      uni.setStorageSync("needBodyProfile", true);
                    })
                    .finally(() => {
                      uni.switchTab({ url: "/pages/profile/index" });
                      uni.showToast({ title: "登录成功", icon: "success" });
                    });
                } else {
                  uni.setStorageSync("needBodyProfile", true);
                  uni.switchTab({ url: "/pages/profile/index" });
                  uni.showToast({ title: "登录成功", icon: "success" });
                }
              }
            })
            .catch((err) => {
              this.message = err.message || "微信登录失败";
              uni.showToast({ title: this.message, icon: "none" });
            })
            .finally(() => {
              this.loading = false;
            });
        },
        fail: () => {
          this.message = "微信登录失败";
          uni.showToast({ title: this.message, icon: "none" });
          this.loading = false;
        }
      });
    },
    loginDemo() {
      this.loading = true;
      this.message = "";
      request("/api/user/login", "POST", { username: "demo", password: "demo" })
        .then((data) => {
          if (data?.token) {
            uni.setStorageSync("token", data.token);
            if (data.userId) {
              uni.setStorageSync("userId", data.userId);
            }
            uni.setStorageSync("loginSource", "demo");
            uni.switchTab({ url: "/pages/index/index" });
            uni.showToast({ title: "登录成功", icon: "success" });
          }
        })
        .catch((err) => {
          this.message = err.message || "演示登录失败";
          uni.showToast({ title: this.message, icon: "none" });
        })
        .finally(() => {
          this.loading = false;
        });
    },
    goPhoneLogin() {}
  }
};
</script>

<style>
.page {
  min-height: 100vh;
  padding: 20px 18px 28px;
  background: #f5f6f8;
  color: #0f172a;
}

.topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 8px;
}

.logo {
  width: 32px;
  height: 32px;
  border-radius: 12px;
  background: #0f172a;
  color: #ffffff;
  font-size: 12px;
  font-weight: 600;
  display: grid;
  place-items: center;
}

.brand-en {
  font-size: 10px;
  letter-spacing: 2px;
  color: #94a3b8;
  text-transform: uppercase;
  display: block;
}

.brand-cn {
  font-size: 16px;
  font-weight: 600;
  color: #0f172a;
  display: block;
}

.version {
  font-size: 11px;
  color: #94a3b8;
}

.banner {
  position: relative;
  border-radius: 20px;
  overflow: hidden;
  height: 160px;
  margin-bottom: 16px;
}

.banner-img {
  width: 100%;
  height: 100%;
}

.banner-mask {
  position: absolute;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
}

.banner-content {
  position: absolute;
  inset: 0;
  padding: 14px;
  color: #ffffff;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.banner-tag {
  font-size: 11px;
  opacity: 0.9;
}

.banner-title {
  font-size: 14px;
  font-weight: 600;
}

.banner-sub {
  font-size: 11px;
  opacity: 0.8;
}

.intro {
  margin-bottom: 12px;
}

.intro-title {
  font-size: 13px;
  font-weight: 600;
  display: block;
}

.intro-sub {
  font-size: 11px;
  color: #64748b;
  display: block;
  margin-top: 4px;
}

.login-card {
  background: #ffffff;
  border-radius: 18px;
  padding: 16px;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.08);
  margin-bottom: 12px;
}

.login-card.slim {
  padding: 12px 16px;
}

.login-row {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 12px;
}

.login-icon {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: #dcfce7;
  color: #16a34a;
  display: grid;
  place-items: center;
  font-size: 12px;
  font-weight: 600;
}

.login-meta {
  flex: 1;
}

.login-title {
  font-size: 12px;
  font-weight: 600;
  display: block;
}

.login-desc {
  font-size: 11px;
  color: #64748b;
  display: block;
  margin-top: 2px;
}

.btn-primary {
  background: #22c55e;
  color: #ffffff;
  border-radius: 14px;
  font-size: 12px;
  padding: 10px 0;
}

.btn-dark {
  background: #0f172a;
  color: #ffffff;
  border-radius: 14px;
  font-size: 12px;
  padding: 10px 0;
}

.policy {
  font-size: 10px;
  color: #94a3b8;
  text-align: center;
  margin-top: 8px;
}

.message {
  font-size: 12px;
  color: #ef4444;
  text-align: center;
  margin-top: 6px;
}

.footer-note {
  font-size: 10px;
  color: #94a3b8;
  text-align: center;
  margin-top: 12px;
}
</style>
