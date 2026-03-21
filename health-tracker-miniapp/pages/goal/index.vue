<template>
  <view class="page-root">
    <!-- 空状态页面 -->
    <view v-if="goals.length === 0" class="empty-page">
      <view class="empty-content">
        <view class="empty-illustration">
          <image class="empty-illustration-icon" src="/static/tabbar/trace_white.png" mode="aspectFit" />
        </view>
        <text class="empty-desc">还没有设定健康目标，AI已为您准备了多项推荐方案，快来开启挑战。</text>

        <view class="empty-features">
          <view class="feature-item">
            <image class="feature-icon" src="/static/tabbar/sport_s.png" mode="aspectFit" />
            <text class="feature-text">运动目标</text>
          </view>
          <view class="feature-item">
            <image class="feature-icon" src="/static/tabbar/weight_s.png" mode="aspectFit" />
            <text class="feature-text">体重管理</text>
          </view>
          <view class="feature-item">
            <image class="feature-icon" src="/static/tabbar/sleep_s.png" mode="aspectFit" />
            <text class="feature-text">睡眠追踪</text>
          </view>
		  <view class="feature-item">
		    <image class="feature-icon" src="/static/tabbar/water_s.png" mode="aspectFit" />
		    <text class="feature-text">饮水目标</text>
		  </view>
        </view>

        <button class="empty-add-btn pill" @tap="openAdd">
          <image class="empty-add-icon" src="/static/tabbar/add.png" mode="aspectFit" />
          <text class="empty-add-text">添加第一个目标</text>
        </button>
      </view>
    </view>

    <!-- 有数据时的页面 -->
    <view v-else class="page">
      <!-- Dashboard Overview Card -->
      <section class="overview-section">
        <view class="overview-card card">
          <!-- Background decorative element -->
          <view class="overview-decor"></view>
          <view class="overview-content">
            <text class="overview-label">当前目标进度总览</text>
            <view class="overview-score">
              <text class="score-number">{{ overallProgress }}</text>
              <text class="score-percent">%</text>
            </view>
            <view class="overview-stats">
              <text class="stats-label">整体完成度</text>
              <text class="stats-value">{{ doneCount }} / {{ totalCount }} 目标</text>
            </view>
            <view class="overview-bar">
              <view class="overview-bar-fill" :style="{ width: `${overallProgress}%` }"></view>
            </view>
            <view class="overview-detail">
              <view class="detail-item card-sm ongoing">
                <text class="detail-label">进行中</text>
                <text class="detail-value">{{ ongoingCount }}</text>
              </view>
              <view class="detail-item card-sm done">
                <text class="detail-label">已完成</text>
                <text class="detail-value">{{ doneCount }}</text>
              </view>
              <view class="detail-item card-sm abandoned">
                <text class="detail-label">已放弃</text>
                <text class="detail-value">{{ abandonedCount }}</text>
              </view>
              <view class="detail-item card-sm expired">
                <text class="detail-label">已过期</text>
                <text class="detail-value">{{ expiredCount }}</text>
              </view>
            </view>
          </view>
        </view>
      </section>

      <!-- Goals List Header -->
      <view class="list-header">
        <text class="list-title">我的健康目标</text>
        <text class="list-subtitle">进行中</text>
      </view>

      <!-- Goals List -->
      <view class="goals-list">
        <view v-for="item in goals" :key="item.id" class="goal-card card" :class="{ 'goal-card-disabled': item.status === 3 || isGoalExpired(item) }" @tap="openEdit(item)">
          <view class="goal-header">
            <view class="goal-icon-wrapper">
              <image class="goal-icon-img" :src="getGoalIcon(item.goalType)" mode="aspectFit" />
            </view>
            <view class="goal-meta">
              <view class="goal-title-row">
                <text class="goal-name">{{ item.goalLabel }}</text>
                <text class="goal-daily-tag">{{ item.periodLabel }}</text>
              </view>
              <text class="goal-progress-text">{{ item.currentValue || 0 }} / {{ item.targetValue }}{{ goalUnitSuffix(item.goalType) }}</text>
            </view>
          </view>

          <view class="goal-progress" v-if="item.status !== 3 && !isGoalExpired(item)">
            <view class="progress-bar">
              <view class="progress-fill" :class="'progress-fill-' + item.goalType" :style="{ width: `${item.progress}%` }"></view>
            </view>
            <view class="progress-info">
              <text class="progress-status">{{ getProgressStatus(item.progress) }}</text>
              <text class="progress-percent">{{ item.progress }}%</text>
            </view>
          </view>
        </view>
      </view>

      <!-- Floating Action Button -->
      <view class="fab-container">
        <view class="fab pill" @tap="openAdd">
          <text class="fab-icon">+</text>
        </view>
      </view>
    </view>

    <!-- Add/Edit Modal -->
    <view v-if="showModal" class="modal-mask" @tap="closeModal">
      <view class="modal-sheet" @tap.stop>
        <view class="modal-sheet-bar" />
        <view class="modal-sheet-head">
          <text class="modal-sheet-title">{{ editingId ? "编辑目标" : "添加目标" }}</text>
          <text class="modal-sheet-close" @tap="closeModal">×</text>
        </view>
        <scroll-view class="modal-sheet-body" scroll-y>
          <view class="field-group">
            <text class="field-title">目标类型</text>
            <view class="type-grid">
              <view
                v-for="item in goalTypes"
                :key="item.value"
                class="type-card card-sm"
                :class="{ active: form.goalType === item.value }"
                @tap="form.goalType = item.value"
              >
                <image class="type-icon-img" :src="item.icon" mode="aspectFit" />
                <text class="type-label">{{ item.label }}</text>
              </view>
            </view>
          </view>

          <view class="field-group">
            <text class="field-title">周期</text>
            <view class="period-tabs">
              <view class="period-tab pill" :class="{ active: form.period === 'day' }" @tap="form.period = 'day'">
                <text>每日</text>
              </view>
              <view class="period-tab pill" :class="{ active: form.period === 'week' }" @tap="form.period = 'week'">
                <text>每周</text>
              </view>
            </view>
          </view>

          <view class="field-group">
            <text class="field-title">目标值</text>
            <input class="input-main" type="number" v-model="form.targetValue" :placeholder="getPlaceholder(form.goalType)" />
          </view>

          <view class="field-group" v-if="editingId">
            <text class="field-title">当前进度</text>
            <input class="input-main" type="number" v-model="form.currentValue" placeholder="请输入当前值" />
          </view>
        </scroll-view>
        <view class="modal-sheet-footer">
          <button class="save-btn pill" @tap="submitAdd" :disabled="saving">
            {{ saving ? "保存中..." : editingId ? "保存修改" : "保存目标" }}
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { request } from "../../utils/api";

