<template>
  <view class="page-root">
    <!-- 空状态页面 -->
    <view v-if="allEmpty" class="empty-page">
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
        <!-- 按时间段分组显示 -->
        <view v-for="group in groupedMeds" :key="group.label" class="time-group">
          <view class="time-group-header">
            <image class="time-group-icon" :src="group.icon" mode="aspectFit" />
            <text class="time-group-label">{{ group.label }}</text>
            <text class="time-group-count">{{ group.items.length }}项</text>
          </view>

          <view v-for="item in group.items" :key="item.id" class="medication-card card" :class="{ 'medication-completed': item.status === 'completed' || item.status === 'overdue' }" @tap="openMedicationEdit(item)">
            <!-- 左侧状态弧线 -->
            <view class="medication-status-bar" :class="item.status"></view>

            <!-- 药品信息 -->
            <view class="medication-content">
              <view class="medication-icon-wrapper">
                <image class="medication-icon" src="/static/tabbar/remind_s.png" mode="aspectFit" />
              </view>
              <view class="medication-info">
                <text class="medication-name">{{ item.drugName }}</text>
                <text class="medication-dosage">{{ item.dosage }} · {{ item.frequency }}</text>
              </view>
            </view>

            <!-- 右侧状态和时间 -->
            <view class="medication-right">
              <text class="medication-status-tag" :class="item.status">{{ getStatusText(item.status) }}</text>
              <text class="medication-time">{{ formatTimeDisplay(item.remindTime) }}</text>
            </view>

            <!-- 操作入口按钮 -->
            <view v-if="item.status === 'pending'" class="medication-action-trigger" @tap.stop="openMedicationAction(item)">
              <view class="action-dots">
                <view class="action-dot"></view>
                <view class="action-dot"></view>
                <view class="action-dot"></view>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 所有药物列表已移除 -->

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

    <!-- 延期弹窗 -->
    <view v-if="showDelayModal" class="modal-mask" @tap="closeDelayModal">
      <view class="modal-sheet" @tap.stop>
        <view class="modal-sheet-bar" />
        <view class="modal-sheet-head">
          <text class="modal-sheet-title">延期用药</text>
          <text class="modal-sheet-close" @tap="closeDelayModal">×</text>
        </view>
        <view class="modal-sheet-body">
          <view class="field-group">
            <text class="field-title">延期日期</text>
            <picker mode="date" :value="delayDate" @change="onDelayDateChange">
              <view class="input-wrapper picker-wrapper">
                <text class="picker-text">{{ delayDate || '请选择日期' }}</text>
                <text class="picker-arrow">›</text>
              </view>
            </picker>
          </view>
          <view class="field-group">
            <text class="field-title">延期时间</text>
            <picker mode="time" :value="delayTime" @change="onDelayTimeChange">
              <view class="input-wrapper picker-wrapper">
                <text class="picker-text">{{ delayTime || '请选择时间' }}</text>
                <text class="picker-arrow">›</text>
              </view>
            </picker>
          </view>
        </view>
        <view class="modal-sheet-footer">
          <button class="save-btn pill" @tap="confirmDelay" :disabled="delaySaving">
            {{ delaySaving ? "保存中..." : "确定延期" }}
          </button>
        </view>
      </view>
    </view>

    <!-- 自定义操作菜单 -->
    <view v-if="showActionSheet" class="action-sheet-mask" @tap="closeActionSheet">
      <view class="action-sheet" @tap.stop>
        <view class="action-sheet-title">用药操作</view>
        <view class="action-sheet-subtitle">请确认您对当前用药的处理状态</view>
        
        <!-- 任务预览卡片 -->
        <view v-if="actionItem" class="action-preview-card">
          <view class="action-preview-icon-wrapper">
            <image class="action-preview-icon" src="/static/tabbar/remind_s.png" mode="aspectFit" />
          </view>
          <view class="action-preview-info">
            <text class="action-preview-status">待服用</text>
            <text class="action-preview-title">{{ actionItem.drugName }} {{ actionItem.dosage }}</text>
          </view>
          <view class="action-preview-time">
            <text class="action-preview-time-label">截止时间</text>
            <text class="action-preview-time-value">{{ formatTimeDisplay(actionItem.remindTime) }}</text>
          </view>
        </view>

        <view class="action-sheet-buttons">
          <view class="action-btn-primary" @tap="handleAction(0)">
            <text class="action-btn-icon">✓</text>
            <text class="action-btn-label">标记为已服用</text>
          </view>
          <view class="action-btn-secondary" @tap="handleAction(1)">
            <text class="action-btn-icon">⏱</text>
            <text class="action-btn-label">稍后提醒</text>
          </view>
        </view>
        <view class="action-sheet-cancel" @tap="closeActionSheet">取消</view>
      </view>
    </view>
  </view>
