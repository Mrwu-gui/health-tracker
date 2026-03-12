<template>
  <view class="page">
    <view class="header">
      <text class="title">提醒</text>
      <text class="subtitle">保持规律作息</text>
    </view>
    <view class="card" v-for="item in reminders" :key="item.id">
      <view class="row">
        <text class="label">{{ item.title }}</text>
        <text class="badge">{{ item.type }}</text>
      </view>
      <text class="hint">{{ item.time }}</text>
    </view>
    <text class="disabled-hint">提醒功能展示中，点击与推送将在下一阶段开放。</text>
    <text v-if="loading" class="status">加载中...</text>
    <text v-if="error" class="status error">{{ error }}</text>
  </view>
</template>

<script>
import { request } from "../../utils/api";

export default {
  data() {
    return {
      loading: false,
      error: "",
      reminders: []
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
      request("/api/medication/list", "GET", { userId })
        .then((data) => {
          if (Array.isArray(data)) {
            this.reminders = data.map((item) => ({
              id: item.id,
              title: item.drugName,
              type: "用药",
              time: item.frequency
            }));
          }
        })
        .catch(() => {
          this.error = "获取提醒失败";
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
  padding: 22px;
  min-height: 100vh;
  background: #f7f4ef;
  color: #2d2a26;
}

.header {
  margin-bottom: 14px;
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
  opacity: 0.75;
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
</style>
