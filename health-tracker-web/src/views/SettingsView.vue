<template>
  <div class="settings">
    <a-card title="个人资料" class="panel">
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="姓名">
              <a-input placeholder="李娜" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="年龄">
              <a-input placeholder="30" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="身高 (厘米)">
              <a-input placeholder="175" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="体重 (千克)">
              <a-input placeholder="70.5" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-button type="primary" :loading="loading" @click="saveProfile">保存</a-button>
        <div class="status-row" v-if="message">{{ message }}</div>
      </a-form>
    </a-card>

    <a-card title="提醒偏好" class="panel">
      <a-space direction="vertical">
        <a-switch checked>运动提醒</a-switch>
        <a-switch checked>用药提醒</a-switch>
        <a-switch>周报邮件</a-switch>
      </a-space>
    </a-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { api } from "../api/http";

const loading = ref(false);
const message = ref("");

async function saveProfile() {
  // placeholder for update profile
}

onMounted(async () => {
  const userId = localStorage.getItem("userId");
  if (!userId) return;
  loading.value = true;
  message.value = "";
  try {
    await api.profile(userId);
  } catch (err) {
    message.value = err.message || "获取资料失败";
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped>
.settings {
  display: grid;
  gap: 18px;
}

.panel {
  border-radius: 16px;
}
</style>
