<template>
  <a-card title="日志中心" class="admin-card">
    <a-tabs v-model:activeKey="activeTab">
      <a-tab-pane key="system" tab="系统日志">
        <div class="admin-toolbar">
          <a-select v-model:value="systemLevel" placeholder="级别" style="width: 140px" allow-clear>
            <a-select-option value="WARN">WARN</a-select-option>
            <a-select-option value="ERROR">ERROR</a-select-option>
          </a-select>
          <a-input-number v-model:value="systemLimit" :min="10" :max="500" />
          <a-button type="primary" :loading="loadingSystem" @click="loadSystem">查询</a-button>
        </div>
        <a-table :columns="systemColumns" :data-source="systemRows" row-key="id" />
      </a-tab-pane>
      <a-tab-pane key="ai" tab="AI 日志">
        <div class="admin-toolbar">
          <a-select v-model:value="aiStatus" placeholder="状态" style="width: 140px" allow-clear>
            <a-select-option :value="0">成功</a-select-option>
            <a-select-option :value="1">失败</a-select-option>
          </a-select>
          <a-input-number v-model:value="aiLimit" :min="10" :max="500" />
          <a-button type="primary" :loading="loadingAi" @click="loadAi">查询</a-button>
        </div>
        <a-table :columns="aiColumns" :data-source="aiRows" row-key="id" />
      </a-tab-pane>
    </a-tabs>
  </a-card>
</template>

<script setup>
import { ref } from "vue";
import { api } from "../../api/http";

const activeTab = ref("system");

const systemLevel = ref();
const systemLimit = ref(200);
const loadingSystem = ref(false);
const systemRows = ref([]);

const aiStatus = ref();
const aiLimit = ref(200);
const loadingAi = ref(false);
const aiRows = ref([]);

const systemColumns = [
  { title: "时间", dataIndex: "createdAt", width: 180 },
  { title: "级别", dataIndex: "level", width: 90 },
  { title: "模块", dataIndex: "module", width: 120 },
  { title: "路径", dataIndex: "path" },
  { title: "方法", dataIndex: "method", width: 90 },
  { title: "消息", dataIndex: "message" }
];

const aiColumns = [
  { title: "时间", dataIndex: "createdAt", width: 180 },
  { title: "UserId", dataIndex: "userId", width: 90 },
  { title: "OpenId", dataIndex: "wxOpenid" },
  { title: "状态", dataIndex: "status", width: 80 },
  { title: "请求", dataIndex: "requestText" },
  { title: "响应", dataIndex: "responseText" },
  { title: "错误", dataIndex: "error" }
];

async function loadSystem() {
  loadingSystem.value = true;
  try {
    systemRows.value = await api.adminSystemLogs({
      level: systemLevel.value || undefined,
      limit: systemLimit.value
    });
  } finally {
    loadingSystem.value = false;
  }
}

async function loadAi() {
  loadingAi.value = true;
  try {
    aiRows.value = await api.adminAiLogs({
      status: aiStatus.value ?? undefined,
      limit: aiLimit.value
    });
  } finally {
    loadingAi.value = false;
  }
}

loadSystem();
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
  flex-wrap: wrap;
}
</style>
