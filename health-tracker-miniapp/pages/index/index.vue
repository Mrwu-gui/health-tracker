<template>
  <view class="page-root">
    <view class="page">
    <view class="header">
      <text class="greeting-text">{{ greetingTitle }}</text>
      <navigator class="icon-btn" url="/pages/profile/index" open-type="switchTab">⚙</navigator>
    </view>

    <!-- AI 入口：凸显 AI 能力 -->
    <view class="ai-hero">
      <view class="ai-hero-bg" />
      <view class="ai-hero-inner">
        <view class="ai-hero-label">
          <text class="ai-hero-dot">✨</text>
          <text>智康AI</text>
        </view>
        <text class="ai-hero-desc">{{ aiGreeting || "有什么想问的？问饮食、运动、睡眠都可以。" }}</text>
        <view class="ai-quick-actions">
          <view class="ai-quick-item" @tap="goAiWithQuery('今天吃什么比较健康？')">
            <text class="ai-quick-icon">🍽</text>
            <text class="ai-quick-text">今天吃什么</text>
          </view>
          <view class="ai-quick-item" @tap="goAiWithQuery('给我一些适合今天的运动建议')">
            <text class="ai-quick-icon">🏃</text>
            <text class="ai-quick-text">运动建议</text>
          </view>
          <view class="ai-quick-item" @tap="goAiWithQuery('如何改善睡眠质量？')">
            <text class="ai-quick-icon">😴</text>
            <text class="ai-quick-text">睡眠建议</text>
          </view>
        </view>
        <navigator class="ai-hero-btn" url="/pages/ai/index" open-type="switchTab">
          <text>去和智康对话</text>
          <text class="ai-hero-arrow">›</text>
        </navigator>
      </view>
    </view>

    <!-- 今日概览 -->
    <view class="section-card">
      <view class="section-card-head">
        <view class="section-title-wrap">
          <view class="section-icon section-icon-overview"><text>📊</text></view>
          <text class="section-title">今日概览</text>
        </view>
      </view>
      <view class="section-card-body">
        <view class="overview-main">
          <view class="overview-left">
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
            <template v-if="overview.weightBmi && overview.weightBmi !== '暂无'">
              <text class="value">{{ overview.weightBmi }}</text>
            </template>
            <view v-else class="overview-empty-cell" @tap="goProfileAndOpenModal">
              <text class="overview-empty-link">去记录</text>
            </view>
          </view>
          <view class="overview-item">
            <text class="label">饮食记录</text>
            <text class="value">{{ overview.dietCount }}</text>
          </view>
          <view class="overview-item">
            <text class="label">血压 / 心率</text>
            <template v-if="overview.bpStatus && overview.bpStatus !== '暂无'">
              <text class="value">{{ overview.bpStatus }}</text>
            </template>
            <view v-else class="overview-empty-cell" @tap="goProfileAndOpenModal">
              <text class="overview-empty-link">去记录</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 今日提醒 -->
    <view class="section-card">
      <view class="section-card-head">
        <view class="section-title-wrap">
          <view class="section-icon section-icon-reminder"><text>🔔</text></view>
          <text class="section-title">今日提醒</text>
        </view>
        <navigator class="section-link" url="/pages/reminders/index">查看全部</navigator>
      </view>
      <view class="section-card-body">
        <view v-if="reminders.length === 0" class="section-empty">
          <image class="section-empty-icon" src="/static/tabbar/remind.png" mode="widthFix" />
          <text class="section-empty-title">暂无今日提醒</text>
          <text class="section-empty-desc">在提醒设置里添加运动、饮食或睡眠提醒</text>
          <navigator class="section-empty-link" url="/pages/reminders/index">去添加</navigator>
        </view>
        <view v-else class="reminder-list">
          <view v-for="item in reminders" :key="item.id" class="list-item">
            <view class="list-item-main">
              <text class="list-item-title">{{ item.title }}</text>
              <text class="list-item-meta">{{ item.time }} · {{ item.content }}</text>
            </view>
            <text class="list-item-tag list-item-tag-reminder">{{ item.typeLabel || "提醒" }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 今日目标 -->
    <view class="section-card">
      <view class="section-card-head">
        <view class="section-title-wrap">
          <view class="section-icon section-icon-goal"><text>🎯</text></view>
          <text class="section-title">今日目标</text>
        </view>
        <navigator class="section-link" url="/pages/goal/index">查看目标</navigator>
      </view>
      <view class="section-card-body">
        <view v-if="todayGoals.length === 0" class="section-empty">
          <image class="section-empty-icon" src="/static/tabbar/goal.png" mode="widthFix" />
          <text class="section-empty-title">暂无今日目标</text>
          <text class="section-empty-desc">在目标管理里设置步数、睡眠或饮食目标</text>
          <navigator class="section-empty-link" url="/pages/goal/index">去设置</navigator>
        </view>
        <view v-else class="goal-list">
          <view v-for="item in todayGoals" :key="item.id" class="list-item">
            <view class="list-item-main">
              <text class="list-item-title">{{ item.goalLabel }}目标</text>
              <text class="list-item-meta">已完成 {{ item.currentValue }} / {{ item.targetValue }}{{ item.unit }}</text>
            </view>
            <text class="list-item-tag list-item-tag-goal">{{ item.progress }}%</text>
          </view>
        </view>
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
    this.updateTabBarSelected(0);
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
            time: this.formatReminderTime(item.remindTime),
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
    formatReminderTime(value) {
      if (!value) return "未设置";
      if (typeof value === "string" && value.length <= 8 && value.includes(":")) {
        return value;
      }
      const date = new Date(value);
      if (isNaN(date.getTime())) return String(value);
      const pad = (num) => String(num).padStart(2, "0");
      const yyyy = date.getFullYear();
      const mm = pad(date.getMonth() + 1);
      const dd = pad(date.getDate());
      const hh = pad(date.getHours());
      const mi = pad(date.getMinutes());
      const ss = pad(date.getSeconds());
      return `${yyyy}-${mm}-${dd} ${hh}:${mi}:${ss}`;
    },
    goStats() {
      uni.switchTab({ url: "/pages/data/index" });
    },
    updateTabBarSelected(index) {
      const pages = getCurrentPages();
      const page = pages[pages.length - 1];
      if (page && typeof page.getTabBar === "function") {
        const tabBar = page.getTabBar();
        if (tabBar && typeof tabBar.setData === "function") {
          tabBar.setData({ selected: index });
        }
      }
    },
    goProfile() {
      uni.switchTab({ url: "/pages/profile/index" });
    },
    goAiWithQuery(query) {
      uni.setStorageSync("aiInitialMessage", query || "");
      uni.switchTab({ url: "/pages/ai/index" });
    },
    goProfileAndOpenModal() {
      uni.setStorageSync("openProfileModal", "1");
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
  background: #faf8f5;
  color: #0f172a;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.page-root {
  min-height: 100vh;
  background: #faf8f5;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.greeting-text {
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
  border: 1px solid #e8e2db;
  font-size: 16px;
  padding: 0;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* AI 入口大卡 */
.ai-hero {
  position: relative;
  border-radius: 20px;
  overflow: hidden;
  margin-bottom: 4px;
}

.ai-hero-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #1e3a5f 0%, #0f172a 50%, #312e81 100%);
}

.ai-hero-inner {
  position: relative;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.ai-hero-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.85);
}

