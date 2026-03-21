<template>
  <view class="page-root">
    <!-- 空状态页面 -->
    <view v-if="list.length === 0" class="empty-page">
      <view class="empty-content">
        <view class="empty-illustration">
          <image class="empty-illustration-icon" src="/static/tabbar/period_w.png" mode="aspectFit" />
        </view>
        <text class="empty-desc">添加后AI将智能预测经期周期，为您提供健康建议。</text>

        <view class="empty-features">
          <view class="feature-item">
            <image class="feature-icon" src="/static/tabbar/period_s.png" mode="aspectFit" />
            <text class="feature-text">经期追踪</text>
          </view>
          <view class="feature-item">
            <image class="feature-icon" src="/static/tabbar/tips_s.png" mode="aspectFit" />
            <text class="feature-text">健康建议</text>
          </view>
          <view class="feature-item">
            <image class="feature-icon" src="/static/tabbar/fork.png" mode="aspectFit" />
            <text class="feature-text">食谱推荐</text>
          </view>
        </view>

        <button class="empty-add-btn" @tap="openAdd">
          <image class="empty-add-icon" src="/static/tabbar/add.png" mode="aspectFit" />
          <text class="empty-add-text">添加第一条记录</text>
        </button>
      </view>
    </view>

    <!-- 有数据时的页面 -->
    <view v-else class="page">
      <!-- 年月显示区域 -->
    <view class="month-header">
      <view class="month-info">
        <text class="month-year">{{ currentYear }}年{{ currentMonth }}月</text>
        <text class="month-predict" v-if="nextEstimate">预计下次经期：{{ formatPredictDate(nextEstimate) }}</text>
      </view>
    </view>

    <!-- 日历选择器 -->
    <view class="calendar-section">
      <view class="calendar-weekdays">
        <text class="weekday" v-for="day in weekDays" :key="day">{{ day }}</text>
      </view>
      <view class="calendar-days">
        <view 
          v-for="(day, index) in calendarDays" 
          :key="index"
          class="calendar-day"
          :class="{ 
            'empty': !day.date,
            'today': day.isToday,
            'period': day.isPeriod,
            'predict': day.isPredict
          }"
          @tap="selectDay(day)"
        >
          <text class="day-number" v-if="day.date">{{ day.date }}</text>
          <view v-if="day.isPeriod" class="period-dot"></view>
          <view v-if="day.isPredict" class="predict-dot"></view>
        </view>
      </view>
    </view>

    <!-- 上次结束于信息卡片 -->
    <view class="last-period-card" v-if="lastPeriodInfo">
      <view class="last-period-left">
        <image class="last-period-icon" src="/static/tabbar/period.png" mode="aspectFit" />
        <view class="last-period-text">
          <text class="last-period-title">上次结束于</text>
          <text class="last-period-date">{{ lastPeriodInfo.endDate }}</text>
        </view>
      </view>
      <view class="last-period-right">
        <text class="last-period-days">本次已过 {{ lastPeriodInfo.daysPassed }} 天</text>
      </view>
    </view>

    <!-- AI智能分析卡片 -->
    <view class="ai-card" v-if="aiSuggestion" @tap="aiSuggestionExpanded = !aiSuggestionExpanded">
      <view class="ai-card-header">
        <image class="ai-icon" src="/static/tabbar/robot-active.png" mode="aspectFit" />
        <text class="ai-title">AI智能分析</text>
        <text class="ai-arrow">{{ aiSuggestionExpanded ? '收起' : '展开' }}</text>
      </view>
      <view class="ai-content" v-if="aiSuggestionExpanded">
        <text class="ai-text">{{ aiSuggestion }}</text>
        <navigator class="ai-link" url="/pages/ai/index" open-type="switchTab">
          <text>去和智康对话</text>
          <text class="link-arrow">→</text>
        </navigator>
      </view>
    </view>

    <!-- 最近记录列表 -->
    <view class="records-section" v-if="list.length > 0">
      <view class="section-header">
        <text class="section-title">最近记录</text>
        <text class="section-more" @tap="showAllRecords">查看全部</text>
      </view>
      <view class="records-list">
        <view 
          v-for="(item, index) in list.slice(0, 3)" 
          :key="item.id"
          class="record-item"
          @tap="onCardTap(item)"
        >
          <view class="record-left">
            <text class="record-date">{{ formatRecordDate(item.startDate) }}</text>
            <text class="record-duration">{{ item.daysText }}</text>
          </view>
          <view class="record-right">
            <text class="record-flow" v-if="item.flowLabel">{{ item.flowLabel }}</text>
            <image class="record-arrow" src="/static/tabbar/all.png" mode="aspectFit" />
          </view>
        </view>
      </view>
    </view>

    <!-- 底部提示卡片 -->
    <view class="tips-section">
      <view class="tip-card warm-tip">
        <image class="tip-icon" src="/static/tabbar/tips.png" mode="aspectFit" />
        <text class="tip-title">暖宫小贴士</text>
        <text class="tip-desc">经期保暖很重要，建议多喝温水</text>
      </view>
      <view class="tip-card food-tip">
        <image class="tip-icon" src="/static/tabbar/food.png" mode="aspectFit" />
        <text class="tip-title">推荐食谱</text>
        <text class="tip-desc">红枣桂圆汤，补血养颜</text>
      </view>
    </view>

    <!-- 添加按钮 -->
    <view class="fab-container">
      <view class="fab" @tap="openAdd">
        <text class="fab-icon">+</text>
      </view>
    </view>
    </view>

    <!-- 添加/编辑经期弹窗 -->
    <view v-if="showModal" class="modal-mask" @tap="closeModal">
      <view class="modal-sheet" @tap.stop>
        <view class="modal-sheet-bar" />
        <view class="modal-sheet-head">
          <text class="modal-sheet-title">{{ editingId ? "编辑经期" : "添加经期" }}</text>
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
            {{ saving ? "保存中..." : editingId ? "保存修改" : "保存" }}
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { request } from "../../utils/api";
import { requestSubscribeByKey } from "../../utils/subscribe";

