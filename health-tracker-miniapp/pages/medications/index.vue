<template>
  <view class="page">
    <view class="header">
      <view />
      <button class="add-btn" @tap="openAdd">+</button>
    </view>

    <view v-if="aiLoadingSuggestion" class="ai-tip-bar">
      <text class="ai-tip-text">智康正在想一句建议…</text>
    </view>
    <view v-else-if="aiSuggestion" class="ai-suggestion-card">
      <view class="ai-suggestion-head" @tap="aiSuggestionExpanded = !aiSuggestionExpanded">
        <text class="ai-suggestion-title">{{ aiSuggestionExpanded ? '✨ 智康建议' : '智康有一句建议，点击查看' }}</text>
        <text class="ai-suggestion-arrow">{{ aiSuggestionExpanded ? '收起' : '展开' }}</text>
      </view>
      <template v-if="aiSuggestionExpanded">
        <text class="ai-suggestion-text">{{ aiSuggestion }}</text>
        <view class="ai-suggestion-actions">
          <navigator class="ai-suggestion-link" url="/pages/ai/index" open-type="switchTab">去和智康对话</navigator>
          <text class="ai-suggestion-close" @tap.stop="aiSuggestion = ''; aiSuggestionExpanded = false">关闭</text>
        </view>
      </template>
    </view>

    <view class="list">
      <view v-if="meds.length === 0" class="empty-state">
        <image class="empty-state-icon" src="/static/tabbar/pills.png" mode="widthFix" />
        <text class="empty-state-title">暂无药物记录</text>
        <text class="empty-state-desc">点击右上角 + 添加需要服用的药物</text>
      </view>
      <view v-for="item in meds" v-else :key="item.id" class="card" @tap="openEdit(item)">
        <view class="row">
          <view>
            <text class="name">{{ item.drugName }} {{ item.dosage }}</text>
            <text class="desc">{{ item.frequency }} · {{ item.remindTime || "未设置" }}</text>
          </view>
          <view class="toggle">{{ item.enabled ? "开" : "关" }}</view>
        </view>
      </view>
    </view>

    <view v-if="showModal" class="modal-mask" @tap="closeModal">
      <view class="modal-sheet" @tap.stop>
        <view class="modal-sheet-bar" />
        <view class="modal-sheet-head">
          <text class="modal-sheet-title">{{ editingId ? "编辑药物" : "添加药物" }}</text>
          <text class="modal-sheet-close" @tap="closeModal">×</text>
        </view>
        <view class="modal-sheet-body">
        <view class="field">
          <text class="field-label">药物名称</text>
          <input class="input" v-model="form.drugName" placeholder="如 氨氯地平" />
        </view>
        <view class="field">
          <text class="field-label">剂量</text>
          <input class="input" v-model="form.dosage" placeholder="如 5mg" />
        </view>
        <view class="field">
          <text class="field-label">用法</text>
          <input class="input" v-model="form.frequency" placeholder="如 每日 1 次" />
        </view>
        <view class="field">
          <text class="field-label">提醒日期</text>
          <picker mode="date" @change="onDateChange">
            <view class="input">{{ form.remindDate || "请选择日期" }}</view>
          </picker>
        </view>
        <view class="field">
          <text class="field-label">提醒时间</text>
          <picker mode="time" @change="onTimeChange">
            <view class="input">{{ form.remindTime || "请选择时间" }}</view>
          </picker>
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
      meds: [],
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
              enabled: true
            }));
            this.setMeds(mapped);
          } else {
            this.setMeds([]);
          }
        })
        .catch(() => {
          this.setMeds([]);
        });
    },
    setMeds(list) {
      this.meds = list;
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
      if (/^\\d{2}:\\d{2}$/.test(trimmed)) {
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
      request("/api/ai/chat", "POST", { message: prompt, store: false })
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
  padding: 14px;
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

.toggle {
  font-size: 10px;
  background: #dbeafe;
  color: #2563eb;
  padding: 4px 8px;
  border-radius: 999px;
}

.ai-tip-bar {
  padding: 10px 14px;
  background: #f8fafc;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
}

.ai-tip-text {
  font-size: 12px;
  color: #64748b;
}

.ai-suggestion-card {
  padding: 12px 16px;
  background: linear-gradient(135deg, #fef3c7 0%, #fef9c3 100%);
  border-radius: 14px;
  border: 1px solid #fde68a;
  margin-bottom: 12px;
}

.ai-suggestion-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 2px 0;
}

.ai-suggestion-title {
  font-size: 13px;
  font-weight: 600;
  color: #92400e;
}

.ai-suggestion-arrow {
  font-size: 12px;
  color: #64748b;
}

.ai-suggestion-text {
  font-size: 13px;
  line-height: 1.5;
  color: #334155;
  display: block;
  margin: 10px 0 8px;
}

.ai-suggestion-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 6px;
}

.ai-suggestion-link {
  font-size: 12px;
  color: #4f46e5;
  font-weight: 500;
}

.ai-suggestion-close {
  font-size: 12px;
  color: #94a3b8;
}

.rate {
  font-size: 12px;
  font-weight: 600;
}

.bar {
  height: 6px;
  background: #e8e2db;
  border-radius: 999px;
  overflow: hidden;
  margin-top: 8px;
}

.bar-fill {
  height: 100%;
  background: #2563eb;
}

.primary {
  background: #2563eb;
  color: #ffffff;
  border-radius: 16px;
  font-size: 12px;
  padding: 12px 0;
}

.primary-btn {
  text-align: center;
  font-weight: 600;
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

.primary {
  background: #2563eb;
  color: #ffffff;
  border-radius: 16px;
  font-size: 12px;
  padding: 12px 0;
}

.primary-btn {
  text-align: center;
  font-weight: 600;
}
</style>