.ai-hero-dot {
  font-size: 14px;
}

.ai-hero-desc {
  font-size: 14px;
  color: #ebe6df;
  line-height: 1.5;
}

.ai-quick-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.ai-quick-item {
  flex: 1;
  min-width: 0;
  background: rgba(255, 255, 255, 0.12);
  border-radius: 14px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  border: 1px solid rgba(255, 255, 255, 0.15);
}

.ai-quick-icon {
  font-size: 22px;
}

.ai-quick-text {
  font-size: 12px;
  color: #fff;
  font-weight: 500;
}

.ai-hero-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 12px 16px;
  background: #fff;
  color: #1e3a5f;
  border-radius: 14px;
  font-size: 14px;
  font-weight: 600;
  margin-top: 4px;
}

.ai-hero-arrow {
  font-size: 18px;
  font-weight: 300;
}

/* 统一区块卡片：今日概览 / 今日提醒 / 今日目标 */
.section-card {
  background: #ffffff;
  border-radius: 18px;
  padding: 0;
  border: 1px solid #e8e2db;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.section-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid #f2ede8;
}

.section-title-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-icon {
  width: 28px;
  height: 28px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  line-height: 1;
}

.section-icon-overview {
  background: #dbeafe;
  color: #1d4ed8;
}

.section-icon-reminder {
  background: #fef3c7;
  color: #b45309;
}

.section-icon-goal {
  background: #d1fae5;
  color: #059669;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #0f172a;
}

.section-link {
  font-size: 12px;
  color: #64748b;
}

.section-card-body {
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 统一列表项样式（提醒、目标共用） */
.list-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fefcf9;
  padding: 12px 14px;
  border-radius: 12px;
}

.list-item-main {
  flex: 1;
  min-width: 0;
}

.list-item-title {
  font-size: 13px;
  font-weight: 600;
  color: #0f172a;
  display: block;
}

.list-item-meta {
  font-size: 11px;
  color: #64748b;
  margin-top: 4px;
  display: block;
}

.list-item-tag {
  font-size: 11px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 999px;
  margin-left: 10px;
  flex-shrink: 0;
}

.list-item-tag-reminder {
  color: #b45309;
  background: #fef3c7;
}

.list-item-tag-goal {
  color: #059669;
  background: #d1fae5;
}

.reminder-list,
.goal-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.section-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 24px 16px;
  background: #fefcf9;
  border-radius: 14px;
  border: 1px dashed #e8e2db;
}

.section-empty-icon {
  width: 36px;
  height: auto;
  margin-bottom: 10px;
  opacity: 0.85;
}

.section-empty-title {
  font-size: 14px;
  font-weight: 600;
  color: #475569;
  margin-bottom: 6px;
}

.section-empty-desc {
  font-size: 12px;
  color: #94a3b8;
  line-height: 1.45;
  margin-bottom: 12px;
}

.section-empty-link {
  font-size: 13px;
  font-weight: 500;
  color: #2563eb;
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
  background: #e8e2db;
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
  border-top: 1px dashed #e8e2db;
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

.overview-empty-cell {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
}

.overview-empty-link {
  font-size: 12px;
  color: #2563eb;
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

.quick-item {
  background: #fefcf9;
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
  background: #e8e2db;
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
  border-bottom: 1px solid #f2ede8;
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
  background: #f5f1eb;
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
  border: 1px solid #e8e2db;
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
