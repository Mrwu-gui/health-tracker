<template>
  <view class="page">
    <view class="card">
      <text class="title">{{ pageTitle }}</text>
      <view v-if="type === 'exercise'" class="form">
        <view class="field">
          <text class="label">类型</text>
          <view class="pill-wrap">
            <view
              v-for="item in exerciseTypes"
              :key="item"
              class="pill"
              :class="{ active: form.exerciseType === item }"
              @tap="form.exerciseType = item"
            >{{ item }}</view>
          </view>
        </view>
        <view class="field">
          <text class="label">步数</text>
          <input class="input" type="number" placeholder="0" v-model="form.steps" />
        </view>
        <view class="field">
          <text class="label">时长(分钟)</text>
          <input class="input" type="number" placeholder="0" v-model="form.duration" />
        </view>
        <view class="field">
          <text class="label">消耗(kcal)</text>
          <input class="input" type="number" placeholder="0" v-model="form.calories" />
        </view>
      </view>
      <view v-else-if="type === 'diet'" class="form">
        <view class="field">
          <text class="label">餐次</text>
          <view class="pill-wrap">
            <view
              v-for="item in dietTypes"
              :key="item"
              class="pill"
              :class="{ active: form.mealType === item }"
              @tap="form.mealType = item"
            >{{ item }}</view>
          </view>
        </view>
        <view class="field">
          <text class="label">食物</text>
          <input class="input" type="text" placeholder="如：米饭、青菜" v-model="form.foodName" />
        </view>
        <view class="field">
          <text class="label">热量(kcal)</text>
          <input class="input" type="number" placeholder="650" v-model="form.dietCalories" />
        </view>
      </view>
      <view v-else-if="type === 'sleep'" class="form">
        <view class="field">
          <text class="label">入睡时间</text>
          <input class="input" type="text" placeholder="22:30" v-model="form.sleepStart" />
        </view>
        <view class="field">
          <text class="label">起床时间</text>
          <input class="input" type="text" placeholder="06:00" v-model="form.sleepEnd" />
        </view>
        <view class="field">
          <text class="label">睡眠时长(小时)</text>
          <input class="input" type="text" placeholder="7.5" v-model="form.sleepDuration" />
        </view>
      </view>
      <view v-else-if="type === 'weight'" class="form">
        <view class="field">
          <text class="label">体重(kg)</text>
          <input class="input" type="digit" placeholder="62.5" v-model="form.weight" />
        </view>
        <view class="field">
          <text class="label">身高(cm)</text>
          <input class="input" type="number" placeholder="170" v-model="form.height" />
        </view>
      </view>
    </view>
    <view class="save-btn-view" @tap="saveThenBack">保存并返回</view>
  </view>
</template>

<script>
import { request } from "../../utils/api";

export default {
  data() {
    return {
      type: "exercise",
      form: {
        exerciseType: "步行",
        steps: "",
        duration: "",
        calories: "",
        mealType: "午餐",
        foodName: "",
        dietCalories: "",
        dietNote: "",
        sleepStart: "",
        sleepEnd: "",
        sleepDuration: "",
        weight: "",
        height: ""
      },
      exerciseTypes: ["步行", "跑步", "骑行"],
      dietTypes: ["早餐", "午餐", "晚餐", "加餐"]
    };
  },
  computed: {
    pageTitle() {
      const t = { exercise: "记录运动", diet: "记录饮食", sleep: "记录睡眠", weight: "记录体重" };
      return t[this.type] || "记录";
    }
  },
  onLoad(query) {
    this.applyType(query && query.type ? query.type : "exercise");
  },
  methods: {
    applyType(nextType) {
      const type = nextType || "exercise";
      this.type = type;
      this.form = {
        exerciseType: "步行",
        steps: "",
        duration: "",
        calories: "",
        mealType: "午餐",
        foodName: "",
        dietCalories: "",
        dietNote: "",
        sleepStart: "",
        sleepEnd: "",
        sleepDuration: "",
        weight: "",
        height: ""
      };
    },
    normalizeDateTime(date, time) {
      if (!time) return "";
      if (time.includes("T")) return time;
      if (time.includes("-")) return time.replace(" ", "T");
      const normalizedTime = time.length <= 5 ? `${time}:00` : time;
      return `${date}T${normalizedTime}`;
    },
    todayDate() {
      const now = new Date();
      const y = now.getFullYear();
      const m = String(now.getMonth() + 1).padStart(2, "0");
      const d = String(now.getDate()).padStart(2, "0");
      return `${y}-${m}-${d}`;
    },
    toInt(value) {
      const num = parseInt(value, 10);
      return Number.isNaN(num) ? 0 : num;
    },
    toFloat(value) {
      const num = parseFloat(value);
      return Number.isNaN(num) ? 0 : num;
    },
    async saveThenBack() {
      const userId = uni.getStorageSync("userId") || 1;
      const today = this.todayDate();
      try {
        if (this.type === "exercise") {
          await request("/api/exercise/add", "POST", {
            userId,
            type: this.form.exerciseType,
            steps: this.toInt(this.form.steps),
            duration: this.toInt(this.form.duration),
            calories: this.toInt(this.form.calories),
            date: today
          });
        } else if (this.type === "diet") {
          await request("/api/diet/add", "POST", {
            userId,
            mealType: this.form.mealType,
            foodName: this.form.foodName || "未填写",
            calories: this.toInt(this.form.dietCalories),
            note: this.form.dietNote || "",
            date: today
          });
        } else if (this.type === "sleep") {
          const startTime = this.normalizeDateTime(today, this.form.sleepStart);
          const endTime = this.normalizeDateTime(today, this.form.sleepEnd);
          await request("/api/sleep/add", "POST", {
            userId,
            startTime,
            endTime,
            quality: this.form.sleepDuration ? `${this.form.sleepDuration}小时` : ""
          });
        } else if (this.type === "weight") {
          const weight = this.toFloat(this.form.weight);
          const height = this.toFloat(this.form.height);
          const bmi = height > 0 ? Number((weight / ((height / 100) ** 2)).toFixed(1)) : null;
          await request("/api/weight/add", "POST", {
            userId,
            weight,
            bmi,
            date: today
          });
        }
        uni.showToast({ title: "已保存", icon: "success" });
        setTimeout(() => uni.navigateBack(), 500);
      } catch (err) {
        uni.showToast({ title: "保存失败", icon: "error" });
      }
    }
  }
};
</script>

<style scoped>
.page { padding: 20px; min-height: 100vh; background: #faf8f5; }
.card { background: #fff; border-radius: 16px; padding: 16px; margin-bottom: 20px; }
.title { font-size: 16px; font-weight: 600; display: block; margin-bottom: 16px; }
.form { display: flex; flex-direction: column; gap: 14px; }
.field { display: flex; flex-direction: column; gap: 6px; }
.label { font-size: 12px; color: #64748b; }
.input { border: 1px solid #e8e2db; border-radius: 10px; padding: 10px 12px; font-size: 14px; }
.pill-wrap { display: flex; flex-wrap: wrap; gap: 8px; }
.pill { padding: 8px 14px; border-radius: 20px; background: #f5f1eb; color: #64748b; font-size: 13px; }
.pill.active { background: #2563eb; color: #fff; }
.save-btn-view { background: #2563eb; color: #fff; text-align: center; padding: 14px; border-radius: 14px; font-size: 15px; font-weight: 600; }
</style>
