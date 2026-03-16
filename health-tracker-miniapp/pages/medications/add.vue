<template>
  <view class="page">
    <view class="card">
      <view class="field">
        <text class="label">药物名称</text>
        <input class="input" v-model="form.drugName" placeholder="如 氨氯地平" />
      </view>
      <view class="field">
        <text class="label">剂量</text>
        <input class="input" v-model="form.dosage" placeholder="如 5mg" />
      </view>
      <view class="field">
        <text class="label">用法</text>
        <input class="input" v-model="form.frequency" placeholder="如 每日1次" />
      </view>
      <view class="field">
        <text class="label">提醒时间</text>
        <input class="input" v-model="form.remindTime" placeholder="如 2026-03-16 08:00" />
      </view>
    </view>
    <view class="save-btn" @tap="save">保存</view>
  </view>
</template>

<script>
import { request } from "../../utils/api";

export default {
  data() {
    return { form: { drugName: "", dosage: "", frequency: "", remindTime: "" } };
  },
  methods: {
    todayDate() {
      const now = new Date();
      const y = now.getFullYear();
      const m = String(now.getMonth() + 1).padStart(2, "0");
      const d = String(now.getDate()).padStart(2, "0");
      return `${y}-${m}-${d}`;
    },
    save() {
      if (!this.form.drugName) {
        uni.showToast({ title: "请填写药物名称", icon: "none" });
        return;
      }
      if (this.form.remindTime && !/^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}$/.test(this.form.remindTime)) {
        uni.showToast({ title: "提醒时间格式应为 yyyy-MM-dd HH:mm", icon: "none" });
        return;
      }
      const userId = uni.getStorageSync("userId") || 1;
      request("/api/medication/add", "POST", {
        userId,
        drugName: this.form.drugName,
        dosage: this.form.dosage,
        frequency: this.form.frequency,
        remindTime: this.form.remindTime || null,
        stock: 30,
        stockThreshold: 5,
        startDate: this.todayDate()
      })
        .then(() => {
          uni.showToast({ title: "已添加", icon: "success" });
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
.page { padding: 20px; min-height: 100vh; background: #faf8f5; }
.card { background: #fff; border-radius: 16px; padding: 16px; margin-bottom: 20px; }
.field { margin-bottom: 14px; }
.label { font-size: 12px; color: #64748b; display: block; margin-bottom: 6px; }
.input { border: 1px solid #e8e2db; border-radius: 10px; padding: 10px 12px; font-size: 14px; width: 100%; box-sizing: border-box; }
.save-btn { background: #2563eb; color: #fff; text-align: center; padding: 14px; border-radius: 14px; font-size: 15px; font-weight: 600; }
</style>