</template>

<script>
import { request } from "../../utils/api";

export default {
  data() {
    return {
      todayMeds: [],
      hasAnyMedication: false,
      aiSuggestion: "",
      aiSuggestionExpanded: false,
      aiLoadingSuggestion: false,
      showModal: false,
      saving: false,
      editingId: null,
      showDelayModal: false,
      delayItem: null,
      delayDate: "",
      delayTime: "",
      delaySaving: false,
      showActionSheet: false,
      actionItem: null,
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
  computed: {
    allEmpty() {
      return !this.hasAnyMedication;
    },
    // 按时间段分组
    groupedMeds() {
      const dawn = { label: '凌晨', icon: '/static/tabbar/star.png', items: [] };
      const morning = { label: '早晨', icon: '/static/tabbar/sun-one.png', items: [] };
      const noon = { label: '中午', icon: '/static/tabbar/sun.png', items: [] };
      const evening = { label: '晚上', icon: '/static/tabbar/moon.png', items: [] };

      this.todayMeds.forEach(item => {
        const time = this.formatTimeDisplay(item.remindTime);
        const hour = parseInt(time.split(':')[0], 10);

        if (hour >= 0 && hour < 6) {
          dawn.items.push(item);
        } else if (hour >= 6 && hour < 12) {
          morning.items.push(item);
        } else if (hour >= 12 && hour < 18) {
          noon.items.push(item);
        } else {
          evening.items.push(item);
        }
      });

      return [dawn, morning, noon, evening].filter(g => g.items.length > 0);
    }
  },
  onShow() {
    this.fetchMeds();
  },
  onLoad(options) {
    if (options && options.drugName) {
      const decoded = decodeURIComponent(options.drugName);
      this.openAddWithDrugName(decoded);
    }
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
            this.hasAnyMedication = true;
            this.todayMeds = mapped.filter(item => {
              const today = new Date().toISOString().slice(0, 10);
              return item.remindTime && item.remindTime.includes(today);
            }).sort((a, b) => {
              // 按时间正序排序
              const timeA = a.remindTime || '';
              const timeB = b.remindTime || '';
              return timeA.localeCompare(timeB);
            });
          } else {
            this.hasAnyMedication = false;
            this.todayMeds = [];
          }
        })
        .catch(() => {
          this.hasAnyMedication = false;
          this.todayMeds = [];
        });
    },
    formatTimeDisplay(value) {
      if (!value) return '未设置';
      const normalized = String(value).replace("T", " ").trim();
      if (normalized.includes(" ")) {
        const parts = normalized.split(" ");
        return parts[1] ? parts[1].slice(0, 5) : '未设置';
      }
      return normalized;
    },
    openMedicationEdit(item) {
      if (!item) return;
      // 已过期/已完成的项不能编辑
      if (item.status === 'completed' || item.status === 'overdue') {
        return;
      }
      this.openEdit(item);
    },
    openMedicationAction(item) {
      if (!item) return;
      // 已过期/已完成的项不能操作
      if (item.status !== 'pending') {
        return;
      }
      this.actionItem = item;
      this.showActionSheet = true;
    },
    closeActionSheet() {
      this.showActionSheet = false;
      this.actionItem = null;
    },
    handleAction(index) {
      const item = this.actionItem;
      this.closeActionSheet();
      if (index === 0) {
        this.confirmMedication(item);
      } else if (index === 1) {
        this.openDelayModal(item);
      }
    },
    openDelayModal(item) {
      if (!item || !item.id || !item.remindTime) {
        uni.showToast({ title: "请先设置提醒时间", icon: "none" });
        return;
      }
      this.delayItem = item;
      // 设置初始延期日期和时间
      const now = new Date();
      now.setMinutes(now.getMinutes() + 30);
      this.delayDate = this.formatDate(now);
      this.delayTime = this.formatTime(now);
      this.showDelayModal = true;
    },
    confirmDelay() {
      if (!this.delayItem || !this.delayItem.id) {
        this.closeDelayModal();
        return;
      }
      if (!this.delayDate || !this.delayTime) {
        uni.showToast({ title: "请选择日期和时间", icon: "none" });
        return;
      }
      // 验证时间不能是过去的时间
      const selectedDateTime = new Date(`${this.delayDate} ${this.delayTime}`);
      if (selectedDateTime <= new Date()) {
        uni.showToast({ title: "请选择未来的时间", icon: "none" });
        return;
      }
      const remindTime = `${this.delayDate} ${this.delayTime}`;
      this.delaySaving = true;
      const payload = this.buildMedicationUpdatePayload(this.delayItem, remindTime);
      request("/api/medication/update", "POST", payload)
        .then(() => {
          uni.showToast({ title: "已延期", icon: "success" });
          this.closeDelayModal();
          this.fetchMeds();
        })
        .catch((err) => {
          uni.showToast({ title: err.message || "延期失败", icon: "none" });
        })
        .finally(() => {
          this.delaySaving = false;
        });
    },
    closeDelayModal() {
      this.showDelayModal = false;
      this.delayItem = null;
    },
    onDelayDateChange(e) {
      this.delayDate = e.detail.value || "";
    },
    onDelayTimeChange(e) {
      this.delayTime = e.detail.value || "";
    },
    getMedicationStatus(item) {
      // 简单的状态判断逻辑，可根据实际需求调整
      if (!item.remindTime) return 'pending';
      const now = new Date();
      const normalized = String(item.remindTime).replace("T", " ").trim();
      const remindDate = new Date(normalized.replace(/-/g, "/"));
      if (Number.isNaN(remindDate.getTime())) {
        return 'pending';
      }
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
      const time = parsed.time ? (parsed.time.length === 5 ? `${parsed.time}:00` : parsed.time) : `${this.formatTime(new Date())}:00`;
      const payload = {
        userId,
        medicationId: item.id,
        date: parsed.date,
        time,
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
      const now = new Date();
      const fallbackStartDate = this.formatDate(now);
      return {
        id: item.id,
        drugName: item.drugName,
        dosage: item.dosage,
        frequency: item.frequency,
        remindTime,
        stock: item.stock,
        stockThreshold: item.stockThreshold,
        startDate: item.startDate || fallbackStartDate,
        endDate: item.endDate,
        notes: item.notes
      };
    },
    openAdd() {
      this.showModal = true;
      this.editingId = null;
      this.form = { drugName: "", dosage: "", frequency: "", remindTime: "", remindDate: "", startDate: "" };
    },
    openAddWithDrugName(name) {
      this.showModal = true;
      this.editingId = null;
      this.form = { drugName: name || "", dosage: "", frequency: "", remindTime: "", remindDate: "", startDate: "" };
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

/* 时间分组 */
.time-group {
  margin-bottom: 24rpx;
}

.time-group-header {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 16rpx;
  padding-left: 4rpx;
}

.time-group-icon {
  width: 36rpx;
  height: 36rpx;
}

.time-group-label {
  font-size: 28rpx;
  font-weight: 600;
  color: #1a1c1a;
}

.time-group-count {
  font-size: 24rpx;
  color: #8B7355;
  margin-left: auto;
}

/* Medication Card */
.medication-card {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
  padding: 20rpx 24rpx;
  padding-right: 80rpx;
  position: relative;
  overflow: hidden;
  border-radius: var(--radius-card);
  background: #fff;
  border: 2rpx solid #E9E1D8;
}

.medication-completed {
  opacity: 0.6;
}

/* 左侧状态条 */
.medication-status-bar {
  position: absolute;
  left: 0;
  top: 20rpx;
  bottom: 20rpx;
  width: 6rpx;
  border-radius: 0 3rpx 3rpx 0;
  background: #F59E0B;
}

.medication-status-bar.completed {
  background: #10B981;
}

.medication-status-bar.pending {
  background: #F59E0B;
}

.medication-status-bar.overdue {
  background: #EF4444;
}

/* 药品内容 */
.medication-content {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex: 1;
  padding-left: 12rpx;
}

.medication-icon-wrapper {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.medication-icon {
  width: 36rpx;
  height: 36rpx;
}

.medication-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.medication-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #1a1c1a;
}

.medication-dosage {
  font-size: 24rpx;
  color: #1a1c1a;
}

/* 右侧状态和时间 */
.medication-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8rpx;
  flex-shrink: 0;
}

.medication-status-tag {
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 12rpx;
  font-weight: 500;
}

.medication-status-tag.completed {
  background: #D1FAE5;
  color: #059669;
}

.medication-status-tag.pending {
  background: #FEF3C7;
  color: #D97706;
}

.medication-status-tag.overdue {
  background: #FEE2E2;
  color: #DC2626;
}

.medication-time {
  font-size: 24rpx;
  color: #A23F00;
  font-weight: 500;
}

.medication-status {
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 16rpx;
  font-weight: 500;
}

.medication-card .status-completed {
  background: #D1FAE5;
  color: #059669;
}

.medication-card .status-pending {
  background: #FEF3C7;
  color: #D97706;
}

.medication-card .status-overdue {
  background: #FEE2E2;
  color: #DC2626;
}

/* 操作触发按钮 */
.medication-action-trigger {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64rpx;
  height: 64rpx;
  position: absolute;
  right: 20rpx;
  top: 50%;
  transform: translateY(-50%);
  border-radius: 50%;
  background: linear-gradient(135deg, #FFF4ED 0%, #FFEBE0 100%);
  box-shadow: 0 4rpx 12rpx rgba(162, 63, 0, 0.08);
  border: 1px solid rgba(162, 63, 0, 0.1);
}

.action-dots {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
  align-items: center;
}

.action-dot {
  width: 8rpx;
  height: 8rpx;
  border-radius: 50%;
  background: #A23F00;
}

.medication-action-trigger:active {
  transform: translateY(-50%) scale(0.95);
  background: linear-gradient(135deg, #FFE8D6 0%, #FFDDD0 100%);
}

/* 自定义操作菜单 */
.action-sheet-mask {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.6);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 300;
}

.action-sheet {
  width: 100%;
  background: #fff;
  border-radius: 32rpx 32rpx 0 0;
  padding: 40rpx 40rpx calc(24rpx + env(safe-area-inset-bottom));
  overflow: hidden;
}

.action-sheet-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1a1c1a;
  text-align: center;
  margin-bottom: 8rpx;
}

.action-sheet-subtitle {
  font-size: 22rpx;
  color: #8B7355;
  text-align: center;
  margin-bottom: 28rpx;
}

/* 预览卡片 */
.action-preview-card {
  background: #F5F5F5;
  border-radius: 20rpx;
  padding: 20rpx 24rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 28rpx;
}

.action-preview-icon-wrapper {
  width: 64rpx;
  height: 64rpx;
  background: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #E9E1D8;
}

.action-preview-icon {
  width: 36rpx;
  height: 36rpx;
}

.action-preview-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.action-preview-status {
  font-size: 18rpx;
  color: #8B7355;
}

.action-preview-title {
  font-size: 26rpx;
  font-weight: 600;
  color: #1a1c1a;
}

.action-preview-time {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4rpx;
}

.action-preview-time-label {
  font-size: 18rpx;
  color: #8B7355;
}

.action-preview-time-value {
  font-size: 24rpx;
  font-weight: 600;
  color: #A23F00;
}

/* 操作按钮 */
.action-sheet-buttons {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.action-btn-primary {
  width: 100%;
  height: 80rpx;
  background: linear-gradient(135deg, #A23F00 0%, #FA7025 100%);
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  box-shadow: 0 6rpx 20rpx rgba(162, 63, 0, 0.3);
}

.action-btn-primary:active {
  transform: scale(0.98);
}

.action-btn-secondary {
  width: 100%;
  height: 80rpx;
  background: #fff;
  border: 2rpx solid #E9E1D8;
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
}

.action-btn-secondary:active {
  background: #FAF8F5;
}

.action-btn-icon {
  font-size: 24rpx;
  color: #fff;
  font-weight: 600;
}

.action-btn-secondary .action-btn-icon {
  color: #1a1c1a;
}

.action-btn-label {
  font-size: 26rpx;
  font-weight: 600;
  color: #fff;
}

.action-btn-secondary .action-btn-label {
  color: #1a1c1a;
}

.action-sheet-cancel {
  font-size: 26rpx;
  color: #8B7355;
  text-align: center;
  padding: 20rpx 0;
  font-weight: 500;
}

.action-sheet-cancel:active {
  color: #A23F00;
}

.medication-actions {
  display: flex;
  justify-content: center;
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
