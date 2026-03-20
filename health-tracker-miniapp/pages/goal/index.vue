<template>
  <view class="page-root">
    <view class="page">
    <view class="header">
      <view />
      <button class="add-btn" @tap="openAdd">+</button>
    </view>

      <view class="tabs">
        <view class="tab" :class="{ active: period === 'day' }" @tap="switchPeriod('day')">今日</view>
        <view class="tab" :class="{ active: period === 'week' }" @tap="switchPeriod('week')">本周</view>
      </view>

      <view class="list">
        <view v-if="goals.length === 0" class="empty-state">
          <image class="empty-state-icon" src="/static/tabbar/goal.png" mode="widthFix" />
          <text class="empty-state-title">暂无目标</text>
          <text class="empty-state-desc">点击右上角 + 添加你的第一个目标吧</text>
        </view>
        <view v-for="item in goals" v-else :key="item.id" class="card" @tap="openEdit(item)">
          <view class="row">
            <view class="info">
              <text class="name">{{ item.goalLabel }}目标</text>
              <text class="desc">{{ item.periodLabel }} {{ item.targetValue }}{{ goalUnitSuffix(item.goalType) }}</text>
            </view>
            <text class="state">{{ item.state }}</text>
          </view>
          <view class="progress-row">
            <text>已完成 {{ item.currentValue }}{{ goalUnitSuffix(item.goalType) }}</text>
            <text>{{ item.progress }}%</text>
          </view>
          <text class="countdown">距离目标结束 {{ item.countdown }}</text>
          <view class="bar">
            <view class="bar-fill" :style="{ width: `${item.progress}%` }"></view>
          </view>
        </view>
      </view>

      <view class="tip">
        <text>再坚持一点点，就能完成今日目标。</text>
      </view>

      <view v-if="showModal" class="modal-mask" @tap="closeModal">
        <view class="modal-sheet" @tap.stop>
          <view class="modal-sheet-bar" />
          <view class="modal-sheet-head">
            <text class="modal-sheet-title">{{ editingId ? "编辑目标" : "添加目标" }}</text>
            <text class="modal-sheet-close" @tap="closeModal">×</text>
          </view>
          <view class="modal-sheet-body">
          <view class="field">
            <text class="field-label">目标类型</text>
            <view class="pill-group">
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
            <text class="field-label">周期</text>
            <view class="pill-group">
              <view class="pill" :class="{ active: form.period === 'day' }" @tap="form.period = 'day'">每日</view>
              <view class="pill" :class="{ active: form.period === 'week' }" @tap="form.period = 'week'">每周</view>
            </view>
          </view>
          <view class="field">
            <text class="field-label">目标值</text>
            <input class="input" type="number" v-model="form.targetValue" placeholder="请输入目标值" />
          </view>
          <view class="field">
            <text class="field-label">当前进度</text>
            <input class="input" type="number" v-model="form.currentValue" placeholder="请输入当前值" />
          </view>
          <button class="modal-sheet-btn primary" @tap="submitAdd" :disabled="saving">
            {{ saving ? "保存中..." : editingId ? "保存修改" : "保存" }}
          </button>
          </view>
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
        { label: "步数", value: 1 },
        { label: "睡眠", value: 2 },
        { label: "饮食热量", value: 3 }
      ],
      form: {
        goalType: 1,
        period: "day",
        targetValue: "",
        currentValue: ""
      }
    };
  },
  onShow() {
    this.fetchGoals();
  },
  methods: {
    goalUnitSuffix(goalType) {
      switch (Number(goalType)) {
        case 1:
          return " 步";
        case 2:
          return " 小时";
        case 3:
          return " kcal";
        default:
          return "";
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
                state: progress >= 100 ? "已完成" : "进行中",
                periodValue: item.period === "weekly" || item.period === "week" ? "week" : "day",
                periodLabel: item.period === "weekly" || item.period === "week" ? "每周" : "每日",
                countdown: this.goalCountdown(item.period)
              };
            });
            this.setGoals(mapped);
          } else {
            this.setGoals([]);
          }
        })
        .catch(() => {
          this.setGoals([]);
        });
    },
    setGoals(list) {
      this.goals = list;
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
      const current = parseInt(this.form.currentValue || 0, 10);
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
      if (type === 2 && (target < 1 || target > 24)) {
        uni.showToast({ title: "睡眠小时需在 1-24", icon: "none" });
        return;
      }
      if (type === 3 && (target < 200 || target > 6000)) {
        uni.showToast({ title: "热量建议 200-6000", icon: "none" });
        return;
      }
      if (Number.isNaN(current) || current < 0) {
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

<style>
.page {
  padding: 18px;
  padding-bottom: calc(56px + env(safe-area-inset-bottom));
  min-height: 100vh;
  background: #FAF8F5;
  color: #1a1c1a;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.page-root {
  min-height: 100vh;
  background: #FAF8F5;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.title {
  font-size: 18px;
  font-weight: 700;
  color: #1a1c1a;
}

.add-btn {
  width: 34px;
  height: 34px;
  border-radius: 24rpx;
  background: #A23F00;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 300;
  box-shadow: 0 6rpx 16rpx rgba(162, 63, 0, 0.4);
}

.tabs {
  display: inline-flex;
  background: #E9E1D8;
  border-radius: 999rpx;
  padding: 2px;
  gap: 4px;
}

.tab {
  font-size: 11px;
  padding: 4px 10px;
  border-radius: 999rpx;
  color: #564337;
}

.tab.active {
  background: #ffffff;
  color: #1a1c1a;
  box-shadow: 0 6rpx 16rpx rgba(0, 0, 0, 0.08);
}

.list {
  display: grid;
  gap: 10px;
}

.empty-state {
  grid-column: 1 / -1;
  background: #FAF8F5;
  border-radius: 24rpx;
  padding: 32px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  border: 1px dashed #e8e2db;
}

.empty-state-icon {
  width: 40px;
  height: auto;
  margin-bottom: 12px;
  opacity: 0.85;
}

.empty-state-title {
  font-size: 15px;
  font-weight: 600;
  color: #475569;
  margin-bottom: 6px;
}

.empty-state-desc {
  font-size: 12px;
  color: #564337;
}

.card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 14px;
  border: 1px solid #E9E1D8;
  box-shadow: 0 6rpx 16rpx rgba(0,0,0,0.04);
  transition: transform 120ms ease;
}

.card:active {
  transform: scale(0.98);
}

.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info .name {
  font-size: 12px;
  font-weight: 700;
  display: block;
}

.info .desc {
  font-size: 10px;
  color: #564337;
}

.state {
  font-size: 11px;
  color: #A23F00;
}

.progress-row {
  display: flex;
  justify-content: space-between;
  font-size: 10px;
  color: #564337;
  margin: 6px 0;
}

.countdown {
  font-size: 10px;
  color: #A23F00;
  margin-bottom: 6px;
  display: block;
}

.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.5);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 100;
}