const FLOW_MAP = { light: "少", medium: "中", heavy: "多" };
const STORAGE_KEY = "periodRecords";

export default {
  data() {
    return {
      list: [],
      showModal: false,
      saving: false,
      editingId: null,
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
      aiSuggestionExpanded: true,
      aiLoadingSuggestion: false,
      currentYear: new Date().getFullYear(),
      currentMonth: new Date().getMonth() + 1,
      weekDays: ['一', '二', '三', '四', '五', '六', '日']
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
    },
    lastPeriodInfo() {
      if (this.list.length === 0) return null;
      const last = this.list[0];
      if (!last.endDate) return null;
      const endDate = new Date(last.endDate.replace(/-/g, "/"));
      const today = new Date();
      const daysPassed = Math.floor((today - endDate) / 86400000);
      return {
        endDate: this.formatRecordDate(last.endDate),
        daysPassed: daysPassed > 0 ? daysPassed : 0
      };
    },
    calendarDays() {
      const year = this.currentYear;
      const month = this.currentMonth;
      const firstDay = new Date(year, month - 1, 1);
      const lastDay = new Date(year, month, 0);
      const daysInMonth = lastDay.getDate();
      const startWeekday = firstDay.getDay() === 0 ? 7 : firstDay.getDay();
      
      const today = new Date();
      const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, "0")}-${String(today.getDate()).padStart(2, "0")}`;
      
      const days = [];
      
      // 填充月初空白
      for (let i = 1; i < startWeekday; i++) {
        days.push({ date: null, isToday: false, isPeriod: false, isPredict: false });
      }
      
      // 填充日期
      for (let d = 1; d <= daysInMonth; d++) {
        const dateStr = `${year}-${String(month).padStart(2, "0")}-${String(d).padStart(2, "0")}`;
        const isToday = dateStr === todayStr;
        const isPeriod = this.isPeriodDate(dateStr);
        const isPredict = this.isPredictDate(dateStr);
        
        days.push({ 
          date: d, 
          dateStr,
          isToday, 
          isPeriod, 
          isPredict 
        });
      }
      
      return days;
    }
  },
  onShow() {
    this.fetchList();
  },
  methods: {
    isPeriodDate(dateStr) {
      return this.list.some(item => {
        if (!item.startDate) return false;
        const start = item.startDate;
        const end = item.endDate || item.startDate;
        return dateStr >= start && dateStr <= end;
      });
    },
    isPredictDate(dateStr) {
      if (!this.nextEstimate) return false;
      const predictStart = new Date(this.nextEstimate.replace(/-/g, "/"));
      const predictEnd = new Date(predictStart);
      predictEnd.setDate(predictEnd.getDate() + 5);
      
      const checkDate = new Date(dateStr.replace(/-/g, "/"));
      return checkDate >= predictStart && checkDate <= predictEnd;
    },
    formatPredictDate(dateStr) {
      if (!dateStr) return "";
      const d = new Date(dateStr.replace(/-/g, "/"));
      return `${d.getMonth() + 1}月${d.getDate()}日`;
    },
    formatRecordDate(dateStr) {
      if (!dateStr) return "";
      const d = new Date(dateStr.replace(/-/g, "/"));
      return `${d.getMonth() + 1}月${d.getDate()}日`;
    },
    selectDay(day) {
      if (!day.date) return;
      // 可以添加点击日期的逻辑
    },
    showCalendarPicker() {
      // 显示日历选择器
    },
    showMoreOptions() {
      // 显示更多选项
    },
    showAllRecords() {
      // 显示全部记录
    },
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
        .map((item, index) => {
          const start = item.startDate || item.start_date || "";
          const end = item.endDate || item.end_date || "";
          const flow = item.flow || "medium";
          let days = 0;
          if (start && end) {
            const a = new Date(start.replace(/-/g, "/"));
            const b = new Date(end.replace(/-/g, "/"));
            days = Math.round((b - a) / 86400000) + 1;
          }
          const id = item.id != null && item.id !== "" ? item.id : "idx-" + index;
          return {
            id,
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
      this.editingId = null;
      this.form = { startDate: today, endDate: today, flow: "medium", note: "" };
      this.showModal = true;
    },
    onCardTap(item) {
      if (!item) return;
      this.openEdit(item);
    },
    openEdit(item) {
      if (!item) return;
      this.editingId = item.id;
      this.showModal = true;
      this.form = {
        startDate: item.startDate || "",
        endDate: item.endDate || item.startDate || "",
        flow: item.flow || "medium",
        note: item.note || ""
      };
    },
    closeModal() {
      this.showModal = false;
      this.editingId = null;
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
    async submitAdd() {
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
      const isEdit = !!this.editingId;
      if (isEdit) {
        payload.id = this.editingId;
        request("/api/period/update", "PUT", payload)
          .then(() => {
            uni.showToast({ title: "已更新", icon: "success" });
            this.closeModal();
            this.fetchList();
          })
          .catch(() => {
            this.updatePeriodLocal();
          })
          .finally(() => {
            this.saving = false;
          });
        return;
      }
      await requestSubscribeByKey("period");
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
    updatePeriodLocal() {
      const payload = {
        startDate: this.form.startDate,
        endDate: this.form.endDate || this.form.startDate,
        flow: this.form.flow,
        note: (this.form.note || "").trim()
      };
      try {
        const raw = uni.getStorageSync(STORAGE_KEY);
        const arr = raw ? JSON.parse(raw) : [];
        const idx = arr.findIndex((item) => String(item.id) === String(this.editingId));
        if (idx >= 0) {
          arr[idx] = { ...arr[idx], ...payload, id: arr[idx].id };
          uni.setStorageSync(STORAGE_KEY, JSON.stringify(arr));
          this.list = this.normalizeList(arr);
          uni.showToast({ title: "已更新（本地）", icon: "success" });
        } else {
          uni.showToast({ title: "更新失败", icon: "none" });
        }
      } catch (e) {
        uni.showToast({ title: "更新失败", icon: "none" });
      }
      this.closeModal();
      this.saving = false;
    },
    fetchAiSuggestionForPeriod(payload) {
      this.aiLoadingSuggestion = true;
      this.aiSuggestionExpanded = true;
      const flowLabel = this.flowOptions.find((o) => o.value === payload.flow)?.label || payload.flow;
      const prompt = `用户刚记录了经期：开始日期 ${payload.startDate}，结束日期 ${payload.endDate || payload.startDate}，经量 ${flowLabel}。请用 1～3 句话给出简要的健康建议或注意事项，语气亲切。`;
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

/* 空状态页面 */
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

.empty-title-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 16rpx;
}

.empty-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #1a1c1a;
  line-height: 1.4;
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

/* 有数据时的页面 */
.page {
  min-height: 100vh;
  background: linear-gradient(180deg, #FFF5F0 0%, #FFF9F7 50%, #FAF8F5 100%);
  padding-bottom: calc(120rpx + env(safe-area-inset-bottom));
}

/* 年月显示区域 */
.month-header {
  padding: 32rpx 32rpx 16rpx;
}

.month-info {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.month-year {
  font-size: 40rpx;
  font-weight: 700;
  color: #1a1c1a;
}

.month-predict {
  font-size: 26rpx;
  color: #FA7025;
  background: rgba(250, 112, 37, 0.1);
  padding: 8rpx 16rpx;
  border-radius: 24rpx;
  display: inline-block;
  align-self: flex-start;
}

/* 日历选择器 */
.calendar-section {
  margin: 16rpx 24rpx;
  background: #fff;
  border-radius: 32rpx;
  padding: 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(162, 63, 0, 0.06);
}

.calendar-weekdays {
  display: flex;
  justify-content: space-around;
  margin-bottom: 16rpx;
  padding-bottom: 16rpx;
  border-bottom: 1px solid #f0e8e0;
}

.weekday {
  font-size: 24rpx;
  color: #8B7355;
  width: 64rpx;
  text-align: center;
}

.calendar-days {
  display: flex;
  flex-wrap: wrap;
}

.calendar-day {
  width: calc(100% / 7);
  height: 72rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
}

.calendar-day.empty {
  height: 72rpx;
}

.day-number {
  font-size: 28rpx;
  color: #1a1c1a;
  z-index: 2;
}

.calendar-day.today .day-number {
  color: #fff;
  background: linear-gradient(135deg, #A23F00 0%, #FA7025 100%);
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
}

.calendar-day.period {
  background: rgba(255, 182, 193, 0.3);
}

.period-dot {
  position: absolute;
  bottom: 8rpx;
  width: 12rpx;
  height: 12rpx;
  background: #FF6B8A;
  border-radius: 50%;
}

.calendar-day.predict {
  background: rgba(250, 112, 37, 0.1);
}

.predict-dot {
  position: absolute;
  bottom: 8rpx;
  width: 12rpx;
  height: 12rpx;
  background: #FA7025;
  border-radius: 50%;
}

/* 上次结束于信息卡片 */
.last-period-card {
  margin: 0 24rpx 24rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 24rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 4rpx 20rpx rgba(162, 63, 0, 0.06);
}

.last-period-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.last-period-icon {
  width: 48rpx;
  height: 48rpx;
}

.last-period-text {
  display: flex;
  flex-direction: column;
}

.last-period-title {
  font-size: 24rpx;
  color: #8B7355;
}

.last-period-date {
  font-size: 30rpx;
  font-weight: 600;
  color: #1a1c1a;
  margin-top: 4rpx;
}

.last-period-right {
  background: rgba(250, 112, 37, 0.1);
  padding: 12rpx 20rpx;
  border-radius: 24rpx;
}

.last-period-days {
  font-size: 24rpx;
  color: #FA7025;
  font-weight: 500;
}

/* AI智能分析卡片 */
.ai-card {
  margin: 0 24rpx 24rpx;
  background: linear-gradient(135deg, #FFF5F0 0%, #FFE8DE 100%);
  border-radius: 24rpx;
  padding: 24rpx;
  border: 1px solid rgba(250, 112, 37, 0.2);
}

.ai-card-header {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.ai-icon {
  width: 40rpx;
  height: 40rpx;
}

.ai-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #A23F00;
  flex: 1;
}

.ai-arrow {
  font-size: 24rpx;
  color: #8B7355;
}

.ai-content {
  margin-top: 16rpx;
  padding-top: 16rpx;
  border-top: 1px solid rgba(162, 63, 0, 0.1);
}

.ai-text {
  font-size: 26rpx;
  color: #4A3728;
  line-height: 1.6;
  display: block;
}

.ai-link {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8rpx;
  margin-top: 16rpx;
  font-size: 24rpx;
  color: #A23F00;
}

.link-arrow {
  font-size: 28rpx;
}

/* 最近记录列表 */
.records-section {
  margin: 0 24rpx 24rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1a1c1a;
}

.section-more {
  font-size: 24rpx;
  color: #8B7355;
}

.records-list {
  background: #fff;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 20rpx rgba(162, 63, 0, 0.06);
}

.record-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx;
  border-bottom: 1px solid #f0e8e0;
}

.record-item:last-child {
  border-bottom: none;
}

.record-left {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.record-date {
  font-size: 28rpx;
  font-weight: 600;
  color: #1a1c1a;
}

.record-duration {
  font-size: 24rpx;
  color: #8B7355;
}

.record-right {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.record-flow {
  font-size: 22rpx;
  color: #FA7025;
  background: rgba(250, 112, 37, 0.1);
  padding: 6rpx 16rpx;
  border-radius: 24rpx;
}

.record-arrow {
  width: 32rpx;
  height: 32rpx;
  opacity: 0.5;
  transform: rotate(-90deg);
}

/* 底部提示卡片 */
.tips-section {
  display: flex;
  gap: 16rpx;
  margin: 0 24rpx 24rpx;
}

.tip-card {
  flex: 1;
  background: #fff;
  border-radius: 24rpx;
  padding: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(162, 63, 0, 0.06);
}

.tip-icon {
  width: 40rpx;
  height: 40rpx;
  margin-bottom: 12rpx;
}

.tip-title {
  font-size: 26rpx;
  font-weight: 600;
  color: #1a1c1a;
  display: block;
  margin-bottom: 8rpx;
}

.tip-desc {
  font-size: 22rpx;
  color: #8B7355;
  display: block;
  line-height: 1.4;
}

.warm-tip {
  border-left: 4rpx solid #FF6B8A;
}

.food-tip {
  border-left: 4rpx solid #FA7025;
}

/* 弹窗样式 */
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
  border-radius: 32rpx 32rpx 0 0;
  padding-bottom: env(safe-area-inset-bottom);
  overflow-y: auto;
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
  padding: 0 40rpx 48rpx;
  box-sizing: border-box;
}

.field {
  margin-bottom: 32rpx;
}

.field-label {
  font-size: 26rpx;
  color: #8B7355;
  display: block;
  margin-bottom: 16rpx;
}

.picker {
  border: 1px solid #E9E1D8;
  border-radius: 24rpx;
  padding: 24rpx 28rpx;
  font-size: 28rpx;
  color: #1a1c1a;
  background: #fff;
  height: 88rpx;
  line-height: 40rpx;
  box-sizing: border-box;
}

.field-note {
  width: 100%;
}

.input-note {
  width: 100%;
  min-height: 160rpx;
  box-sizing: border-box;
  border: 1px solid #E9E1D8;
  border-radius: 24rpx;
  padding: 24rpx 28rpx;
  font-size: 28rpx;
  line-height: 1.5;
  color: #1a1c1a;
}

.pill-wrap {
  display: flex;
  gap: 20rpx;
}

.pill {
  padding: 16rpx 32rpx;
  border-radius: 48rpx;
  background: #f5f1eb;
  color: #8B7355;
  font-size: 26rpx;
}

.pill.active {
  background: linear-gradient(135deg, #A23F00 0%, #FA7025 100%);
  color: #fff;
}

.modal-sheet-btn {
  width: 100%;
  height: 96rpx;
  border-radius: 48rpx;
  font-size: 32rpx;
  font-weight: 600;
  margin-top: 16rpx;
}

.modal-sheet-btn.primary {
  background: linear-gradient(135deg, #A23F00 0%, #FA7025 100%);
  color: #fff;
  border: none;
}

.modal-sheet-btn::after {
  border: none;
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
</style>
