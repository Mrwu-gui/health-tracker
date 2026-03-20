<template>
  <view class="page">
    <view class="quick-entry-bar">
      <view class="quick-entry-item" @tap="goRecord('diet')">
        <view class="quick-entry-icon">
          <image class="quick-entry-icon-img" src="/static/tabbar/food2.png" mode="aspectFit" />
        </view>
        <text class="quick-entry-text">饮食记录</text>
      </view>
      <view class="quick-entry-item" @tap="goRecord('sleep')">
        <view class="quick-entry-icon">
          <image class="quick-entry-icon-img" src="/static/tabbar/sleep2.png" mode="aspectFit" />
        </view>
        <text class="quick-entry-text">昨晚睡眠</text>
      </view>
      <view class="quick-entry-item" @tap="goRecord('weight')">
        <view class="quick-entry-icon">
          <image class="quick-entry-icon-img" src="/static/tabbar/weight.png" mode="aspectFit" />
        </view>
        <text class="quick-entry-text">记录体重</text>
      </view>
    </view>
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
          <input
            class="input"
            type="text"
            placeholder="如：米饭、青菜、番茄炒蛋"
            v-model="form.foodName"
            @blur="onFoodBlur"
          />
          <text class="field-hint">输入后点击其他处可自动估算热量</text>
        </view>
        <view class="field">
          <text class="label">热量(kcal)</text>
          <input
            class="input"
            type="number"
            placeholder="自动估算或手动填写"
            v-model="form.dietCalories"
            :disabled="dietCaloriesEstimating"
          />
          <text v-if="dietCaloriesEstimating" class="field-hint">估算中…</text>
        </view>
      </view>
      <view v-else-if="type === 'sleep'" class="form">
        <view class="field">
          <text class="label">入睡时间</text>
          <picker mode="time" :value="form.sleepStart" @change="onSleepStartChange">
            <view class="picker">{{ form.sleepStart || "选择时间" }}</view>
          </picker>
        </view>
        <view class="field">
          <text class="label">起床时间</text>
          <picker mode="time" :value="form.sleepEnd" @change="onSleepEndChange">
            <view class="picker">{{ form.sleepEnd || "选择时间" }}</view>
          </picker>
        </view>
        <view class="field">
          <text class="label">睡眠质量</text>
          <view class="pill-wrap">
            <view
              v-for="opt in sleepQualityOptions"
              :key="opt.value"
              class="pill"
              :class="{ active: form.sleepQuality === opt.value }"
              @tap="form.sleepQuality = opt.value"
            >{{ opt.label }}</view>
          </view>
        </view>
      </view>
      <view v-else-if="type === 'weight'" class="form">
        <view class="field">
          <text class="label">体重(kg)</text>
          <input class="input" type="digit" placeholder="62.5" v-model="form.weight" />
        </view>
      </view>
      <view v-else-if="type === 'period'" class="form">
        <view class="field">
          <text class="label">经期开始日期</text>
          <picker mode="date" :value="form.periodStart" @change="onPeriodStartChange">
            <view class="picker">{{ form.periodStart || "选择日期" }}</view>
          </picker>
        </view>
        <view class="field">
          <text class="label">经期结束日期</text>
          <picker mode="date" :value="form.periodEnd" @change="onPeriodEndChange">
            <view class="picker">{{ form.periodEnd || "选择日期" }}</view>
          </picker>
        </view>
        <view class="field">
          <text class="label">经量</text>
          <view class="pill-wrap">
            <view
              v-for="opt in periodFlowOptions"
              :key="opt.value"
              class="pill"
              :class="{ active: form.periodFlow === opt.value }"
              @tap="form.periodFlow = opt.value"
            >{{ opt.label }}</view>
          </view>
        </view>
      </view>
    </view>
    <view class="save-btn-view" @tap="saveThenBack">保存并返回</view>
  </view>
</template>