export default {
  data() {
    return {
      goals: [],
      period: "day",
      showModal: false,
      saving: false,
      editingId: null,
      goalTypes: [
        { label: "步数", value: 1, icon: "/static/tabbar/sport.png" },
        { label: "体重", value: 2, icon: "/static/tabbar/weight.png" },
        { label: "饮水", value: 3, icon: "/static/tabbar/water_h.png" },
        { label: "睡眠", value: 4, icon: "/static/tabbar/sleep.png" }
      ],
      form: {
        goalType: 1,
        period: "day",
        targetValue: "",
        currentValue: ""
      }
    };
  },
  computed: {
    overallProgress() {
      if (this.goals.length === 0) return 0;
      const total = this.goals.reduce((sum, item) => sum + item.progress, 0);
      return Math.round(total / this.goals.length);
    },
    totalCount() {
      return this.goals.length;
    },
    ongoingCount() {
      return this.goals.filter(item => item.status === 1 && !this.isGoalExpired(item)).length;
    },
    doneCount() {
      return this.goals.filter(item => item.status === 2).length;
    },
    abandonedCount() {
      return this.goals.filter(item => item.status === 3).length;
    },
    expiredCount() {
      return this.goals.filter(item => this.isGoalExpired(item) && item.status !== 3).length;
    }
  },
  onShow() {
    this.fetchGoals();
  },
  methods: {
    goalUnitSuffix(goalType) {
      switch (Number(goalType)) {
        case 1: return " 步";
        case 2: return " kg";
        case 3: return " ml";
        case 4: return " 小时";
        default: return "";
      }
    },
    goalLabel(goalType) {
      switch (Number(goalType)) {
        case 1: return "步数";
        case 2: return "体重";
        case 3: return "饮水";
        case 4: return "睡眠";
        default: return "目标";
      }
    },
    goalCountdown(period) {
      const now = new Date();
      let end;
      if (String(period).toLowerCase().includes("week")) {
        const day = now.getDay() || 7;
        const diff = 7 - day;
        end = new Date(now.getFullYear(), now.getMonth(), now.getDate() + diff, 23, 59, 59);
      } else {
        end = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59);
      }
      const diffMs = end.getTime() - now.getTime();
      if (diffMs <= 0) return "已结束";
      const totalMinutes = Math.floor(diffMs / 60000);
      const hours = Math.floor(totalMinutes / 60);
      const minutes = totalMinutes % 60;
      return `${hours}小时${minutes}分钟`;
    },
    getGoalIcon(goalType) {
      switch (Number(goalType)) {
        case 1: return "/static/tabbar/sport.png";
        case 2: return "/static/tabbar/weight.png";
        case 3: return "/static/tabbar/water_h.png";
        case 4: return "/static/tabbar/sleep.png";
        default: return "/static/tabbar/goal.png";
      }
    },
    getTypeIcon(goalType) {
      switch (Number(goalType)) {
        case 1: return "/static/tabbar/sport.png";
        case 2: return "/static/tabbar/weight.png";
        case 3: return "/static/tabbar/water_h.png";
        case 4: return "/static/tabbar/sleep.png";
        default: return "/static/tabbar/goal.png";
      }
    },
    getProgressStatus(progress) {
      if (progress >= 100) return "表现优异";
      if (progress >= 80) return "即将达成";
      if (progress >= 50) return "进展顺利";
      return "稳定保持";
    },
    isGoalExpired(item) {
      if (!item.endDate) return false;
      const endDate = new Date(item.endDate.replace(/-/g, "/"));
      return endDate < new Date();
    },
    getPlaceholder(goalType) {
      switch (Number(goalType)) {
        case 1: return "如 10000";
        case 2: return "如 65";
        case 3: return "如 2000";
        case 4: return "如 8";
        default: return "请输入目标值";
      }
    },
    switchPeriod(value) {
      this.period = value;
      this.fetchGoals();
    },
    fetchGoals() {
      const userId = uni.getStorageSync("userId") || 1;
      request("/api/goal/list", "GET", { userId, period: this.period })
        .then((list) => {
          if (Array.isArray(list) && list.length > 0) {
            const mapped = list
              .filter((item) => Number(item.goalType) !== 4)
              .map((item) => {
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
                status: item.status || 1, // 1:进行中 2:已完成 3:已放弃
                endDate: item.endDate || "",
                state: progress >= 100 ? "已完成" : "进行中",
                periodValue: item.period === "weekly" || item.period === "week" ? "week" : "day",
                periodLabel: item.period === "weekly" || item.period === "week" ? "每周" : "每日",
                countdown: this.goalCountdown(item.period)
              };
            });
            this.goals = mapped;
          } else {
            this.goals = [];
          }
        })
        .catch(() => {
          this.goals = [];
        });
    },
    openAdd() {
      this.showModal = true;
      this.editingId = null;
      this.form = { goalType: 1, period: "day", targetValue: "", currentValue: "" };
    },
    openEdit(item) {
      if (!item) return;
      this.editingId = item.id;
      this.showModal = true;
      this.form = {
        goalType: Number(item.goalType || 1),
        period: item.periodValue || "day",
        targetValue: item.targetValue,
        currentValue: item.currentValue
      };
    },
    closeModal() {
      this.showModal = false;
    },
    submitAdd() {
      const target = parseInt(this.form.targetValue, 10);
      const current = this.editingId ? parseInt(this.form.currentValue || 0, 10) : 0;
      if (!this.form.goalType) {
        uni.showToast({ title: "请选择目标类型", icon: "none" });
        return;
      }
      if (!target || target <= 0) {
        uni.showToast({ title: "请输入有效目标值", icon: "none" });
        return;
      }
      const type = Number(this.form.goalType);
      if (type === 1 && (target < 100 || target > 50000)) {
        uni.showToast({ title: "步数建议 100-50000", icon: "none" });
        return;
      }
      if (type === 2 && (target < 30 || target > 200)) {
        uni.showToast({ title: "体重建议 30-200 kg", icon: "none" });
        return;
      }
      if (type === 3 && (target < 500 || target > 5000)) {
        uni.showToast({ title: "饮水量建议 500-5000 ml", icon: "none" });
        return;
      }
      if (type === 4 && (target < 1 || target > 24)) {
        uni.showToast({ title: "睡眠小时需在 1-24", icon: "none" });
        return;
      }
      if (this.editingId && (Number.isNaN(current) || current < 0)) {
        uni.showToast({ title: "当前进度需大于等于 0", icon: "none" });
        return;
      }
      this.saving = true;
      const userId = uni.getStorageSync("userId") || 1;
      const payload = {
        userId,
        goalType: this.form.goalType,
        targetValue: target,
        currentValue: current,
        period: this.form.period
      };
      const url = this.editingId ? "/api/goal/update" : "/api/goal/add";
      if (this.editingId) {
        payload.id = this.editingId;
      }
      request(url, "POST", payload)
        .then(() => {
          uni.showToast({ title: "已保存", icon: "success" });
          this.closeModal();
          this.fetchGoals();
        })
        .catch((err) => {
          uni.showToast({ title: err.message || "保存失败", icon: "none" });
        })
        .finally(() => {
          this.saving = false;
        });
    }
  }
};
</script>

