<template>
  <div class="medications">
    <h1 class="page-title">用药管理</h1>

    <!-- 药物列表 -->
    <div class="section">
      <h2 class="section-title">药物列表</h2>
      <div class="medicine-list">
        <div 
          class="medicine-card" 
          v-for="item in medications" 
          :key="item.id"
          :class="{ paused: item.status === 'paused' }"
        >
          <div class="medicine-icon">💊</div>
          <div class="medicine-info">
            <span class="medicine-name">{{ item.name }}</span>
            <span class="medicine-dosage">{{ item.dosage }} · {{ item.frequency }}</span>
          </div>
          <div class="medicine-time" v-if="item.time">{{ item.time }}</div>
          <div class="medicine-status" :class="item.status">
            {{ statusMap[item.status] }}
          </div>
        </div>
      </div>
    </div>

    <!-- 用药记录 -->
    <div class="section">
      <h2 class="section-title">用药记录</h2>
      <div class="record-table">
        <table>
          <thead>
            <tr>
              <th>药物名称</th>
              <th>服用时间</th>
              <th>状态</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in medicationRecords" :key="item.id">
              <td>{{ item.medicationName }}</td>
              <td>{{ item.takeTime }}</td>
              <td>
                <span class="status-tag" :class="item.status">
                  {{ recordStatusMap[item.status] }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="medications.length === 0" class="empty">
      暂无用药记录
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { mockData } from "../mock/data";
import { getUserId, getMedicationList, getMedicationRecordList } from "../api";

const medications = ref([]);
const medicationRecords = ref([]);

const statusMap = {
  active: "应用中",
  paused: "已暂停"
};

const recordStatusMap = {
  taken: "已服用",
  missed: "已错过"
};

onMounted(async () => {
  const userId = getUserId();
  try {
    const [list, records] = await Promise.all([
      getMedicationList(userId),
      getMedicationRecordList(userId)
    ]);
    if (list) medications.value = list;
    if (records) medicationRecords.value = records;
  } catch (e) {
    medications.value = mockData.medications;
    medicationRecords.value = mockData.medicationRecords;
  }
});
</script>

<style scoped>
.medications {
  max-width: 1000px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 24px;
  color: #333;
}

.section {
  margin-bottom: 40px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #333;
}

.medicine-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.medicine-card {
  display: flex;
  align-items: center;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  gap: 16px;
}

.medicine-card.paused {
  opacity: 0.6;
}

.medicine-icon {
  font-size: 32px;
}

.medicine-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.medicine-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.medicine-dosage {
  font-size: 13px;
  color: #999;
}

.medicine-time {
  font-size: 13px;
  color: #666;
}

.medicine-status {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
}

.medicine-status.active {
  background: #f6ffed;
  color: #52c41a;
}

.medicine-status.paused {
  background: #f0f0f0;
  color: #999;
}

/* 记录表格 */
.record-table {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 14px 20px;
  text-align: left;
}

th {
  background: #fafafa;
  font-weight: 500;
  color: #666;
  font-size: 13px;
}

td {
  border-top: 1px solid #f0f0f0;
  color: #333;
}

.status-tag {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
}

.status-tag.taken {
  background: #f6ffed;
  color: #52c41a;
}

.status-tag.missed {
  background: #fff1f0;
  color: #ff4d4f;
}

.empty {
  text-align: center;
  padding: 60px;
  color: #999;
}
</style>
