<template>
  <view class="page">
    <view class="card">
      <view class="field">
        <text class="label">目标类型</text>
        <view class="pill-wrap">
          <view
            v-for="item in goalTypes"
            :key="item.value"
            class="pill"
            :class="{ active: form.goalType === item.value }"
            @tap="form.goalType = item.value"
          >{{ item.label }}</view>
        </view>
      </view>
      <view class="field">
        <text class="label">周期</text>
        <view class="pill-wrap">
          <view class="pill" :class="{ active: form.period === 'day' }" @tap="form.period = 'day'">每日</view>
          <view class="pill" :class="{ active: form.period === 'week' }" @tap="form.period = 'week'">每周</view>
        </view>
      </view>
      <view class="field">
        <text class="label">目标值</text>
        <input class="input" type="number" v-model="form.targetValue" :placeholder="form.goalType === '步数' ? '如 10000' : '请输入'" />
      </view>
    </view>
    <view class="save-btn" @tap="save">保存目标</view>
  </view>
</template>

<script>
import { request } from "../../utils/api";

export default {
  data() {
    return {
      form: { goalType: "步数", period: "day", targetValue: "10000" },
      goalTypes: [
        { value: "步数", label: "步数" },
        { value: "睡眠", label: "睡眠" },
        { value: "饮食热量", label: "饮食热量" },
        { value: "用药", label: "用药" }
      ]
    };
  },
  methods: {
    save() {
      const userId = uni.getStorageSync("userId") || 1;
      const targetValue = Number(this.form.targetValue || 0);
      if (!targetValue) {
        uni.showToast({ title: "请输入目标值", icon: "none" });
        return;
      }
      request("/api/goal/add", "POST", {
        userId,
        goalType: this.form.goalType,
        targetValue,
        period: this.form.period
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
.page { padding: 20px; min-height: 100vh; background: #faf8f5; }
.card { background: #fff; border-radius: 16px; padding: 16px; margin-bottom: 20px; }
.field { margin-bottom: 14px; }
.label { font-size: 12px; color: #64748b; display: block; margin-bottom: 8px; }
.pill-wrap { display: flex; flex-wrap: wrap; gap: 8px; }
.pill { padding: 8px 14px; border-radius: 20px; background: #f5f1eb; color: #64748b; font-size: 13px; }
.pill.active { background: #2563eb; color: #fff; }
.input { border: 1px solid #e8e2db; border-radius: 10px; padding: 10px 12px; font-size: 14px; width: 100%; box-sizing: border-box; margin-top: 6px; }
.save-btn { background: #2563eb; color: #fff; text-align: center; padding: 14px; border-radius: 14px; font-size: 15px; font-weight: 600; }
</style>
