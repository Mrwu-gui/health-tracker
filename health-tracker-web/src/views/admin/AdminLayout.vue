<template>
  <a-layout class="admin-layout">
    <a-layout-sider width="220" class="admin-layout__sider">
      <div class="admin-layout__brand">健康管理后台</div>
      <a-menu
        theme="dark"
        mode="inline"
        :selectedKeys="[activeKey]"
        @click="onMenuClick"
      >
        <a-menu-item key="users">用户管理</a-menu-item>
        <a-menu-item key="tasks">定时任务</a-menu-item>
        <a-menu-item key="logs">日志中心</a-menu-item>
      </a-menu>
    </a-layout-sider>
    <a-layout>
      <a-layout-header class="admin-layout__header">
        <div class="admin-layout__header-right">
          <a-button size="small" @click="logout">退出</a-button>
        </div>
      </a-layout-header>
      <a-layout-content class="admin-layout__content">
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup>
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { clearAdminKey } from "../../api/http";

const route = useRoute();
const router = useRouter();

const activeKey = computed(() => {
  if (route.path.includes("/admin/users")) return "users";
  if (route.path.includes("/admin/tasks")) return "tasks";
  return "logs";
});

function onMenuClick({ key }) {
  router.push(`/admin/${key}`);
}

function logout() {
  clearAdminKey();
  router.push("/admin/login");
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
}
.admin-layout__sider {
  background: #0f1a2c;
}
.admin-layout__brand {
  height: 56px;
  color: #fff;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  letter-spacing: 1px;
}
.admin-layout__header {
  background: #fff;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}
.admin-layout__content {
  padding: 24px;
  background: #f5f7fb;
  min-height: calc(100vh - 64px);
}
.admin-layout__header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
</style>