<style scoped>
.page-root {
  min-height: 100vh;
  background: #FAF9F6;
}

.page {
  padding: 16rpx 24rpx 180rpx;
  min-height: 100vh;
  background: #FAF9F6;
}

/* Overview Section */
.overview-section {
  margin-bottom: 24rpx;
}

.overview-card {
  background: #ffffff;
  border-radius: var(--radius-card);
  padding: 16rpx 14rpx;
  box-shadow: var(--shadow-card);
  position: relative;
  overflow: hidden;
}

.overview-decor {
  position: absolute;
  top: -40rpx;
  right: -40rpx;
  width: 160rpx;
  height: 160rpx;
  background: #A23F00;
  opacity: 0.05;
  border-radius: 50%;
  filter: blur(40rpx);
}

.overview-content {
  position: relative;
  z-index: 1;
}

.overview-label {
  font-size: 20rpx;
  color: #564337;
  font-weight: 500;
  opacity: 0.7;
  margin-bottom: 12rpx;
  display: block;
}

.overview-score {
  display: flex;
  align-items: baseline;
  gap: 6rpx;
  margin-bottom: 24rpx;
}

.score-number {
  font-size: 40rpx;
  font-weight: 800;
  color: #A23F00;
  line-height: 1;
  font-family: 'Manrope', sans-serif;
}

