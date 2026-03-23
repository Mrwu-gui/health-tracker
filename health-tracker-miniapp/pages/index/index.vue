<template>
  <view class="page-root">
    <view class="page">

      <!-- 顶栏 -->
      <view class="header">
        <view class="header-left">
          <text class="greeting-text">{{ greetingTitle }}</text>
          <text v-if="aiGreeting" class="greeting-sub">{{ aiGreeting }}</text>
        </view>
        <navigator class="icon-btn" url="/pages/profile/index" open-type="switchTab">
          <image class="icon-btn-img" src="/static/tabbar/profile.png" mode="aspectFit" />
        </navigator>
      </view>


      <!-- 核心指标 一行4个 -->
      <view class="metrics-row">

        <!-- 步数 -->
        <view class="metric-mini metric-mini-steps card-sm">
          <view class="metric-mini-top">
            <view class="metric-mini-icon metric-mini-icon-steps">
              <image class="metric-mini-icon-img" src="/static/tabbar/sport.png" mode="aspectFit" />
            </view>
            <view v-if="stepsDelta !== 0" class="metric-diff-badge pill" :class="stepsDeltaIsUp ? 'diff-up' : 'diff-down'">
              <text class="diff-badge-text">{{ stepsDeltaIsUp ? '↑' : '↓' }}{{ Math.abs(stepsDelta) > 999 ? (Math.abs(stepsDelta)/1000).toFixed(1)+'k' : Math.abs(stepsDelta) }}</text>
            </view>
          </view>
          <text class="metric-mini-value">{{ stepsNum > 999 ? (stepsNum/1000).toFixed(1)+'k' : stepsNum }}</text>
          <text class="metric-mini-label">步数</text>
        </view>

        <!-- 睡眠 -->
        <view class="metric-mini metric-mini-sleep card-sm">
          <view class="metric-mini-top">
            <view class="metric-mini-icon metric-mini-icon-sleep">
              <image class="metric-mini-icon-img" src="/static/tabbar/sleep.png" mode="aspectFit" />
            </view>
            <view v-if="sleepDeltaMinutes !== 0" class="metric-diff-badge pill" :class="sleepDeltaMinutes > 0 ? 'diff-up' : 'diff-down'">
              <text class="diff-badge-text">{{ sleepDeltaMinutes > 0 ? '↑' : '↓' }}{{ Math.abs(sleepDiffHours) }}h</text>
            </view>
          </view>
          <text class="metric-mini-value">{{ sleepDisplay }}</text>
          <text class="metric-mini-label">睡眠</text>
        </view>

        <!-- 体重 -->
        <view class="metric-mini metric-mini-weight card-sm">
          <view class="metric-mini-top">
            <view class="metric-mini-icon metric-mini-icon-weight">
              <image class="metric-mini-icon-img" src="/static/tabbar/weight.png" mode="aspectFit" />
            </view>
            <view v-if="weightDelta !== 0 && overview.weight" class="metric-diff-badge pill" :class="weightDelta > 0 ? 'diff-up' : 'diff-down'">
              <text class="diff-badge-text">{{ weightDelta > 0 ? '↑' : '↓' }}{{ Math.abs(weightDelta).toFixed(1) }}</text>
            </view>
          </view>
          <text class="metric-mini-value">{{ overview.weight ? overview.weight : '--' }}</text>
          <text class="metric-mini-label">体重(kg)</text>
        </view>

        <!-- 饮食 -->
        <view class="metric-mini metric-mini-diet card-sm">
          <view class="metric-mini-top">
            <view class="metric-mini-icon metric-mini-icon-diet">
              <image class="metric-mini-icon-img" src="/static/tabbar/food.png" mode="aspectFit" />
            </view>
            <view v-if="dietDeltaCalories !== 0 && dietCaloriesToday > 0" class="metric-diff-badge pill" :class="dietDeltaCalories > 0 ? 'diff-up' : 'diff-down'">
              <text class="diff-badge-text">{{ dietDeltaCalories > 0 ? '↑' : '↓' }}{{ Math.abs(dietDeltaCalories) > 999 ? (Math.abs(dietDeltaCalories)/1000).toFixed(1)+'k' : Math.abs(dietDeltaCalories) }}</text>
            </view>
          </view>
          <text class="metric-mini-value">{{ dietCaloriesToday > 0 ? dietCaloriesToday : '--' }}</text>
          <text class="metric-mini-label">热量(kcal)</text>
        </view>

      </view>

      <!-- 今日AI健康解读 -->
      <view class="daily-ai-card card">
        <view class="daily-ai-head">
          <view class="daily-ai-title-block">
            <text class="daily-ai-title">看看智康怎么说</text>
          </view>
          <view v-if="!dailyAiLoading && dailyAiText" class="daily-ai-refresh-btn pill" @tap="fetchDailyAi(true)">
            <text class="daily-ai-refresh-text">刷新</text>
          </view>
        </view>
        <view class="daily-ai-body">
          <view v-if="dailyAiLoading" class="daily-ai-loading">
            <view class="loading-dots">
              <view class="loading-dot"></view>
              <view class="loading-dot"></view>
              <view class="loading-dot"></view>
            </view>
            <text class="daily-ai-loading-text">正在分析你的健康数据…</text>
          </view>
          <text v-else-if="dailyAiText" class="daily-ai-text">{{ dailyAiText }}</text>
          <view v-else class="daily-ai-empty" @tap="fetchDailyAi(true)">
            <text class="daily-ai-empty-icon">✦</text>
            <text class="daily-ai-empty-text">点击生成今日解读</text>
          </view>
        </view>
      </view>

      <!-- 快捷入口 -->
      <view class="quick-entrance-card card">
        <view class="quick-entrance-head">
          <view class="quick-entrance-head-info">
            <text class="quick-entrance-title">和智康聊聊</text>
          </view>
        </view>
        <view class="quick-entrance-row">
          <view class="quick-entrance-item card-sm" @tap="goAiWithQuery('今天吃什么比较健康？')">
            <image class="quick-entrance-icon" src="/static/tabbar/food2.png" mode="aspectFit" />
            <text class="quick-entrance-text">今天吃什么？</text>
          </view>
          <view class="quick-entrance-item card-sm" @tap="goAiWithQuery('给我一些适合今天的运动建议')">
            <image class="quick-entrance-icon" src="/static/tabbar/sport.png" mode="aspectFit" />
            <text class="quick-entrance-text">动一动！</text>
          </view>
          <view class="quick-entrance-item card-sm" @tap="goAiWithQuery('如何改善睡眠质量？')">
            <image class="quick-entrance-icon" src="/static/tabbar/sleep.png" mode="aspectFit" />
            <text class="quick-entrance-text">睡个好觉</text>
          </view>
        </view>
      </view>

      <!-- 今日待办 -->
      <view class="section-card card" v-if="pendingGoals.length > 0 || pendingReminders.length > 0">
        <view class="section-card-head">
          <view class="todo-tabs">
            <view class="todo-tab" :class="{ 'active': todoTab === 0 }" @tap="todoTab = 0">
              <text class="todo-tab-text">目标</text>
              <view v-if="pendingGoals.length > 0" class="todo-tab-badge">{{ pendingGoals.length }}</view>
            </view>
            <view class="todo-tab" :class="{ 'active': todoTab === 1 }" @tap="todoTab = 1">
              <text class="todo-tab-text">提醒</text>
              <view v-if="pendingReminders.length > 0" class="todo-tab-badge">{{ pendingReminders.length }}</view>
            </view>
          </view>
        </view>
        <!-- 目标列表 -->
        <view v-if="todoTab === 0" class="section-card-body">
          <view v-if="pendingGoals.length === 0" class="todo-empty">
            <text class="todo-empty-text">暂无待完成目标</text>
          </view>
          <view v-else class="list-body">
            <view v-for="item in pendingGoals" :key="item.id" class="list-item">
              <view class="list-item-main">
                <text class="list-item-title">{{ item.goalLabel }}</text>
                <view class="goal-progress-row">
                  <view class="goal-progress-bar">
                    <view class="goal-progress-fill" :style="{ width: item.progress + '%' }"></view>
                  </view>
                  <text class="goal-progress-num">{{ item.progress }}%</text>
                </view>
              </view>
              <view class="goal-status-badge pill goal-badge-going">
                <text class="goal-status-text">进行中</text>
              </view>
            </view>
          </view>
          <navigator class="todo-more" url="/pages/goal/index">查看全部目标 ›</navigator>
        </view>
        <!-- 提醒列表 -->
        <view v-if="todoTab === 1" class="section-card-body">
          <view v-if="pendingReminders.length === 0" class="todo-empty">
            <text class="todo-empty-text">暂无待处理提醒</text>
          </view>
          <view v-else class="list-body">
            <view v-for="item in pendingReminders" :key="item.id" class="list-item">
              <view class="list-item-main">
                <text class="list-item-title">{{ item.title }}</text>
                <text class="list-item-meta">{{ item.time }} · {{ item.content }}</text>
              </view>
              <text class="list-item-tag list-item-tag-reminder pill">{{ item.typeLabel || "提醒" }}</text>
            </view>
          </view>
          <navigator class="todo-more" url="/pages/reminders/index">查看全部提醒 ›</navigator>
        </view>
      </view>

      <text v-if="error" class="status error">{{ error }}</text>
    </view>

    <!-- 遮罩（+记录展开时） -->
    <view v-if="fabOpen" class="fab-mask" @tap="fabOpen = false"></view>


    <!-- +记录 固定按钮 + 展开菜单 -->
    <view
      class="add-record-wrap"
      :style="{ left: fabPos.x + 'px', top: fabPos.y + 'px' }"
      @touchstart="onFabTouchStart"
      @touchmove.stop.prevent="onFabTouchMove"
      @touchend="onFabTouchEnd"
    >
      <!-- 展开的菜单项（从下往上） -->
      <view v-if="fabOpen" class="add-record-menu">
        <view class="add-record-item" @tap="fabRecord('weight')">
          <view class="add-record-icon add-record-icon-weight">
            <image class="add-record-icon-img" src="/static/tabbar/weight.png" mode="aspectFit" />
          </view>
          <text class="add-record-label">体重</text>
        </view>
        <view class="add-record-item" @tap="fabRecord('diet')">
          <view class="add-record-icon add-record-icon-diet">
            <image class="add-record-icon-img" src="/static/tabbar/food.png" mode="aspectFit" />
          </view>
          <text class="add-record-label">饮食</text>
        </view>
        <view class="add-record-item" @tap="fabRecord('sleep')">
          <view class="add-record-icon add-record-icon-sleep">
            <image class="add-record-icon-img" src="/static/tabbar/sleep.png" mode="aspectFit" />
          </view>
          <text class="add-record-label">睡眠</text>
        </view>
      </view>
      <!-- +记录 主按钮 -->
      <view class="add-record-btn" :class="{ 'add-record-btn-open': fabOpen }" @tap="toggleFab">
        <text class="add-record-btn-icon">{{ fabOpen ? '✕' : '+' }}</text>
        <text class="add-record-btn-text">{{ fabOpen ? '关闭' : '记录' }}</text>
      </view>
    </view>
    <!-- 自定义底部导航 -->
    <custom-tabbar :current="0" />
  </view>
