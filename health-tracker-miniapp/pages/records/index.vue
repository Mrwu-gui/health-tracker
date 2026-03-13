<template>
  <view class="page">
    <view class="header">
      <text class="title">记录</text>
      <text class="subtitle">点按展开编辑，拖动卡片调整顺序</text>
    </view>
    <view
      class="card"
      v-for="(card, index) in cards"
      :key="card.id"
      :class="{ expanded: card.expanded, dragging: dragIndex === index }"
      @tap="handleCardTap(card)"
      @longpress="handleCardLongPress(card, index, $event)"
      @touchstart="onTouchStart(index)"
      @touchmove="onTouchMove($event)"
      @touchend="onTouchEnd"
    >
      <view class="row">
        <text class="label">{{ card.title }}</text>
      </view>
      <text class="value">{{ card.summary }}</text>
      <text class="hint">{{ card.hint }}</text>
      <view v-if="card.expanded" class="editor-body" @tap.stop>
        <input
          class="input"
          v-for="(field, idx) in card.fields"
          :key="idx"
          v-model="field.value"
          :placeholder="field.label"
        />
        <view v-if="card.showDelete" class="delete-row">
          <button class="danger" @tap.stop="confirmDelete(card.id)">删除卡片</button>
        </view>
        <text class="edit-hint">点击卡片即可保存并收起。</text>
      </view>
    </view>
    <text v-if="message" class="status">{{ message }}</text>
    <text v-if="loading" class="status">加载中...</text>
    <text v-if="error" class="status error">{{ error }}</text>
  </view>
</template>

<script>
import { request } from "../../utils/api";

