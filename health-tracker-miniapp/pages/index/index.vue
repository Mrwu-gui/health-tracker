<template>
  <view class="page-root">
    <view class="page">
    <view class="header">
      <text class="header-title">健康管家</text>
      <navigator class="icon-btn" url="/pages/profile/index" open-type="switchTab">⚙</navigator>
    </view>

    <view class="care-card">
      <view class="care-icon">
        <text class="fa-solid fa-sun">☀</text>
      </view>
      <view class="care-body">
        <text class="care-title">{{ greetingTitle }}</text>
        <text class="care-sub">
          {{ aiGreeting || "今天也坚持记录一下吧，每一小步，都是向健康靠近的一大步。" }}
        </text>
      </view>
    </view>

    <view class="overview-card">
      <view class="overview-main">
        <view class="overview-left">
          <text class="label">今日概览</text>
          <text class="steps">
            {{ overview.steps }}
            <text class="unit">步</text>
          </text>
          <text class="meta">
            运动 {{ overview.exerciseMinutes }} 分钟 · 卡路里约
            <text class="strong">{{ overview.calories }} kcal</text>
          </text>
        </view>
        <view class="progress">
          <text class="progress-label">完成</text>
          <text class="progress-value">{{ overview.progress }}%</text>
        </view>
      </view>
      <view class="overview-grid">
        <view class="overview-item">
          <text class="label">睡眠</text>
          <text class="value">{{ overview.sleep }}</text>
        </view>
        <view class="overview-item">
          <text class="label">体重 / BMI</text>
          <text class="value">{{ overview.weightBmi }}</text>
        </view>
        <view class="overview-item">
          <text class="label">饮食记录</text>
          <text class="value">{{ overview.dietCount }}</text>
        </view>
        <view class="overview-item">
          <text class="label">血压 / 心率</text>
          <text class="value">{{ overview.bpStatus }}</text>
        </view>
      </view>
      <!-- #ifdef MP-WEIXIN -->
      <button
        v-if="needsSteps"
        class="sync-btn"
        @tap="syncSteps"
        :disabled="syncLoading"
      >
        {{ syncLoading ? "同步中..." : "获取步数" }}
      </button>
      <button v-if="needsSteps" class="manual-btn" @tap="manualSteps">
        手动填写步数（本地测试）
      </button>
      <!-- #endif -->
    </view>

    <view class="card">
      <view class="card-head">
        <text class="card-title">今日提醒</text>
        <navigator class="link" url="/pages/reminders/index">查看全部</navigator>
      </view>
      <view class="reminder-list overview-list">
      <view v-for="item in reminders" :key="item.id" class="reminder-item">
          <view>
            <text class="reminder-title">{{ item.title }}</text>
            <text class="reminder-meta">{{ item.time }} · {{ item.content }}</text>
          </view>
          <text class="reminder-tag">{{ item.typeLabel || "提醒" }}</text>
        </view>
        <text v-if="reminders.length === 0" class="empty">今天还没有提醒，记得在提醒设置里添加哦。</text>
      </view>
    </view>

    <view class="card">
      <view class="card-head">
        <text class="card-title">今日目标</text>
        <navigator class="link" url="/pages/goal/index">查看目标</navigator>
      </view>
      <view class="goal-list overview-list">
        <view v-for="item in todayGoals" :key="item.id" class="goal-item">
          <view class="goalinfo">
            <text class="goal-name">{{ item.goalLabel }}目标</text>
            <text class="goal-desc">已完成 {{ item.currentValue }} / {{ item.targetValue }}{{ item.unit }}</text>
          </view>
          <text class="goal-progress">{{ item.progress }}%</text>
        </view>
        <text v-if="todayGoals.length === 0" class="empty">还没有设置目标，去目标管理里设置一下吧。</text>
      </view>
    </view>

    <text v-if="loading" class="status">加载中...</text>
    <text v-if="error" class="status error">{{ error }}</text>
    </view>
  </view>
</template>

