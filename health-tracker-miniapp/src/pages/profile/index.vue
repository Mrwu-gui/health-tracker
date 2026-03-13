<template>
  <view class="page">
    <view class="profile">
      <view class="avatar">{{ profile.name.slice(0, 1) }}</view>
      <view>
        <text class="name">{{ profile.name }}</text>
        <text class="tag">{{ profile.tag }}</text>
      </view>
    </view>

    <view class="card">
      <text class="label">身高</text>
      <text class="value">{{ profile.height || "未维护" }}</text>
    </view>
    <view class="card">
      <text class="label">体重</text>
      <text class="value">{{ profile.weight || "未维护" }}</text>
    </view>
    <view class="card">
      <text class="label">目标</text>
      <text class="value">{{ profile.goal }}</text>
    </view>
    <view class="card phone-card" @tap="openPhoneModal">
      <text class="label">手机号</text>
      <text class="value">{{ profile.phone || "未绑定（点击绑定）" }}</text>
    </view>
    <view v-if="showPhoneModal" class="modal-mask" @tap="closePhoneModal">
      <view class="modal" @tap.stop>
        <text class="modal-title">绑定手机号</text>
        <!-- #ifdef MP-WEIXIN -->
        <button
          class="primary"
          open-type="getPhoneNumber"
          @getphonenumber="bindWeChatPhone"
          :disabled="phoneLoading"
        >
          {{ phoneLoading ? "授权中..." : "微信手机号授权" }}
        </button>
        <!-- #endif -->
        <view class="divider">或</view>
        <input
          class="input"
          v-model="phoneForm.phone"
          placeholder="输入手机号"
        />
        <view class="sms-row">
          <input
            class="input code"
            v-model="phoneForm.captcha"
            placeholder="图形验证码"
          />
          <view class="captcha-box" @tap="loadCaptcha">
            <image v-if="captchaImage" :src="captchaImage" mode="aspectFill" />
            <text v-else>点击刷新</text>
          </view>
        </view>
        <view class="sms-row">
          <input
            class="input code"
            v-model="phoneForm.code"
            placeholder="验证码"
          />
          <button class="ghost" @tap="sendSms" :disabled="smsLoading">
            {{ smsLoading ? "发送中..." : "发送验证码" }}
          </button>
        </view>
        <button class="primary" @tap="bindBySms" :disabled="bindLoading">
          {{ bindLoading ? "绑定中..." : "绑定手机号" }}
        </button>
      </view>
    </view>
    <text v-if="message" class="status">{{ message }}</text>
    <text v-if="loading" class="status">加载中...</text>
    <text v-if="error" class="status error">{{ error }}</text>
  </view>
</template>

<script>
import { request } from "../../utils/api";

