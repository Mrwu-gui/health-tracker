<template>
  <view class="page-root">
    <!-- 空状态 -->
    <view v-if="reminders.length === 0" class="empty-page">
      <view class="empty-content">
        <view class="empty-illustration">
          <image class="empty-illustration-icon" src="/static/tabbar/remind-disable.png" mode="aspectFit" />
        </view>
        <text class="empty-desc">添加后将按时间提醒，帮助您稳定执行健康计划。</text>

        <view class="empty-features">
          <view class="feature-item">
            <image class="feature-icon" src="/static/tabbar/remind_s.png" mode="aspectFit" />
            <text class="feature-text">按时提醒</text>
          </view>
          <view class="feature-item">
            <image class="feature-icon" src="/static/tabbar/history_s.png" mode="aspectFit" />
            <text class="feature-text">保留历史</text>
          </view>
          <view class="feature-item">
            <image class="feature-icon" src="/static/tabbar/tips_s.png" mode="aspectFit" />
            <text class="feature-text">AI建议</text>
          </view>
        </view>

        <button class="empty-add-btn" @tap="openAdd">
          <image class="empty-add-icon" src="/static/tabbar/add.png" mode="aspectFit" />
          <text class="empty-add-text">添加第一个提醒</text>
        </button>
      </view>
    </view>

    <!-- 有数据时 -->
    <view v-else class="page">
      <!-- 标题栏 -->
      <view class="page-header">
        <text class="page-title">今日提醒</text>
        <text class="page-count">{{ pendingCount }} 项待处理</text>
      </view>

      <!-- 筛选标签 -->
      <view class="chips">
        <view class="chip" :class="{ active: tabFilter === 0 }" @tap="tabFilter = 0">全部</view>
        <view class="chip" :class="{ active: tabFilter === 1 }" @tap="tabFilter = 1">用药</view>
        <view class="chip" :class="{ active: tabFilter === 2 }" @tap="tabFilter = 2">运动</view>
        <view class="chip" :class="{ active: tabFilter === 3 }" @tap="tabFilter = 3">饮食</view>
        <view class="chip" :class="{ active: tabFilter === 4 }" @tap="tabFilter = 4">睡眠</view>
      </view>

      <!-- 时间线列表 -->
      <view class="reminder-timeline">
        <view v-for="(item, index) in filteredReminders" :key="item.id" class="timeline-item">
          <!-- 时间线 -->
          <view class="timeline-line">
            <view class="timeline-dot" :class="item.statusTag === '已完成' ? 'status-completed' : (item.statusTag === '已忽略' ? 'status-ignored' : 'status-pending')"></view>
            <view v-if="index < filteredReminders.length - 1" class="timeline-connector"></view>
          </view>

          <!-- 提醒卡片 -->
          <view class="reminder-card card" :class="{ 'reminder-completed': item.statusTag === '已完成' }" @tap="openEdit(item)">
            <view class="reminder-header">
              <text class="reminder-time">{{ item.timeDisplay }}</text>
              <text class="reminder-status" :class="item.statusTag === '已完成' ? 'status-completed' : (item.statusTag === '已忽略' ? 'status-ignored' : 'status-pending')">{{ item.statusTag || '待处理' }}</text>
            </view>

            <view class="reminder-body">
              <view class="reminder-icon-wrapper">
                <image class="reminder-icon" :src="item.icon" mode="aspectFit" />
              </view>
              <view class="reminder-details">
                <text class="reminder-title">{{ item.title }}</text>
                <text class="reminder-content">{{ item.content || '提醒事项' }}</text>
              </view>
            </view>

            <view v-if="item.statusTag !== '已完成' && item.statusTag !== '已忽略' && item.statusTag !== '已提醒'" class="reminder-actions">
              <view class="action-btn confirm-btn" @tap.stop="setStatus(item, REMINDER_STATUS.DONE)">完成</view>
              <view class="action-btn delay-btn" @tap.stop="openPostpone(item)">延期</view>
            </view>
          </view>
        </view>
      </view>

      <!-- 添加按钮 -->
      <view class="fab-container">
        <view class="fab" @tap="openAdd">
          <text class="fab-icon">+</text>
        </view>
      </view>

      <text class="note">需在「授权与隐私」中开启订阅消息</text>
    </view>

    <view v-if="showModal" class="modal-mask" @tap="closeModal">
      <view class="modal-sheet" @tap.stop>
        <view class="modal-sheet-bar" />
        <view class="modal-sheet-head">
          <text class="modal-sheet-title">{{ editingId ? "编辑提醒" : "添加提醒" }}</text>
          <text class="modal-sheet-close" @tap="closeModal">×</text>
        </view>
        <view class="modal-sheet-body">
          <view class="field">
            <text class="field-label">提醒类型</text>
            <view class="pill-wrap">
              <view
                v-for="item in titleOptions"
                :key="item.type"
                class="pill"
                :class="{ active: form.type === item.type }"
                @tap="form.type = item.type"
              >
                {{ item.label }}
              </view>
            </view>
          </view>

          <view class="field">
            <text class="field-label">提醒日期</text>
            <picker mode="date" :value="form.remindDate" :start="minDate" :end="maxDate" @change="onDateChange">
              <view class="picker">{{ form.remindDate || "选择日期" }}</view>
            </picker>
          </view>

          <view class="field">
            <text class="field-label">提醒时间</text>
            <picker mode="time" :value="form.remindTime || minTime" @change="onTimeChange">
              <view class="picker">{{ form.remindTime || "选择时间" }}</view>
            </picker>
          </view>

          <view class="field">
            <text class="field-label">备注（选填）</text>
            <input class="input" v-model="form.content" placeholder="补充说明..." />
          </view>

          <button class="modal-sheet-btn primary" @tap="submitAdd" :disabled="saving">
            {{ saving ? "保存中..." : editingId ? "保存修改" : "保存提醒" }}
          </button>
        </view>
      </view>
    </view>

    <view v-if="showPostponeModal" class="modal-mask" @tap="closePostpone">
      <view class="modal-sheet postpone-sheet" @tap.stop>
        <view class="modal-sheet-bar" />
        <view class="modal-sheet-head">
          <text class="modal-sheet-title">延期提醒</text>
          <text class="modal-sheet-close" @tap="closePostpone">×</text>
        </view>
        <view class="modal-sheet-body">
          <view class="field">
            <text class="field-label">新的提醒时间</text>
            <view class="picker-group">
              <picker mode="date" :value="postponeDate" :start="minDate" :end="maxDate" @change="onPostponeDateChange">
                <view class="picker">{{ postponeDate || "选择日期" }}</view>
              </picker>
              <picker mode="time" :value="postponeTime || minTime" @change="onPostponeTimeChange">
                <view class="picker">{{ postponeTime || "选择时间" }}</view>
              </picker>
            </view>
          </view>
          <button class="modal-sheet-btn primary" @tap="confirmPostpone" :disabled="postponeSaving">
            {{ postponeSaving ? "保存中..." : "确定延期" }}
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { request } from "../../utils/api";
import { REMINDER_TYPE, REMINDER_STATUS } from "../../constants/enums";