<script>
import { request } from "../../utils/api";
export default {
  data() {
    return {
      overview: {
        steps: "0",
        exerciseMinutes: "0",
        calories: "0",
        progress: "0",
        sleep: "0小时0分",
        weightBmi: "暂无",
        dietCount: "已记录 0 餐",
        bpStatus: "暂无"
      },
      loading: false,
      error: "",
      syncLoading: false,
      userName: "",
      greetingTitle: "你好",
      reminders: [],
      todayGoals: [],
      aiGreeting: ""
    };
  },
  onLoad() {
    const cachedSteps = uni.getStorageSync("steps");
    if (cachedSteps) {
      this.overview.steps = String(cachedSteps);
    }
    const storedName = uni.getStorageSync("userName");
    const wxProfile = uni.getStorageSync("wxProfile") || {};
    this.userName = storedName || wxProfile.nickName || "朋友";
    this.greetingTitle = `${this.timePeriod(new Date().getHours())}好，${this.userName || "朋友"}`;
    this.fetchAiGreeting();
    // #ifdef MP-WEIXIN
    this.maybeAutoSyncSteps();
    // #endif
  },
  onShow() {
    this.fetchOverview();
  },
  computed: {
    needsSteps() {
      const raw = String(this.overview.steps || "0");
      const num = parseInt(raw.replace(/[^\d]/g, ""), 10);
      const today = new Date();
      const key = `${today.getFullYear()}-${today.getMonth() + 1}-${today.getDate()}`;
      const lastSync = uni.getStorageSync("stepsSyncDate");
      const notSyncedToday = lastSync !== key;
      return !num || Number.isNaN(num) || notSyncedToday;
    }
  },
  methods: {
    async fetchOverview() {
	  const that = this
      this.loading = true;
      this.error = "";
      const userId = uni.getStorageSync("userId") || 1;
      try {
        const data = await request("/api/statistics/overview", "GET", { userId, period: "day" });
        if (data) {
          this.overview.steps = data.steps || this.overview.steps;
          this.overview.sleep = data.sleep || this.overview.sleep;
          this.overview.calories = data.calories || this.overview.calories;
          this.overview.exerciseMinutes = data.exerciseMinutes || "0";
          const weight = data.weight ? `${data.weight} kg` : "";
          const bmi = data.bmi ? ` · ${data.bmi}` : "";
          this.overview.weightBmi = weight ? `${weight}${bmi}` : "";
          this.overview.dietCount = `已记录 ${data.dietCount || 0} 餐`;
          this.overview.bpStatus = data.bpStatus || "";
          const steps = parseInt(this.overview.steps, 10);
          const progress = Math.min(100, Math.round((steps / 10000) * 100));
          this.overview.progress = String(isNaN(progress) ? 0 : progress);
        }
      } catch (err) {
        this.error = "获取概览失败";
      }

      try {
        const list = await request("/api/reminder/list", "GET", { userId });
        const raw = Array.isArray(list)
          ? list
          : Array.isArray(list?.records)
            ? list.records
            : Array.isArray(list?.list)
              ? list.list
              : [];
        if (raw.length > 0) {
          const mapped = raw
            .filter((item) => Number(item.type) !== 4)
            .slice(0, 3)
            .map((item) => ({
            id: item.id,
            title: item.title,
            content: item.content || "提醒事项",
            time: item.remindTime || "未设置",
            typeLabel: this.reminderTagLabel(item.type)
          }));
		this.reminders = mapped
        } else {
          this.reminders = []
        }
      } catch (err) {
        this.reminders = []
      }

      try {
        const list = await request("/api/goal/list", "GET", { userId, period: "day" });
        const raw = Array.isArray(list)
          ? list
          : Array.isArray(list?.records)
            ? list.records
            : Array.isArray(list?.list)
              ? list.list
              : [];
        if (raw.length > 0) {
          const mapped = raw.slice(0, 3).map((item) => {
            const target = Number(item.targetValue || 0);
            const current = Number(item.currentValue || 0);
            const progress = target > 0 ? Math.min(100, Math.round((current / target) * 100)) : 0;
            return {
              id: item.id,
              goalType: item.goalType,
              goalLabel: this.goalLabel(item.goalType),
              targetValue: target,
              currentValue: current,
              progress,
              unit: this.goalUnitSuffix(item.goalType)
            };
          });
		  this.todayGoals = mapped
        } else {
          this.todayGoals = []
        }
      } catch (err) {
        this.todayGoals = []
      } finally {
        this.loading = false;
      }
    },
    async fetchAiGreeting() {
      const today = new Date();
      const hour = today.getHours();
      const period = this.timePeriod(hour);
      this.greetingTitle = `${period}好，${this.userName || "朋友"}`;
      const slot = Math.floor(hour / 3);
      const key = `ai_greeting_${period}_${slot}_${today.getFullYear()}-${today.getMonth() + 1}-${today.getDate()}`;
      const cached = uni.getStorageSync(key);
      if (cached) {
        this.aiGreeting = cached;
        return;
      }
      try {
        const prompt = `请根据当前时间段生成一句简短温暖的问候语，称呼用户${this.userName || "朋友"}，不超过20字。时间段：${period}。`;
        const data = await request("/api/ai/chat", "POST", { message: prompt, store: false });
        if (data && data.content) {
          this.aiGreeting = data.content;
          uni.setStorageSync(key, data.content);
        }
      } catch (err) {
        this.aiGreeting = "";
      }
    },
    timePeriod(hour) {
      if (hour < 5) return "深夜";
      if (hour < 9) return "早上";
      if (hour < 12) return "上午";
      if (hour < 14) return "中午";
      if (hour < 18) return "下午";
      if (hour < 21) return "晚上";
      return "夜间";
    },
    applyDefaultOverview() {
      this.overview.steps = "0";
      this.overview.exerciseMinutes = "0";
      this.overview.calories = "0";
      this.overview.sleep = "0小时0分";
      this.overview.weightBmi = "暂无";
      this.overview.dietCount = "已记录 0 餐";
      this.overview.bpStatus = "暂无";
    },
    maybeAutoSyncSteps() {
      if (this.syncLoading || !this.needsSteps) return;
      const today = new Date();
      const key = `${today.getFullYear()}-${today.getMonth() + 1}-${today.getDate()}`;
      const lastSync = uni.getStorageSync("stepsSyncDate");
      if (lastSync === key) return;
      this.syncSteps();
    },
    syncSteps() {
      this.syncLoading = true;
      // #ifdef MP-WEIXIN
      const token = uni.getStorageSync("token");
      const ensureLogin = token
        ? Promise.resolve()
        : new Promise((resolve, reject) => {
            uni.login({
              provider: "weixin",
              success: (loginRes) => {
                request("/api/auth/mini/login", "POST", { code: loginRes.code })
                  .then((data) => {
                    if (data?.token) {
                      uni.setStorageSync("token", data.token);
                      if (data.userId) {
                        uni.setStorageSync("userId", data.userId);
                      }
                    }
                    resolve();
                  })
                  .catch(reject);
              },
              fail: reject
            });
          });
      ensureLogin
        .then(
          () =>
            new Promise((resolve, reject) => {
              uni.login({
                provider: "weixin",
                success: (res) => resolve(res.code),
                fail: reject
              });
            })
        )
        .then((code) => {
          uni.getWeRunData({
            success: (werun) => {
              request("/api/auth/mini/werun", "POST", {
                code,
                encryptedData: werun.encryptedData,
                iv: werun.iv
              })
                .then((data) => {
                  if (data?.steps) {
                    const steps = String(data.steps);
                    this.overview.steps = steps;
                    uni.setStorageSync("steps", steps);
                    const today = new Date();
                    const key = `${today.getFullYear()}-${today.getMonth() + 1}-${today.getDate()}`;
                    uni.setStorageSync("stepsSyncDate", key);
                    uni.showToast({ title: "已同步步数", icon: "success" });
                    this.fetchOverview();
                  } else {
                    uni.showToast({ title: "未获取到步数", icon: "none" });
                  }
                })
                .catch((err) => {
                  uni.showToast({ title: err.message || "同步失败", icon: "none" });
                })
                .finally(() => {
                  this.syncLoading = false;
                });
            },
            fail: () => {
              uni.showToast({ title: "未授权微信运动", icon: "none" });
              this.syncLoading = false;
            }
          });
        })
        .catch(() => {
          uni.showToast({ title: "微信登录失败", icon: "none" });
          this.syncLoading = false;
        });
      // #endif
      // #ifndef MP-WEIXIN
      this.syncLoading = false;
      uni.showToast({ title: "请在微信小程序内操作", icon: "none" });
      // #endif
    },
    goalUnitSuffix(goalType) {
      switch (Number(goalType)) {
        case 1:
          return "步";
        case 2:
          return "小时";
        case 3:
          return "kcal";
        default:
          return "";
      }
    },
    reminderTagLabel(type) {
      switch (Number(type)) {
        case 1:
          return "运动";
        case 2:
          return "饮食";
        case 3:
          return "睡眠";
        default:
          return "提醒";
      }
    },
    goalLabel(goalType) {
      switch (Number(goalType)) {
        case 1:
          return "步数";
        case 2:
          return "睡眠";
        case 3:
          return "饮食热量";
        default:
          return "目标";
      }
    },
    goStats() {
      uni.switchTab({ url: "/pages/data/index" });
    },
    goProfile() {
      uni.switchTab({ url: "/pages/profile/index" });
    },
    openRecord(type) {
      uni.navigateTo({ url: `/pages/record/index?type=${type}&t=${Date.now()}` });
    },
    manualSteps() {
      this.openRecord("exercise");
    }
  }
};
</script>

