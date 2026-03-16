<template>
  <div class="admin-login">
    <a-card class="admin-login__card" title="后台登录">
      <a-form layout="vertical">
        <a-form-item label="Admin Key">
          <a-input v-model:value="form.key" placeholder="输入后台密钥" />
        </a-form-item>
        <a-button type="primary" block :loading="loading" @click="submit">
          进入后台
        </a-button>
      </a-form>
      <div class="admin-login__hint">{{ message }}</div>
    </a-card>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { setAdminKey } from "../../api/http";

const router = useRouter();
const form = reactive({ key: "" });
const loading = ref(false);
const message = ref("");

function submit() {
  message.value = "";
  loading.value = true;
  setTimeout(() => {
    setAdminKey(form.key.trim());
    loading.value = false;
    router.push("/admin/users");
  }, 200);
}
</script>

<style scoped>
.admin-login {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fb;
}
.admin-login__card {
  width: 360px;
}
.admin-login__hint {
  margin-top: 12px;
  color: #999;
  font-size: 12px;
}
</style>