export default {
  data() {
    return {
      reminders: [],
      tabFilter: 0,
      showModal: false,
      saving: false,
      editingId: null,
      titleOptions: [
        { label: "用药提醒", type: REMINDER_TYPE.MEDICATION },
        { label: "运动提醒", type: REMINDER_TYPE.EXERCISE },
        { label: "饮食提醒", type: REMINDER_TYPE.DIET },
        { label: "睡眠提醒", type: REMINDER_TYPE.SLEEP }
      ],
      form: {
        type: REMINDER_TYPE.MEDICATION,
        content: "",
        remindDate: "",
        remindTime: ""
      },
      showPostponeModal: false,
      postponeItem: null,
      postponeDate: "",
      postponeTime: "",
      postponeSaving: false,
      actionItem: null,
      REMINDER_STATUS
    };
  },
  computed: {
    minDate() {
      const n = new Date();
      return `${n.getFullYear()}-${String(n.getMonth() + 1).padStart(2, "0")}-${String(n.getDate()).padStart(2, "0")}`;
    },
    minTime() {
      const n = new Date();
      return `${String(n.getHours()).padStart(2, "0")}:${String(n.getMinutes()).padStart(2, "0")}`;
    },
    maxDate() {
      const d = new Date();
      d.setFullYear(d.getFullYear() + 10);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
    },
    titleLabel() {
      const match = this.titleOptions.find((item) => item.type === this.form.type);
      return match ? match.label : "请选择标题";
    },
    filteredReminders() {
      if (this.tabFilter === 0) return this.reminders;
      // tabFilter 1=用药, 2=运动, 3=饮食, 4=睡眠
      const typeMap = {
        1: REMINDER_TYPE.MEDICATION,
        2: REMINDER_TYPE.EXERCISE,
        3: REMINDER_TYPE.DIET,
        4: REMINDER_TYPE.SLEEP
      };
      return this.reminders.filter((item) => Number(item.type) === typeMap[this.tabFilter]);
    },
    pendingCount() {
      return this.reminders.filter((item) => Number(item.status) === REMINDER_STATUS.PENDING).length;
    }
  },
  onShow() {
    this.fetchReminders();
  },
  methods: {
    fetchReminders() {
      const userId = uni.getStorageSync("userId") || 1;
      request("/api/reminder/list", "GET", { userId })
        .then((data) => {
          const raw = Array.isArray(data) ? data : (data?.list || data?.records || []);
          const mapped = raw
            .filter((item) => this.isTodayReminder(item.remindTime))
            .map((item) => ({
              id: item.id,
              title: item.title,
              content: item.content || "提醒事项",
              time: item.remindTime || "",
              type: Number(item.type || 0),
              status: Number(item.status ?? 0)
            }));
          this.setReminders(this.decorate(mapped));
        })
        .catch(() => {
          this.setReminders([]);
        });
    },
    setReminders(list) {
      this.reminders = list;
    },
    openAdd() {
      this.showModal = true;
      this.editingId = null;
      this.form = {
        type: REMINDER_TYPE.MEDICATION,
        content: "",
        remindDate: "",
        remindTime: ""
      };
    },
    openEdit(item) {
      if (!item) return;
      const parts = this.splitDateTime(item.time);
      this.editingId = item.id;
      this.showModal = true;
      this.form = {
        type: Number(item.type || REMINDER_TYPE.MEDICATION),
        content: item.content || "",
        remindDate: parts.date,
        remindTime: parts.time
      };
    },
    closeModal() {
      this.showModal = false;
    },
    onTitleChange(e) {
      const idx = Number(e.detail.value || 0);
      const option = this.titleOptions[idx];
      if (option) {
        this.form.type = option.type;
      }
    },
    onDateChange(e) {
      this.form.remindDate = e.detail.value;
    },
    onTimeChange(e) {
      this.form.remindTime = e.detail.value;
    },
    submitAdd() {
      if (!this.form.remindDate || !this.form.remindTime) {
        uni.showToast({ title: "请选择提醒时间", icon: "none" });
        return;
      }
      if (!/^\d{4}-\d{2}-\d{2}$/.test(this.form.remindDate)) {
        uni.showToast({ title: "日期格式不正确", icon: "none" });
        return;
      }
      if (!/^\d{2}:\d{2}$/.test(this.form.remindTime)) {
        uni.showToast({ title: "时间格式不正确", icon: "none" });
        return;
      }
      const now = new Date();
      const todayStr = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}-${String(now.getDate()).padStart(2, "0")}`;
      if (this.form.remindDate === todayStr) {
        const [h, m] = this.form.remindTime.split(":").map(Number);
        const chosenMin = h * 60 + (m || 0);
        const currentMin = now.getHours() * 60 + now.getMinutes();
        if (chosenMin <= currentMin) {
          uni.showToast({ title: "请选择未来的时间", icon: "none" });
          return;
        }
      }
      this.saving = true;
      const userId = uni.getStorageSync("userId") || 1;
      const remindTime = `${this.form.remindDate} ${this.form.remindTime}:00`;
      const payload = {
        userId,
        title: this.titleLabel,
        type: this.form.type,
        content: this.form.content,
        remindTime
      };
      const url = this.editingId ? "/api/reminder/update" : "/api/reminder/add";
      if (this.editingId) {
        payload.id = this.editingId;
      }
      request(url, "POST", payload)
        .then(() => {
          uni.showToast({ title: "已保存", icon: "success" });
          this.closeModal();
          this.fetchReminders();
        })
        .catch((err) => {
          uni.showToast({ title: err.message || "保存失败", icon: "none" });
        })
        .finally(() => {
          this.saving = false;
        });
    },
    decorate(list) {
      return list.map((item) => {
        const timeDisplay = this.formatTimeDisplay(item.time);
        const tagLabel = this.tagLabel(item.type);
        const statusTag = this.statusTagLabel(item.status, item.time);
        const icon = this.typeIcon(item.type);
        return { ...item, timeDisplay, tagLabel, statusTag, icon };
      });
    },
    formatTimeDisplay(value) {
      if (!value) return "未设置";
      const timeStr = value.includes("T") ? value.replace("T", " ") : value;
      const parts = timeStr.split(" ");
      if (parts.length >= 2) {
        return parts[1].slice(0, 5); // 只返回 HH:mm
      }
      return timeStr;
    },
    statusTagLabel(status, remindTime) {
      const s = Number(status);
      if (s === REMINDER_STATUS.DONE) return "已完成";
      if (s === REMINDER_STATUS.IGNORED) return "已忽略";
      if (s === REMINDER_STATUS.PENDING && this.isPastReminder(remindTime)) return "已提醒";
      return "";
    },
    tagLabel(type) {
      switch (Number(type)) {
        case REMINDER_TYPE.EXERCISE:
          return "运动";
        case REMINDER_TYPE.DIET:
          return "饮食";
        case REMINDER_TYPE.SLEEP:
          return "睡眠";
        case REMINDER_TYPE.MEDICATION:
          return "用药";
        default:
          return "提醒";
      }
    },
    typeIcon(type) {
      switch (Number(type)) {
        case REMINDER_TYPE.EXERCISE:
          return "/static/tabbar/sport.png";
        case REMINDER_TYPE.DIET:
          return "/static/tabbar/food.png";
        case REMINDER_TYPE.SLEEP:
          return "/static/tabbar/sleep.png";
        case REMINDER_TYPE.MEDICATION:
          return "/static/tabbar/pills.png";
        default:
          return "/static/tabbar/remind.png";
      }
    },
    formatTime(value) {
      if (!value) return "未设置";
      const normalized = value.includes("T") ? value.replace("T", " ") : value;
      if (/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}$/.test(normalized)) {
        return `${normalized}:00`;
      }
      return normalized;
    },
    timeLeft(value) {
      if (!value) return "";
      const timeStr = value.includes("T") ? value.replace("T", " ") : value;
      const normalized = /^\d{4}-\d{2}-\d{2} \d{2}:\d{2}$/.test(timeStr)
        ? `${timeStr}:00`
        : timeStr;
      const date = new Date(normalized.replace(/-/g, "/"));
      if (Number.isNaN(date.getTime())) {
        return "";
      }
      const diff = date.getTime() - Date.now();
      if (diff <= 0) {
        return "已到提醒时间";
      }
      const mins = Math.floor(diff / 60000);
      const hours = Math.floor(mins / 60);
      const leftMins = mins % 60;
      if (hours <= 0) {
        return `还有 ${leftMins} 分钟`;
      }
      return `还有 ${hours} 小时 ${leftMins} 分钟`;
    },
    splitDateTime(value) {
      if (!value) {
        return { date: "", time: "" };
      }
      const timeStr = value.includes("T") ? value.replace("T", " ") : value;
      const parts = timeStr.split(" ");
      if (parts.length >= 2) {
        const date = parts[0];
        const time = parts[1].slice(0, 5);
        return { date, time };
      }
      if (/^\d{2}:\d{2}/.test(timeStr)) {
        const now = new Date();
        const y = now.getFullYear();
        const m = String(now.getMonth() + 1).padStart(2, "0");
        const d = String(now.getDate()).padStart(2, "0");
        return { date: `${y}-${m}-${d}`, time: timeStr.slice(0, 5) };
      }
      return { date: "", time: "" };
    },
    openActionMenu(item) {
      if (!item) return;
      this.actionItem = item;
      uni.showActionSheet({
        itemList: ["延期", "忽略"],
        success: (res) => {
          const it = this.actionItem;
          this.actionItem = null;
          if (res.tapIndex === 0) this.openPostpone(it);
          else if (res.tapIndex === 1) this.setStatus(it, REMINDER_STATUS.IGNORED);
        },
        fail: () => {
          this.actionItem = null;
        }
      });
    },
    setStatus(item, status) {
      if (!item || !item.id) return;
      request("/api/reminder/status", "POST", { id: item.id, status })
        .then(() => {
          uni.showToast({
            title: status === REMINDER_STATUS.DONE ? "已标记完成" : "已忽略",
            icon: "success"
          });
          this.fetchReminders();
        })
        .catch((err) => {
          uni.showToast({ title: err.message || "操作失败", icon: "none" });
        });
    },
    openPostpone(item) {
      if (!item || !item.id) return;
      const parts = this.splitDateTime(item.time);
      this.postponeItem = item;
      this.postponeDate = parts.date || this.minDate;
      this.postponeTime = parts.time || "09:00";
      this.showPostponeModal = true;
    },
    closePostpone() {
      this.showPostponeModal = false;
      this.postponeItem = null;
    },
    onPostponeDateChange(e) {
      this.postponeDate = e.detail.value || "";
    },
    onPostponeTimeChange(e) {
      this.postponeTime = e.detail.value || "";
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
    isPastReminder(value) {
      const date = this.parseReminderDate(value);
      if (!date) return false;
      return date.getTime() <= Date.now();
    },
    confirmPostpone() {
      if (!this.postponeItem || !this.postponeItem.id) {
        this.closePostpone();
        return;
      }
      if (!this.postponeDate || !this.postponeTime) {
        uni.showToast({ title: "请选择日期和时间", icon: "none" });
        return;
      }
      const remindTime = `${this.postponeDate} ${this.postponeTime}:00`;
      this.postponeSaving = true;
      request("/api/reminder/status", "POST", { id: this.postponeItem.id, remind_time: remindTime })
        .then(() => {
          uni.showToast({ title: "已延期", icon: "success" });
          this.closePostpone();
          this.fetchReminders();
        })
        .catch((err) => {
          uni.showToast({ title: err.message || "延期失败", icon: "none" });
        })
        .finally(() => {
          this.postponeSaving = false;
        });
    }
  }
};
</script>

<style>
/* 页面根容器 */
.page-root {
  min-height: 100vh;
  background: #FAF8F5;
}

.page {
  padding: 24rpx 32rpx 180rpx;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 4rpx;
}

.page-title {
  font-size: 40rpx;
  font-weight: 800;
  color: #1a1c1a;
}

.page-count {
  font-size: 24rpx;
  color: #8B7355;
  background: #FFF4ED;
  padding: 6rpx 20rpx;
  border-radius: 24rpx;
  font-weight: 500;
}

/* 筛选标签 */
.chips {
  display: flex;
  gap: 16rpx;
  padding: 0 4rpx;
}

.chip {
  padding: 12rpx 28rpx;
  border-radius: 999rpx;
  background: #fff;
  color: #564337;
  font-size: 26rpx;
  font-weight: 500;
  border: 1px solid #E9E1D8;
}

.chip.active {
  background: #A23F00;
  color: #fff;
  border-color: #A23F00;
}

/* 时间线 */
.reminder-timeline {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.timeline-item {
  display: flex;
  gap: 20rpx;
}

.timeline-line {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 24rpx;
  flex-shrink: 0;
  padding-top: 8rpx;
}

.timeline-dot {
  width: 20rpx;
  height: 20rpx;
  border-radius: 50%;
  flex-shrink: 0;
  z-index: 1;
  border: 4px solid #fff;
  box-shadow: 0 0 0 2px currentColor;
}

.timeline-connector {
  width: 2rpx;
  flex: 1;
  background: #E9E1D8;
  margin: 8rpx 0;
}

/* 提醒卡片 */
.reminder-card {
  flex: 1;
  margin-bottom: 20rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 20rpx;
  border: 1px solid #E9E1D8;
}

.reminder-completed {
  opacity: 0.6;
}

.reminder-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.reminder-time {
  font-size: 28rpx;
  font-weight: 600;
  color: #A23F00;
}

.reminder-status {
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 16rpx;
  font-weight: 500;
}

.reminder-status.status-completed {
  background: #D1FAE5;
  color: #059669;
}

.reminder-status.status-pending {
  background: #FEF3C7;
  color: #D97706;
}

.reminder-status.status-ignored {
  background: #FEE2E2;
  color: #DC2626;
}

.timeline-dot.status-completed {
  background: #10B981;
  color: #10B981;
}

.timeline-dot.status-pending {
  background: #F59E0B;
  color: #F59E0B;
}

.timeline-dot.status-ignored {
  background: #EF4444;
  color: #EF4444;
}

.reminder-body {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 20rpx;
}

.reminder-icon-wrapper {
  width: 56rpx;
  height: 56rpx;
  background: #FAF8F5;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border: 1px solid #E9E1D8;
}

.reminder-icon {
  width: 32rpx;
  height: 32rpx;
}

.reminder-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.reminder-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1a1c1a;
}

.reminder-content {
  font-size: 24rpx;
  color: #564337;
}

.reminder-actions {
  display: flex;
  gap: 16rpx;
}

.action-btn {
  flex: 1;
  padding: 20rpx 24rpx;
  font-size: 26rpx;
  font-weight: 600;
  border-radius: 30rpx;
  text-align: center;
}

.confirm-btn {
  background: #A23F00;
  color: #fff;
}

.delay-btn {
  background: #FAF8F5;
  color: #564337;
  border: 1px solid #E9E1D8;
}

/* FAB 按钮 */
.fab-container {
  position: fixed;
  bottom: 180rpx;
  right: 32rpx;
  z-index: 50;
}

.fab {
  width: 100rpx;
  height: 100rpx;
  background: linear-gradient(135deg, #A23F00 0%, #FA7025 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(162, 63, 0, 0.3);
}

.fab-icon {
  font-size: 48rpx;
  color: #fff;
  font-weight: 300;
}

/* 空状态 */
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

/* 弹窗样式 */
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
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.field-label {
  font-size: 26rpx;
  color: #8B7355;
  font-weight: 500;
}

.picker {
  border: 1px solid #E9E1D8;
  border-radius: 24rpx;
  padding: 24rpx 28rpx;
  font-size: 28rpx;
  background: #fff;
  color: #1a1c1a;
  height: 88rpx;
  line-height: 40rpx;
  box-sizing: border-box;
}

.picker-group {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20rpx;
}

.input {
  border: 1px solid #E9E1D8;
  border-radius: 24rpx;
  padding: 24rpx 28rpx;
  font-size: 28rpx;
  color: #1a1c1a;
  background: #fff;
  box-sizing: border-box;
  height: 88rpx;
  line-height: 40rpx;
}

.pill-wrap {
  display: flex;
  gap: 16rpx;
}

.pill-wrap .pill {
  flex: 1;
  padding: 20rpx 24rpx;
  border-radius: 24rpx;
  border: 1px solid #E9E1D8;
  background: #fff;
  color: #564337;
  font-size: 26rpx;
  text-align: center;
  transition: all 0.2s;
}

.pill-wrap .pill.active {
  background: #A23F00;
  border-color: #A23F00;
  color: #fff;
}

.modal-sheet-btn.primary {
  width: 100%;
  height: 96rpx;
  border-radius: 48rpx;
  background: linear-gradient(135deg, #A23F00 0%, #FA7025 100%);
  color: #fff;
  font-size: 32rpx;
  font-weight: 600;
  border: none;
  margin-top: 16rpx;
}

.modal-sheet-btn::after {
  border: none;
}

.note {
  font-size: 20rpx;
  color: #8B7355;
  padding: 20rpx 4rpx;
}
</style>
