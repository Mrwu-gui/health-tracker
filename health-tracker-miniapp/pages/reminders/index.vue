<template>
  <view class="page">
    <view class="header">
      <view />
      <button class="add-btn" @tap="openAdd">+</button>
    </view>

    <view class="tabs">
      <button class="tab" :class="{ active: tabFilter === 0 }" @tap="tabFilter = 0">全部</button>
      <button class="tab" :class="{ active: tabFilter === 1 }" @tap="tabFilter = 1">运动</button>
      <button class="tab" :class="{ active: tabFilter === 2 }" @tap="tabFilter = 2">饮食</button>
      <button class="tab" :class="{ active: tabFilter === 3 }" @tap="tabFilter = 3">睡眠</button>
      
    </view>

    <view class="list">
      <view v-if="filteredReminders.length === 0" class="empty-state">
        <image class="empty-state-icon" src="/static/tabbar/remind.png" mode="widthFix" />
        <text class="empty-state-title">暂无提醒</text>
        <text class="empty-state-desc">点击右上角 + 添加运动、饮食或睡眠提醒</text>
      </view>
      <view v-for="item in filteredReminders" v-else :key="item.id" class="card" @tap="openEdit(item)">
        <view class="row">
          <view>
            <text class="name">{{ item.title }}</text>
            <text class="desc">{{ item.timeLabel }} · {{ item.content }}</text>
          </view>
          <text class="tag">{{ item.tagLabel }}</text>
        </view>
        <text v-if="item.countdown" class="countdown">{{ item.countdown }}</text>
      </view>
    </view>

    <text class="note">需在「授权与隐私」中开启订阅消息</text>

    <view v-if="showModal" class="modal-mask" @tap="closeModal">
      <view class="modal-sheet" @tap.stop>
        <view class="modal-sheet-bar" />
        <view class="modal-sheet-head">
          <text class="modal-sheet-title">{{ editingId ? "编辑提醒" : "添加提醒" }}</text>
          <text class="modal-sheet-close" @tap="closeModal">×</text>
        </view>
        <view class="modal-sheet-body">
        <view class="field">
          <text class="field-label">提醒标题</text>
          <picker mode="selector" :range="titleLabels" :value="titleIndex" @change="onTitleChange">
            <view class="picker">{{ titleLabel }}</view>
          </picker>
        </view>
        <view class="field">
          <text class="field-label">提醒内容</text>
          <input class="input" v-model="form.content" placeholder="提醒内容" />
        </view>
        <view class="field">
          <text class="field-label">提醒时间</text>
          <view class="picker-group">
            <picker mode="date" :value="form.remindDate" :start="minDate" :end="maxDate" @change="onDateChange">
              <view class="picker">{{ form.remindDate || "选择日期" }}</view>
            </picker>
            <picker mode="time" @change="onTimeChange">
              <view class="picker">{{ form.remindTime || "选择时间" }}</view>
            </picker>
          </view>
        </view>
        <button class="modal-sheet-btn primary" @tap="submitAdd" :disabled="saving">
          {{ saving ? "保存中..." : editingId ? "保存修改" : "保存" }}
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
      reminders: [],
      tabFilter: 0,
      showModal: false,
      saving: false,
      editingId: null,
      titleOptions: [
        { label: "运动提醒", type: 1 },
        { label: "饮食提醒", type: 2 },
        { label: "睡眠提醒", type: 3 }
      ],
      form: {
        title: "",
        type: 1,
        content: "",
        remindDate: "",
        remindTime: ""
      }
    };
  },
  computed: {
    minDate() {
      const n = new Date();
      return `${n.getFullYear()}-${String(n.getMonth() + 1).padStart(2, "0")}-${String(n.getDate()).padStart(2, "0")}`;
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
    titleLabels() {
      return this.titleOptions.map((item) => item.label);
    },
    titleIndex() {
      const idx = this.titleOptions.findIndex((item) => item.type === this.form.type);
      return idx === -1 ? 0 : idx;
    },
    filteredReminders() {
      if (this.tabFilter === 0) return this.reminders;
      return this.reminders.filter((item) => Number(item.type) === this.tabFilter);
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
            .filter((item) => Number(item.type) !== 4)
            .map((item) => ({
              id: item.id,
              title: item.title,
              content: item.content || "提醒事项",
              time: item.remindTime || "",
              type: Number(item.type || 0)
            }));
          this.setReminders(this.decorate(mapped));
        })
        .catch(() => {
          this.setReminders([]);
        });
    },
    setReminders(list) {
      if (typeof this.$set === "function") {
        this.$set(this, "reminders", list);
      } else {
        this.reminders = list;
      }
    },
    openAdd() {
      this.showModal = true;
      this.editingId = null;
      this.form = { title: "", type: 1, content: "", remindDate: "", remindTime: "" };
    },
    openEdit(item) {
      if (!item) return;
      const parts = this.splitDateTime(item.time);
      this.editingId = item.id;
      this.showModal = true;
      this.form = {
        title: item.title || "",
        type: Number(item.type || 1),
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
        this.form.title = option.label;
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
        title: this.form.title || this.titleLabel,
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
        const timeLabel = this.formatTime(item.time);
        const tagLabel = this.tagLabel(item.type);
        const countdown = item.type === 4 ? this.timeLeft(item.time) : "";
        return { ...item, timeLabel, tagLabel, countdown };
      });
    },
    tagLabel(type) {
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
    }
  }
};
</script>

