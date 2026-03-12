<template>
  <header class="topbar">
    <div>
      <div class="topbar__title">下午好，{{ displayName }}</div>
      <div class="topbar__subtitle">这是你今天的健康概览。</div>
    </div>
    <div class="topbar__actions">
      <a-input-search placeholder="搜索记录" style="width: 220px" />
      <a-button type="primary">快速记录</a-button>
      <a-button @click="logout">退出登录</a-button>
    </div>
  </header>
</template>

<script setup>
import { computed } from "vue";
import { useRouter } from "vue-router";
import { clearToken } from "../api/http";

const router = useRouter();
const displayName = computed(() => localStorage.getItem("username") || "访客");

function logout() {
  clearToken();
  localStorage.removeItem("userId");
  localStorage.removeItem("username");
  router.push("/login");
}
</script>

<style scoped>
.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 28px 12px;
  background: #f8fafc;
}

.topbar__title {
  font-weight: 600;
  font-size: 1.1rem;
}

.topbar__subtitle {
  color: #64748b;
  font-size: 0.9rem;
}

.topbar__actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

@media (max-width: 900px) {
  .topbar {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>