export default {
  data() {
    return {
      profile: {
        name: "未登录",
        tag: "请先登录",
        height: "",
        weight: "",
        goal: "暂无目标",
        phone: ""
      },
      loading: false,
      error: "",
      message: "",
      phoneLoading: false,
      showPhoneModal: false,
      smsLoading: false,
      bindLoading: false,
      captchaKey: "",
      captchaImage: "",
      phoneForm: {
        phone: "",
        code: "",
        captcha: ""
      }
    };
  },
  onLoad() {
    this.fetchProfile();
  },
  methods: {
    fetchProfile() {
      this.loading = true;
      this.error = "";
      this.message = "";
      const userId = uni.getStorageSync("userId") || 1;
      request("/api/user/profile", "GET", { userId })
        .then((data) => {
          if (data) {
            this.profile.name = data.wxNickname || data.username || this.profile.name;
            if (data.height) this.profile.height = `${data.height} 厘米`;
            if (data.weight) this.profile.weight = `${data.weight} 千克`;
            this.profile.phone = data.phone || data.wxPhone || "";
          }
        })
        .catch(() => {
          this.error = "获取资料失败";
        })
        .finally(() => {
          this.loading = false;
        });
    },
    openPhoneModal() {
      if (this.profile.phone) return;
      this.showPhoneModal = true;
      this.phoneForm.phone = "";
      this.phoneForm.code = "";
      this.phoneForm.captcha = "";
      this.loadCaptcha();
    },
    closePhoneModal() {
      this.showPhoneModal = false;
    },
    bindWeChatPhone(event) {
      this.phoneLoading = true;
      this.message = "";
      const detail = event?.detail || {};
      if (!detail.encryptedData || !detail.iv) {
        this.message = "未获取到手机号授权";
        this.phoneLoading = false;
        return;
      }
      uni.login({
        provider: "weixin",
        success: (res) => {
          const token = uni.getStorageSync("token");
          const ensureLogin = token
            ? Promise.resolve()
            : request("/api/auth/mini/login", "POST", { code: res.code })
                .then((data) => {
                  if (data?.token) {
                    uni.setStorageSync("token", data.token);
                    if (data.userId) {
                      uni.setStorageSync("userId", data.userId);
                    }
                  }
                });
          ensureLogin
            .then(() =>
              request("/api/auth/mini/bind-phone", "POST", {
                code: res.code,
                encryptedData: detail.encryptedData,
                iv: detail.iv
              })
            )
            .then((data) => {
              this.profile.phone = data.phone;
              this.message = "手机号绑定成功";
              uni.showToast({ title: this.message, icon: "success" });
              this.showPhoneModal = false;
            })
            .catch((err) => {
              this.message = err.message || "手机号绑定失败";
              uni.showToast({ title: this.message, icon: "none" });
            })
            .finally(() => {
              this.phoneLoading = false;
            });
        },
        fail: () => {
          this.message = "微信登录失败";
          uni.showToast({ title: this.message, icon: "none" });
          this.phoneLoading = false;
        }
      });
    },
    sendSms() {
      if (!this.phoneForm.phone) {
        this.message = "请输入手机号";
        return;
      }
      if (!this.phoneForm.captcha || !this.captchaKey) {
        this.message = "请先输入图形验证码";
        return;
      }
      this.smsLoading = true;
      request("/api/auth/phone/send", "POST", {
        phone: this.phoneForm.phone,
        captchaKey: this.captchaKey,
        captchaCode: this.phoneForm.captcha
      })
        .then(() => {
          this.message = "验证码已发送";
          uni.showToast({ title: "验证码已发送", icon: "success" });
        })
        .catch((err) => {
          this.message = err.message || "发送失败";
          uni.showToast({ title: this.message, icon: "none" });
          this.loadCaptcha();
        })
        .finally(() => {
          this.smsLoading = false;
        });
    },
    bindBySms() {
      if (!this.phoneForm.phone || !this.phoneForm.code) {
        this.message = "请输入手机号和验证码";
        return;
      }
      this.bindLoading = true;
      request("/api/auth/phone/login", "POST", {
        phone: this.phoneForm.phone,
        code: this.phoneForm.code
      })
        .then((data) => {
          if (data?.token) {
            uni.setStorageSync("token", data.token);
            if (data.userId) {
              uni.setStorageSync("userId", data.userId);
            }
          }
          this.profile.phone = this.phoneForm.phone;
          this.message = "手机号绑定成功";
          uni.showToast({ title: this.message, icon: "success" });
          this.showPhoneModal = false;
        })
        .catch((err) => {
          this.message = err.message || "绑定失败";
          uni.showToast({ title: this.message, icon: "none" });
        })
        .finally(() => {
          this.bindLoading = false;
        });
    },
    loadCaptcha() {
      request("/api/auth/captcha", "GET")
        .then((data) => {
          this.captchaKey = data.key;
          this.captchaImage = data.image;
        })
        .catch(() => {
          this.message = "获取验证码失败";
        });
    }
  }
};
</script>

<style>
.page {
  padding: 22px;
  min-height: 100vh;
  background: #f7f4ef;
  color: #2d2a26;
}

.profile {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.avatar {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  background: #efe7dd;
  color: #5c5046;
  display: grid;
  place-items: center;
  font-weight: 600;
}

.name {
  font-weight: 600;
  display: block;
}

.tag {
  color: #7c736b;
  font-size: 12px;
}

.card {
  background: #ffffff;
  border-radius: 16px;
  padding: 14px 16px;
  margin-bottom: 12px;
  border: 1px solid #efe7dd;
  box-shadow: 0 8px 18px rgba(30, 25, 18, 0.05);
}

.label {
  color: #7c736b;
  font-size: 12px;
  display: block;
}

.value {
  font-weight: 600;
  margin-top: 6px;
  display: block;
}

.status {
  display: block;
  color: #7c736b;
  margin-top: 8px;
}

.status.error {
  color: #ef4444;
}

.primary {
  background: #ede3d7;
  color: #5c5046;
  border-radius: 999px;
}

.secondary {
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e6dccf;
  color: #6a5f55;
}

.phone-card {
  border: 1px dashed #e6dccf;
}

.modal-mask {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

.modal {
  background: #ffffff;
  width: 82%;
  border-radius: 18px;
  padding: 18px;
  border: 1px solid #efe7dd;
}

.modal-title {
  font-weight: 600;
  margin-bottom: 12px;
  display: block;
}

.divider {
  color: #8d847c;
  font-size: 12px;
  text-align: center;
  margin: 10px 0;
}

.input {
  border: 1px solid #efe7dd;
  border-radius: 12px;
  padding: 8px 12px;
  background: #fbf9f6;
  margin-bottom: 10px;
}

.sms-row {
  display: flex;
  gap: 8px;
  align-items: center;
}

.captcha-box {
  width: 100px;
  height: 36px;
  border-radius: 10px;
  background: #f1ede6;
  display: grid;
  place-items: center;
  color: #7c736b;
  font-size: 12px;
  border: 1px solid #efe7dd;
}

.captcha-box image {
  width: 100%;
  height: 100%;
  border-radius: 10px;
}

.code {
  flex: 1;
}

.ghost {
  border-radius: 999px;
  background: #ffffff;
  border: 1px solid #e6dccf;
  color: #6a5f55;
  font-size: 12px;
  padding: 6px 12px;
}

</style>