export default {
  data() {
    return {
      loading: false,
      error: "",
      message: "",
      dragIndex: -1,
      dragStartY: 0,
      draggingEnabled: false,
      cards: []
    };
  },
  onLoad() {
    this.fetchSummary();
  },
  methods: {
    fetchSummary() {
      this.loading = true;
      this.error = "";
      this.message = "";
      const userId = uni.getStorageSync("userId") || 1;
      request("/api/records/summary", "GET", { userId })
        .then((data) => {
          this.cards = this.buildCards(data || {});
        })
        .catch(() => {
          this.error = "获取记录失败";
          this.cards = this.buildCards({});
        })
        .finally(() => {
          this.loading = false;
        });
    },
    buildCards(data) {
      const exercise = data.exercise || {};
      const diet = data.diet || {};
      const sleep = data.sleep || {};
      const weight = data.weight || {};
      const health = data.health || {};
      const medication = data.medication || {};
      const exerciseSummary =
        exercise.steps || exercise.type
          ? `步数 ${exercise.steps || 0}，${exercise.type || "未填写"} ${exercise.duration || 0} 分钟`
          : "暂无记录";
      const exerciseHint = exercise.calories ? `消耗 ${exercise.calories} 千卡` : "暂无消耗记录";
      return [
        {
          id: "exercise",
          title: "运动管理",
          summary: exerciseSummary,
          hint: exerciseHint,
          expanded: false,
          showDelete: false,
          fields: [
            { key: "steps", label: "步数", value: exercise.steps || "" },
            { key: "type", label: "类型（如 跑步）", value: exercise.type || "" },
            { key: "duration", label: "时长（分钟）", value: exercise.duration || "" },
            { key: "calories", label: "消耗卡路里", value: exercise.calories || "" }
          ]
        },
        {
          id: "diet",
          title: "饮食管理",
          summary: diet.mealType ? `${diet.mealType} ${diet.calories || 0} 千卡` : "暂无记录",
          hint: diet.mealType
            ? `蛋白 ${diet.protein || 0}g / 碳水 ${diet.carbs || 0}g / 脂肪 ${diet.fat || 0}g`
            : "暂无营养记录",
          expanded: false,
          showDelete: false,
          fields: [
            { key: "mealType", label: "餐次", value: diet.mealType || "" },
            { key: "foodName", label: "食物名称", value: diet.foodName || "" },
            { key: "calories", label: "热量（千卡）", value: diet.calories || "" },
            { key: "protein", label: "蛋白（g）", value: diet.protein || "" },
            { key: "carbs", label: "碳水（g）", value: diet.carbs || "" },
            { key: "fat", label: "脂肪（g）", value: diet.fat || "" }
          ]
        },
        {
          id: "sleep",
          title: "睡眠管理",
          summary: sleep.duration || "0小时0分",
          hint: `质量：${sleep.quality || "未填写"} / 作息：${sleep.routine || "未填写"}`,
          expanded: false,
          showDelete: false,
          fields: [
            { key: "routine", label: "作息（如 23:30-07:05）", value: sleep.routine || "" },
            { key: "quality", label: "质量（优/良/一般）", value: sleep.quality || "" }
          ]
        },
        {
          id: "weight",
          title: "体重管理",
          summary: weight.weight ? `${weight.weight} kg（BMI ${weight.bmi || "--"}）` : "暂无记录",
          hint: `趋势：${weight.trend || "暂无"}`,
          expanded: false,
          showDelete: false,
          fields: [
            { key: "weight", label: "体重（kg）", value: weight.weight || "" },
            { key: "bmi", label: "BMI（可选）", value: weight.bmi || "" }
          ]
        },
        {
          id: "health",
          title: "血压管理",
          summary: health.systolic ? `${health.systolic} / ${health.diastolic} mmHg` : "暂无记录",
          hint: health.heartRate ? `心率 ${health.heartRate} 次/分` : "心率未记录",
          expanded: false,
          showDelete: false,
          fields: [
            { key: "systolic", label: "收缩压", value: health.systolic || "" },
            { key: "diastolic", label: "舒张压", value: health.diastolic || "" },
            { key: "heartRate", label: "心率", value: health.heartRate || "" }
          ]
        },
        {
          id: "medication",
          title: "药物管理",
          summary: medication.drugName ? `${medication.drugName} ${medication.dosage || ""}`.trim() : "暂无用药",
          hint: medication.drugName
            ? `库存 ${medication.stock || 0}，提醒 ${medication.remindTime || "未设置"}`
            : "暂无用药记录",
          expanded: false,
          showDelete: false,
          fields: [
            { key: "drugName", label: "药品名称", value: medication.drugName || "" },
            { key: "dosage", label: "剂量", value: medication.dosage || "" },
            { key: "frequency", label: "服用频率", value: medication.frequency || "" },
            { key: "remindTime", label: "提醒时间", value: medication.remindTime || "" },
            { key: "stock", label: "库存", value: medication.stock || "" },
            { key: "stockThreshold", label: "库存提醒阈值", value: medication.stockThreshold || "" }
          ]
        }
      ];
    },
    handleCardTap(card) {
      if (card.expanded) {
        this.saveCard(card);
        card.expanded = false;
        card.showDelete = false;
        return;
      }
      this.cards.forEach((item) => {
        if (item.id !== card.id) {
          item.expanded = false;
          item.showDelete = false;
        }
      });
      card.expanded = true;
    },
    handleCardLongPress(card, index, event) {
      this.cards.forEach((item) => {
        if (item.id !== card.id) {
          item.expanded = false;
          item.showDelete = false;
        }
      });
      card.expanded = true;
      card.showDelete = true;
      this.draggingEnabled = true;
      this.dragIndex = index;
      this.dragStartY = event.touches[0].clientY;
    },
    confirmDelete(cardId) {
      uni.showModal({
        title: "确认删除",
        content: "删除后将从记录页面移除该卡片",
        success: (res) => {
          if (res.confirm) {
            this.cards = this.cards.filter((item) => item.id !== cardId);
            this.message = "已删除卡片";
          }
        }
      });
    },
    onTouchStart() {
    },
    onTouchMove(event) {
      if (!this.draggingEnabled || this.dragIndex < 0) return;
      const index = this.dragIndex;
      if (this.cards[index]?.expanded && !this.cards[index]?.showDelete) return;
      const currentY = event.touches[0].clientY;
      const deltaY = currentY - this.dragStartY;
      if (Math.abs(deltaY) < 24) return;
      const direction = deltaY > 0 ? 1 : -1;
      const targetIndex = index + direction;
      if (targetIndex < 0 || targetIndex >= this.cards.length) return;
      const moved = this.cards.splice(index, 1)[0];
      this.cards.splice(targetIndex, 0, moved);
      this.dragStartY = currentY;
      this.dragIndex = targetIndex;
    },
    onTouchEnd() {
      this.dragIndex = -1;
      this.draggingEnabled = false;
    },
    saveCard(card) {
      this.message = "";
      this.error = "";
      const userId = uni.getStorageSync("userId") || 1;
      const today = new Date().toISOString().slice(0, 10);
      const payload = {};
      card.fields.forEach((field) => {
        payload[field.key] = field.value;
      });
      if (card.id === "exercise") {
        const steps = parseInt(payload.steps || 0, 10);
        const duration = parseInt(payload.duration || 0, 10);
        const calories = parseInt(payload.calories || 0, 10);
        const type = payload.type || "";
        const tasks = [];
        if (steps > 0) {
          tasks.push(
            request("/api/werun/add", "POST", {
              userId,
              steps,
              date: today
            })
          );
        }
        if (type && duration > 0) {
          tasks.push(
            request("/api/exercise/add", "POST", {
              userId,
              type,
              duration,
              calories,
              date: today
            })
          );
        }
        if (tasks.length === 0) {
          this.error = "请填写运动信息";
          return;
        }
        Promise.all(tasks)
          .then(() => {
            this.message = "运动记录已保存";
            this.fetchSummary();
          })
          .catch(() => {
            this.error = "保存失败";
          });
        return;
      }
      if (card.id === "diet") {
        if (!payload.mealType && !payload.foodName && !payload.calories) {
          this.error = "请填写饮食信息";
          return;
        }
        request("/api/diet/add", "POST", {
          userId,
          mealType: payload.mealType || "餐次",
          foodName: payload.foodName || "食物",
          calories: parseInt(payload.calories || 0, 10),
          protein: payload.protein ? Number(payload.protein) : null,
          carbs: payload.carbs ? Number(payload.carbs) : null,
          fat: payload.fat ? Number(payload.fat) : null,
          date: today
        })
          .then(() => {
            this.message = "饮食记录已保存";
            this.fetchSummary();
          })
          .catch(() => {
            this.error = "保存失败";
          });
        return;
      }
      if (card.id === "sleep") {
        const routine = payload.routine || "";
        const match = routine.match(/^(\d{1,2}:\d{2})\s*-\s*(\d{1,2}:\d{2})$/);
        if (!match) {
          this.error = "请填写作息，例如 23:30-07:05";
          return;
        }
        const [startHour, startMinute] = match[1].split(":").map(Number);
        const [endHour, endMinute] = match[2].split(":").map(Number);
        const startMinutes = startHour * 60 + startMinute;
        const endMinutes = endHour * 60 + endMinute;
        const start = `${today}T${match[1]}:00`;
        let endDate = today;
        if (endMinutes <= startMinutes) {
          const next = new Date(today);
          next.setDate(next.getDate() + 1);
          endDate = next.toISOString().slice(0, 10);
        }
        const end = `${endDate}T${match[2]}:00`;
        request("/api/sleep/add", "POST", {
          userId,
          startTime: start,
          endTime: end,
          quality: payload.quality || ""
        })
          .then(() => {
            this.message = "睡眠记录已保存";
            this.fetchSummary();
          })
          .catch(() => {
            this.error = "保存失败";
          });
        return;
      }
      if (card.id === "weight") {
        if (!payload.weight) {
          this.error = "请填写体重";
          return;
        }
        request("/api/weight/add", "POST", {
          userId,
          weight: payload.weight ? Number(payload.weight) : 0,
          bmi: payload.bmi ? Number(payload.bmi) : null,
          date: today
        })
          .then(() => {
            this.message = "体重记录已保存";
            this.fetchSummary();
          })
          .catch(() => {
            this.error = "保存失败";
          });
        return;
      }
      if (card.id === "health") {
        if (!payload.systolic && !payload.diastolic && !payload.heartRate) {
          this.error = "请填写血压或心率";
          return;
        }
        request("/api/health-record/add", "POST", {
          userId,
          systolic: payload.systolic ? Number(payload.systolic) : null,
          diastolic: payload.diastolic ? Number(payload.diastolic) : null,
          heartRate: payload.heartRate ? Number(payload.heartRate) : null,
          date: today
        })
          .then(() => {
            this.message = "血压记录已保存";
            this.fetchSummary();
          })
          .catch(() => {
            this.error = "保存失败";
          });
        return;
      }
      if (card.id === "medication") {
        if (!payload.drugName) {
          this.error = "请填写药品名称";
          return;
        }
        request("/api/medication/add", "POST", {
          userId,
          drugName: payload.drugName || "药品",
          dosage: payload.dosage || "1 次",
          frequency: payload.frequency || "每日",
          remindTime: payload.remindTime || "",
          stock: payload.stock ? Number(payload.stock) : null,
          stockThreshold: payload.stockThreshold ? Number(payload.stockThreshold) : null,
          startDate: today
        })
          .then(() => {
            this.message = "用药记录已保存";
            this.fetchSummary();
          })
          .catch(() => {
            this.error = "保存失败";
          });
      }
    }
  }
};
</script>