.score-percent {
  font-size: 28rpx;
  font-weight: 700;
  color: #564337;
}

.overview-stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.stats-label {
  font-size: 24rpx;
  color: #564337;
  font-weight: 600;
}

.stats-value {
  font-size: 24rpx;
  color: #A23F00;
  font-weight: 700;
}

.overview-bar {
  height: 12rpx;
  background: #E9E1D8;
  border-radius: var(--radius-pill);
  overflow: hidden;
  margin-bottom: 28rpx;
}

.overview-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #A23F00 0%, #FA7025 100%);
  border-radius: var(--radius-pill);
}

.overview-detail {
  display: flex;
  gap: 20rpx;
}

.detail-item {
  background: #FAF9F6;
  border: 1px solid #E9E1D8;
  border-radius: var(--radius-card-sm);
  padding: 12rpx 20rpx;
  min-width: 100rpx;
}

.detail-label {
  font-size: 18rpx;
  color: #865300;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 1rpx;
  display: block;
  margin-bottom: 4rpx;
}

.detail-value {
  font-size: 28rpx;
  font-weight: 800;
  color: #1a1c1a;
  font-family: 'Manrope', sans-serif;
}

/* 状态颜色 */
.detail-item.ongoing {
  background: #FFF4ED;
  border-color: #FFD4B8;
}
.detail-item.ongoing .detail-label { color: #A23F00; }
.detail-item.ongoing .detail-value { color: #A23F00; }

.detail-item.done {
  background: #E8F5E9;
  border-color: #C8E6C9;
}
.detail-item.done .detail-label { color: #2E7D32; }
.detail-item.done .detail-value { color: #2E7D32; }

.detail-item.abandoned {
  background: #F5F5F5;
  border-color: #E0E0E0;
}
.detail-item.abandoned .detail-label { color: #757575; }
.detail-item.abandoned .detail-value { color: #757575; }

.detail-item.expired {
  background: #FFF3E0;
  border-color: #FFE0B2;
}
.detail-item.expired .detail-label { color: #E65100; }
.detail-item.expired .detail-value { color: #E65100; }

/* List Header */
.list-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 20rpx;
}

.list-title {
  font-size: 28rpx;
  font-weight: 800;
  color: #1a1c1a;
  font-family: 'Manrope', sans-serif;
}

.list-subtitle {
  font-size: 24rpx;
  color: #865300;
  font-weight: 500;
}

/* Goals List */
.goals-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

/* Empty Page */
.empty-page {
  min-height: 100vh;
  background: #FAF8F5;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 0 32rpx;
}

.empty-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  margin-top: -120rpx;
}

.empty-illustration {
  width: 200rpx;
  height: 200rpx;
  background: linear-gradient(135deg, #A23F00 0%, #FA7025 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 40rpx;
  box-shadow: 0 8rpx 24rpx rgba(162, 63, 0, 0.3);
}

.empty-illustration-icon {
  width: 100rpx;
  height: 100rpx;
}

.empty-content .empty-title-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 16rpx;
}

.empty-content .empty-title-wrap .empty-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #1a1c1a;
  line-height: 1.4;
}

.empty-content .empty-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #1a1c1a;
  margin-bottom: 16rpx;
}

.empty-content .empty-desc {
  font-size: 28rpx;
  color: #564337;
  margin-bottom: 60rpx;
}

.empty-features {
  display: flex;
  gap: 32rpx;
  margin-bottom: 60rpx;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
}

.feature-icon {
  width: 48rpx;
  height: 48rpx;
}

.feature-text {
  font-size: 22rpx;
  color: #564337;
  white-space: nowrap;
}

.empty-add-btn {
  width: 480rpx;
  height: 88rpx;
  background: linear-gradient(135deg, #A23F00 0%, #FA7025 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  box-shadow: 0 8rpx 24rpx rgba(162, 63, 0, 0.3);
  border: none;
  border-radius: 999rpx;
  position: relative;
  z-index: 10;
}

.empty-add-btn::after {
  border: none;
}

.empty-add-icon {
  width: 36rpx;
  height: 36rpx;
}

.empty-add-text {
  font-size: 28rpx;
  font-weight: 700;
  color: #ffffff;
}

/* Old empty-state (for backward compatibility) */
.empty-state {
  background: #ffffff;
  border-radius: var(--radius-card);
  padding: 80rpx 32rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  border: 2rpx dashed #E9E1D8;
}

.empty-icon {
  width: 80rpx;
  height: 80rpx;
  margin-bottom: 24rpx;
}

.empty-title {
  font-size: 28rpx;
  font-weight: 700;
  color: #1a1c1a;
  margin-bottom: 12rpx;
  display: block;
}

.empty-desc {
  font-size: 24rpx;
  color: #564337;
  display: block;
  opacity: 0.7;
}

/* Goal Card */
.goal-card {
  background: #ffffff;
  border-radius: var(--radius-card);
  padding: 24rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  border: 2rpx solid #E9E1D8;
  transition: all 0.3s ease;
  box-shadow: var(--shadow-card);
  overflow: hidden;
  box-sizing: border-box;
  width: 100%;
}

.goal-card:active {
  background: #ffffff;
  border-color: #A23F00;
  box-shadow: 0 8rpx 24rpx rgba(162, 63, 0, 0.15);
  transform: translateY(-4rpx);
}

.goal-header {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 16rpx;
}

.goal-info {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.goal-icon-wrapper {
  width: 64rpx;
  height: 64rpx;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #FFF4ED;
  border-radius: 20rpx;
}

.goal-icon-img {
  width: 36rpx;
  height: 36rpx;
}

.goal-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.goal-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.goal-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #1a1c1a;
}

.goal-daily-tag {
  font-size: 20rpx;
  color: #A23F00;
  background: #FFF4ED;
  padding: 4rpx 12rpx;
  border-radius: 12rpx;
}

.goal-progress-text {
  font-size: 24rpx;
  color: #A23F00;
  font-weight: 500;
}

.goal-target {
  font-size: 32rpx;
  font-weight: 700;
  color: #A23F00;
}

.goal-period {
  font-size: 22rpx;
  color: #8B7355;
  background: #FAF8F5;
  padding: 4rpx 14rpx;
  border-radius: 16rpx;
}

.goal-badge {
  padding: 8rpx 20rpx;
  border-radius: var(--radius-pill);
  font-size: 20rpx;
  font-weight: 700;
}

.badge-progress {
  background: #A23F00;
  color: #ffffff;
}

.badge-success {
  background: #006D37;
  color: #ffffff;
}

/* Progress Section */
.goal-progress {
  margin-top: 16rpx;
}

.progress-bar {
  height: 12rpx;
  background: #E9E1D8;
  border-radius: var(--radius-pill);
  overflow: hidden;
  margin-bottom: 8rpx;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.progress-status {
  font-size: 22rpx;
  color: #564337;
}

.progress-percent {
  font-size: 24rpx;
  font-weight: 600;
  color: #A23F00;
}

.progress-bar {
  height: 8rpx;
  background: #E9E1D8;
  border-radius: var(--radius-pill);
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: var(--radius-pill);
}

.progress-fill-1 {
  background: linear-gradient(90deg, #A23F00 0%, #FA7025 100%);
}

.progress-fill-2 {
  background: linear-gradient(90deg, #865300 0%, #FEA520 100%);
}

.progress-fill-3 {
  background: linear-gradient(90deg, #FA7025 0%, #FEA520 100%);
}

/* FAB */
.fab-container {
  position: fixed;
  bottom: 160rpx;
  right: 48rpx;
  z-index: 100;
}

.fab {
  width: 128rpx;
  height: 128rpx;
  background: #A23F00;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 16rpx 40rpx rgba(162, 63, 0, 0.4);
  transition: all 0.2s ease;
}

.fab:active {
  transform: scale(0.9);
}

.fab-icon {
  font-size: 64rpx;
  color: #ffffff;
  font-weight: 300;
}

/* Modal */
.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(26, 28, 26, 0.5);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 200;
}

.modal-sheet {
  width: 100%;
  max-height: 85vh;
  background: #fff;
  border-radius: 32rpx 32rpx 0 0;
  padding-bottom: env(safe-area-inset-bottom);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-sizing: border-box;
}

.modal-sheet-bar {
  width: 72rpx;
  height: 8rpx;
  background: #E9E1D8;
  border-radius: 8rpx;
  margin: 20rpx auto;
}

.modal-sheet-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 40rpx 32rpx;
}

.modal-sheet-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #1a1c1a;
}

.modal-sheet-close {
  font-size: 48rpx;
  color: #8B7355;
  padding: 8rpx;
}

.modal-sheet-body {
  flex: 1;
  overflow-y: auto;
  padding: 0 40rpx 48rpx;
  box-sizing: border-box;
  width: 100%;
}

.field-group {
  margin-bottom: 32rpx;
}

.field-title {
  font-size: 26rpx;
  font-weight: 500;
  color: #8B7355;
  display: block;
  margin-bottom: 16rpx;
}

/* Type Grid */
.type-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16rpx;
}

.type-card {
  background: #ffffff;
  border-radius: var(--radius-card-sm);
  padding: 16rpx 12rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 8rpx;
  border: 2rpx solid #E9E1D8;
  opacity: 0.7;
  transition: all 0.3s ease;
}

.type-card.active {
  opacity: 1;
  border-color: #A23F00;
  background: #FFDBCD;
  box-shadow: 0 10rpx 30rpx rgba(86, 67, 55, 0.04);
}

.type-icon-img {
  width: 32rpx;
  height: 32rpx;
}

.type-label {
  font-size: 20rpx;
  font-weight: 500;
  color: #1a1c1a;
}

/* Period Tabs */
.period-tabs {
  display: flex;
  gap: 16rpx;
}

.period-tab {
  flex: 1;
  padding: 16rpx;
  border-radius: var(--radius-pill);
  background: #FAF9F6;
  text-align: center;
  transition: all 0.3s ease;
}

.period-tab.active {
  background: #A23F00;
  box-shadow: 0 12rpx 24rpx rgba(162, 63, 0, 0.2);
}

.period-tab text {
  font-size: 24rpx;
  font-weight: 700;
}

.period-tab.active text {
  color: #ffffff;
}

.period-tab:not(.active) text {
  color: #564337;
}

/* Input */
.input-main {
  width: 100%;
  padding: 24rpx 28rpx;
  font-size: 28rpx;
  color: #1a1c1a;
  background: #fff;
  border: 1px solid #E9E1D8;
  border-radius: 24rpx;
  box-sizing: border-box;
  height: 88rpx;
  line-height: 40rpx;
}

/* Footer */
.modal-sheet-footer {
  padding: 20rpx 32rpx 32rpx;
  border-top: 1px solid #FAF9F6;
}

.save-btn {
  width: 100%;
  height: 96rpx;
  border-radius: 48rpx;
  border: none;
  color: #fff;
  font-size: 32rpx;
  font-weight: 600;
  background: linear-gradient(135deg, #A23F00 0%, #FA7025 100%);
}

.save-btn::after {
  border: none;
}

.save-btn:active {
  transform: scale(0.98);
}
</style>