<script>
import { request } from "../../utils/api";
import { requestSubscribeByKey } from "../../utils/subscribe";

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
        sleepQuality: "normal",
        weight: "",
        height: "",
        periodStart: "",
        periodEnd: "",
        periodFlow: "medium"
      },
      exerciseTypes: ["步行", "跑步", "骑行"],
      dietTypes: ["早餐", "午餐", "晚餐", "加餐"],
      sleepQualityOptions: [
        { value: "light", label: "轻度" },
        { value: "normal", label: "正常" },
        { value: "good", label: "好" }
      ],
      dietCaloriesEstimating: false,
      periodFlowOptions: [
        { value: "light", label: "少" },
        { value: "medium", label: "中" },
        { value: "heavy", label: "多" }
      ]
    };
  },
  computed: {
    pageTitle() {
      const t = { exercise: "记录运动", diet: "记录饮食", sleep: "记录睡眠", weight: "记录体重", period: "记录经期" };
      return t[this.type] || "记录";
    }
  },
  onLoad(query) {
    const type = (query && query.type) ? query.type : "exercise";
    this.applyType(type, query);
  },
  methods: {
    goRecord(type) {
      if (this.type === type) return;
      uni.redirectTo({ url: `/pages/record/index?type=${type}&t=${Date.now()}` });
    },
    applyType(nextType, query) {
      const type = nextType || "exercise";
      this.type = type;
      const today = this.todayDate();
      const yesterday = this.yesterdayDate();
      this.form = {
        exerciseType: "步行",
        steps: "",
        duration: "",
        calories: "",
        mealType: (query && query.meal && type === "diet") ? decodeURIComponent(query.meal) : "午餐",
        foodName: "",
        dietCalories: "",
        dietNote: "",
        sleepStart: type === "sleep" ? "23:00" : "",
        sleepEnd: type === "sleep" ? "07:00" : "",
        sleepQuality: "normal",
        weight: "",
        height: "",
        periodStart: today,
        periodEnd: today,
        periodFlow: "medium"
      };
    },
    yesterdayDate() {
      const d = new Date();
      d.setDate(d.getDate() - 1);
      const y = d.getFullYear();
      const m = String(d.getMonth() + 1).padStart(2, "0");
      const day = String(d.getDate()).padStart(2, "0");
      return `${y}-${m}-${day}`;
    },
    onSleepStartChange(e) {
      this.form.sleepStart = e.detail.value || "";
    },
    onSleepEndChange(e) {
      this.form.sleepEnd = e.detail.value || "";
    },
    onFoodBlur() {
      const name = (this.form.foodName || "").trim();
      if (!name || this.dietCaloriesEstimating) return;
      this.estimateDietCalories(name);
    },
    async estimateDietCalories(foodName) {
      if (!foodName || this.dietCaloriesEstimating) return;
      this.dietCaloriesEstimating = true;
      try {
        const userId = uni.getStorageSync("userId") || 1;
        const res = await request("/api/ai/chat", "POST", {
          userId,
          message: `根据以下食物估算热量(kcal)。只回复一个数字，不要单位、不要解释、不要其他文字。食物：${foodName}`,
          store: false
        });
        const content = (res && (res.content != null ? res.content : (res.data && res.data.content != null ? res.data.content : null)));
        const raw = content != null ? String(content).trim() : "";
        const num = this.parseCaloriesFromText(raw);
        if (num >= 0 && num <= 5000) {
          this.form.dietCalories = String(num);
        }
      } catch (e) {
        // 估算失败时保留用户已填或留空
      } finally {
        this.dietCaloriesEstimating = false;
      }
    },
    parseCaloriesFromText(text) {
      if (!text) return NaN;
      const s = text.trim();
      const rangeMatch = s.match(/(\d+)\s*[-~至]\s*(\d+)/);
      if (rangeMatch) {
        const a = parseInt(rangeMatch[1], 10);
        const b = parseInt(rangeMatch[2], 10);
        if (!Number.isNaN(a) && !Number.isNaN(b) && a <= 5000 && b <= 5000) {
          return Math.round((a + b) / 2);
        }
        if (!Number.isNaN(a)) return a;
        if (!Number.isNaN(b)) return b;
      }
      const numMatch = s.match(/\d+/);
      if (numMatch) {
        const n = parseInt(numMatch[0], 10);
        return (!Number.isNaN(n) && n >= 0 && n <= 5000) ? n : NaN;
      }
      return NaN;
    },
    onPeriodStartChange(e) {
      this.form.periodStart = e.detail.value || "";
      if (!this.form.periodEnd || this.form.periodEnd < this.form.periodStart) this.form.periodEnd = this.form.periodStart;
    },
    onPeriodEndChange(e) {
      this.form.periodEnd = e.detail.value || "";
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
          const yesterday = this.yesterdayDate();
          const startTime = this.normalizeDateTime(yesterday, this.form.sleepStart);
          const endTime = this.normalizeDateTime(today, this.form.sleepEnd);
          await request("/api/sleep/add", "POST", {
            userId,
            startTime,
            endTime,
            quality: this.form.sleepQuality || "normal"
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
        } else if (this.type === "period") {
          const payload = {
            userId,
            startDate: this.form.periodStart || today,
            endDate: this.form.periodEnd || this.form.periodStart || today,
            flow: this.form.periodFlow || "medium",
            note: ""
          };
          try {
            await requestSubscribeByKey("period");
            await request("/api/period/add", "POST", payload);
          } catch (e) {
            const key = "periodRecords";
            const raw = uni.getStorageSync(key);
            const arr = raw ? JSON.parse(raw) : [];
            arr.unshift({
              id: "local_" + Date.now(),
              startDate: payload.startDate,
              endDate: payload.endDate,
              flow: payload.flow,
              note: payload.note
            });
            uni.setStorageSync(key, JSON.stringify(arr));
          }
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
.page { padding: 20px; min-height: 100vh; background: #FAF8F5; padding-top: 12px; }
.quick-entry-bar { display: flex; gap: 10px; margin-bottom: 16px; }
.quick-entry-item { flex: 1; display: flex; align-items: center; justify-content: center; gap: 6px; padding: 10px 12px; background: #fff; border-radius: 12px; border: 1px solid #e8e2db; }
.quick-entry-icon { width: 24px; height: 24px; display: flex; align-items: center; justify-content: center; }
.quick-entry-icon-img { width: 20px; height: 20px; }
.quick-entry-text { font-size: 13px; color: #475569; font-weight: 500; }
.card { background: #fff; border-radius: 16px; padding: 16px; margin-bottom: 20px; }
.title { font-size: 16px; font-weight: 600; display: block; margin-bottom: 16px; }
.form { display: flex; flex-direction: column; gap: 14px; }
.field { display: flex; flex-direction: column; gap: 6px; }
.label { font-size: 12px; color: #64748b; }
.field-hint { font-size: 11px; color: #94a3b8; margin-top: 4px; display: block; }
.input { border: 1px solid #e8e2db; border-radius: 10px; padding: 10px 12px; font-size: 14px; }
.picker { border: 1px solid #e8e2db; border-radius: 10px; padding: 10px 12px; font-size: 14px; color: #0f172a; background: #fff; }
.pill-wrap { display: flex; flex-wrap: wrap; gap: 8px; }
.pill { padding: 8px 14px; border-radius: 20px; background: #f5f1eb; color: #64748b; font-size: 13px; }
.pill.active { background: #A23F00; color: #fff; }
.save-btn-view { background: #A23F00; color: #fff; text-align: center; padding: 14px; border-radius: 14px; font-size: 15px; font-weight: 600; }
</style>
