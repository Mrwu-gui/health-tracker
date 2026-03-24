import { createRouter, createWebHistory } from "vue-router";
import { isLoggedIn, isAdminLoggedIn } from "../api";

// 用户端页面
import HomeView from "../views/HomeView.vue";
import AnalysisView from "../views/AnalysisView.vue";
import RemindersView from "../views/RemindersView.vue";
import MedicationsView from "../views/MedicationsView.vue";
import FamilyView from "../views/FamilyView.vue";
import LoginView from "../views/LoginView.vue";
import DashboardView from "../views/DashboardView.vue";
import ReportsView from "../views/ReportsView.vue";
import GoalsView from "../views/GoalsView.vue";
import RecordsView from "../views/RecordsView.vue";
import SettingsView from "../views/SettingsView.vue";

// 管理端页面
import AdminUsersView from "../views/admin/AdminUsersView.vue";
import AdminLogsView from "../views/admin/AdminLogsView.vue";
import AdminAILogsView from "../views/admin/AdminAILogsView.vue";
import AdminSubscribeTasksView from "../views/admin/AdminSubscribeTasksView.vue";
import AdminTasksView from "../views/admin/AdminTasksView.vue";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    // 登录页
    {
      path: "/login",
      name: "login",
      component: LoginView
    },

    // ========== 用户端（需要登录）==========
    {
      path: "/",
      name: "home",
      component: HomeView,
      meta: { requiresAuth: true }
    },
    {
      path: "/dashboard",
      name: "dashboard",
      component: HomeView,
      meta: { requiresAuth: true }
    },
    {
      path: "/analysis",
      name: "analysis",
      component: AnalysisView,
      meta: { requiresAuth: true }
    },
    {
      path: "/reminders",
      name: "reminders",
      component: RemindersView,
      meta: { requiresAuth: true }
    },
    {
      path: "/medications",
      name: "medications",
      component: MedicationsView,
      meta: { requiresAuth: true }
    },
    {
      path: "/family",
      name: "family",
      component: FamilyView,
      meta: { requiresAuth: true }
    },
    {
      path: "/reports",
      name: "reports",
      component: ReportsView,
      meta: { requiresAuth: true }
    },
    {
      path: "/goals",
      name: "goals",
      component: GoalsView,
      meta: { requiresAuth: true }
    },
    {
      path: "/records",
      name: "records",
      component: RecordsView,
      meta: { requiresAuth: true }
    },
    {
      path: "/settings",
      name: "settings",
      component: SettingsView,
      meta: { requiresAuth: true }
    },

    // ========== 管理端（需要管理员登录）==========
    {
      path: "/admin/users",
      name: "admin-users",
      component: AdminUsersView,
      meta: { requiresAdminAuth: true }
    },
    {
      path: "/admin/logs/system",
      name: "admin-logs-system",
      component: AdminLogsView,
      meta: { requiresAdminAuth: true }
    },
    {
      path: "/admin/logs/ai",
      name: "admin-logs-ai",
      component: AdminAILogsView,
      meta: { requiresAdminAuth: true }
    },
    {
      path: "/admin/subscribe-tasks",
      name: "admin-subscribe-tasks",
      component: AdminSubscribeTasksView,
      meta: { requiresAdminAuth: true }
    },
    {
      path: "/admin/tasks",
      name: "admin-tasks",
      component: AdminTasksView,
      meta: { requiresAdminAuth: true }
    }
  ]
});

// 路由守卫
router.beforeEach((to, _from, next) => {
  // 登录页直接放行
  if (to.name === "login") {
    next();
    return;
  }

  // 管理端需要管理员登录
  if (to.meta.requiresAdminAuth) {
    if (!isAdminLoggedIn()) {
      next("/login");
      return;
    }
  }

  // 用户端需要用户登录
  if (to.meta.requiresAuth) {
    if (!isLoggedIn()) {
      next("/login");
      return;
    }
  }

  next();
});

export default router;
