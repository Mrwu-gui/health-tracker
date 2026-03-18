<template>
  <div class="reminders">
    <h1 class="page-title">提醒列表</h1>

    <div class="reminder-list">
      <div 
        class="reminder-card" 
        v-for="item in reminders" 
        :key="item.id"
        :class="{ completed: item.status === 'completed' }"
      >
        <div class="reminder-time">{{ item.time }}</div>
        <div class="reminder-content">
          <span class="reminder-text">{{ item.content }}</span>
          <span class="reminder-type">{{ typeMap[item.type] }}</span>
        </div>
        <div class="reminder-status" :class="item.status">
          {{ statusMap[item.status] }}
        </div>
      </div>
    </div>

    <div v-if="reminders.length === 0" class="empty">
      暂无提醒记录
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { mockData } from "../mock/data";
import { getUserId, getReminderList } from "../api";

const reminders = ref([]);

const typeMap = {
  medication: "用药",
  habit: "习惯",
  exercise: "运动",
  sleep: "睡眠",
  diet: "饮食"
};

const statusMap = {
  pending: "待执行",
  completed: "已完成",
  missed: "已错过"
};

onMounted(async () => {
  const userId = getUserId();
  try {
    const data = await getReminderList(userId);
    if (data) reminders.value = data;
  } catch (e) {
    reminders.value = mockData.reminders;
  }
});
</script>

<style scoped>
.reminders {
  max-width: 900px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 24px;
  color: #333;
}

.reminder-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.reminder-card {
  display: flex;
  align-items: center;
  padding: 20px 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  gap: 24px;
}

.reminder-card.completed {
  opacity: 0.6;
}

.reminder-time {
  font-size: 18px;
  font-weight: 600;
  color: #ff7a45;
  min-width: 60px;
}

.reminder-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.reminder-text {
  font-size: 15px;
  color: #333;
}

.reminder-type {
  font-size: 12px;
  color: #999;
}

.reminder-status {
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
}

.reminder-status.pending {
  background: #fff7e6;
  color: #fa8c16;
}

.reminder-status.completed {
  background: #f6ffed;
  color: #52c41a;
}

.reminder-status.missed {
  background: #fff1f0;
  color: #ff4d4f;
}

.empty {
  text-align: center;
  padding: 60px;
  color: #999;
}
</style>
