<template>
  <div class="app">
    <!-- 管理端布局 -->
    <template v-if="isAdminPage">
      <aside class="sidebar">
        <div class="sidebar-brand">
          <img src="/logo.png" alt="智康AI" class="logo-img" />
        </div>
        <nav class="sidebar-menu">
          <router-link to="/admin/users" class="sidebar-item" :class="{ active: route.path === '/admin/users' }">用户管理</router-link>
          <router-link to="/admin/logs/system" class="sidebar-item" :class="{ active: route.path === '/admin/logs/system' }">系统日志</router-link>
          <router-link to="/admin/logs/ai" class="sidebar-item" :class="{ active: route.path === '/admin/logs/ai' }">AI调用日志</router-link>
          <router-link to="/admin/subscribe-tasks" class="sidebar-item" :class="{ active: route.path === '/admin/subscribe-tasks' }">订阅任务</router-link>
          <router-link to="/admin/tasks" class="sidebar-item" :class="{ active: route.path === '/admin/tasks' }">任务管理</router-link>
        </nav>
        <div class="sidebar-footer">
          <router-link to="/" class="back-home">返回用户端</router-link>
          <a class="logout-link" @click="handleLogout">退出</a>
        </div>
      </aside>
      <main class="admin-content">
        <router-view />
      </main>
    </template>

    <!-- 用户端/登录页：避免重复卸载重建，减少输入闪烁 -->
    <router-view v-else />

    <footer class="footer">
      <p class="footer-links">
        <router-link to="/privacy-policy">隐私政策</router-link>
        <span class="footer-separator">|</span>
        <router-link to="/user-agreement">用户协议</router-link>
        <span class="footer-separator">|</span>
        <a href="https://beian.miit.gov.cn" target="_blank" rel="noopener noreferrer">
          晋ICP备2026003386号-1
        </a>
      </p>
      <p class="footer-copy">© 2026 智康AI</p>
    </footer>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { logout, isAdminLoggedIn } from "./api";

const route = useRoute();
const router = useRouter();

const isAdminPage = computed(() => route.path.startsWith("/admin") && route.path !== "/admin/login");

function handleLogout() {
  logout();
  router.push("/login");
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
  background: #f5f7fa;
  color: #333;
}

.app {
  min-height: 100vh;
}

.footer {
  text-align: center;
  padding: 24px 0 32px;
  color: #8c8c8c;
  font-size: 12px;
}

.footer-links {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  flex-wrap: wrap;
}

.footer-separator {
  color: #bdbdbd;
}

.footer-copy {
  margin-top: 8px;
}

.footer a {
  color: inherit;
  text-decoration: none;
}

.footer a:hover {
  color: #666;
  text-decoration: underline;
}

/* 用户端导航 */
.navbar {
  display: flex;
  align-items: center;
  padding: 0 40px;
  height: 64px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 100;
}

.navbar-brand .logo {
  font-size: 20px;
  font-weight: 700;
  color: #ff7a45;
}

.navbar-menu {
  display: flex;
  margin-left: 60px;
  gap: 8px;
}

.nav-item {
  padding: 8px 20px;
  color: #666;
  text-decoration: none;
  border-radius: 6px;
  transition: all 0.2s;
}

.nav-item:hover {
  background: #fff7f5;
  color: #ff7a45;
}

.nav-item.active {
  background: #fff0eb;
  color: #ff7a45;
  font-weight: 500;
}

.navbar-user {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-name {
  color: #666;
}

.admin-link, .logout-link {
  padding: 6px 16px;
  background: #f0f0f0;
  color: #666;
  text-decoration: none;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
}

.admin-link:hover, .logout-link:hover {
  background: #e0e0e0;
}

.logout-link {
  border: none;
}

.main-content {
  padding: 24px 40px;
  max-width: 1400px;
  margin: 0 auto;
}

/* 管理端侧边栏 */
.sidebar {
  position: fixed;
  left: 0;
  top: 0;
  width: 220px;
  height: 100vh;
  background: #1f1f1f;
  display: flex;
  flex-direction: column;
}

.sidebar-brand {
  padding: 24px 20px;
  border-bottom: 1px solid #333;
}

.sidebar-brand .logo-img {
  height: 40px;
  width: auto;
}

.sidebar-menu {
  flex: 1;
  padding: 20px 0;
}

.sidebar-item {
  display: block;
  padding: 12px 20px;
  color: #999;
  text-decoration: none;
  transition: all 0.2s;
}

.sidebar-item:hover {
  background: #2a2a2a;
  color: #fff;
}

.sidebar-item.active {
  background: #ff7a45;
  color: #fff;
}

.sidebar-footer {
  padding: 20px;
  border-top: 1px solid #333;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.back-home {
  color: #666;
  text-decoration: none;
  font-size: 13px;
}

.back-home:hover {
  color: #fff;
}

.logout-link {
  color: #666;
  font-size: 13px;
  cursor: pointer;
}

.logout-link:hover {
  color: #ff4d4f;
}

.admin-content {
  margin-left: 220px;
  padding: 24px;
  min-height: 100vh;
  background: #f5f7fa;
}
</style>
