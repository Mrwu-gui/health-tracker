<template>
  <view class="page-root">
    <view class="page">
    <view class="header">
      <text class="title">目标管理</text>
      <button class="add-btn" @tap="openAdd">+</button>
    </view>

      <view class="tabs">
        <view class="tab" :class="{ active: period === 'day' }" @tap="switchPeriod('day')">今日</view>
        <view class="tab" :class="{ active: period === 'week' }" @tap="switchPeriod('week')">本周</view>
      </view>

      <view class="list">
        <view v-for="item in goals" :key="item.id" class="card" @tap="openEdit(item)">
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
        <view class="modal" @tap.stop>
          <text class="modal-title">{{ editingId ? "编辑目标" : "添加目标" }}</text>
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
          <button class="primary" @tap="submitAdd" :disabled="saving">
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
  onLoad() {
    this.useDefaultGoals();
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
            this.useDefaultGoals();
          }
        })
        .catch(() => {
          this.useDefaultGoals();
        });
    },
    setGoals(list) {
      if (typeof this.$set === "function") {
        this.$set(this, "goals", list);
      } else {
        this.goals = list;
      }
    },
    useDefaultGoals() {
      this.goals = [
        { id: 1, goalType: 1, goalLabel: "步数", targetValue: 10000, currentValue: 8240, progress: 82, state: "接近完成", periodLabel: "每日", periodValue: "day", countdown: this.goalCountdown("day") },
        { id: 2, goalType: 2, goalLabel: "睡眠", targetValue: 7, currentValue: 7.1, progress: 101, state: "状态良好", periodLabel: "每日", periodValue: "day", countdown: this.goalCountdown("day") },
        { id: 3, goalType: 3, goalLabel: "饮食热量", targetValue: 2000, currentValue: 1650, progress: 83, state: "可适当加餐", periodLabel: "每日", periodValue: "day", countdown: this.goalCountdown("day") }
      ];
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
  padding-bottom: calc(60px + env(safe-area-inset-bottom));
  min-height: 100vh;
  background: #f4f5f7;
  color: #0f172a;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.page-root {
  min-height: 100vh;
  background: #f4f5f7;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.title {
  font-size: 18px;
  font-weight: 600;
}

.add-btn {
  width: 34px;
  height: 34px;
  border-radius: 17px;
  background: #2563eb;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 300;
}

.tabs {
  display: inline-flex;
  background: #e2e8f0;
  border-radius: 999px;
  padding: 2px;
  gap: 4px;
}

.tab {
  font-size: 11px;
  padding: 4px 10px;
  border-radius: 999px;
  color: #64748b;
}

.tab.active {
  background: #ffffff;
  color: #0f172a;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.08);
}

.list {
  display: grid;
  gap: 10px;
}

.card {
  background: #ffffff;
  border-radius: 16px;
  padding: 14px;
  border: 1px solid #e2e8f0;
}

.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info .name {
  font-size: 12px;
  font-weight: 600;
  display: block;
}

.info .desc {
  font-size: 10px;
  color: #94a3b8;
}

.state {
  font-size: 11px;
  color: #2563eb;
}

.progress-row {
  display: flex;
  justify-content: space-between;
  font-size: 10px;
  color: #64748b;
  margin: 6px 0;
}

.countdown {
  font-size: 10px;
  color: #f59e0b;
  margin-bottom: 6px;
  display: block;
}

.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  padding: 20px;
}

.modal {
  width: 100%;
  max-width: 320px;
  background: #fff;
  border-radius: 16px;
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.modal-title {
  font-size: 15px;
  font-weight: 600;
  color: #0f172a;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field-label {
  font-size: 12px;
  color: #64748b;
}

.pill-group {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.pill {
  padding: 8px 12px;
  border-radius: 999px;
  border: 1px solid #e2e8f0;
  font-size: 12px;
  color: #64748b;
  background: #fff;
}

.pill.active {
  color: #2563eb;
  border-color: #2563eb;
  background: #eff6ff;
}

.input {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 10px 12px;
  font-size: 14px;
  color: #0f172a;
  background: #fff;
}

.primary {
  width: 100%;
  padding: 12px 0;
  border-radius: 12px;
  background: #0f172a;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
}

.bar {
  height: 6px;
  background: #e2e8f0;
  border-radius: 999px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: #2563eb;
}

.tip {
  background: #e2e8f0;
  border-radius: 16px;
  padding: 10px 12px;
  font-size: 11px;
  color: #475569;
}
</style>
