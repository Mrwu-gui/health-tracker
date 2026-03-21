<template>
  <view class="page-root">
    <!-- 空状态页面 -->
    <view v-if="meds.length === 0" class="empty-page">
      <view class="empty-content">
        <view class="empty-illustration">
          <image class="empty-illustration-icon" src="/static/tabbar/chiyao_w.png" mode="aspectFit" />
        </view>
        <text class="empty-desc">暂无用药记录，点击下方按钮添加首个药品，让AI准时提醒您。</text>

        <view class="empty-features">
          <view class="feature-item">
            <image class="feature-icon" src="/static/tabbar/clock.png" mode="aspectFit" />
            <text class="feature-text">准时提醒服药</text>
          </view>
          <view class="feature-item">
            <image class="feature-icon" src="/static/tabbar/history_s.png" mode="aspectFit" />
            <text class="feature-text">记录用药历史</text>
          </view>
          <view class="feature-item">
            <image class="feature-icon" src="/static/tabbar/tips_s.png" mode="aspectFit" />
            <text class="feature-text">智能健康建议</text>
          </view>
        </view>

        <button class="empty-add-btn pill" @tap="openAdd">
          <image class="empty-add-icon" src="/static/tabbar/add.png" mode="aspectFit" />
          <text class="empty-add-text">添加第一种药物</text>
        </button>
      </view>
    </view>

    <!-- 有数据时的页面 -->
    <view v-else class="page">
      <!-- AI 建议卡片 -->
      <view v-if="aiLoadingSuggestion" class="ai-tip-card card">
        <text class="ai-tip-text">✨ 智康正在分析您的用药情况...</text>
      </view>
      <view v-else-if="aiSuggestion" class="ai-suggestion-card card" @tap="aiSuggestionExpanded = !aiSuggestionExpanded">
        <view class="ai-suggestion-head">
          <image class="ai-suggestion-icon" src="/static/tabbar/tips.png" mode="aspectFit" />
          <text class="ai-suggestion-title">智康建议</text>
          <text class="ai-suggestion-arrow">{{ aiSuggestionExpanded ? '收起' : '展开' }}</text>
        </view>
        <text v-if="aiSuggestionExpanded" class="ai-suggestion-text">{{ aiSuggestion }}</text>
      </view>

      <!-- 今日用药标题 -->
      <view class="section-header">
        <text class="section-title">今日用药</text>
        <text class="section-count">{{ todayMeds.length }} 项</text>
      </view>

      <!-- 用药时间线 -->
      <view class="medication-timeline">
        <view v-if="todayMeds.length === 0" class="empty-today card">
          <text class="empty-today-icon">📅</text>
          <text class="empty-today-title">今日暂无用药计划</text>
          <text class="empty-today-desc">点击上方卡片添加</text>
        </view>

        <view v-for="(item, index) in todayMeds" :key="item.id" class="timeline-item">
          <!-- 时间线 -->
          <view class="timeline-line">
            <view class="timeline-dot" :class="{ 'status-completed': item.status === 'completed', 'status-pending': item.status === 'pending', 'status-overdue': item.status === 'overdue' }"></view>
            <view v-if="index < todayMeds.length - 1" class="timeline-connector"></view>
          </view>

          <!-- 药品卡片 -->
          <view class="medication-card card" :class="{ 'medication-completed': item.status === 'completed' }">
            <view class="medication-header">
              <view class="medication-info">
                <text class="medication-time">{{ item.remindTime || '未设置' }}</text>
                <text class="medication-status" :class="{ 'status-completed': item.status === 'completed', 'status-pending': item.status === 'pending', 'status-overdue': item.status === 'overdue' }">{{ getStatusText(item.status) }}</text>
              </view>
            </view>

            <view class="medication-body">
              <view class="medication-icon-wrapper">
                <image class="medication-icon" src="/static/tabbar/pills.png" mode="aspectFit" />
              </view>
              <view class="medication-details">
                <text class="medication-name">{{ item.drugName }}</text>
                <text class="medication-dosage">{{ item.dosage }} · {{ item.frequency }}</text>
              </view>
            </view>

            <view class="medication-actions">
              <button
                v-if="item.status === 'pending'"
                class="action-btn confirm-btn pill"
                @tap.stop="confirmMedication(item)"
              >
                确认服用
              </button>
              <button
                v-if="item.status === 'pending'"
                class="action-btn delay-btn pill"
                @tap.stop="delayMedication(item)"
              >
                延期
              </button>
              <button
                v-if="item.status === 'completed'"
                class="action-btn completed-btn pill"
                disabled
              >
                已完成
              </button>
            </view>
          </view>
        </view>
      </view>

      <!-- 所有药物标题 -->
      <view class="section-header">
        <text class="section-title">所有药物</text>
        <text class="section-count">{{ meds.length }} 种</text>
      </view>

      <!-- 所有药物列表 -->
      <view class="medication-list">
        <view v-for="item in meds" :key="item.id" class="medication-item card" @tap="openEdit(item)">
          <view class="medication-item-icon">
            <image class="medication-item-icon-img" src="/static/tabbar/pills.png" mode="aspectFit" />
          </view>
          <view class="medication-item-content">
            <text class="medication-item-name">{{ item.drugName }}</text>
            <text class="medication-item-info">{{ item.dosage }} · {{ item.frequency }}</text>
          </view>
          <view class="medication-item-status" :class="{ active: item.enabled }">
            <text>{{ item.enabled ? '已启用' : '已停用' }}</text>
          </view>
        </view>
      </view>

      <view class="fab-container">
        <view class="fab pill" @tap="openAdd">
          <text class="fab-icon">+</text>
        </view>
      </view>
    </view>

    <!-- 添加/编辑弹窗 -->
    <view v-if="showModal" class="modal-mask" @tap="closeModal">
      <view class="modal-sheet" @tap.stop>
        <view class="modal-sheet-bar" />
        <view class="modal-sheet-head">
          <text class="modal-sheet-title">{{ editingId ? "编辑药物" : "添加药物" }}</text>
          <text class="modal-sheet-close" @tap="closeModal">×</text>
        </view>
        <scroll-view class="modal-sheet-body" scroll-y>
          <view class="field-group">
            <text class="field-title">药物名称</text>
            <view class="input-wrapper">
              <image class="input-icon" src="/static/tabbar/pills.png" mode="aspectFit" />
              <input class="input-main" v-model="form.drugName" placeholder="如 氨氯地平" />
            </view>
          </view>

          <view class="field-row">
            <view class="field-group field-half">
              <text class="field-title">剂量</text>
              <view class="input-wrapper">
                <input class="input-main" v-model="form.dosage" placeholder="如 5mg" />
              </view>
            </view>
            <view class="field-group field-half">
              <text class="field-title">用法</text>
              <view class="input-wrapper">
                <input class="input-main" v-model="form.frequency" placeholder="每日1次" />
              </view>
            </view>
          </view>

          <view class="field-group">
            <text class="field-title">提醒日期</text>
            <picker mode="date" @change="onDateChange">
              <view class="input-wrapper picker-wrapper">
                <image class="input-icon" src="/static/tabbar/remind.png" mode="aspectFit" />
                <text class="picker-text">{{ form.remindDate || "请选择日期" }}</text>
                <text class="picker-arrow">›</text>
              </view>
            </picker>
          </view>

          <view class="field-group">
            <text class="field-title">提醒时间</text>
            <picker mode="time" @change="onTimeChange">
              <view class="input-wrapper picker-wrapper">
                <image class="input-icon" src="/static/tabbar/alarm-clock.png" mode="aspectFit" />
                <text class="picker-text">{{ form.remindTime || "请选择时间" }}</text>
                <text class="picker-arrow">›</text>
              </view>
            </picker>
          </view>
        </scroll-view>
        <view class="modal-sheet-footer">
          <button class="save-btn pill" @tap="submitAdd" :disabled="saving">
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
      meds: [],
      todayMeds: [],
      aiSuggestion: "",
      aiSuggestionExpanded: false,
      aiLoadingSuggestion: false,
      showModal: false,
      saving: false,
      editingId: null,
      form: {
        drugName: "",
        dosage: "",
        frequency: "",
        remindTime: "",
        remindDate: "",
        startDate: ""
      }
    };
  },
  onShow() {
    this.fetchMeds();
  },
  methods: {
    fetchMeds() {
      const userId = uni.getStorageSync("userId") || 1;
      request("/api/medication/list", "GET", { userId })
        .then((list) => {
          if (Array.isArray(list) && list.length > 0) {
            const mapped = list.map((item) => ({
              ...item,
              enabled: true,
              status: this.getMedicationStatus(item)
            }));
            this.meds = mapped;
            this.todayMeds = mapped.filter(item => {
              const today = new Date().toISOString().slice(0, 10);
              return item.remindTime && item.remindTime.includes(today);
            });
          } else {
            this.meds = [];
            this.todayMeds = [];
          }
        })
        .catch(() => {
          this.meds = [];
          this.todayMeds = [];
        });
    },
    getMedicationStatus(item) {
      // 简单的状态判断逻辑，可根据实际需求调整
      if (!item.remindTime) return 'pending';
      const now = new Date();
      const remindDate = new Date(item.remindTime);
      if (remindDate < now) return 'overdue';
      return 'pending';
    },
    getStatusClass(status) {
      const statusMap = {
        'completed': 'status-completed',
        'pending': 'status-pending',
        'overdue': 'status-overdue'
      };
      return statusMap[status] || 'status-pending';
    },
    getStatusText(status) {
      const statusMap = {
        'completed': '已完成',
        'pending': '待服用',
        'overdue': '已过期'
      };
      return statusMap[status] || '待服用';
    },
    confirmMedication(item) {
      if (!item || !item.id) return;
      const userId = uni.getStorageSync("userId") || 1;
      const parsed = this.parseRemindDateTime(item.remindTime);
      const payload = {
        userId,
        medicationId: item.id,
        date: parsed.date,
        time: parsed.time,
        status: 1
      };
      request("/api/medication/record/add", "POST", payload)
        .then(() => {
          uni.showToast({ title: "已确认服用", icon: "success" });
          this.fetchMeds();
        })
        .catch((err) => {
          uni.showToast({ title: err.message || "操作失败", icon: "none" });
        });
    },
    delayMedication(item) {
      if (!item || !item.id || !item.remindTime) {
        uni.showToast({ title: "请先设置提醒时间", icon: "none" });
        return;
      }
      const nextTime = this.addMinutes(item.remindTime, 30);
      if (!nextTime) {
        uni.showToast({ title: "时间格式不正确", icon: "none" });
        return;
      }
      const payload = this.buildMedicationUpdatePayload(item, nextTime);
      request("/api/medication/update", "POST", payload)
        .then(() => {
          uni.showToast({ title: "已延期 30 分钟", icon: "success" });
          this.fetchMeds();
        })
        .catch((err) => {
          uni.showToast({ title: err.message || "延期失败", icon: "none" });
        });
    },
    parseRemindDateTime(value) {
      const now = new Date();
      if (!value) {
        return {
          date: this.formatDate(now),
          time: this.formatTime(now)
        };
      }
      const normalized = String(value).replace("T", " ").trim();
      if (normalized.includes(" ")) {
        const parts = normalized.split(" ");
        return {
          date: parts[0] || this.formatDate(now),
          time: (parts[1] || "00:00").slice(0, 5)
        };
      }
      if (/^\d{2}:\d{2}$/.test(normalized)) {
        return { date: this.formatDate(now), time: normalized };
      }
      return { date: this.formatDate(now), time: this.formatTime(now) };
    },
    formatDate(date) {
      const pad = (n) => String(n).padStart(2, "0");
      return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
    },
    formatTime(date) {
      const pad = (n) => String(n).padStart(2, "0");
      return `${pad(date.getHours())}:${pad(date.getMinutes())}`;
    },
    addMinutes(value, minutes) {
      const normalized = String(value).replace("T", " ").trim();
      const parsed = new Date(normalized.replace(/-/g, "/"));
      if (Number.isNaN(parsed.getTime())) return null;
      parsed.setMinutes(parsed.getMinutes() + minutes);
      return `${this.formatDate(parsed)} ${this.formatTime(parsed)}`;
    },
    buildMedicationUpdatePayload(item, remindTime) {
      return {
        id: item.id,
        drugName: item.drugName,
        dosage: item.dosage,
        frequency: item.frequency,
        remindTime,
        stock: item.stock,
        stockThreshold: item.stockThreshold,
        startDate: item.startDate,
        endDate: item.endDate,
        notes: item.notes
      };
    },
    openAdd() {
      this.showModal = true;
      this.editingId = null;
      this.form = { drugName: "", dosage: "", frequency: "", remindTime: "", remindDate: "", startDate: "" };
    },
    openEdit(item) {
      if (!item) return;
      const split = this.splitDateTime(item.remindTime);
      this.editingId = item.id;
      this.showModal = true;
      this.form = {
        drugName: item.drugName || "",
        dosage: item.dosage || "",
        frequency: item.frequency || "",
        remindTime: split.time || "",
        remindDate: split.date || "",
        startDate: item.startDate || ""
      };
    },
    closeModal() {
      this.showModal = false;
    },
    onDateChange(e) {
      this.form.remindDate = e.detail.value;
    },
    onTimeChange(e) {
      this.form.remindTime = e.detail.value;
    },
    splitDateTime(value) {
      if (!value || typeof value !== "string") return { date: "", time: "" };
      const trimmed = value.trim();
      if (trimmed.includes(" ")) {
        const parts = trimmed.split(" ");
        return { date: parts[0] || "", time: (parts[1] || "").slice(0, 5) };
      }
      if (/^\d{2}:\d{2}$/.test(trimmed)) {
        return { date: "", time: trimmed };
      }
      return { date: "", time: "" };
    },
    submitAdd() {
      if (!this.form.drugName || !this.form.dosage || !this.form.frequency) {
        uni.showToast({ title: "请完整填写药物信息", icon: "none" });
        return;
      }
      this.saving = true;
      const userId = uni.getStorageSync("userId") || 1;
      const today = new Date().toISOString().slice(0, 10);
      if ((this.form.remindDate && !this.form.remindTime) || (!this.form.remindDate && this.form.remindTime)) {
        uni.showToast({ title: "请选择完整的提醒日期和时间", icon: "none" });
        this.saving = false;
        return;
      }
      const remindAt = this.form.remindDate && this.form.remindTime
        ? `${this.form.remindDate} ${this.form.remindTime}`
        : null;
      const payload = {
        userId,
        drugName: this.form.drugName,
        dosage: this.form.dosage,
        frequency: this.form.frequency,
        remindTime: remindAt,
        startDate: this.form.startDate || today
      };
      const url = this.editingId ? "/api/medication/update" : "/api/medication/add";
      if (this.editingId) {
        payload.id = this.editingId;
      }
      request(url, "POST", payload)
        .then(() => {
          uni.showToast({ title: "已保存", icon: "success" });
          this.closeModal();
          this.fetchMeds();
          if (!this.editingId) this.fetchAiSuggestionForMedication(payload);
        })
        .catch((err) => {
          uni.showToast({ title: err.message || "保存失败", icon: "none" });
        })
        .finally(() => {
          this.saving = false;
        });
    },
    fetchAiSuggestionForMedication(payload) {
      this.aiLoadingSuggestion = true;
      this.aiSuggestionExpanded = false;
      const prompt = `用户刚添加了药物：${payload.drugName} ${payload.dosage}，用法 ${payload.frequency || "未填"}，提醒时间 ${payload.remindTime || "未设置"}。请用 1～3 句话给出简要的用药提醒或健康建议，语气亲切。`;
      const userId = uni.getStorageSync("userId") || 1;
      request("/api/ai/chat", "POST", { userId, message: prompt, store: false })
        .then((res) => {
          if (res && res.content) this.aiSuggestion = String(res.content).trim();
        })
        .catch(() => {})
        .finally(() => {
          this.aiLoadingSuggestion = false;
        });
    }
  }
};
</script>

