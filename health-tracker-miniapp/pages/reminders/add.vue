<template>
  <view class="page">
    <view class="card">
      <view class="field">
        <text class="label">提醒标题</text>
        <picker :range="titleOptions" range-key="label" @change="onTitleChange">
          <view class="picker">{{ titleLabel }}</view>
        </picker>
      </view>
      <view class="field">
        <text class="label">提醒内容</text>
        <input class="input" v-model="form.content" placeholder="提醒内容" />
      </view>
      <view class="field">
        <text class="label">提醒时间</text>
        <picker mode="time" @change="onTimeChange">
          <view class="picker">{{ form.remindTime || "请选择时间" }}</view>
        </picker>
      </view>
    </view>
    <view class="save-btn" @tap="save">保存提醒</view>
  </view>
</template>

<script>
import { request } from "../../utils/api";

export default {
  data() {
    return {
      titleOptions: [
        { label: "运动提醒", type: 1 },
        { label: "饮食提醒", type: 2 },
        { label: "睡眠提醒", type: 3 },
        { label: "用药提醒", type: 4 }
      ],
      form: { title: "", type: 1, content: "", remindTime: "" }
    };
  },
  computed: {
    titleLabel() {
      const match = this.titleOptions.find((item) => item.type === this.form.type);
      return match ? match.label : "请选择标题";
    }
  },
  methods: {
    todayDate() {
      const now = new Date();
      const y = now.getFullYear();
      const m = String(now.getMonth() + 1).padStart(2, "0");
      const d = String(now.getDate()).padStart(2, "0");
      return `${y}-${m}-${d}`;
    },
    onTitleChange(e) {
      const idx = Number(e.detail.value || 0);
      const option = this.titleOptions[idx];
      if (option) {
        this.form.type = option.type;
        this.form.title = option.label;
      }
    },
    onTimeChange(e) {
      this.form.remindTime = e.detail.value;
    },
    save() {
      if (!this.form.remindTime) {
        uni.showToast({ title: "请填写标题和时间", icon: "none" });
        return;
      }
      const userId = uni.getStorageSync("userId") || 1;
      const remindTime = `${this.todayDate()} ${this.form.remindTime}`;
      request("/api/reminder/add", "POST", {
        userId,
        title: this.form.title || this.titleLabel,
        type: this.form.type,
        content: this.form.content,
        remindTime
      })
        .then(() => {
          uni.showToast({ title: "已保存", icon: "success" });
          setTimeout(() => uni.navigateBack(), 500);
        })
        .catch((err) => {
          uni.showToast({ title: err.message || "保存失败", icon: "none" });
        });
    }
  }
};
</script>

<style scoped>
.page { padding: 20px; min-height: 100vh; background: #f4f5f7; }
.card { background: #fff; border-radius: 16px; padding: 16px; margin-bottom: 20px; }
.field { margin-bottom: 14px; }
.label { font-size: 12px; color: #64748b; display: block; margin-bottom: 6px; }
.input { border: 1px solid #e2e8f0; border-radius: 10px; padding: 10px 12px; font-size: 14px; width: 100%; box-sizing: border-box; }
.picker { border: 1px solid #e2e8f0; border-radius: 10px; padding: 10px 12px; font-size: 14px; color: #0f172a; background: #fff; }
.save-btn { background: #2563eb; color: #fff; text-align: center; padding: 14px; border-radius: 14px; font-size: 15px; font-weight: 600; }
</style>
