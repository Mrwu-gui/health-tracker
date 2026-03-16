<template>
  <a-card title="定时任务管理" class="admin-card">
    <div class="admin-toolbar">
      <a-select v-model:value="status" placeholder="状态" style="width: 140px" allow-clear>
        <a-select-option :value="0">待发送</a-select-option>
        <a-select-option :value="1">已发送</a-select-option>
        <a-select-option :value="2">失败</a-select-option>
      </a-select>
      <a-input-number v-model:value="userId" placeholder="UserId" />
      <a-input-number v-model:value="limit" :min="10" :max="500" />
      <a-button type="primary" :loading="loading" @click="load">查询</a-button>
      <a-button @click="openCreate">新增任务</a-button>
    </div>

    <a-table :columns="columns" :data-source="rows" :loading="loading" row-key="id">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="statusColor(record.status)">{{ statusLabel(record.status) }}</a-tag>
        </template>
        <template v-if="column.key === 'actions'">
          <a-space>
            <a-button size="small" @click="openEdit(record)">编辑</a-button>
          </a-space>
        </template>
      </template>
    </a-table>
  </a-card>

  <a-modal v-model:open="editVisible" title="编辑任务" @ok="saveEdit" :confirm-loading="saving">
    <a-form layout="vertical">
      <a-form-item label="状态">
        <a-select v-model:value="editForm.status" style="width: 100%">
          <a-select-option :value="0">待发送</a-select-option>
          <a-select-option :value="1">已发送</a-select-option>
          <a-select-option :value="2">失败</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="发送时间">
        <a-input v-model:value="editForm.sendTime" placeholder="yyyy-MM-dd HH:mm:ss" />
      </a-form-item>
      <a-form-item label="响应信息">
        <a-textarea v-model:value="editForm.response" rows="3" />
      </a-form-item>
    </a-form>
  </a-modal>

  <a-modal v-model:open="createVisible" title="新增任务" @ok="saveCreate" :confirm-loading="saving">
    <a-form layout="vertical">
      <a-form-item label="UserId">
        <a-input-number v-model:value="createForm.userId" style="width: 100%" />
      </a-form-item>
      <a-form-item label="OpenId">
        <a-input v-model:value="createForm.openid" />
      </a-form-item>
      <a-form-item label="模板ID">
        <a-input v-model:value="createForm.templateId" />
      </a-form-item>
      <a-form-item label="页面">
        <a-input v-model:value="createForm.page" placeholder="pages/reminders/index" />
      </a-form-item>
      <a-form-item label="模板数据 JSON">
        <a-textarea v-model:value="createForm.dataJson" rows="4" />
      </a-form-item>
      <a-form-item label="发送时间">
        <a-input v-model:value="createForm.sendTime" placeholder="yyyy-MM-dd HH:mm:ss" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
import { reactive, ref } from "vue";
import { api } from "../../api/http";

const status = ref();
const userId = ref();
const limit = ref(200);
const loading = ref(false);
const rows = ref([]);
const saving = ref(false);

const editVisible = ref(false);
const createVisible = ref(false);
const editForm = reactive({ id: null, status: 0, sendTime: "", response: "" });
const createForm = reactive({
  userId: null,
  openid: "",
  templateId: "",
  page: "",
  dataJson: "",
  sendTime: ""
});

const columns = [
  { title: "ID", dataIndex: "id", width: 80 },
  { title: "UserId", dataIndex: "userId", width: 90 },
  { title: "OpenId", dataIndex: "openid" },
  { title: "模板ID", dataIndex: "templateId" },
  { title: "发送时间", dataIndex: "sendTime" },
  { title: "状态", dataIndex: "status", key: "status", width: 90 },
  { title: "响应", dataIndex: "response" },
  { title: "操作", key: "actions", width: 100 }
];

function statusLabel(value) {
  if (value === 1) return "已发送";
  if (value === 2) return "失败";
  return "待发送";
}

function statusColor(value) {
  if (value === 1) return "green";
  if (value === 2) return "red";
  return "blue";
}

async function load() {
  loading.value = true;
  try {
    rows.value = await api.adminSubscribeTasks({
      status: status.value ?? undefined,
      userId: userId.value ?? undefined,
      limit: limit.value
    });
  } finally {
    loading.value = false;
  }
}

function openEdit(record) {
  editForm.id = record.id;
  editForm.status = record.status;
  editForm.sendTime = record.sendTime || "";
  editForm.response = record.response || "";
  editVisible.value = true;
}

function openCreate() {
  createForm.userId = null;
  createForm.openid = "";
  createForm.templateId = "";
  createForm.page = "";
  createForm.dataJson = "";
  createForm.sendTime = "";
  createVisible.value = true;
}

async function saveEdit() {
  saving.value = true;
  try {
    await api.adminSubscribeTaskUpdate({
      id: editForm.id,
      status: editForm.status,
      sendTime: editForm.sendTime || undefined,
      response: editForm.response || undefined
    });
    editVisible.value = false;
    load();
  } finally {
    saving.value = false;
  }
}

async function saveCreate() {
  saving.value = true;
  try {
    await api.adminSubscribeTaskAdd({
      userId: createForm.userId,
      openid: createForm.openid,
      templateId: createForm.templateId,
      page: createForm.page,
      dataJson: createForm.dataJson,
      sendTime: createForm.sendTime
    });
    createVisible.value = false;
    load();
  } finally {
    saving.value = false;
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
  flex-wrap: wrap;
}
</style>
