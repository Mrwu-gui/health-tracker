<template>
  <div class="admin-logs">
    <div class="page-header">
      <h1>系统日志</h1>
      <div class="filters">
        <select v-model="level" @change="loadLogs">
          <option value="">全部级别</option>
          <option value="info">Info</option>
          <option value="warn">Warn</option>
          <option value="error">Error</option>
        </select>
      </div>
    </div>

    <div class="table-card">
      <table>
        <thead>
          <tr>
            <th>级别</th>
            <th>模块</th>
            <th>路径</th>
            <th>消息</th>
            <th>时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="log in logs" :key="log.id">
            <td>
              <span class="level-tag" :class="log.level">
                {{ log.level.toUpperCase() }}
              </span>
            </td>
            <td>{{ log.module }}</td>
            <td class="path">{{ log.path }}</td>
            <td>{{ log.message }}</td>
            <td>{{ log.time }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="logs.length === 0" class="empty">
      暂无日志记录
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { getSystemLogs } from "../../api";

const level = ref("");
const logs = ref([]);

async function loadLogs() {
  try {
    const data = await getSystemLogs(level.value, 200);
    if (data) logs.value = data;
  } catch (e) {
    console.error("加载系统日志失败", e);
    logs.value = [];
  }
}

onMounted(() => {
  loadLogs();
});
</script>

<style scoped>
.admin-logs {
  max-width: 1200px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h1 {
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.filters select {
  padding: 8px 16px;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  font-size: 14px;
  background: #fff;
}

.table-card {
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

.level-tag {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
}

.level-tag.info {
  background: #fff0eb;
  color: #ff7a45;
}

.level-tag.warn {
  background: #fff7e6;
  color: #fa8c16;
}

.level-tag.error {
  background: #fff1f0;
  color: #ff4d4f;
}

.path {
  font-family: monospace;
  font-size: 12px;
  color: #666;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.empty {
  text-align: center;
  padding: 60px;
  color: #999;
}
</style>
