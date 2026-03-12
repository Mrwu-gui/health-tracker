import { createRouter, createWebHistory } from "vue-router";
import DashboardView from "../views/DashboardView.vue";
import RecordsView from "../views/RecordsView.vue";
import GoalsView from "../views/GoalsView.vue";
import RemindersView from "../views/RemindersView.vue";
import ReportsView from "../views/ReportsView.vue";
import SettingsView from "../views/SettingsView.vue";
import LoginView from "../views/LoginView.vue";
import HomeView from "../views/HomeView.vue";
import { getToken } from "../api/http";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: "/", name: "home", component: HomeView },
    { path: "/login", name: "login", component: LoginView },
    { path: "/dashboard", name: "dashboard", component: DashboardView },
    { path: "/records", name: "records", component: RecordsView },
    { path: "/goals", name: "goals", component: GoalsView },
    { path: "/reminders", name: "reminders", component: RemindersView },
    { path: "/reports", name: "reports", component: ReportsView },
    { path: "/settings", name: "settings", component: SettingsView }
  ]
});

router.beforeEach((to) => {
  if (to.name === "login" || to.name === "home") {
    if (to.name === "home" && getToken()) {
      return "/dashboard";
    }
    return true;
  }
  if (!getToken()) return "/login";
  return true;
});

export default router;
