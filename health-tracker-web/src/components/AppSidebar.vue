<template>
  <a-layout-sider class="sidebar" width="240">
    <div class="brand">
      <div class="brand__logo">HT</div>
      <div>
      <div class="brand__title">健康管理</div>
      <div class="brand__subtitle">健康中枢</div>
      </div>
    </div>
    <a-menu :selected-keys="[activeKey]" mode="inline" class="menu">
      <a-menu-item key="dashboard">
        <router-link to="/dashboard">仪表盘</router-link>
      </a-menu-item>
      <a-menu-item key="records">
        <router-link to="/records">记录</router-link>
      </a-menu-item>
      <a-menu-item key="goals">
        <router-link to="/goals">目标</router-link>
      </a-menu-item>
      <a-menu-item key="reminders">
        <router-link to="/reminders">提醒</router-link>
      </a-menu-item>
      <a-menu-item key="reports">
        <router-link to="/reports">报告</router-link>
      </a-menu-item>
      <a-menu-item key="settings">
        <router-link to="/settings">设置</router-link>
      </a-menu-item>
    </a-menu>
    <div class="sidebar__footer">
      <div class="profile">
        <div class="profile__avatar">{{ initials }}</div>
        <div>
        <div class="profile__name">{{ username }}</div>
        <div class="profile__role">普通用户</div>
        </div>
      </div>
    </div>
  </a-layout-sider>
</template>

<script setup>
import { computed } from "vue";
import { useRoute } from "vue-router";

const route = useRoute();
const activeKey = computed(() => route.name ?? "dashboard");
const username = computed(() => localStorage.getItem("username") || "访客");
const initials = computed(() => (username.value || "G").slice(0, 1).toUpperCase());
</script>

<style scoped>
.sidebar {
  background: linear-gradient(180deg, #0f172a 0%, #111827 100%);
  color: #e5e7eb;
  display: flex;
  flex-direction: column;
  padding: 20px 16px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 28px;
}

.brand__logo {
  background: #38bdf8;
  color: #0f172a;
  font-weight: 700;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: grid;
  place-items: center;
}

.brand__title {
  font-weight: 600;
}

.brand__subtitle {
  font-size: 0.75rem;
  color: #94a3b8;
}

.menu {
  background: transparent;
  color: #e5e7eb;
  border-right: none;
}

.menu :deep(.ant-menu-item) {
  border-radius: 10px;
  margin: 4px 0;
}

.menu :deep(.ant-menu-item-selected) {
  background: rgba(56, 189, 248, 0.2);
}

.menu :deep(a) {
  color: inherit;
}

.sidebar__footer {
  margin-top: auto;
  padding-top: 20px;
  border-top: 1px solid rgba(148, 163, 184, 0.2);
}

.profile {
  display: flex;
  gap: 12px;
  align-items: center;
}

.profile__avatar {
  width: 36px;
  height: 36px;
  border-radius: 12px;
  background: #1f2937;
  display: grid;
  place-items: center;
  font-weight: 600;
}

.profile__name {
  font-weight: 600;
}

.profile__role {
  font-size: 0.75rem;
  color: #9ca3af;
}
</style>
