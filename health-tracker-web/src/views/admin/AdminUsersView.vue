<template>
  <div class="admin-users">
    <div class="page-header">
      <h1>用户管理</h1>
      <div class="search-box">
        <input 
          type="text" 
          v-model="keyword" 
          placeholder="搜索用户昵称/手机号..."
          @input="handleSearch"
        />
      </div>
    </div>

    <div class="table-card">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>昵称</th>
            <th>手机号</th>
            <th>微信OpenID</th>
            <th>创建时间</th>
            <th>状态</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in users" :key="user.id">
            <td>{{ user.id }}</td>
            <td>{{ user.nickname }}</td>
            <td>{{ user.phone || '-' }}</td>
            <td class="openid">{{ user.openid }}</td>
            <td>{{ user.createTime }}</td>
            <td>
              <span class="status-tag" :class="user.status">
                {{ user.status === 'active' ? '正常' : '禁用' }}
              </span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { getAdminUsers } from "../../api";

const keyword = ref("");
const users = ref([]);

async function loadUsers() {
  try {
    const data = await getAdminUsers(keyword.value, 200);
    if (data) users.value = data;
  } catch (e) {
    console.error("加载用户列表失败", e);
    users.value = [];
  }
}

function handleSearch() {
  loadUsers();
}

onMounted(() => {
  loadUsers();
});
</script>

<style scoped>
.admin-users {
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

.search-box input {
  width: 280px;
  padding: 10px 16px;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  font-size: 14px;
}

.search-box input:focus {
  outline: none;
  border-color: #ff7a45;
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

.openid {
  font-family: monospace;
  font-size: 12px;
  color: #999;
}

.status-tag {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
}

.status-tag.active {
  background: #f6ffed;
  color: #52c41a;
}

.status-tag.inactive {
  background: #fff1f0;
  color: #ff4d4f;
}
</style>