<style>
.page {
  padding: 18px;
  min-height: 100vh;
  background: #faf8f5;
  color: #0f172a;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 18px;
  font-weight: 600;
}

.add-btn {
  width: 36px;
  height: 36px;
  border-radius: 18px;
  border: 1px solid #e8e2db;
  background: #ffffff;
  color: #2563eb;
  font-size: 18px;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tabs {
  display: inline-flex;
  background: #e8e2db;
  border-radius: 999px;
  padding: 2px;
  gap: 4px;
}

.tab {
  font-size: 10px;
  padding: 4px 8px;
  border-radius: 999px;
  color: #64748b;
}

.tab.active {
  background: #ffffff;
  color: #0f172a;
}

.tag {
  font-size: 10px;
  color: #2563eb;
  background: #dbeafe;
  padding: 4px 8px;
  border-radius: 999px;
  flex-shrink: 0;
}

.countdown {
  display: block;
  margin-top: 6px;
  font-size: 10px;
  color: #f59e0b;
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
  border-radius: 20px 20px 0 0;
  padding-bottom: env(safe-area-inset-bottom);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.modal-sheet-bar {
  width: 36px;
  height: 4px;
  border-radius: 2px;
  background: #e8e2db;
  margin: 10px auto 0;
}

.modal-sheet-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px 16px;
  border-bottom: 1px solid #f2ede8;
}

.modal-sheet-title {
  font-size: 17px;
  font-weight: 600;
  color: #0f172a;
}

.modal-sheet-close {
  font-size: 24px;
  color: #94a3b8;
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
  color: #64748b;
}

.picker {
  border: 1px solid #e8e2db;
  border-radius: 12px;
  padding: 12px 14px;
  font-size: 14px;
  background: #fff;
  color: #0f172a;
}

.picker-group {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.input {
  border: 1px solid #e8e2db;
  border-radius: 12px;
  padding: 12px 14px;
  font-size: 14px;
  color: #0f172a;
  background: #fff;
}

.modal-sheet-btn.primary {
  width: 100%;
  padding: 14px;
  border-radius: 14px;
  background: #2563eb;
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  border: none;
}

.list {
  display: grid;
  gap: 10px;
}

.empty-state {
  background: #fefcf9;
  border-radius: 16px;
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
  color: #94a3b8;
}

.card {
  background: #ffffff;
  border-radius: 16px;
  padding: 12px;
  border: 1px solid #e8e2db;
}

.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.name {
  font-size: 12px;
  font-weight: 600;
  display: block;
}

.desc {
  font-size: 10px;
  color: #94a3b8;
  margin-top: 4px;
  display: block;
}

.note {
  font-size: 10px;
  color: #94a3b8;
}
</style>
