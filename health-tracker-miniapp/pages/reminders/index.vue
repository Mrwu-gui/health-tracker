<template>
  <view class="page">
    <view class="header">
      <text class="title">提醒</text>
      <text class="subtitle">保持规律作息</text>
    </view>
    <view class="actions">
      <button class="primary" @tap="openModal">新增提醒</button>
      <button class="ghost" @tap="subscribeMessage">开启微信推送</button>
    </view>
    <view class="card" v-for="item in reminders" :key="item.id">
      <view class="row">
        <text class="label">{{ item.title }}</text>
        <text class="badge">{{ item.type }}</text>
      </view>
      <text class="hint">{{ item.time }}</text>
    </view>
    <text class="disabled-hint" v-if="!reminders.length && !loading">暂无提醒</text>
    <text v-if="loading" class="status">加载中...</text>
    <text v-if="error" class="status error">{{ error }}</text>

    <view v-if="showModal" class="modal-mask" @tap="closeModal">
      <view class="modal" @tap.stop>
        <text class="modal-title">新增提醒</text>
        <input class="input" v-model="form.title" placeholder="提醒标题" />
        <input class="input" v-model="form.type" placeholder="类型（如 用药/运动）" />
        <input class="input" v-model="form.content" placeholder="提醒内容" />
        <input class="input" v-model="form.remindTime" placeholder="提醒时间 2026-03-13 18:30" />
        <button class="primary" @tap="createReminder" :disabled="saving">
          {{ saving ? "创建中..." : "创建提醒" }}
        </button>
      </view>
    </view>
  </view>
</template>

<script>
import { request } from "../../utils/api";
const TEMPLATE_ID = process.env.UNI_WX_TEMPLATE_ID || "";

export default {
  data() {
    return {
      loading: false,
      error: "",
      reminders: [],
      showModal: false,
      saving: false,
      form: {
        title: "",
        type: "",
        content: "",
        remindTime: ""
      }
    };
  },
  onLoad() {
    this.fetchReminders();
  },
  methods: {
    fetchReminders() {
      this.loading = true;
      this.error = "";
      const userId = uni.getStorageSync("userId") || 1;
      request("/api/reminder/list", "GET", { userId })
        .then((data) => {
          if (Array.isArray(data)) {
            this.reminders = data.map((item) => ({
              id: item.id,
              title: item.title,
              type: item.type,
              time: item.remindTime || "未设置"
            }));
          }
        })
        .catch(() => {
          this.error = "获取提醒失败";
        })
        .finally(() => {
          this.loading = false;
        });
    },
    openModal() {
      this.showModal = true;
      this.form = { title: "", type: "", content: "", remindTime: "" };
    },
    closeModal() {
      this.showModal = false;
    },
    createReminder() {
      if (!this.form.title || !this.form.type) {
        uni.showToast({ title: "请完善提醒信息", icon: "none" });
        return;
      }
      this.saving = true;
      request("/api/reminder/add", "POST", this.form)
        .then(() => {
          uni.showToast({ title: "已创建提醒", icon: "success" });
          this.showModal = false;
          this.fetchReminders();
        })
        .catch((err) => {
          uni.showToast({ title: err.message || "创建失败", icon: "none" });
        })
        .finally(() => {
          this.saving = false;
        });
    },
    subscribeMessage() {
      if (!TEMPLATE_ID) {
        uni.showToast({ title: "请配置模板ID", icon: "none" });
        return;
      }
      if (process.env.UNI_PLATFORM !== "mp-weixin") {
        uni.showToast({ title: "请在微信小程序内操作", icon: "none" });
        return;
      }
      uni.requestSubscribeMessage({
        tmplIds: [TEMPLATE_ID],
        success: () => {
          uni.showToast({ title: "已开启提醒", icon: "success" });
        },
        fail: () => {
          uni.showToast({ title: "订阅失败", icon: "none" });
        }
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

.header {
  margin-bottom: 14px;
}

.actions {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
}

.primary {
  background: #5a4b3b;
  color: #ffffff;
  border-radius: 12px;
}

.ghost {
  background: #f1ede6;
  color: #6a5f55;
  border-radius: 12px;
}

.title {
  font-size: 18px;
  font-weight: 600;
}

.subtitle {
  display: block;
  color: #7c736b;
  margin-top: 4px;
}

.card {
  background: #ffffff;
  border-radius: 16px;
  padding: 14px 16px;
  margin-bottom: 12px;
  border: 1px solid #efe7dd;
  box-shadow: 0 8px 18px rgba(30, 25, 18, 0.05);
}

.row {
  display: flex;
  justify-content: space-between;
}

.label {
  font-weight: 600;
}

.badge {
  background: #f3ece4;
  color: #6a5f55;
  border-radius: 999px;
  padding: 4px 10px;
  font-size: 12px;
}

.hint {
  color: #8d847c;
  font-size: 12px;
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

.disabled-hint {
  display: block;
  color: #8d847c;
  font-size: 12px;
  margin-top: 8px;
}

.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.3);
  display: grid;
  place-items: center;
}

.modal {
  width: 80%;
  background: #ffffff;
  border-radius: 18px;
  padding: 18px;
}

.modal-title {
  font-weight: 600;
  display: block;
  margin-bottom: 10px;
}

.input {
  width: 100%;
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid #efe7dd;
  margin-bottom: 10px;
  background: #fbf9f6;
}
</style>
