<template>
  <div class="family">
    <h1 class="page-title">家人管理</h1>

    <div class="family-list">
      <div class="family-card" v-for="item in families" :key="item.id">
        <div class="family-avatar">
          {{ item.name.charAt(0) }}
        </div>
        <div class="family-info">
          <div class="family-name">{{ item.name }}</div>
          <div class="family-detail">
            <span>{{ item.relation }}</span>
            <span>·</span>
            <span>{{ item.age }}岁</span>
          </div>
          <div class="family-contact">{{ item.phone }}</div>
        </div>
        <div class="family-status" :class="getHealthClass(item.healthStatus)">
          {{ item.healthStatus }}
        </div>
      </div>
    </div>

    <div v-if="families.length === 0" class="empty">
      暂无家人记录
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { mockData } from "../mock/data";
import { getUserId, getFamilyList } from "../api";

const families = ref([]);

function getHealthClass(status) {
  if (status === "良好") return "good";
  if (status === "一般") return "normal";
  return "poor";
}

onMounted(async () => {
  const userId = getUserId();
  try {
    const data = await getFamilyList(userId);
    if (data) families.value = data;
  } catch (e) {
    families.value = mockData.families;
  }
});
</script>

<style scoped>
.family {
  max-width: 900px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 24px;
  color: #333;
}

.family-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.family-card {
  display: flex;
  align-items: center;
  padding: 24px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  gap: 20px;
}

.family-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff7a45, #ffa070);
  color: #fff;
  font-size: 24px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.family-info {
  flex: 1;
}

.family-name {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.family-detail {
  font-size: 14px;
  color: #999;
  margin-bottom: 4px;
}

.family-contact {
  font-size: 13px;
  color: #666;
}

.family-status {
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
}

.family-status.good {
  background: #f6ffed;
  color: #52c41a;
}

.family-status.normal {
  background: #fff7e6;
  color: #fa8c16;
}

.family-status.poor {
  background: #fff1f0;
  color: #ff4d4f;
}

.empty {
  text-align: center;
  padding: 60px;
  color: #999;
}
</style>