</template>

<script>
import { request } from "../../utils/api";
import { REMINDER_TYPE } from "../../constants/enums";
import CustomTabbar from "@/components/custom-tabbar/custom-tabbar.vue";

export default {
  components: {
    CustomTabbar
  },
  data() {
    return {
      overview: {
        steps: "0",
        calories: "0",
        sleep: "0小时0分",
        dietCount: "已记录 0 餐",
        reminderSummary: "0 条",
        goalSummary: "--",
        weight: "",
        bmi: ""
      },
      dietRecords: [],
      sleepRecord: null,
      loading: false,
      error: "",
      syncLoading: false,
      userName: "",
      greetingTitle: "你好",
      reminders: [],
      todayGoals: [],
      aiGreeting: "",
      aiLoading: false,
      dailyAiText: "",
      dailyAiLoading: false,
      userSex: "",
      stepsGoal: 0,
      sleepGoalHours: 8,
      dietGoalCalories: 0,
      weightGoal: 0,
      streakDays: 0,
      weekDays: [],
      fabOpen: false,
      fabPos: { x: 0, y: 0 },
      fabDragging: false,
      fabTouch: { x: 0, y: 0, offsetX: 0, offsetY: 0, moved: false },
      fabBounds: { maxX: 0, maxY: 0 },
      stepsDelta: 0,
      stepsToday: 0,
      stepsYesterday: 0,
      sleepDeltaMinutes: 0,
      sleepMinutesToday: 0,
      sleepMinutesYesterday: 0,
      dietDeltaCalories: 0,
      weightDelta: 0,
      dietCaloriesToday: 0,
      dietCaloriesYesterday: 0,
      weightToday: 0,
      weightYesterday: 0,
      todoTab: 0
    };
  },
  computed: {
    pendingGoals() {
      return this.todayGoals.filter(item => item.progress < 100);
    },
    pendingReminders() {
      return this.reminders.filter(item => item.status !== 1);
    },
    showPeriod() {
      const sex = this.userSex || uni.getStorageSync("userSex") || "";
      return String(sex).includes("女");
    },
    stepsNum() {
      return parseInt(String(this.overview.steps || "0").replace(/[^\d]/g, ""), 10) || 0;
    },
    stepsPercent() {
      if (!this.stepsGoal) return 0;
      return Math.min(100, Math.round((this.stepsNum / this.stepsGoal) * 100));
    },
    stepsDeltaIsUp() {
      return this.stepsDelta > 0;
    },
    sleepMinutes() {
      if (!this.sleepRecord) return 0;
      const start = this.parseDateTime(this.sleepRecord.startTime);
      const end = this.parseDateTime(this.sleepRecord.endTime);
      if (!start || !end) return 0;
      return Math.max(0, Math.round((end - start) / 60000));
    },
    sleepHours() {
      return parseFloat((this.sleepMinutes / 60).toFixed(1));
    },
    sleepDisplay() {
      if (!this.sleepRecord) return "--";
      const h = Math.floor(this.sleepMinutes / 60);
      const m = this.sleepMinutes % 60;
      return `${h}h ${m}m`;
    },
    sleepPercent() {
      if (!this.sleepGoalHours || !this.sleepRecord) return 0;
      return Math.min(100, Math.round((this.sleepHours / this.sleepGoalHours) * 100));
    },
    sleepDiffHours() {
      return parseFloat((Math.abs(this.sleepDeltaMinutes) / 60).toFixed(1));
    },
    dietDisplay() {
      if (this.dietCaloriesToday > 0) return `${this.dietCaloriesToday} kcal`;
      return "未记录";
    },
    needsSteps() {
      const num = parseInt(String(this.overview.steps || "0").replace(/[^\d]/g, ""), 10);
      const today = new Date();
      const key = `${today.getFullYear()}-${today.getMonth() + 1}-${today.getDate()}`;
      const lastSync = uni.getStorageSync("stepsSyncDate");
      return !num || Number.isNaN(num) || lastSync !== key;
    }
  },
  onLoad() {
    const token = uni.getStorageSync("token");
    const userId = uni.getStorageSync("userId");
    if (!token || !userId) {
      uni.reLaunch({ url: "/pages/login/index" });
      return;
    }
    const cachedSteps = uni.getStorageSync("steps");
    if (cachedSteps) this.overview.steps = String(cachedSteps);
    const storedName = uni.getStorageSync("userName");
    const wxProfile = uni.getStorageSync("wxProfile") || {};
    this.userName = storedName || wxProfile.nickName || "朋友";
    this.greetingTitle = `${this.timePeriod(new Date().getHours())}好，${this.userName}`;
    this.fetchAiGreeting();
    this.initFabPos();
    // #ifdef MP-WEIXIN
    this.maybeAutoSyncSteps();
    // #endif
  },
  onShow() {
    this.updateTabBarSelected(0);
    this.userSex = uni.getStorageSync("userSex") || "";
    this.fetchOverview();
  },
  methods: {
    initFabPos() {
      if (this.fabPos.x || this.fabPos.y) return;
      const info = uni.getSystemInfoSync();
      const safeBottom = info.safeAreaInsets ? info.safeAreaInsets.bottom : 0;
      const buttonWidth = 92;
      const buttonHeight = 56;
      const marginRight = 20;
      const marginBottom = 80;
      const maxX = info.windowWidth - buttonWidth;
      const maxY = info.windowHeight - buttonHeight - marginBottom - safeBottom;
      this.fabBounds = { maxX, maxY };
      this.fabPos = { x: Math.max(0, maxX - marginRight), y: Math.max(0, maxY) };
    },
    onFabTouchStart(e) {
      const touch = e.touches && e.touches[0];
      if (!touch) return;
      this.fabDragging = true;
      this.fabTouch.moved = false;
      this.fabTouch.x = touch.clientX;
      this.fabTouch.y = touch.clientY;
      this.fabTouch.offsetX = touch.clientX - this.fabPos.x;
      this.fabTouch.offsetY = touch.clientY - this.fabPos.y;
    },
    onFabTouchMove(e) {
      if (!this.fabDragging) return;
      const touch = e.touches && e.touches[0];
      if (!touch) return;
      const nextX = touch.clientX - this.fabTouch.offsetX;
      const nextY = touch.clientY - this.fabTouch.offsetY;
      const maxX = this.fabBounds.maxX || 0;
      const maxY = this.fabBounds.maxY || 0;
      this.fabPos = {
        x: Math.min(Math.max(0, nextX), maxX),
        y: Math.min(Math.max(0, nextY), maxY)
      };
      this.fabTouch.moved = true;
    },
    onFabTouchEnd() {
      this.fabDragging = false;
    },
    toggleFab() {
      if (this.fabTouch.moved) {
        this.fabTouch.moved = false;
        return;
      }
      this.fabOpen = !this.fabOpen;
    },
    async fetchOverview() {
      this.loading = true;
      this.error = "";
      const userId = uni.getStorageSync("userId") || 1;
      const today = new Date();
      const todayStr = this.formatDate(today);
      const yesterday = new Date(today.getTime() - 86400000);
      const yesterdayStr = this.formatDate(yesterday);

      try {
        const data = await request("/api/statistics/overview", "GET", { userId, period: "day" });
        if (data) {
          this.overview.steps = data.steps != null ? String(data.steps) : this.overview.steps;
          this.overview.sleep = data.sleep || this.overview.sleep;
          this.overview.dietCount = `已记录 ${data.dietCount || 0} 餐`;
          const stepsNum = parseInt(this.overview.steps, 10) || 0;
          this.overview.calories = String(Math.round(stepsNum * 0.04));
          if (data.weight != null) this.overview.weight = String(data.weight);
          this.stepsDelta = Number(data.stepsDelta || 0);
          this.stepsToday = Number(data.stepsToday || 0);
          this.stepsYesterday = Number(data.stepsYesterday || 0);
          this.sleepDeltaMinutes = Number(data.sleepDeltaMinutes || 0);
          this.sleepMinutesToday = Number(data.sleepMinutesToday || 0);
          this.sleepMinutesYesterday = Number(data.sleepMinutesYesterday || 0);
          this.dietDeltaCalories = Number(data.dietDeltaCalories || 0);
          this.weightDelta = Number(data.weightDelta || 0);
          this.dietCaloriesToday = Number(data.dietCaloriesToday || 0);
          this.dietCaloriesYesterday = Number(data.dietCaloriesYesterday || 0);
          this.weightToday = Number(data.weightToday || 0);
          this.weightYesterday = Number(data.weightYesterday || 0);
        }
      } catch (err) { this.error = ""; }

      try {
        const list = await request("/api/reminder/list", "GET", { userId });
        const raw = Array.isArray(list) ? list : Array.isArray(list?.records) ? list.records : Array.isArray(list?.list) ? list.list : [];
        this.reminders = raw
          .filter(item => Number(item.type) !== 4)
          .filter(item => this.isTodayReminder(item.remindTime))
          .slice(0, 5)
          .map(item => ({
          id: item.id,
          title: item.title,
          content: item.content || "提醒事项",
          time: this.formatReminderTime(item.remindTime),
          typeLabel: this.reminderTagLabel(item.type),
          status: item.status || 0
        }));
      } catch (err) { this.reminders = []; }

      try {
        const dietList = await request("/api/diet/list", "GET", { userId, date: todayStr });
        const raw = Array.isArray(dietList) ? dietList : Array.isArray(dietList?.records) ? dietList.records : Array.isArray(dietList?.list) ? dietList.list : [];
        this.overview.dietCount = `已记录 ${raw.length} 餐`;
        this.dietRecords = raw.slice(0, 3);
      } catch (err) {
        this.overview.dietCount = "已记录 0 餐";
        this.dietRecords = [];
      }

      try {
        const sleepListToday = await request("/api/sleep/list", "GET", { userId, date: todayStr });
        let raw = Array.isArray(sleepListToday) ? sleepListToday : Array.isArray(sleepListToday?.records) ? sleepListToday.records : Array.isArray(sleepListToday?.list) ? sleepListToday.list : [];
        if (raw.length === 0) {
          const sleepListYesterday = await request("/api/sleep/list", "GET", { userId, date: yesterdayStr });
          raw = Array.isArray(sleepListYesterday) ? sleepListYesterday : Array.isArray(sleepListYesterday?.records) ? sleepListYesterday.records : Array.isArray(sleepListYesterday?.list) ? sleepListYesterday.list : [];
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
            const recordDate = latest.item.recordDate || (latest.item.startTime ? latest.item.startTime.slice(0, 10) : "");
            if (recordDate && recordDate !== todayStr) {
              this.sleepDeltaMinutes = 0;
            }
          }
        }
      } catch (err) { this.sleepRecord = null; }

      try {
        const list = await request("/api/goal/list", "GET", { userId, period: "day" });
        const raw = Array.isArray(list) ? list : Array.isArray(list?.records) ? list.records : Array.isArray(list?.list) ? list.list : [];
        if (raw.length > 0) {
          const mapped = raw.slice(0, 3).map(item => {
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
          const done = mapped.filter(item => item.progress >= 100).length;
          this.overview.goalSummary = `${done}/${mapped.length} 达成`;
          const stepsGoalItem = raw.find(item => Number(item.goalType) === 1);
          if (stepsGoalItem) this.stepsGoal = Number(stepsGoalItem.targetValue || 0);
          const sleepGoalItem = raw.find(item => Number(item.goalType) === 2);
          if (sleepGoalItem) this.sleepGoalHours = Number(sleepGoalItem.targetValue || 8);
          const dietGoalItem = raw.find(item => Number(item.goalType) === 3);
          if (dietGoalItem) this.dietGoalCalories = Number(dietGoalItem.targetValue || 0);
          const weightGoalItem = raw.find(item => Number(item.goalType) === 4);
          if (weightGoalItem) this.weightGoal = Number(weightGoalItem.targetValue || 0);
        } else {
          this.todayGoals = [];
          this.overview.goalSummary = "--";
        }
      } catch (err) {
        this.todayGoals = [];
        this.overview.goalSummary = "--";
      } finally {
        this.loading = false;
        this.fetchDailyAi();
      }
    },
    async fetchDailyAi(forceRefresh) {
      if (this.dailyAiLoading) return;
      const today = new Date();
      const dateStr = this.formatDate(today);
      const cacheKey = `daily_ai_insight_${dateStr}`;
      if (!forceRefresh) {
        const cached = uni.getStorageSync(cacheKey);
        if (cached) { this.dailyAiText = cached; return; }
      }
      this.dailyAiLoading = true;
      this.dailyAiText = "";
      const stepsToday = this.stepsToday || this.stepsNum;
      const stepsYesterday = this.stepsYesterday || Math.max(0, stepsToday - this.stepsDelta);
      const sleepTodayMinutes = this.sleepMinutesToday || this.sleepMinutes;
      const sleepYesterdayMinutes = this.sleepMinutesYesterday || 0;
      const sleepTodayDesc = sleepTodayMinutes > 0 ? this.formatMinutes(sleepTodayMinutes) : "未记录";
      const sleepYesterdayDesc = sleepYesterdayMinutes > 0 ? this.formatMinutes(sleepYesterdayMinutes) : "未记录";
      const dietToday = this.dietCaloriesToday || 0;
      const dietYesterday = this.dietCaloriesYesterday || 0;
      const weightToday = this.weightToday || (this.overview.weight ? parseFloat(this.overview.weight) : 0);
      const weightYesterday = this.weightYesterday || (weightToday ? parseFloat((weightToday - this.weightDelta).toFixed(1)) : 0);
      const prompt = `你是一位贴心的健康助手。请基于“今日 vs 昨日”对比给出建议：今日步数${stepsToday}步，昨日${stepsYesterday}步；今日睡眠${sleepTodayDesc}，昨日睡眠${sleepYesterdayDesc}；今日饮食${dietToday}kcal，昨日饮食${dietYesterday}kcal；今日体重${weightToday ? weightToday + 'kg' : '未记录'}，昨日体重${weightYesterday ? weightYesterday + 'kg' : '未记录'}。请用3～4句温暖的话给出最关键的改进点或鼓励，不要寒暄，直接输出内容。`;
      try {
        const userId = uni.getStorageSync("userId") || 1;
        const data = await request("/api/ai/chat", "POST", { userId, message: prompt, store: false });
        if (data && data.content) {
          this.dailyAiText = String(data.content).trim();
          uni.setStorageSync(cacheKey, this.dailyAiText);
        }
      } catch (e) {
        this.dailyAiText = "";
      } finally {
        this.dailyAiLoading = false;
      }
    },
    async fetchAiGreeting() {
      const today = new Date();
      const hour = today.getHours();
      const period = this.timePeriod(hour);
      this.greetingTitle = `${period}好，${this.userName || "朋友"}`;
      const slot = Math.floor(hour / 3);
      const sexFlag = this.showPeriod ? "f" : "n";
      const key = `ai_greeting_norepeat_${sexFlag}_${period}_${slot}_${today.getFullYear()}-${today.getMonth() + 1}-${today.getDate()}`;
      const cached = uni.getStorageSync(key);
      if (cached) {
        if (!this.showPeriod && /经期|月经|例假/i.test(cached)) {
          uni.removeStorageSync(key);
        } else {
          this.aiGreeting = cached;
          return;
        }
      }
      this.aiLoading = true;
      try {
        const periodGuard = this.showPeriod ? "" : "请不要提及经期、月经、例假或女性健康相关内容。";
        const prompt = `页面已显示「${period}好，${this.userName || "朋友"}」，请直接生成一句承接语或引导语（不超过20字），不要重复问候语，语气温暖自然。${periodGuard}`;
        const userId = uni.getStorageSync("userId") || 1;
        const data = await request("/api/ai/chat", "POST", { userId, message: prompt, store: false });
        if (data && data.content) {
          this.aiGreeting = data.content;
          uni.setStorageSync(key, data.content);
        }
      } catch (err) {
        this.aiGreeting = "";
      } finally {
        this.aiLoading = false;
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
    maybeAutoSyncSteps() {
      if (this.syncLoading || !this.needsSteps) return;
      const today = new Date();
      const key = `${today.getFullYear()}-${today.getMonth() + 1}-${today.getDate()}`;
      if (uni.getStorageSync("stepsSyncDate") === key) return;
      this.syncSteps();
    },
    syncSteps() {
      this.syncLoading = true;
      // #ifdef MP-WEIXIN
      const token = uni.getStorageSync("token");
      const ensureLogin = token ? Promise.resolve() : new Promise((resolve, reject) => {
        uni.login({
          provider: "weixin",
          success: loginRes => {
            request("/api/auth/mini/login", "POST", { code: loginRes.code }).then(data => {
              if (data?.token) {
                uni.setStorageSync("token", data.token);
                if (data.userId) uni.setStorageSync("userId", data.userId);
              }
              resolve();
            }).catch(reject);
          },
          fail: reject
        });
      });
      ensureLogin.then(() => new Promise((resolve, reject) => {
        uni.login({ provider: "weixin", success: res => resolve(res.code), fail: reject });
      })).then(code => {
        uni.getWeRunData({
          success: werun => {
            request("/api/auth/mini/werun", "POST", { code, encryptedData: werun.encryptedData, iv: werun.iv }).then(data => {
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
            }).catch(err => {
              uni.showToast({ title: err.message || "同步失败", icon: "none" });
            }).finally(() => { this.syncLoading = false; });
          },
          fail: () => {
            uni.showToast({ title: "未授权微信运动", icon: "none" });
            this.syncLoading = false;
          }
        });
      }).catch(() => {
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
        case 1: return "步";
        case 2: return "小时";
        case 3: return "kcal";
        case 4: return "kg";
        default: return "";
      }
    },
    reminderTagLabel(type) {
      switch (Number(type)) {
        case REMINDER_TYPE.EXERCISE: return "运动";
        case REMINDER_TYPE.DIET: return "饮食";
        case REMINDER_TYPE.SLEEP: return "睡眠";
        default: return "提醒";
      }
    },
    goalLabel(goalType) {
      switch (Number(goalType)) {
        case 1: return "步数目标";
        case 2: return "睡眠目标";
        case 3: return "热量目标";
        case 4: return "体重目标";
        default: return "目标";
      }
    },
    formatReminderTime(value) {
      if (!value) return "未设置";
      if (typeof value === "string" && value.length <= 8 && value.includes(":")) return value;
      const date = new Date(value);
      if (isNaN(date.getTime())) return String(value);
      const pad = num => String(num).padStart(2, "0");
      return `${pad(date.getHours())}:${pad(date.getMinutes())}`;
    },
    parseReminderDate(value) {
      if (!value) return null;
      let normalized = String(value).replace("T", " ").trim();
      if (/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}$/.test(normalized)) {
        normalized += ":00";
      }
      const date = new Date(normalized.replace(/-/g, "/"));
      return Number.isNaN(date.getTime()) ? null : date;
    },
    isTodayReminder(value) {
      const date = this.parseReminderDate(value);
      if (!date) return false;
      const now = new Date();
      return (
        date.getFullYear() === now.getFullYear() &&
        date.getMonth() === now.getMonth() &&
        date.getDate() === now.getDate()
      );
    },
    updateTabBarSelected(index) {
      const pages = getCurrentPages();
      const page = pages[pages.length - 1];
      if (page && typeof page.getTabBar === "function") {
        const tabBar = page.getTabBar();
        if (tabBar && typeof tabBar.setData === "function") tabBar.setData({ selected: index });
      }
    },
    goAiWithQuery(query) {
      uni.setStorageSync("aiInitialMessage", query || "");
      uni.switchTab({ url: "/pages/ai/index" });
    },
    fabRecord(type) {
      this.fabOpen = false;
      this.$nextTick(() => this.openRecord(type));
    },
    openRecord(type) {
      uni.navigateTo({ url: `/pages/record/index?type=${type}&t=${Date.now()}` });
    }
  }
};
</script>

<style>
.page-root {
  min-height: 100vh;
  background: #FAF8F5;
}

.page {
  padding: 16px;
  padding-bottom: calc(80px + env(safe-area-inset-bottom));
  min-height: 100vh;
  background: #FAF8F5;
  color: #1a1c1a;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 顶栏 */
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 4px;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.greeting-text {
  font-size: 20px;
  font-weight: 700;
  color: #1a1c1a;
}

.greeting-sub {
  font-size: 12px;
  color: #564337;
  font-weight: 400;
}

.icon-btn {
  width: 38px;
  height: 38px;
  border-radius: var(--radius-card-sm);
  background: #fff;
  border: 1px solid #E9E1D8;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.icon-btn-img {
  width: 20px;
  height: 20px;
}


/* 指标卡 一行4个 */
.metrics-row {
  display: flex;
  gap: 10px;
}

.metric-mini {
  flex: 1;
  background: #fff;
  border: 1px solid #E9E1D8;
  border-radius: var(--radius-card);
  padding: 14px 10px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
  box-shadow: var(--shadow-card);
  transition: transform 120ms ease;
  overflow: hidden;
}

.metric-mini:active {
  transform: scale(0.98);
}

.metric-mini-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}

.metric-mini-icon {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-card-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.metric-mini-icon-steps { background: #FAF8F5; }
.metric-mini-icon-sleep { background: #FAF8F5; }
.metric-mini-icon-weight { background: #FAF8F5; }
.metric-mini-icon-diet { background: #FAF8F5; }

.metric-mini-icon-img {
  width: 18px;
  height: 18px;
}

.metric-diff-badge {
  border-radius: var(--radius-card);
  padding: 0px 3px;
}

.diff-up { background: #d1fae5; }
.diff-down { background: #fee2e2; }

.diff-badge-text {
  font-size: 8px;
  font-weight: 600;
}

.diff-up .diff-badge-text { color: #16a34a; }
.diff-down .diff-badge-text { color: #dc2626; }

.metric-mini-value {
  font-size: 20px;
  font-weight: 700;
  color: #1a1c1a;
  line-height: 1.1;
  white-space: nowrap;
  overflow: hidden;
}

.metric-mini-label {
  font-size: 11px;
  color: #564337;
  white-space: nowrap;
  overflow: hidden;
}

/* AI解读卡 */
.daily-ai-card {
  background: #fff;
  border: 1px solid #E9E1D8;
  border-radius: var(--radius-card);
  overflow: hidden;
  box-shadow: var(--shadow-card);
  transition: transform 120ms ease;
}

.daily-ai-card:active {
  transform: scale(0.98);
}

.daily-ai-head {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 16px 14px;
  background: linear-gradient(135deg, #FAF8F5 0%, #fff 60%);
  border-bottom: 1px solid #E9E1D8;
}

.daily-ai-icon-wrap {
  width: 34px;
  height: 34px;
  border-radius: var(--radius-card);
  background: linear-gradient(135deg, #A23F00 0%, #8B3500 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4px 10px rgba(162,63,0,0.3);
}

.daily-ai-icon-char {
  font-size: 15px;
  font-weight: 800;
  color: #fff;
  line-height: 1;
}

.daily-ai-icon-img {
  width: 18px;
  height: 18px;
}

.daily-ai-title-block {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.daily-ai-title {
  font-size: 14px;
  font-weight: 700;
  color: #1a1c1a;
}

.daily-ai-subtitle {
  font-size: 10px;
  color: #564337;
}

.daily-ai-refresh-btn {
  background: #fff;
  border: 1px solid #E9E1D8;
  border-radius: var(--radius-pill);
  padding: 4px 10px;
}

.daily-ai-refresh-text {
  font-size: 11px;
  color: #A23F00;
  font-weight: 500;
}

.daily-ai-body {
  padding: 16px;
  min-height: 60px;
  display: flex;
  align-items: flex-start;
}

.daily-ai-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  width: 100%;
}

.loading-dots {
  display: flex;
  gap: 5px;
  align-items: center;
}

.loading-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #A23F00;
  animation: dotBounce 0.6s ease-in-out infinite;
}

.loading-dot:nth-child(2) { animation-delay: 0.12s; }
.loading-dot:nth-child(3) { animation-delay: 0.24s; }

@keyframes dotBounce {
  0%, 60% { transform: translateY(0) scale(1); opacity: 0.4; }
  30% { transform: translateY(-5px) scale(1.2); opacity: 1; }
}

.daily-ai-loading-text {
  font-size: 12px;
  color: #564337;
}

.daily-ai-text {
  font-size: 13px;
  color: #334155;
  line-height: 1.8;
  display: block;
  flex: 1;
}

.daily-ai-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 6px 0;
}

.daily-ai-empty-icon {
  font-size: 14px;
  color: #A23F00;
}

.daily-ai-empty-text {
  font-size: 13px;
  color: #564337;
}

/* 快捷入口 */
.quick-entrance-card {
  background: #fff;
  border: 1px solid #E9E1D8;
  border-radius: var(--radius-card);
  overflow: hidden;
  box-shadow: var(--shadow-card);
}

.quick-entrance-head {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 16px 14px;
  border-bottom: 1px solid #E9E1D8;
}

.quick-entrance-icon-wrap {
  width: 34px;
  height: 34px;
  border-radius: var(--radius-card);
  background: linear-gradient(135deg, #A23F00 0%, #8B3500 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4px 10px rgba(162,63,0,0.28);
}

.quick-entrance-icon-char {
  font-size: 15px;
  font-weight: 800;
  color: #fff;
  line-height: 1;
}

.quick-entrance-head-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.quick-entrance-title {
  font-size: 14px;
  font-weight: 700;
  color: #1a1c1a;
}

.quick-entrance-sub {
  font-size: 10px;
  color: #564337;
}

.quick-entrance-row {
  display: flex;
  padding: 14px 16px 16px;
  gap: 10px;
}

.quick-entrance-item {
  flex: 1;
  min-width: 0;
  background: #FAF8F5;
  border: 1px solid #E9E1D8;
  border-radius: var(--radius-card);
  padding: 14px 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  transition: transform 120ms ease;
}

.quick-entrance-item:active {
  transform: scale(0.98);
}

.quick-entrance-icon {
  width: 28px;
  height: 28px;
}

.quick-entrance-text {
  font-size: 12px;
  color: #475569;
  font-weight: 500;
  white-space: nowrap;
}

/* 本周打卡卡片（已移除，样式保留空壳） */

/* AI引导卡 */
.ai-guide-card {
  background: #FAF8F5;
  border: 1px solid #E9E1D8;
  border-radius: var(--radius-card);
  overflow: hidden;
  margin-top: 8px;
  transition: transform 120ms ease;
}

.ai-guide-card:active {
  transform: scale(0.98);
}

.ai-guide-head {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 13px 14px 12px;
  border-bottom: 1px solid #E9E1D8;
}

.ai-guide-icon-wrap {
  width: 34px;
  height: 34px;
  border-radius: var(--radius-card);
  background: linear-gradient(135deg, #A23F00 0%, #8B3500 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4px 10px rgba(162,63,0,0.28);
}

.ai-guide-icon-char {
  font-size: 15px;
  font-weight: 800;
  color: #fff;
  line-height: 1;
}

.ai-guide-head-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.ai-guide-title {
  font-size: 14px;
  font-weight: 700;
  color: #1a1c1a;
}

.ai-guide-sub {
  font-size: 10px;
  color: #564337;
}

.ai-guide-go-btn {
  background: #FAF8F5;
  border: 1px solid #E9E1D8;
  border-radius: var(--radius-pill);
  padding: 4px 10px;
}

.ai-guide-go-text {
  font-size: 11px;
  color: #A23F00;
  font-weight: 600;
}

.ai-guide-scenes {
  display: flex;
  padding: 12px 14px 14px;
  gap: 10px;
}

.ai-guide-scene {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 7px;
}

.ai-scene-icon-wrap {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-card);
  display: flex;
  align-items: center;
  justify-content: center;
}

.ai-scene-icon-diet   { background: #FAF8F5; border: 1px solid #E9E1D8; }
.ai-scene-icon-sleep  { background: #FAF8F5; border: 1px solid #E9E1D8; }
.ai-scene-icon-sport  { background: #FAF8F5; border: 1px solid #E9E1D8; }
.ai-scene-icon-weight { background: #FAF8F5; border: 1px solid #E9E1D8; }

.ai-scene-emoji {
  font-size: 20px;
  line-height: 1;
}

.ai-scene-label {
  font-size: 11px;
  color: #475569;
  font-weight: 500;
}

/* section 通用 */
.section-card {
  background: #fff;
  border: 1px solid #E9E1D8;
  border-radius: var(--radius-card);
  overflow: hidden;
  box-shadow: var(--shadow-card);
  transition: transform 120ms ease;
}

.section-card:active {
  transform: scale(0.98);
}

.section-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid #E9E1D8;
}

/* 今日待办 Tab 样式 */
.todo-tabs {
  display: flex;
  flex: 1;
  gap: 16rpx;
}

.todo-tab {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  padding: 10rpx 24rpx;
  border-radius: var(--radius-pill);
  background: #F5F3F0;
  transition: all 0.2s;
}

.todo-tab.active {
  background: #A23F00;
}

.todo-tab-text {
  font-size: 26rpx;
  color: #5C5C5C;
  font-weight: 600;
}

.todo-tab.active .todo-tab-text {
  color: #FFFFFF;
}

.todo-tab-badge {
  font-size: 20rpx;
  color: #FFFFFF;
  background: rgba(0,0,0,0.15);
  padding: 2rpx 12rpx;
  border-radius: 16rpx;
  min-width: 32rpx;
  text-align: center;
}

.todo-tab.active .todo-tab-badge {
  background: rgba(255,255,255,0.3);
}

.todo-empty {
  padding: 40rpx 0;
  text-align: center;
}

.todo-empty-text {
  font-size: 26rpx;
  color: #999;
}

.todo-more {
  display: block;
  text-align: center;
  padding: 20rpx 0;
  font-size: 24rpx;
  color: #A23F00;
  border-top: 1px solid #E9E1D8;
  margin-top: 12rpx;
}

.section-title-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-icon {
  width: 26px;
  height: 26px;
  border-radius: var(--radius-card);
  display: flex;
  align-items: center;
  justify-content: center;
}

.section-icon-reminder { background: #FAF8F5; }
.section-icon-goal { background: #FAF8F5; }

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #1a1c1a;
}

.section-link {
  font-size: 12px;
  color: #564337;
}

.section-card-body {
  padding: 12px 14px 14px;
}

.list-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.list-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 14px;
  background: #FAF8F5;
  border-radius: var(--radius-card);
  border: 1px solid #E9E1D8;
}

.list-item-main {
  flex: 1;
  min-width: 0;
}

.list-item-title {
  font-size: 13px;
  font-weight: 600;
  color: #1a1c1a;
  display: block;
}

.list-item-meta {
  font-size: 11px;
  color: #564337;
  margin-top: 2px;
  display: block;
}

.list-item-tag {
  font-size: 11px;
  font-weight: 600;
  padding: 3px 8px;
  border-radius: var(--radius-pill);
  margin-left: 10px;
  flex-shrink: 0;
}

.list-item-tag-reminder { color: #A23F00; background: #FAF8F5; }
.list-item-tag-goal { color: #A23F00; background: #FAF8F5; }

/* 目标进度条 */
.goal-progress-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 4px;
}

.goal-progress-bar {
  flex: 1;
  height: 4px;
  background: #E9E1D8;
  border-radius: var(--radius-pill);
  overflow: hidden;
}

.goal-progress-fill {
  height: 4px;
  background: linear-gradient(90deg, #A23F00, #8B3500);
  border-radius: var(--radius-pill);
  transition: width 0.3s;
}

.goal-progress-num {
  font-size: 10px;
  color: #564337;
  flex-shrink: 0;
}

.goal-status-badge {
  padding: 3px 8px;
  border-radius: var(--radius-pill);
  margin-left: 10px;
  flex-shrink: 0;
}

.goal-badge-done { background: #d1fae5; }
.goal-badge-going { background: #faf9f6; }

.goal-status-text {
  font-size: 11px;
  font-weight: 600;
  color: #059669;
}

.goal-badge-going .goal-status-text {
  color: #564337;
}

.icon-img {
  width: 14px;
  height: 14px;
}

/* 遮罩 */
.fab-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.2);
  z-index: 99;
}


/* +记录 固定按钮 */
.add-record-wrap {
  position: fixed;
  left: 0;
  top: 0;
  z-index: 10000;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
}

.add-record-menu {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.add-record-item {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #fff;
  border: 1px solid #E9E1D8;
  border-radius: var(--radius-pill);
  padding: 8px 16px 8px 10px;
  box-shadow: var(--shadow-card);
}

.add-record-icon {
  width: 30px;
  height: 30px;
  border-radius: var(--radius-card);
  display: flex;
  align-items: center;
  justify-content: center;
}

.add-record-icon-weight { background: #FAF8F5; }
.add-record-icon-diet   { background: #FAF8F5; }
.add-record-icon-sleep  { background: #FAF8F5; }

.add-record-icon-img {
  width: 16px;
  height: 16px;
}

.add-record-label {
  font-size: 13px;
  font-weight: 600;
  color: #1a1c1a;
}

.add-record-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  background: linear-gradient(135deg, #A23F00 0%, #8B3500 100%);
  border-radius: var(--radius-pill);
  padding: 11px 20px;
  box-shadow: 0 6rpx 16rpx rgba(162,63,0,0.4);
}

.add-record-btn-open {
  background: linear-gradient(135deg, #A23F00 0%, #8B3500 100%);
  box-shadow: 0 6rpx 16rpx rgba(162,63,0,0.3);
}

.add-record-btn-icon {
  font-size: 18px;
  font-weight: 700;
  color: #fff;
  line-height: 1;
}

.add-record-btn-text {
  font-size: 14px;
  font-weight: 700;
  color: #fff;
}

.status {
  text-align: center;
  color: #64748b;
  font-size: 12px;
}

.status.error { color: #ef4444; }
</style>
