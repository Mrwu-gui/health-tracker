<template>
  <div class="admin-login-page">
    <div class="admin-login-card">
      <h1>管理员后台登录</h1>
      <p class="hint">请输入后台账号和密码</p>
      <form class="form" @submit.prevent="submit">
        <label for="admin-username">账号</label>
        <input id="admin-username" v-model="form.username" type="text" autocomplete="username" required />

        <label for="admin-password">密码</label>
        <input id="admin-password" v-model="form.password" type="password" autocomplete="current-password" required />

        <p v-if="errorMsg" class="error">{{ errorMsg }}</p>

        <button type="submit" :disabled="loading">
          {{ loading ? "登录中..." : "进入后台" }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { adminLogin, setAdminToken } from "../../api";

const router = useRouter();
const form = reactive({ username: "", password: "" });
const loading = ref(false);
const errorMsg = ref("");

async function submit() {
  errorMsg.value = "";
  if (!form.username || !form.password) {
    errorMsg.value = "请输入账号和密码";
    return;
  }
  loading.value = true;
  try {
    const data = await adminLogin(form.username, form.password);
    if (!data?.token) {
      throw new Error("登录失败，请检查账号或密码");
    }
    setAdminToken(data.token);
    router.replace("/admin/users");
  } catch (error) {
    errorMsg.value = error.message || "登录失败，请稍后重试";
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.admin-login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8fafc;
}

.admin-login-card {
  width: 360px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 30px rgba(15, 23, 42, 0.08);
  padding: 24px;
}

h1 {
  font-size: 24px;
  margin-bottom: 8px;
}

.hint {
  margin-bottom: 16px;
  color: #999;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

label {
  font-size: 14px;
  color: #666;
}

input {
  padding: 10px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 10px;
}

button {
  margin-top: 8px;
  border: none;
  background: #a23f00;
  color: #fff;
  border-radius: 999px;
  padding: 10px 0;
  font-weight: 600;
  cursor: pointer;
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error {
  color: #dc2626;
  font-size: 13px;
}
</style>
