<template>
  <div class="reminders">
    <a-card title="即将提醒" class="panel">
      <a-list :data-source="reminders" bordered :loading="loading">
        <template #renderItem="{ item }">
          <a-list-item>
            <div class="list-row">
              <div>
                <div class="list-title">{{ item.title }}</div>
                <div class="list-sub">{{ item.time }}</div>
              </div>
              <a-tag :color="item.color">{{ item.type }}</a-tag>
            </div>
          </a-list-item>
        </template>
      </a-list>
      <div v-if="!loading && reminders.length === 0" class="empty">暂无提醒</div>
    </a-card>
  </div>
</template>

<script setup>
  import { onMounted, ref } from "vue";
  import { api } from "../api/http";

  const reminders = ref([]);
  const loading = ref(false);

  const fetchReminders = async () => {
    loading.value = true;
    try {
      const userId = Number(localStorage.getItem("userId") || 1);
      const list = await api.medicationList(userId);
      reminders.value = Array.isArray(list)
        ? list.map((item) => ({
            title: item.drugName || "用药提醒",
            time: item.remindTime ? `提醒 ${item.remindTime}` : item.frequency || "未设置时间",
            type: "用药",
            color: "green"
          }))
        : [];
    } finally {
      loading.value = false;
    }
  };

  onMounted(fetchReminders);
</script>

<style scoped>
.reminders {
  display: grid;
  gap: 18px;
}

.panel {
  border-radius: 16px;
}

.list-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.list-title {
  font-weight: 600;
}

.list-sub {
  color: #64748b;
  font-size: 0.85rem;
}

.empty {
  color: #94a3b8;
  font-size: 0.9rem;
  margin-top: 12px;
  text-align: center;
}
</style>
