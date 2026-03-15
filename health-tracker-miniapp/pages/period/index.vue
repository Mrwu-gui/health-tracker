<template>
  <view class="page">
    <view class="header">
      <view />
      <button class="add-btn" @tap="openAdd">+</button>
    </view>

    <view v-if="nextEstimate" class="estimate-card">
      <text class="estimate-label">预计下次经期</text>
      <text class="estimate-value">{{ nextEstimate }}</text>
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
      <view v-if="list.length === 0" class="empty-state">
        <image class="empty-state-icon" src="/static/tabbar/remind.png" mode="widthFix" />
        <text class="empty-state-title">暂无经期记录</text>
        <text class="empty-state-desc">点击右上角 + 记录经期开始与结束</text>
      </view>
      <view v-for="(item, i) in list" :key="item.id || i" class="card">
        <view class="card-row">
          <text class="card-date">{{ item.startDate }}</text>
          <text v-if="item.flowLabel" class="card-tag">{{ item.flowLabel }}</text>
        </view>
        <text class="card-desc">{{ item.daysText }}</text>
        <text v-if="item.note" class="card-note">{{ item.note }}</text>
      </view>
    </view>

    <view v-if="showModal" class="modal-mask" @tap="closeModal">
      <view class="modal-sheet" @tap.stop>
        <view class="modal-sheet-bar" />
        <view class="modal-sheet-head">
          <text class="modal-sheet-title">记录经期</text>
          <text class="modal-sheet-close" @tap="closeModal">×</text>
        </view>
        <view class="modal-sheet-body">
          <view class="field">
            <text class="field-label">开始日期</text>
            <picker mode="date" :value="form.startDate" @change="onStartDateChange">
              <view class="picker">{{ form.startDate || "选择日期" }}</view>
            </picker>
          </view>
          <view class="field">
            <text class="field-label">结束日期</text>
            <picker mode="date" :value="form.endDate" @change="onEndDateChange">
              <view class="picker">{{ form.endDate || "选择日期" }}</view>
            </picker>
          </view>
          <view class="field">
            <text class="field-label">经量</text>
            <view class="pill-wrap">
              <view
                v-for="opt in flowOptions"
                :key="opt.value"
                class="pill"
                :class="{ active: form.flow === opt.value }"
                @tap="form.flow = opt.value"
              >{{ opt.label }}</view>
            </view>
          </view>
          <view class="field field-note">
            <text class="field-label">备注（选填）</text>
            <textarea
              class="input-note"
              v-model="form.note"
              placeholder="如：痛经、情绪、身体反应等"
              maxlength="200"
            />
          </view>
          <button class="modal-sheet-btn primary" @tap="submitAdd" :disabled="saving">
            {{ saving ? "保存中..." : "保存" }}
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { request } from "../../utils/api";

const FLOW_MAP = { light: "少", medium: "中", heavy: "多" };
const STORAGE_KEY = "periodRecords";

