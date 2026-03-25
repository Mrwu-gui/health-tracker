<template>
  <div class="admin-ai-logs">
    <div class="page-header">
      <h1>AI 调用日志</h1>
    </div>

    <div class="table-card">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>用户ID</th>
            <th>OpenID</th>
            <th>请求内容</th>
            <th>响应</th>
            <th>状态</th>
            <th>错误信息</th>
            <th>时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="log in logs" :key="log.id">
            <td>{{ log.id }}</td>
            <td>{{ log.userId }}</td>
            <td class="openid">{{ log.openid }}</td>
            <td class="request">{{ log.request }}</td>
            <td class="response">{{ log.response || '-' }}</td>
            <td>
              <span class="status-tag" :class="log.status">
                {{ log.status === 'success' ? '成功' : '失败' }}
              </span>
            </td>
            <td class="error">{{ log.error || '-' }}</td>
            <td>{{ log.time }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="logs.length === 0" class="empty">
      暂无AI调用日志
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { getAILogs } from "../../api";

const logs = ref([]);

async function loadLogs() {
  try {
    const data = await getAILogs(200);
    if (data) logs.value = data;
  } catch (e) {
    console.error("加载AI日志失败", e);
    logs.value = [];
  }
}

onMounted(() => {
  loadLogs();
});
</script>

<style scoped>
.admin-ai-logs {
  max-width: 1400px;
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

.table-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  min-width: 1000px;
}

th, td {
  padding: 14px 16px;
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

.openid {
  font-family: monospace;
  font-size: 12px;
  color: #999;
}

.request {
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.response {
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #666;
}

.status-tag {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
}

.status-tag.success {
  background: #f6ffed;
  color: #52c41a;
}

.status-tag.error {
  background: #fff1f0;
  color: #ff4d4f;
}

.error {
  color: #ff4d4f;
  font-size: 12px;
}

.empty {
  text-align: center;
  padding: 60px;
  color: #999;
}
</style>
