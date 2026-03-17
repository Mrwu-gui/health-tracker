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

      <!-- 健康评分卡 - 紧凑型 -->
      <view class="score-card" @tap="openScoreRule">
        <view class="score-card-content">
          <view class="score-info">
            <text class="score-label">今日评分</text>
            <view class="score-main-row">
              <text class="score-num">{{ healthScore }}</text>
              <view class="score-badge" :class="scoreBadgeClass">{{ scoreStatus }}</view>
            </view>
          </view>
          <view class="score-rule-btn">
            <text class="score-rule-text">评分规则</text>
            <text class="score-rule-arrow">›</text>
          </view>
        </view>
      </view>

      <!-- 核心指标 一行4个 -->
      <view class="metrics-row">

        <!-- 步数 -->
        <view class="metric-mini metric-mini-steps">
          <view class="metric-mini-top">
            <view class="metric-mini-icon metric-mini-icon-steps">
              <image class="metric-mini-icon-img" src="/static/tabbar/sport.png" mode="aspectFit" />
            </view>
            <view v-if="stepsDelta !== 0" class="metric-diff-badge" :class="stepsDeltaIsUp ? 'diff-up' : 'diff-down'">
              <text class="diff-badge-text">{{ stepsDeltaIsUp ? '↑' : '↓' }}{{ Math.abs(stepsDelta) > 999 ? (Math.abs(stepsDelta)/1000).toFixed(1)+'k' : Math.abs(stepsDelta) }}</text>
            </view>
          </view>
          <text class="metric-mini-value">{{ stepsNum > 999 ? (stepsNum/1000).toFixed(1)+'k' : stepsNum }}</text>
          <text class="metric-mini-label">步数</text>
        </view>

        <!-- 睡眠 -->
        <view class="metric-mini metric-mini-sleep" @tap="openRecord('sleep')">
          <view class="metric-mini-top">
            <view class="metric-mini-icon metric-mini-icon-sleep">
              <image class="metric-mini-icon-img" src="/static/tabbar/sleep.png" mode="aspectFit" />
            </view>
            <view v-if="sleepDeltaMinutes !== 0" class="metric-diff-badge" :class="sleepDeltaMinutes > 0 ? 'diff-up' : 'diff-down'">
              <text class="diff-badge-text">{{ sleepDeltaMinutes > 0 ? '↑' : '↓' }}{{ Math.abs(sleepDiffHours) }}h</text>
            </view>
          </view>
          <text class="metric-mini-value">{{ sleepDisplay }}</text>
          <text class="metric-mini-label">睡眠</text>
        </view>

        <!-- 体重 -->
        <view class="metric-mini metric-mini-weight" @tap="openRecord('weight')">
          <view class="metric-mini-top">
            <view class="metric-mini-icon metric-mini-icon-weight">
              <image class="metric-mini-icon-img" src="/static/tabbar/weight.png" mode="aspectFit" />
            </view>
            <view v-if="weightDelta !== 0 && overview.weight" class="metric-diff-badge" :class="weightDelta > 0 ? 'diff-up' : 'diff-down'">
              <text class="diff-badge-text">{{ weightDelta > 0 ? '↑' : '↓' }}{{ Math.abs(weightDelta).toFixed(1) }}</text>
            </view>
          </view>
          <text class="metric-mini-value">{{ overview.weight ? overview.weight : '--' }}</text>
          <text class="metric-mini-label">体重(kg)</text>
        </view>

        <!-- 饮食 -->
        <view class="metric-mini metric-mini-diet" @tap="openRecord('diet')">
          <view class="metric-mini-top">
            <view class="metric-mini-icon metric-mini-icon-diet">
              <image class="metric-mini-icon-img" src="/static/tabbar/food.png" mode="aspectFit" />
            </view>
            <view v-if="dietDeltaCalories !== 0 && dietCaloriesToday > 0" class="metric-diff-badge" :class="dietDeltaCalories > 0 ? 'diff-up' : 'diff-down'">
              <text class="diff-badge-text">{{ dietDeltaCalories > 0 ? '↑' : '↓' }}{{ Math.abs(dietDeltaCalories) > 999 ? (Math.abs(dietDeltaCalories)/1000).toFixed(1)+'k' : Math.abs(dietDeltaCalories) }}</text>
            </view>
          </view>
          <text class="metric-mini-value">{{ dietCaloriesToday > 0 ? dietCaloriesToday : '--' }}</text>
          <text class="metric-mini-label">热量(kcal)</text>
        </view>

      </view>

      <!-- 今日AI健康解读 -->
      <view class="daily-ai-card">
        <view class="daily-ai-head">
          <view class="daily-ai-title-block">
            <text class="daily-ai-title">看看智康怎么说</text>
          </view>
          <view v-if="!dailyAiLoading && dailyAiText" class="daily-ai-refresh-btn" @tap="fetchDailyAi(true)">
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
      <view class="quick-entrance-card">
        <view class="quick-entrance-head">
          <view class="quick-entrance-head-info">
            <text class="quick-entrance-title">和智康聊聊</text>
          </view>
        </view>
        <view class="quick-entrance-row">
          <view class="quick-entrance-item" @tap="goAiWithQuery('今天吃什么比较健康？')">
            <image class="quick-entrance-icon" src="/static/tabbar/food2.png" mode="aspectFit" />
            <text class="quick-entrance-text">今天吃什么？</text>
          </view>
          <view class="quick-entrance-item" @tap="goAiWithQuery('给我一些适合今天的运动建议')">
            <image class="quick-entrance-icon" src="/static/tabbar/sport.png" mode="aspectFit" />
            <text class="quick-entrance-text">动一动！</text>
          </view>
          <view class="quick-entrance-item" @tap="goAiWithQuery('如何改善睡眠质量？')">
            <image class="quick-entrance-icon" src="/static/tabbar/sleep.png" mode="aspectFit" />
            <text class="quick-entrance-text">睡个好觉</text>
          </view>
        </view>
      </view>

      <!-- 今日提醒 -->
      <view class="section-card" v-if="reminders.length > 0">
        <view class="section-card-head">
          <view class="section-title-wrap">
            <view class="section-icon section-icon-reminder">
              <image class="icon-img" src="/static/tabbar/remind.png" mode="widthFix" />
            </view>
            <text class="section-title">今日提醒</text>
          </view>
          <navigator class="section-link" url="/pages/reminders/index">全部 ›</navigator>
        </view>
        <view class="section-card-body">
          <view class="list-body">
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
      <view class="section-card" v-if="todayGoals.length > 0">
        <view class="section-card-head">
          <view class="section-title-wrap">
            <view class="section-icon section-icon-goal">
              <image class="icon-img" src="/static/tabbar/mubiao.png" mode="widthFix" />
            </view>
            <text class="section-title">今日目标</text>
          </view>
          <navigator class="section-link" url="/pages/goal/index">查看 ›</navigator>
        </view>
        <view class="section-card-body">
          <view class="list-body">
            <view v-for="item in todayGoals" :key="item.id" class="list-item">
              <view class="list-item-main">
                <text class="list-item-title">{{ item.goalLabel }}</text>
                <view class="goal-progress-row">
                  <view class="goal-progress-bar">
                    <view class="goal-progress-fill" :style="{ width: item.progress + '%' }"></view>
                  </view>
                  <text class="goal-progress-num">{{ item.progress }}%</text>
                </view>
              </view>
              <view class="goal-status-badge" :class="item.progress >= 100 ? 'goal-badge-done' : 'goal-badge-going'">
                <text class="goal-status-text">{{ item.progress >= 100 ? '达成' : '进行中' }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <text v-if="error" class="status error">{{ error }}</text>
    </view>

    <!-- 遮罩（+记录展开时） -->
    <view v-if="fabOpen" class="fab-mask" @tap="fabOpen = false"></view>

    <!-- 评分规则弹窗 -->
    <view v-if="scoreRuleOpen" class="rule-mask" @tap="scoreRuleOpen = false"></view>
    <view v-if="scoreRuleOpen" class="rule-sheet">
      <view class="rule-head">
        <text class="rule-title">健康评分说明</text>
        <view class="rule-close-btn" @tap="scoreRuleOpen = false">
          <text class="rule-close">✕</text>
        </view>
      </view>
      <view class="rule-body">
        <text class="rule-summary">{{ scoreRule.summary || "评分由步数、睡眠、饮食、体重四项综合计算得出，满分100分" }}</text>
        <view class="rule-items-grid">
          <view v-for="(item, idx) in scoreRule.items" :key="idx" class="rule-grid-item">
            <view class="rule-grid-icon" :style="{ background: ruleItemColor(idx) }">
              <image class="rule-grid-icon-img" :src="ruleItemIcon(idx)" mode="aspectFit" />
            </view>
            <text class="rule-grid-title">{{ item.label }}</text>
            <text class="rule-grid-desc">{{ item.desc }}</text>
          </view>
        </view>
        <view v-if="showScoreTips" class="rule-tips">
          <text class="rule-tips-title">加分项</text>
          <text v-for="(tip, idx) in scoreRule.tips" :key="idx" class="rule-tip">• {{ tip }}</text>
        </view>
      </view>
    </view>

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
      healthScore: 0,
      scoreDiffText: "",
      scoreRuleOpen: false,
      scoreRule: {
        summary: "",
        items: [],
        tips: []
      },
      scoreBreakdown: {},
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
      weightYesterday: 0
    };
  },
  computed: {
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
    scoreStatus() {
      if (this.healthScore >= 90) return "优秀";
      if (this.healthScore >= 75) return "良好";
      if (this.healthScore >= 60) return "一般";
      return "较差";
    },
    scoreBadgeClass() {
      if (this.healthScore >= 75) return "badge-green";
      if (this.healthScore >= 60) return "badge-amber";
      return "badge-red";
    },
    scoreDiff() {
      return this.scoreDiffText || `综合评估 · 状态${this.scoreStatus}`;
    },
    scoreArcStyle() {
      const deg = Math.round(this.healthScore * 3.6);
      return {
        background: `conic-gradient(#f97316 ${deg}deg, transparent ${deg}deg)`
      };
    },
    scoreBreakdownItems() {
      const bd = this.scoreBreakdown || {};
      const items = [
        { label: "步数", value: bd.steps || 0, max: 30, color: "#f97316" },
        { label: "睡眠", value: bd.sleep || 0, max: 25, color: "#6366f1" },
        { label: "饮食", value: bd.diet || 0, max: 25, color: "#10b981" },
        { label: "体重", value: bd.weight || 0, max: 20, color: "#f59e0b" }
      ];
      return items.map(i => ({ ...i, pct: i.max > 0 ? Math.min(100, Math.round((i.value / i.max) * 100)) : 0 }));
    },
    showScoreTips() {
      if (!this.scoreRule || !Array.isArray(this.scoreRule.tips)) return false;
      const cleaned = this.scoreRule.tips.filter(t => t && !/打卡|连续/i.test(t));
      return cleaned.length > 0;
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
    openScoreRule() {
      this.scoreRuleOpen = true;
      if (!this.scoreRule.items.length) this.fetchScoreRule();
    },
    ruleItemIcon(idx) {
      return [
        "/static/tabbar/sport.png",
        "/static/tabbar/sleep.png",
        "/static/tabbar/food.png",
        "/static/tabbar/weight.png"
      ][idx] || "/static/tabbar/all.png";
    },
    ruleItemColor(idx) {
      return ["#fff7ed", "#eff6ff", "#f0fdf4", "#fefce8"][idx] || "#f5f1eb";
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
        this.reminders = raw.filter(item => Number(item.type) !== 4).slice(0, 3).map(item => ({
          id: item.id,
          title: item.title,
          content: item.content || "提醒事项",
          time: this.formatReminderTime(item.remindTime),
          typeLabel: this.reminderTagLabel(item.type)
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
        await this.fetchHealthScore();
        this.fetchDailyAi();
      }
    },
    async fetchScoreRule() {
      try {
        const data = await request("/api/score/rule", "GET", {});
        if (data) {
          const rawTips = Array.isArray(data.tips) ? data.tips : [];
          const tips = rawTips.filter(t => t && !/打卡|连续/i.test(t));
          this.scoreRule = { ...data, tips };
        }
      } catch (err) {
        this.scoreRule = {
          summary: "评分由步数、睡眠、饮食、体重四项综合计算，满分100分",
          items: [
            { label: "步数/运动", desc: "按目标完成度，最高30分" },
            { label: "睡眠质量", desc: "7~9小时最佳，最高25分" },
            { label: "饮食记录", desc: "规律进餐得分更高，最高25分" },
            { label: "体重管理", desc: "稳定或接近目标，最高20分" }
          ],
          tips: []
        };
      }
    },
    async fetchHealthScore() {
      const userId = uni.getStorageSync("userId") || 1;
      try {
        const data = await request("/api/score/today", "GET", { userId });
        if (data) {
          this.healthScore = data.score || this.healthScore;
          this.scoreDiffText = data.diffText || this.scoreDiffText;
          this.scoreBreakdown = data.breakdown || {};
        }
      } catch (err) { this.scoreDiffText = ""; }
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
        case 1: return "运动";
        case 2: return "饮食";
        case 3: return "睡眠";
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
  background: #faf8f5;
}

.page {
  padding: 16px;
  padding-bottom: calc(80px + env(safe-area-inset-bottom));
  min-height: 100vh;
  background: #faf8f5;
  color: #0f172a;
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
  color: #0f172a;
}

.greeting-sub {
  font-size: 12px;
  color: #94a3b8;
  font-weight: 400;
}

.icon-btn {
  width: 38px;
  height: 38px;
  border-radius: 19px;
  background: #fff;
  border: 1px solid #e8e2db;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-btn-img {
  width: 20px;
  height: 20px;
}

/* 健康评分卡 - 紧凑型 */
.score-card {
  background: #fff;
  border-radius: 16px;
  border: 1px solid #e8e2db;
  overflow: hidden;
}

.score-card-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
}

.score-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.score-label {
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
}

.score-main-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.score-num {
  font-size: 32px;
  font-weight: 800;
  color: #f97316;
  line-height: 1;
}

.score-badge {
  font-size: 10px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 999px;
}

.badge-green { background: #d1fae5; color: #059669; }
.badge-amber { background: #fef3c7; color: #d97706; }
.badge-red { background: #fee2e2; color: #dc2626; }

.score-rule-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background: #fff7ed;
  border-radius: 999px;
}

.score-rule-text {
  font-size: 12px;
  color: #f97316;
  font-weight: 500;
}

.score-rule-arrow {
  font-size: 14px;
  color: #f97316;
}

/* 指标卡 一行4个 */
.metrics-row {
  display: flex;
  gap: 10px;
}

.metric-mini {
  flex: 1;
  background: #fff;
  border: 1px solid #e8e2db;
  border-radius: 16px;
  padding: 14px 10px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
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
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.metric-mini-icon-steps { background: #fff7ed; }
.metric-mini-icon-sleep { background: #eff6ff; }
.metric-mini-icon-weight { background: #f0fdf4; }
.metric-mini-icon-diet { background: #fefce8; }

.metric-mini-icon-img {
  width: 18px;
  height: 18px;
}

.metric-diff-badge {
  border-radius: 4px;
  padding: 0px 3px;
}

.diff-up { background: #dcfce7; }
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
  color: #0f172a;
  line-height: 1.1;
  white-space: nowrap;
  overflow: hidden;
}

.metric-mini-label {
  font-size: 11px;
  color: #94a3b8;
  white-space: nowrap;
  overflow: hidden;
}

/* AI解读卡 */
.daily-ai-card {
  background: #fff;
  border: 1px solid #fed7aa;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(249,115,22,0.06);
}

.daily-ai-head {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 16px 14px;
  background: linear-gradient(135deg, #fff7ed 0%, #fff 60%);
  border-bottom: 1px solid #fff7ed;
}

.daily-ai-icon-wrap {
  width: 34px;
  height: 34px;
  border-radius: 12px;
  background: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4px 10px rgba(249,115,22,0.3);
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
  color: #0f172a;
}

.daily-ai-subtitle {
  font-size: 10px;
  color: #94a3b8;
}

.daily-ai-refresh-btn {
  background: #fff;
  border: 1px solid #fed7aa;
  border-radius: 999px;
  padding: 4px 10px;
}

.daily-ai-refresh-text {
  font-size: 11px;
  color: #f97316;
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
  background: #f97316;
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
  color: #94a3b8;
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
  color: #f97316;
}

.daily-ai-empty-text {
  font-size: 13px;
  color: #94a3b8;
}

/* 快捷入口 */
.quick-entrance-card {
  background: #fff;
  border: 1px solid #e8e2db;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.quick-entrance-head {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 16px 14px;
  border-bottom: 1px solid #f5f1eb;
}

.quick-entrance-icon-wrap {
  width: 34px;
  height: 34px;
  border-radius: 12px;
  background: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4px 10px rgba(249,115,22,0.28);
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
  color: #0f172a;
}

.quick-entrance-sub {
  font-size: 10px;
  color: #94a3b8;
}

.quick-entrance-row {
  display: flex;
  padding: 14px 16px 16px;
  gap: 10px;
}

.quick-entrance-item {
  flex: 1;
  min-width: 0;
  background: #faf8f5;
  border: 1px solid #f0ece6;
  border-radius: 14px;
  padding: 14px 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
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
  background: #fff7ed;
  border: 1px solid #f3d9c2;
  border-radius: 20px;
  overflow: hidden;
  margin-top: 8px;
}

.ai-guide-head {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 13px 14px 12px;
  border-bottom: 1px solid #f5f1eb;
}

.ai-guide-icon-wrap {
  width: 34px;
  height: 34px;
  border-radius: 12px;
  background: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4px 10px rgba(249,115,22,0.28);
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
  color: #0f172a;
}

.ai-guide-sub {
  font-size: 10px;
  color: #94a3b8;
}

.ai-guide-go-btn {
  background: #fff7ed;
  border: 1px solid #fed7aa;
  border-radius: 999px;
  padding: 4px 10px;
}

.ai-guide-go-text {
  font-size: 11px;
  color: #f97316;
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
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ai-scene-icon-diet   { background: #fff7ed; border: 1px solid #fed7aa; }
.ai-scene-icon-sleep  { background: #eff6ff; border: 1px solid #bfdbfe; }
.ai-scene-icon-sport  { background: #f0fdf4; border: 1px solid #bbf7d0; }
.ai-scene-icon-weight { background: #fefce8; border: 1px solid #fde68a; }

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
  border: 1px solid #e8e2db;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.section-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 16px 14px;
  border-bottom: 1px solid #f5f1eb;
}

.section-title-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-icon {
  width: 26px;
  height: 26px;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.section-icon-reminder { background: #fef3c7; }
.section-icon-goal { background: #d1fae5; }

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
}

.section-link {
  font-size: 12px;
  color: #94a3b8;
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
  background: #fefcf9;
  border-radius: 14px;
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
  color: #94a3b8;
  margin-top: 2px;
  display: block;
}

.list-item-tag {
  font-size: 11px;
  font-weight: 600;
  padding: 3px 8px;
  border-radius: 999px;
  margin-left: 10px;
  flex-shrink: 0;
}

.list-item-tag-reminder { color: #b45309; background: #fef3c7; }
.list-item-tag-goal { color: #059669; background: #d1fae5; }

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
  background: #f5f1eb;
  border-radius: 999px;
  overflow: hidden;
}

.goal-progress-fill {
  height: 4px;
  background: linear-gradient(90deg, #f97316, #ea580c);
  border-radius: 999px;
  transition: width 0.3s;
}

.goal-progress-num {
  font-size: 10px;
  color: #94a3b8;
  flex-shrink: 0;
}

.goal-status-badge {
  padding: 3px 8px;
  border-radius: 999px;
  margin-left: 10px;
  flex-shrink: 0;
}

.goal-badge-done { background: #d1fae5; }
.goal-badge-going { background: #f5f1eb; }

.goal-status-text {
  font-size: 11px;
  font-weight: 600;
  color: #059669;
}

.goal-badge-going .goal-status-text {
  color: #94a3b8;
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

/* 评分规则弹窗 */
.rule-mask {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.4);
  z-index: 98;
}

.rule-sheet {
  position: fixed;
  left: 16px;
  right: 16px;
  bottom: 20px;
  background: #fff;
  border-radius: 20px;
  z-index: 99;
  overflow: hidden;
  border: 1px solid #e8e2db;
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.14);
}

.rule-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px 16px;
  border-bottom: 1px solid #f5f1eb;
}

.rule-title {
  font-size: 15px;
  font-weight: 700;
  color: #0f172a;
}

.rule-close-btn {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #f5f1eb;
  display: flex;
  align-items: center;
  justify-content: center;
}

.rule-close {
  font-size: 12px;
  color: #64748b;
}

.rule-body {
  padding: 14px 16px 18px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.rule-summary {
  font-size: 12px;
  color: #64748b;
  line-height: 1.6;
}

.rule-items-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.rule-grid-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
  padding: 12px 12px;
  background: #fefcf9;
  border-radius: 14px;
  border: 1px solid #f0ece6;
}

.rule-grid-icon {
  width: 30px;
  height: 30px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.rule-grid-icon-img {
  width: 18px;
  height: 18px;
  display: block;
}

.rule-grid-title {
  font-size: 12px;
  font-weight: 600;
  color: #0f172a;
}

.rule-grid-desc {
  font-size: 11px;
  color: #64748b;
  line-height: 1.4;
}

.rule-tips {
  background: #fff7ed;
  border-radius: 12px;
  padding: 10px 12px;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.rule-tips-title {
  font-size: 11px;
  font-weight: 700;
  color: #b45309;
  margin-bottom: 2px;
}

.rule-tip {
  font-size: 11px;
  color: #b45309;
  line-height: 1.5;
}

/* +记录 固定按钮 */
.add-record-wrap {
  position: fixed;
  left: 0;
  top: 0;
  z-index: 100;
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
  border: 1px solid #e8e2db;
  border-radius: 999px;
  padding: 8px 16px 8px 10px;
  box-shadow: 0 4px 14px rgba(15,23,42,0.1);
}

.add-record-icon {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.add-record-icon-weight { background: #f0fdf4; }
.add-record-icon-diet   { background: #fff7ed; }
.add-record-icon-sleep  { background: #eff6ff; }

.add-record-icon-img {
  width: 16px;
  height: 16px;
}

.add-record-label {
  font-size: 13px;
  font-weight: 600;
  color: #0f172a;
}

.add-record-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  background: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
  border-radius: 999px;
  padding: 11px 20px;
  box-shadow: 0 4px 16px rgba(249,115,22,0.4);
}

.add-record-btn-open {
  background: linear-gradient(135deg, #64748b 0%, #475569 100%);
  box-shadow: 0 4px 16px rgba(100,116,139,0.3);
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
