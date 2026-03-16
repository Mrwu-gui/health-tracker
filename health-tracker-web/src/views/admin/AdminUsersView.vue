<template>
  <a-card title="用户管理" class="admin-card">
    <div class="admin-toolbar">
      <a-input
        v-model:value="keyword"
        placeholder="搜索用户名 / 手机 / OpenId"
        style="width: 260px"
        allow-clear
      />
      <a-input-number v-model:value="limit" :min="10" :max="500" />
      <a-button type="primary" :loading="loading" @click="load">查询</a-button>
    </div>
    <a-table :columns="columns" :data-source="rows" :loading="loading" row-key="id" />
  </a-card>
</template>

<script setup>
import { ref } from "vue";
import { api } from "../../api/http";

const keyword = ref("");
const limit = ref(200);
const loading = ref(false);
const rows = ref([]);

const columns = [
  { title: "ID", dataIndex: "id", width: 80 },
  { title: "用户名", dataIndex: "username" },
  { title: "手机号", dataIndex: "phone" },
  { title: "OpenId", dataIndex: "wxOpenid" },
  { title: "微信昵称", dataIndex: "wxNickname" },
  { title: "创建时间", dataIndex: "createdAt" }
];

async function load() {
  loading.value = true;
  try {
    rows.value = await api.adminUsers({
      keyword: keyword.value || undefined,
      limit: limit.value
    });
  } finally {
    loading.value = false;
  }
}

load();
</script>

<style scoped>
.admin-card {
  border-radius: 12px;
}
.admin-toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  align-items: center;
}
</style>