<style>
.page {
  padding: 18px;
  padding-bottom: calc(60px + env(safe-area-inset-bottom));
  min-height: 100vh;
  background: #f4f5f7;
  color: #0f172a;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.page-root {
  min-height: 100vh;
  background: #f4f5f7;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #0f172a;
}

.header .title {
  font-size: 18px;
  font-weight: 600;
  color: #0f172a;
}

.icon-btn {
  width: 40px;
  height: 40px;
  border-radius: 20px;
  background: #fff;
  color: #64748b;
  border: 1px solid #e2e8f0;
  font-size: 16px;
  padding: 0;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.care-card {
  background: #0f172a;
  color: #ffffff;
  border-radius: 18px;
  padding: 12px;
  display: flex;
  gap: 12px;
  align-items: center;
}

.care-icon {
  width: 32px;
  height: 32px;
  border-radius: 16px;
  background: #1e293b;
  display: grid;
  place-items: center;
  color: #fbbf24;
  font-size: 14px;
}

.care-title {
  font-size: 13px;
  font-weight: 600;
  display: block;
}

.care-sub {
  font-size: 11px;
  color: #cbd5f5;
  margin-top: 4px;
  display: block;
}

.overview-card {
  background: #ffffff;
  border-radius: 18px;
  padding: 14px;
  border: 1px solid #f1f5f9;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.overview-main {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.overview-left {
  flex: 1;
}

.label {
  font-size: 11px;
  color: #94a3b8;
}

.steps {
  font-size: 28px;
  font-weight: 600;
}

.unit {
  font-size: 12px;
  color: #64748b;
  margin-left: 4px;
}

.meta {
  font-size: 11px;
  color: #64748b;
  margin-top: 6px;
}

.strong {
  font-weight: 600;
  color: #0f172a;
}

.progress {
  width: 64px;
  height: 64px;
  border-radius: 32px;
  background: #e2e8f0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #0f172a;
}

.progress-label {
  font-size: 10px;
  color: #94a3b8;
}

.progress-value {
  font-size: 14px;
  font-weight: 600;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  border-top: 1px dashed #e2e8f0;
  padding-top: 10px;
}

.overview-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.value {
  font-size: 13px;
  font-weight: 600;
}

.sync-btn {
  margin-top: 6px;
  align-self: flex-start;
  background: #2563eb;
  color: #ffffff;
  border-radius: 999px;
  padding: 6px 14px;
  font-size: 11px;
}

.manual-btn {
  margin-top: 8px;
  align-self: flex-start;
  background: #f1f5f9;
  color: #475569;
  border-radius: 999px;
  padding: 6px 14px;
  font-size: 11px;
}

.card {
  background: #ffffff;
  border-radius: 18px;
  padding: 14px;
  border: 1px solid #f1f5f9;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 12px;
  font-weight: 600;
  color: #1e293b;
}

.card-title.with-icon {
  display: flex;
  align-items: center;
  gap: 6px;
}


.card-sub {
  font-size: 11px;
  color: #94a3b8;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.reminder-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.reminder-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f8fafc;
  padding: 10px 12px;
  border-radius: 14px;
}

.reminder-title {
  font-size: 12px;
  font-weight: 600;
  color: #0f172a;
  display: block;
}

.reminder-meta {
  font-size: 10px;
  color: #94a3b8;
  margin-top: 4px;
  display: block;
}

.reminder-tag {
  font-size: 10px;
  color: #2563eb;
  background: #e0f2fe;
  padding: 4px 8px;
  border-radius: 999px;
}

.goal-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.goal-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f8fafc;
  padding: 10px 12px;
  border-radius: 14px;
}

