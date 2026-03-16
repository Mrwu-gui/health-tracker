<template>
  <view class="page-root">
    <view class="page">
    <view class="header">
      <text class="greeting-text">{{ greetingTitle }}</text>
      <navigator class="icon-btn" url="/pages/profile/index" open-type="switchTab">⚙</navigator>
    </view>

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
            <view class="ai-quick-icon">
              <image class="ai-quick-icon-img" src="/static/tabbar/food2.png" mode="aspectFit" />
            </view>
            <text class="ai-quick-text">今天吃什么</text>
          </view>
          <view class="ai-quick-item" @tap="goAiWithQuery('给我一些适合今天的运动建议')">
            <view class="ai-quick-icon">
              <image class="ai-quick-icon-img" src="/static/tabbar/sport.png" mode="aspectFit" />
            </view>
            <text class="ai-quick-text">运动建议</text>
          </view>
          <view class="ai-quick-item" @tap="goAiWithQuery('如何改善睡眠质量？')">
            <view class="ai-quick-icon">
              <image class="ai-quick-icon-img" src="/static/tabbar/sleep2.png" mode="aspectFit" />
            </view>
            <text class="ai-quick-text">睡眠建议</text>
          </view>
        </view>
        <navigator class="ai-hero-btn" url="/pages/ai/index" open-type="switchTab">
          <text>去和智康对话</text>
          <text class="ai-hero-arrow">›</text>
        </navigator>
      </view>
    </view>

    <view class="section-card">
      <view class="section-card-head">
        <view class="section-title-wrap">
          <view class="section-icon section-icon-overview">
			  <image class="icon-img" src="/static/tabbar/gailan.png" mode="widthFix"></image>
		  </view>
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
            <text class="meta">约 <text class="strong">{{ overview.calories }}</text> kcal</text>
          </view>
        </view>
        <view class="overview-grid overview-grid-slim">
          <view class="overview-item">
            <text class="label">睡眠</text>
            <text class="value">{{ overview.sleep }}</text>
          </view>
          <view class="overview-item">
            <text class="label">饮食</text>
            <text class="value">{{ overview.dietCount }}</text>
          </view>
          <view class="overview-item">
            <text class="label">今日提醒</text>
            <text class="value">{{ overview.reminderSummary }}</text>
          </view>
          <view class="overview-item">
            <text class="label">今日目标</text>
            <text class="value">{{ overview.goalSummary }}</text>
          </view>
        </view>
      </view>
    </view>

    <view class="section-card">
      <view class="section-card-head">
        <view class="section-title-wrap">
          <view class="section-icon section-icon-sleep">
			  <image class="icon-img" src="/static/tabbar/sleep2.png" mode="widthFix"></image>
		  </view>
          <text class="section-title">睡眠打卡</text>
        </view>
      </view>
      <view class="section-card-body">
        <view class="sleep-quick" v-if="!sleepRecord">
          <view class="sleep-quick-btn" @tap="openRecord('sleep')">
            <image class="icon-img" src="/static/tabbar/sleep2.png" mode="widthFix"></image>
            <text class="sleep-quick-text">昨晚睡眠</text>
            <text class="sleep-quick-hint">默认 23:00–07:00，可改</text>
          </view>
        </view>
        <view v-if="sleepRecord" class="list-item">
          <view class="list-item-main">
            <text class="list-item-title">入睡 {{ formatClock(sleepRecord.startTime) }} - 起床 {{ formatClock(sleepRecord.endTime) }}</text>
          </view>
          <text class="list-item-tag list-item-tag-reminder">{{ sleepQualityLabel(sleepRecord.quality) }}</text>
        </view>
      </view>
    </view>

    <view class="section-card">
      <view class="section-card-head">
        <view class="section-title-wrap">
          <view class="section-icon section-icon-diet">
			  <image class="icon-img" src="/static/tabbar/food2.png" mode="widthFix"></image>
		  </view>
          <text class="section-title">今日饮食</text>
        </view>
        <view class="section-link-inline" @tap="openRecord('diet')">记录一餐</view>
      </view>
      <view class="section-card-body">
        <view class="diet-quick" v-if="!dietRecords.length">
          <view class="diet-quick-item" v-for="meal in dietMeals" :key="meal.type" @tap="openRecordWithMeal(meal.type)">
            <text class="diet-quick-label">{{ meal.label }}</text>
          </view>
        </view>
        <view v-if="dietRecords.length" class="reminder-list">
          <view v-for="item in dietRecords" :key="item.id" class="list-item">
            <view class="list-item-main">
              <text class="list-item-title">{{ item.foodName || "未命名" }}</text>
              <text class="list-item-meta">
                {{ item.mealType || "其他" }} · {{ item.calories || 0 }} 卡
              </text>
            </view>
            <text class="list-item-tag list-item-tag-goal">{{ item.mealType || "饮食" }}</text>
          </view>
        </view>
      </view>
    </view>

    <view class="section-card">
      <view class="section-card-head">
        <view class="section-title-wrap">
          <view class="section-icon section-icon-reminder">
			   <image class="icon-img" src="/static/tabbar/remind.png" mode="widthFix"></image>
		  </view>
          <text class="section-title">今日提醒</text>
        </view>
        <navigator class="section-link" url="/pages/reminders/index">查看全部</navigator>
      </view>
      <view class="section-card-body">
        <view v-if="reminders.length === 0" class="section-empty">
          <image class="section-empty-icon" src="/static/tabbar/remind.png" mode="widthFix" />
          <text class="section-empty-title">暂无今日提醒</text>
          <text class="section-empty-desc">添加提醒后在此展示</text>
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

    <view class="section-card">
      <view class="section-card-head">
        <view class="section-title-wrap">
          <view class="section-icon section-icon-goal">
			   <image class="icon-img" src="/static/tabbar/mubiao.png" mode="widthFix"></image>
		  </view>
          <text class="section-title">今日目标</text>
        </view>
        <navigator class="section-link" url="/pages/goal/index">查看目标</navigator>
      </view>
      <view class="section-card-body">
        <view v-if="todayGoals.length === 0" class="section-empty">
          <image class="section-empty-icon" src="/static/tabbar/goal.png" mode="widthFix" />
          <text class="section-empty-title">暂无今日目标</text>
          <text class="section-empty-desc">设置目标后在此展示</text>
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
        calories: "0",
        sleep: "0小时0分",
        dietCount: "已记录 0 餐",
        reminderSummary: "0 条",
        goalSummary: "--"
      },
      dietMeals: [
        { type: "早餐", label: "早餐" },
        { type: "午餐", label: "午餐" },
        { type: "晚餐", label: "晚餐" },
        { type: "加餐", label: "加餐" }
      ],
      dietRecords: [],
      sleepRecord: null,
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
      const today = new Date();
      const todayStr = this.formatDate(today);
      const yesterday = new Date(today.getTime() - 24 * 60 * 60 * 1000);
      const yesterdayStr = this.formatDate(yesterday);
      try {
        const data = await request("/api/statistics/overview", "GET", { userId, period: "day" });
        if (data) {
          this.overview.steps = data.steps != null ? String(data.steps) : this.overview.steps;
          this.overview.sleep = data.sleep || this.overview.sleep;
          this.overview.dietCount = `已记录 ${data.dietCount || 0} 餐`;
          const stepsNum = parseInt(this.overview.steps, 10) || 0;
          this.overview.calories = String(Math.round(stepsNum * 0.04));
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
          this.reminders = mapped;
          this.overview.reminderSummary = `${raw.filter((item) => Number(item.type) !== 4).length} 条`;
        } else {
          this.reminders = [];
          this.overview.reminderSummary = "0 条";
        }
      } catch (err) {
        this.reminders = [];
        this.overview.reminderSummary = "0 条";
      }

      try {
        const dietList = await request("/api/diet/list", "GET", { userId, date: todayStr });
        const raw = Array.isArray(dietList)
          ? dietList
          : Array.isArray(dietList?.records)
            ? dietList.records
            : Array.isArray(dietList?.list)
              ? dietList.list
              : [];
        this.overview.dietCount = `已记录 ${raw.length} 餐`;
        this.dietRecords = raw.slice(0, 3);
      } catch (err) {
        this.overview.dietCount = "已记录 0 餐";
        this.dietRecords = [];
      }

      try {
        const sleepListToday = await request("/api/sleep/list", "GET", { userId, date: todayStr });
        let raw = Array.isArray(sleepListToday)
          ? sleepListToday
          : Array.isArray(sleepListToday?.records)
            ? sleepListToday.records
            : Array.isArray(sleepListToday?.list)
              ? sleepListToday.list
              : [];
        if (raw.length === 0) {
          const sleepListYesterday = await request("/api/sleep/list", "GET", { userId, date: yesterdayStr });
          raw = Array.isArray(sleepListYesterday)
            ? sleepListYesterday
            : Array.isArray(sleepListYesterday?.records)
              ? sleepListYesterday.records
              : Array.isArray(sleepListYesterday?.list)
                ? sleepListYesterday.list
                : [];
        }
        if (raw.length > 0) {
          const latest = raw.reduce((acc, item) => {
            const end = this.parseDateTime(item.endTime) || this.parseDateTime(item.startTime);
            if (!end) return acc;
            if (!acc || end > acc.end) return { item, end };
            return acc;
          }, null);
          if (latest) {
            this.sleepRecord = latest.item;
            const start = this.parseDateTime(latest.item.startTime);
            const end = this.parseDateTime(latest.item.endTime);
            if (start && end) {
              const minutes = Math.max(0, Math.round((end - start) / 60000));
              this.overview.sleep = this.formatMinutes(minutes);
            }
          }
        }
      } catch (err) {
        this.overview.sleep = this.overview.sleep || "0小时0分";
        this.sleepRecord = null;
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
          this.todayGoals = mapped;
          const done = mapped.filter((item) => item.progress >= 100).length;
          this.overview.goalSummary = `${done}/${mapped.length} 达成`;
        } else {
          this.todayGoals = [];
          this.overview.goalSummary = "--";
        }
      } catch (err) {
        this.todayGoals = [];
        this.overview.goalSummary = "--";
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
      const key = `ai_greeting_norepeat_${period}_${slot}_${today.getFullYear()}-${today.getMonth() + 1}-${today.getDate()}`;
      const cached = uni.getStorageSync(key);
      if (cached) {
        this.aiGreeting = cached;
        return;
      }
      try {
        const prompt = `页面顶部已经显示了「${period}好，${this.userName || "朋友"}」，你不要再重复「${period}好」。请直接生成一句简短的承接语或引导语（例如愿您今日舒心、有什么想问的都可以问我等），不要包含「${period}好」或「早上好/下午好/晚上好」等时段问候，不超过20字。`;
        const userId = uni.getStorageSync("userId") || 1;
        const data = await request("/api/ai/chat", "POST", { userId, message: prompt, store: false });
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
      this.overview.calories = "0";
      this.overview.sleep = "0小时0分";
      this.overview.dietCount = "已记录 0 餐";
      this.overview.reminderSummary = "0 条";
      this.overview.goalSummary = "--";
      this.dietRecords = [];
      this.sleepRecord = null;
    },
    formatDate(date) {
      const y = date.getFullYear();
      const m = String(date.getMonth() + 1).padStart(2, "0");
      const d = String(date.getDate()).padStart(2, "0");
      return `${y}-${m}-${d}`;
    },
    parseDateTime(value) {
      if (!value) return null;
      if (typeof value === "string") {
        const normalized = value.replace("T", " ").replace(/-/g, "/");
        const parsed = new Date(normalized);
        return Number.isNaN(parsed.getTime()) ? null : parsed;
      }
      const parsed = new Date(value);
      return Number.isNaN(parsed.getTime()) ? null : parsed;
    },
    formatMinutes(total) {
      const hours = Math.floor(total / 60);
      const minutes = total % 60;
      return `${hours}小时${minutes}分`;
    },
    formatClock(value) {
      const date = this.parseDateTime(value);
      if (!date) return "--:--";
      const h = String(date.getHours()).padStart(2, "0");
      const m = String(date.getMinutes()).padStart(2, "0");
      return `${h}:${m}`;
    },
    sleepQualityLabel(value) {
      if (!value) return "睡眠";
      const text = String(value).toLowerCase();
      if (text === "good") return "良好";
      if (text === "normal") return "正常";
      if (text === "light") return "轻度";
      if (text === "poor") return "较差";
      return value;
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
    openRecordWithMeal(mealType) {
      const enc = encodeURIComponent(mealType);
      uni.navigateTo({ url: `/pages/record/index?type=diet&meal=${enc}&t=${Date.now()}` });
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
  padding-bottom: calc(56px + env(safe-area-inset-bottom));
  min-height: 100vh;
  background: #faf8f5;
  color: #0f172a;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.page-root {
  min-height: 100vh;
  background: #faf8f5;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
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
  margin-top: 2px;
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
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ai-quick-icon-img {
  width: 24px;
  height: 24px;
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
  font-size: 15px;
  font-weight: 700;
  line-height: 1;
}

.section-icon-txt {
  color: inherit;
}

.section-icon-overview {
  background: #dbeafe;
  color: #1d4ed8;
}

.section-icon-sleep {
  background: #e0e7ff;
  color: #4f46e5;
}

.section-icon-diet {
  background: #fef3c7;
  color: #b45309;
}

.section-icon-reminder {
  background: #fef3c7;
  color: #b45309;
}

.section-icon-goal {
  background: #d1fae5;
  color: #059669;
}

.section-link-inline {
  font-size: 12px;
  color: #2563eb;
  font-weight: 500;
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

.overview-grid-slim {
  grid-template-columns: 1fr 1fr;
}

.sleep-quick {
  display: flex;
  justify-content: center;
}

.sleep-quick-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 16px 28px;
  background: #f8fafc;
  border-radius: 14px;
  border: 1px dashed #cbd5e1;
}

.sleep-quick-icon {
  font-size: 24px;
}

.sleep-quick-text {
  font-size: 15px;
  font-weight: 600;
  color: #0f172a;
}

.sleep-quick-hint {
  font-size: 11px;
  color: #94a3b8;
}

.diet-quick {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.diet-quick-item {
  padding: 10px 16px;
  background: #fefcf9;
  border-radius: 12px;
  border: 1px solid #e8e2db;
}

.diet-quick-label {
  font-size: 13px;
  color: #475569;
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
