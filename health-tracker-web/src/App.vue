<template>
  <div class="app">
    <router-view v-if="isAuthPage" />
    <a-layout v-else class="shell">
      <AppSidebar />
      <a-layout class="content">
        <AppTopbar />
        <main class="page">
          <router-view />
        </main>
      </a-layout>
    </a-layout>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { useRoute } from "vue-router";
import AppSidebar from "./components/AppSidebar.vue";
import AppTopbar from "./components/AppTopbar.vue";

const route = useRoute();
const isAuthPage = computed(() => route.name === "login" || route.name === "home");
</script>

<style>
:root {
  --bg-start: #0f172a;
  --bg-end: #0f766e;
  --accent: #38bdf8;
  --glass: rgba(255, 255, 255, 0.12);
  --glass-border: rgba(255, 255, 255, 0.18);
}

body {
  margin: 0;
  background: radial-gradient(circle at 20% 20%, #1f2937 0%, #0f172a 45%, #0f766e 120%);
  color: #0f172a;
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
}

.app {
  min-height: 100vh;
  background:
    radial-gradient(circle at 10% 10%, rgba(56, 189, 248, 0.25), transparent 40%),
    radial-gradient(circle at 90% 20%, rgba(16, 185, 129, 0.18), transparent 45%),
    radial-gradient(circle at 30% 80%, rgba(248, 113, 113, 0.16), transparent 50%),
    linear-gradient(135deg, rgba(15, 23, 42, 0.95), rgba(15, 118, 110, 0.92));
}

.shell {
  min-height: 100vh;
  background: transparent;
}

.content {
  background: transparent;
}

.page {
  padding: 28px;
}

.ant-card {
  backdrop-filter: blur(16px);
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(255, 255, 255, 0.6);
}
</style>