.goal-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.goal-name {
  font-size: 12px;
  font-weight: 600;
  color: #0f172a;
}

.goal-desc {
  font-size: 10px;
  color: #94a3b8;
}

.goal-progress {
  font-size: 12px;
  font-weight: 600;
  color: #2563eb;
}

.quick-item {
  background: #f8fafc;
  border-radius: 14px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  width: 100%;
  box-sizing: border-box;
}

.quick-item-hover {
  opacity: 0.85;
  background: #e2e8f0;
}

.quick-icon {
  width: 36px;
  height: 36px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.quick-icon.blue {
  background: #dbeafe;
  color: #2563eb;
}

.quick-icon.green {
  background: #d1fae5;
  color: #059669;
}

.quick-icon.indigo {
  background: #e0e7ff;
  color: #4f46e5;
}

.quick-icon.amber {
  background: #fef3c7;
  color: #d97706;
}

.quick-title {
  font-size: 12px;
  font-weight: 600;
}

.quick-sub {
  font-size: 10px;
  color: #94a3b8;
}

.link {
  font-size: 11px;
  color: #94a3b8;
}


.status {
  text-align: center;
  color: #64748b;
  font-size: 12px;
}

.status.error {
  color: #ef4444;
}

.debug-flag {
  color: #94a3b8;
}

.modal-mask {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 9999;
}

.modal-card {
  width: 100%;
  max-width: 360px;
  background: #ffffff;
  border-top-left-radius: 24px;
  border-top-right-radius: 24px;
  padding-bottom: 16px;
}

.modal-head {
  padding: 16px;
  border-bottom: 1px solid #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.modal-title {
  font-size: 16px;
  font-weight: 600;
}

.modal-close {
  font-size: 16px;
  color: #94a3b8;
  background: transparent;
}

.modal-body {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.modal-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.field-label {
  font-size: 11px;
  color: #94a3b8;
  display: block;
  margin-bottom: 4px;
}

.field-value {
  font-size: 13px;
  color: #0f172a;
  margin-bottom: 6px;
  display: block;
}

.pill-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.pill {
  padding: 6px 12px;
  border-radius: 12px;
  background: #f1f5f9;
  color: #64748b;
  font-size: 11px;
}

.pill.active {
  background: #dbeafe;
  color: #1d4ed8;
  font-weight: 600;
}

.grid-3 {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.grid-2 {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.field-block {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.input {
  width: 100%;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 8px 10px;
  font-size: 12px;
  background: #ffffff;
}

.range-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.range {
  flex: 1;
}

.range-value {
  font-size: 12px;
  color: #475569;
  width: 40px;
  text-align: right;
}

.hint-text {
  font-size: 11px;
  color: #94a3b8;
}

.modal-foot {
  padding: 0 16px 16px;
}

.primary-btn {
  width: 100%;
  background: #2563eb;
  color: #ffffff;
  border-radius: 16px;
  padding: 12px;
  font-size: 12px;
  font-weight: 600;
}

.icon-img {
  /* 图标大小：适配 36px 容器，留边距更美观 */
    width: 18px;
    height: 18px;
    /* 核心：保证图标透明底，不覆盖背景色 */
    background: transparent !important;
    background-color: transparent !important;
    border: none !important;
    /* 去掉图片默认间隙 */
    vertical-align: middle;
} 
</style>