export default {
  data() {
    return {
      list: [],
      showModal: false,
      saving: false,
      form: {
        startDate: "",
        endDate: "",
        flow: "medium",
        note: ""
      },
      flowOptions: [
        { value: "light", label: "少" },
        { value: "medium", label: "中" },
        { value: "heavy", label: "多" }
      ],
      aiSuggestion: "",
      aiSuggestionExpanded: false,
      aiLoadingSuggestion: false
    };
  },
  computed: {
    nextEstimate() {
      if (this.list.length === 0) return "";
      const last = this.list[0];
      const start = last.startDate;
      if (!start) return "";
      const cycleDays = 28;
      const d = new Date(start.replace(/-/g, "/"));
      d.setDate(d.getDate() + cycleDays);
      const y = d.getFullYear();
      const m = String(d.getMonth() + 1).padStart(2, "0");
      const day = String(d.getDate()).padStart(2, "0");
      return `${y}-${m}-${day}`;
    }
  },
  onShow() {
    this.fetchList();
  },
  methods: {
    todayStr() {
      const n = new Date();
      return `${n.getFullYear()}-${String(n.getMonth() + 1).padStart(2, "0")}-${String(n.getDate()).padStart(2, "0")}`;
    },
    fetchList() {
      const userId = uni.getStorageSync("userId") || 1;
      request("/api/period/list", "GET", { userId })
        .then((data) => {
          this.list = this.normalizeList(Array.isArray(data) ? data : []);
        })
        .catch(() => {
          try {
            const raw = uni.getStorageSync(STORAGE_KEY);
            this.list = this.normalizeList(raw ? JSON.parse(raw) : []);
          } catch (e) {
            this.list = [];
          }
        });
    },
    normalizeList(arr) {
      return (arr || [])
        .map((item) => {
          const start = item.startDate || item.start_date || "";
          const end = item.endDate || item.end_date || "";
          const flow = item.flow || "medium";
          let days = 0;
          if (start && end) {
            const a = new Date(start.replace(/-/g, "/"));
            const b = new Date(end.replace(/-/g, "/"));
            days = Math.round((b - a) / 86400000) + 1;
          }
          return {
            id: item.id,
            startDate: start,
            endDate: end,
            flow,
            flowLabel: FLOW_MAP[flow] || "",
            daysText: end ? `共 ${days} 天` : "进行中",
            note: item.note || ""
          };
        })
        .sort((a, b) => (b.startDate || "").localeCompare(a.startDate || ""));
    },
    openAdd() {
      const today = this.todayStr();
      this.form = { startDate: today, endDate: today, flow: "medium", note: "" };
      this.showModal = true;
    },
    closeModal() {
      this.showModal = false;
    },
    onStartDateChange(e) {
      this.form.startDate = e.detail.value || "";
      if (!this.form.endDate || this.form.endDate < this.form.startDate) {
        this.form.endDate = this.form.startDate;
      }
    },
    onEndDateChange(e) {
      this.form.endDate = e.detail.value || "";
    },
    submitAdd() {
      if (!this.form.startDate) {
        uni.showToast({ title: "请选择开始日期", icon: "none" });
        return;
      }
      this.saving = true;
      const userId = uni.getStorageSync("userId") || 1;
      const payload = {
        userId,
        startDate: this.form.startDate,
        endDate: this.form.endDate || this.form.startDate,
        flow: this.form.flow,
        note: (this.form.note || "").trim()
      };
      request("/api/period/add", "POST", payload)
        .then(() => {
          uni.showToast({ title: "已保存", icon: "success" });
          this.closeModal();
          this.fetchList();
          this.fetchAiSuggestionForPeriod(payload);
        })
        .catch(() => {
          const newItem = {
            id: "local_" + Date.now(),
            startDate: payload.startDate,
            endDate: payload.endDate,
            flow: payload.flow,
            note: payload.note
          };
          try {
            const raw = uni.getStorageSync(STORAGE_KEY);
            const arr = raw ? JSON.parse(raw) : [];
            arr.unshift(newItem);
            uni.setStorageSync(STORAGE_KEY, JSON.stringify(arr));
            this.list = this.normalizeList(arr);
            uni.showToast({ title: "已保存（本地）", icon: "success" });
            this.closeModal();
            this.fetchAiSuggestionForPeriod(payload);
          } catch (e) {
            uni.showToast({ title: "保存失败", icon: "none" });
          }
        })
        .finally(() => {
          this.saving = false;
        });
    },
    fetchAiSuggestionForPeriod(payload) {
      this.aiLoadingSuggestion = true;
      this.aiSuggestionExpanded = false;
      const flowLabel = this.flowOptions.find((o) => o.value === payload.flow)?.label || payload.flow;
      const prompt = `用户刚记录了经期：开始日期 ${payload.startDate}，结束日期 ${payload.endDate || payload.startDate}，经量 ${flowLabel}。请用 1～3 句话给出简要的健康建议或注意事项，语气亲切。`;
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

<style scoped>
.page {
  min-height: 100vh;
  background: #faf8f5;
  color: #0f172a;
  padding-bottom: env(safe-area-inset-bottom);
}

.header {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 12px 16px;
}

.add-btn {
  width: 36px;
  height: 36px;
  border-radius: 18px;
  background: #2563eb;
  color: #fff;
  font-size: 20px;
  line-height: 1;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.add-btn::after {
  border: none;
}

.estimate-card {
  background: #fefcf9;
  border-radius: 14px;
  margin: 0 16px 16px;
  padding: 14px 16px;
  border: 1px solid #e8e2db;
}

.estimate-label {
  font-size: 12px;
  color: #64748b;
  display: block;
  margin-bottom: 4px;
}

.estimate-value {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.ai-tip-bar {
  margin: 0 16px 12px;
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
  margin: 0 16px 16px;
  padding: 12px 16px;
  background: linear-gradient(135deg, #fef3c7 0%, #fef9c3 100%);
  border-radius: 14px;
  border: 1px solid #fde68a;
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

.list {
  padding: 0 16px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 48px 24px;
  text-align: center;
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
  margin-bottom: 8px;
}

.empty-state-desc {
  font-size: 13px;
  color: #94a3b8;
}

.card {
  background: #fff;
  border-radius: 14px;
  padding: 14px 16px;
  margin-bottom: 12px;
  border: 1px solid #e8e2db;
}

.card-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.card-date {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
}

.card-tag {
  font-size: 11px;
  color: #6366f1;
  background: #e0e7ff;
  padding: 2px 8px;
  border-radius: 6px;
}

.card-desc {
  font-size: 13px;
  color: #64748b;
  display: block;
}

.card-note {
  font-size: 12px;
  color: #94a3b8;
  display: block;
  margin-top: 6px;
}

.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 999;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.modal-sheet {
  width: 100%;
  max-height: 80vh;
  background: #fff;
  border-radius: 20px 20px 0 0;
  padding-bottom: env(safe-area-inset-bottom);
  overflow-y: auto;
}

.modal-sheet-bar {
  width: 36px;
  height: 4px;
  background: #e2e8f0;
  border-radius: 2px;
  margin: 10px auto;
}

.modal-sheet-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 20px 16px;
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
}

.modal-sheet-body {
  padding: 0 20px 24px;
  box-sizing: border-box;
}

.field {
  margin-bottom: 16px;
}

.field-label {
  font-size: 13px;
  color: #64748b;
  display: block;
  margin-bottom: 8px;
}

.picker {
  border: 1px solid #e8e2db;
  border-radius: 10px;
  padding: 12px 14px;
  font-size: 14px;
  color: #0f172a;
  background: #fff;
}

.input {
  border: 1px solid #e8e2db;
  border-radius: 10px;
  padding: 12px 14px;
  font-size: 14px;
  width: 100%;
  box-sizing: border-box;
}

.field-note {
  width: 100%;
}

.input-note {
  width: 100%;
  min-height: 80px;
  box-sizing: border-box;
  border: 1px solid #e8e2db;
  border-radius: 10px;
  padding: 12px 14px;
  font-size: 14px;
  line-height: 1.5;
  color: #0f172a;
}

.pill-wrap {
  display: flex;
  gap: 10px;
}

.pill {
  padding: 8px 16px;
  border-radius: 20px;
  background: #f5f1eb;
  color: #64748b;
  font-size: 13px;
}

.pill.active {
  background: #2563eb;
  color: #fff;
}

.modal-sheet-btn {
  width: 100%;
  height: 48px;
  border-radius: 14px;
  font-size: 16px;
  font-weight: 600;
  margin-top: 8px;
}

.modal-sheet-btn.primary {
  background: #2563eb;
  color: #fff;
  border: none;
}

.modal-sheet-btn::after {
  border: none;
}
</style>