.modal-sheet {
  width: 100%;
  max-width: 400px;
  max-height: 85vh;
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
  padding-bottom: env(safe-area-inset-bottom);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.modal-sheet-bar {
  width: 36px;
  height: 4px;
  border-radius: 999rpx;
  background: #E9E1D8;
  margin: 10px auto 0;
}

.modal-sheet-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px 16px;
  border-bottom: 1px solid #faf9f6;
}

.modal-sheet-title {
  font-size: 17px;
  font-weight: 700;
  color: #1a1c1a;
}

.modal-sheet-close {
  font-size: 24px;
  color: #564337;
  padding: 4px;
  line-height: 1;
}

.modal-sheet-body {
  padding: 20px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field-label {
  font-size: 13px;
  color: #564337;
}

.pill-group {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.pill {
  padding: 10px 14px;
  border-radius: 24rpx;
  border: 1px solid #E9E1D8;
  font-size: 13px;
  color: #564337;
  background: #fff;
}

.pill.active {
  color: #A23F00;
  border-color: #A23F00;
  background: #FAF8F5;
}

.input {
  border: 1px solid #E9E1D8;
  border-radius: 24rpx;
  padding: 12px 14px;
  font-size: 14px;
  color: #1a1c1a;
  background: #fff;
}

.modal-sheet-btn.primary {
  width: 100%;
  padding: 14px;
  border-radius: 24rpx;
  background: #A23F00;
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  border: none;
}

.bar {
  height: 6px;
  background: #e8e2db;
  border-radius: 24rpx;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: #A23F00;
}

.tip {
  background: #e8e2db;
  border-radius: 24rpx;
  padding: 10px 12px;
  font-size: 11px;
  color: #475569;
}
</style>