<style scoped>
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

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 40rpx;
  font-weight: 800;
  color: #1a1c1a;
  font-family: 'Manrope', sans-serif;
}

.add-btn {
  width: 68rpx;
  height: 68rpx;
  background: linear-gradient(135deg, #A23F00 0%, #FA7025 100%);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(162, 63, 0, 0.3);
  padding: 0;
  border: none;
}

.add-btn-icon {
  width: 40rpx;
  height: 40rpx;
}

/* 添加今日药品卡片 */
.add-today-card {
  background: linear-gradient(135deg, #A23F00 0%, #FA7025 100%);
  display: flex;
  align-items: center;
  gap: 24rpx;
  padding: 32rpx;
  overflow: hidden;
}

.add-today-icon {
  width: 80rpx;
  height: 80rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: var(--radius-card-sm);
  display: flex;
  align-items: center;
  justify-content: center;
}

.add-today-icon-img {
  width: 48rpx;
  height: 48rpx;
}

.add-today-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.add-today-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #ffffff;
}

.add-today-desc {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}

.add-today-arrow {
  font-size: 40rpx;
  color: rgba(255, 255, 255, 0.6);
}

/* AI 建议卡片 */
.ai-tip-card {
  background: linear-gradient(135deg, #FEF3C7 0%, #FEF9C3 100%);
  border: 2rpx solid #FDE68A;
}

.ai-tip-text {
  font-size: 24rpx;
  color: #92400E;
  font-weight: 500;
}

.ai-suggestion-card {
  background: #fff;
  border: 1px solid #E9E1D8;
  padding: 24rpx;
}

.ai-suggestion-head {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.ai-suggestion-icon {
  width: 40rpx;
  height: 40rpx;
}

.ai-suggestion-title {
  flex: 1;
  font-size: 28rpx;
  font-weight: 600;
  color: #A23F00;
}

.ai-suggestion-arrow {
  font-size: 24rpx;
  color: #8B7355;
  padding: 4rpx 16rpx;
  background: #FAF8F5;
  border-radius: 20rpx;
}

.ai-suggestion-text {
  font-size: 26rpx;
  line-height: 1.7;
  color: #564337;
  margin-top: 16rpx;
  padding-top: 16rpx;
  border-top: 1px solid #FAF8F5;
  display: block;
}

/* Section Header */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 24rpx;
  margin-bottom: 16rpx;
  padding: 0 4rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1a1c1a;
}

.section-count {
  font-size: 24rpx;
  color: #8B7355;
  font-weight: 500;
  background: #FAF8F5;
  padding: 4rpx 16rpx;
  border-radius: 20rpx;
}

/* Timeline */
.medication-timeline {
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

.status-completed {
  background: #10B981;
  color: #10B981;
}

.status-pending {
  background: #F59E0B;
  color: #F59E0B;
}

.status-overdue {
  background: #EF4444;
  color: #EF4444;
}

/* Medication Card */
.medication-card {
  flex: 1;
  margin-bottom: 20rpx;
  overflow: hidden;
  padding: 20rpx;
}

.medication-completed {
  opacity: 0.6;
}

.medication-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.medication-info {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.medication-time {
  font-size: 28rpx;
  font-weight: 600;
  color: #A23F00;
}

.medication-status {
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 16rpx;
  font-weight: 500;
}

.status-completed {
  background: #D1FAE5;
  color: #059669;
}

.status-pending {
  background: #FEF3C7;
  color: #D97706;
}

.status-overdue {
  background: #FEE2E2;
  color: #DC2626;
}

.medication-body {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 20rpx;
}

.medication-icon-wrapper {
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

.medication-icon {
  width: 32rpx;
  height: 32rpx;
}

.medication-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.medication-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #1a1c1a;
}

.medication-dosage {
  font-size: 24rpx;
  color: #564337;
}

.medication-actions {
  display: flex;
  gap: 16rpx;
}

.action-btn {
  flex: 1;
  padding: 20rpx 24rpx;
  font-size: 26rpx;
  font-weight: 600;
  border: none;
  border-radius: 30rpx;
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

.completed-btn {
  background: #D1FAE5;
  color: #059669;
}

/* Medication List */
.medication-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.medication-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 24rpx;
}

.medication-item-icon {
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

.medication-item-icon-img {
  width: 32rpx;
  height: 32rpx;
}

.medication-item-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.medication-item-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #1a1c1a;
}

.medication-item-info {
  font-size: 24rpx;
  color: #564337;
}

.medication-item-status {
  font-size: 20rpx;
  padding: 6rpx 16rpx;
  border-radius: 16rpx;
  background: #F3F4F6;
  color: #94A3B8;
  font-weight: 500;
}

.medication-item-status.active {
  background: #FEF3C7;
  color: #A23F00;
}

/* Empty State */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx 32rpx;
  text-align: center;
  border: 2rpx dashed #E9E1D8;
  background: #ffffff;
}

.empty-icon {
  font-size: 64rpx;
  margin-bottom: 16rpx;
  opacity: 0.5;
}

.empty-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1a1c1a;
  margin-bottom: 8rpx;
}

.empty-desc {
  font-size: 24rpx;
  color: #564337;
  opacity: 0.7;
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

.fab-container {
  position: fixed;
  bottom: 160rpx;
  right: 48rpx;
  z-index: 100;
}

.fab {
  width: 128rpx;
  height: 128rpx;
  background: #a23f00;
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

/* Empty Today */
.empty-today {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 48rpx 32rpx;
  text-align: center;
  border: 2rpx dashed #E9E1D8;
}

.empty-today-icon {
  font-size: 48rpx;
  margin-bottom: 12rpx;
  opacity: 0.5;
}

.empty-today-title {
  font-size: 24rpx;
  font-weight: 600;
  color: #1a1c1a;
  margin-bottom: 6rpx;
}

.empty-today-desc {
  font-size: 20rpx;
  color: #564337;
  opacity: 0.7;
}

/* Card */
.card {
  background: #ffffff;
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-card);
  overflow: hidden;
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
}

.field-group {
  margin-bottom: 32rpx;
}

.field-half {
  flex: 1;
}

.field-row {
  display: flex;
  gap: 20rpx;
}

.field-title {
  font-size: 26rpx;
  font-weight: 500;
  color: #8B7355;
  display: block;
  margin-bottom: 16rpx;
}

.input-wrapper {
  background: #ffffff;
  border-radius: 24rpx;
  border: 1px solid #E9E1D8;
  padding: 0 28rpx;
  display: flex;
  align-items: center;
  gap: 12rpx;
  box-sizing: border-box;
  height: 88rpx;
}

.input-icon {
  width: 32rpx;
  height: 32rpx;
}

.input-main {
  flex: 1;
  padding: 24rpx 0;
  font-size: 28rpx;
  color: #1a1c1a;
  background: transparent;
  border: none;
}

.picker-wrapper {
  cursor: pointer;
}

.picker-text {
  flex: 1;
  padding: 24rpx 0;
  font-size: 28rpx;
  color: #1a1c1a;
}

.picker-arrow {
  font-size: 32rpx;
  color: #94A3B8;
}

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

.save-btn::after {
  border: none;
}

.save-btn:active {
  transform: scale(0.98);
}
</style>