<style>
.page {
  padding: 22px;
  min-height: 100vh;
  background: #f7f4ef;
  color: #2d2a26;
}

.header {
  margin-bottom: 14px;
}

.title {
  font-size: 18px;
  font-weight: 600;
}

.subtitle {
  display: block;
  color: #7c736b;
  margin-top: 4px;
}

.card {
  background: #ffffff;
  border-radius: 16px;
  padding: 14px 16px;
  margin-bottom: 12px;
  border: 1px solid #efe7dd;
  box-shadow: 0 8px 18px rgba(30, 25, 18, 0.05);
}

.row {
  display: flex;
  justify-content: space-between;
}

.label {
  font-weight: 600;
}

.value {
  color: #2d2a26;
  font-weight: 600;
  margin-top: 6px;
  display: block;
}

.hint {
  color: #8d847c;
  font-size: 12px;
  margin-top: 6px;
  display: block;
}

.status {
  display: block;
  color: #7c736b;
  margin-top: 8px;
}

.status.error {
  color: #ef4444;
}

.editor-body {
  display: grid;
  gap: 8px;
  margin-top: 10px;
}

.input {
  border: 1px solid #efe7dd;
  border-radius: 12px;
  padding: 8px 12px;
  background: #fbf9f6;
}

.edit-hint {
  color: #8d847c;
  font-size: 12px;
  margin-top: 4px;
}

.delete-row {
  margin-top: 4px;
}

.danger {
  background: #fcebea;
  color: #b84b46;
  border-radius: 10px;
}

.card.expanded {
  border: 1px solid #e6dccf;
  box-shadow: 0 14px 26px rgba(30, 25, 18, 0.08);
}

.card.dragging {
  opacity: 0.6;
}
</style>
