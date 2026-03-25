<template>
  <div class="admin-subscribe">
    <div class="page-header">
      <h1>订阅任务队列</h1>
      <div class="filters">
        <select v-model="status" @change="loadTasks">
          <option value="">全部状态</option>
          <option value="pending">待发送</option>
          <option value="sent">已发送</option>
          <option value="failed">发送失败</option>
        </select>
        <input 
          type="text" 
          v-model="userId" 
          placeholder="用户ID"
          @input="loadTasks"
        />
      </div>
    </div>

    <div class="table-card">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>用户ID</th>
            <th>OpenID</th>
            <th>模板ID</th>
            <th>内容</th>
            <th>状态</th>
            <th>创建时间</th>
            <th>发送时间</th>
            <th>错误信息</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="task in tasks" :key="task.id">
            <td>{{ task.id }}</td>
            <td>{{ task.userId }}</td>
            <td class="openid">{{ task.openid }}</td>
            <td class="template">{{ task.templateId }}</td>
            <td class="content">{{ task.content }}</td>
            <td>
              <span class="status-tag" :class="task.status">
                {{ statusMap[task.status] }}
              </span>
            </td>
            <td>{{ task.createTime }}</td>
            <td>{{ task.sendTime || '-' }}</td>
            <td class="error">{{ task.error || '-' }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="tasks.length === 0" class="empty">
      暂无任务记录
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { getSubscribeTasks } from "../../api";

const status = ref("");
const userId = ref("");
const tasks = ref([]);

const statusMap = {
  pending: "待发送",
  sent: "已发送",
  failed: "失败"
};

async function loadTasks() {
  try {
    const data = await getSubscribeTasks(status.value, userId.value, 200);
    if (data) tasks.value = data;
  } catch (e) {
    console.error("加载订阅任务失败", e);
    tasks.value = [];
  }
}

onMounted(() => {
  loadTasks();
});
</script>

<style scoped>
.admin-subscribe {
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

.filters {
  display: flex;
  gap: 12px;
}

.filters select,
.filters input {
  padding: 8px 16px;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  font-size: 14px;
  background: #fff;
}

.filters input {
  width: 120px;
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
  min-width: 1100px;
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

.template {
  font-family: monospace;
  font-size: 12px;
  color: #666;
}

.content {
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-tag {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
}

.status-tag.pending {
  background: #fff7e6;
  color: #fa8c16;
}

.status-tag.sent {
  background: #f6ffed;
  color: #52c41a;
}

.status-tag.failed {
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
