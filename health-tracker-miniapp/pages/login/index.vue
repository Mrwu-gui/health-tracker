<template>
  <view class="page">
    <view class="hero">
      <text class="badge">健康助手</text>
      <text class="hero-title">更懂你的健康记录</text>
      <text class="hero-subtitle">每天一点点，养成好习惯</text>
    </view>
    <view class="card">
      <text class="title">欢迎回来</text>
      <text class="subtitle">登录后同步步数、饮食、睡眠与用药提醒</text>
      <!-- #ifdef MP-WEIXIN -->
      <button class="primary" @click="loginWeChat" :disabled="loading">
        {{ loading ? "登录中..." : "微信一键登录" }}
      </button>
      <!-- #endif -->
      <!-- #ifndef MP-WEIXIN -->
      <button class="primary" @click="loginDemo" :disabled="loading">
        {{ loading ? "登录中..." : "演示账号登录" }}
      </button>
      <!-- #endif -->
      <text v-if="message" class="message">{{ message }}</text>
    </view>
    <view class="tips">
      <text>• 今日步数自动同步</text>
      <text>• 睡眠支持手动记录</text>
      <text>• 用药提醒更安心</text>
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
  methods: {
    loginWeChat() {
      this.loading = true;
      this.message = "";
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
                uni.switchTab({ url: "/pages/index/index" });
                uni.showToast({ title: "登录成功", icon: "success" });
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
    }
  }
};
</script>

<style>
.page {
  min-height: 100vh;
  padding: 28px 22px 32px;
  background: linear-gradient(180deg, #f6efe6 0%, #f9f6f1 40%, #ffffff 100%);
  color: #2d2a26;
}

.hero {
  margin-top: 12px;
  margin-bottom: 18px;
}

.badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 999px;
  background: #efe3d2;
  color: #7b5f3c;
  font-size: 11px;
}

.hero-title {
  display: block;
  margin-top: 8px;
  font-size: 22px;
  font-weight: 700;
}

.hero-subtitle {
  display: block;
  margin-top: 6px;
  color: #8a7a67;
  font-size: 12px;
}

.card {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  padding: 24px;
  text-align: center;
  box-shadow: 0 20px 40px rgba(15, 23, 42, 0.15);
  width: 100%;
  max-width: 340px;
  margin: 0 auto;
}

.title {
  font-size: 20px;
  font-weight: 600;
}

.subtitle {
  display: block;
  color: #64748b;
  margin: 12px 0 20px;
  font-size: 13px;
}

.primary {
  background: #5a4b3b;
  color: #ffffff;
  border-radius: 12px;
}

.message {
  display: block;
  margin-top: 12px;
  color: #ef4444;
  font-size: 12px;
}

.tips {
  margin-top: 18px;
  color: #8a7a67;
  font-size: 12px;
  display: grid;
  gap: 6px;
}
</style>
