<template>
  <view class="page">
    <view class="card">
      <view class="field">
        <text class="label">姓名</text>
        <input class="input" v-model="form.name" placeholder="请输入姓名" />
      </view>
      <view class="field">
        <text class="label">年龄</text>
        <input class="input" type="number" v-model="form.age" placeholder="请输入年龄" />
      </view>
      <view class="field">
        <text class="label">关系</text>
        <input class="input" v-model="form.relation" placeholder="如 家人/父母/孩子" />
      </view>
      <view class="field">
        <text class="label">健康状况/备注</text>
        <input class="input" v-model="form.conditionText" placeholder="如 高血压、健康" />
      </view>
    </view>
    <view class="save-btn" @tap="save">保存</view>
  </view>
</template>

<script>
import { request } from "../../utils/api";

export default {
  data() {
    return { form: { name: "", age: "", relation: "", conditionText: "" } };
  },
  methods: {
    save() {
      if (!this.form.name || !this.form.age) {
        uni.showToast({ title: "请填写姓名和年龄", icon: "none" });
        return;
      }
      const userId = uni.getStorageSync("userId") || 1;
      request("/api/family/add", "POST", {
        userId,
        name: this.form.name,
        age: Number(this.form.age),
        relation: this.form.relation,
        conditionText: this.form.conditionText,
        role: "成员",
        status: 1
      })
        .then(() => {
          uni.showToast({ title: "已添加", icon: "success" });
          setTimeout(() => uni.navigateBack(), 500);
        })
        .catch((err) => {
          uni.showToast({ title: err.message || "添加失败", icon: "none" });
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
.save-btn { background: #f97316; color: #fff; text-align: center; padding: 14px; border-radius: 14px; font-size: 15px; font-weight: 600; }
</style>
