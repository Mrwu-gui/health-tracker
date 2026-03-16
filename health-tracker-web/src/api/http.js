const BASE_URL = import.meta.env.VITE_API_BASE || "";

export function getToken() {
  return localStorage.getItem("token") || "";
}

export function getAdminKey() {
  return localStorage.getItem("admin_key") || "";
}

export function setToken(token) {
  localStorage.setItem("token", token);
}

export function setAdminKey(key) {
  localStorage.setItem("admin_key", key);
}

export function clearToken() {
  localStorage.removeItem("token");
}

export function clearAdminKey() {
  localStorage.removeItem("admin_key");
}

async function request(path, options = {}) {
  const headers = {
    "Content-Type": "application/json",
    ...(options.headers || {})
  };
  const token = getToken();
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }
  const resp = await fetch(`${BASE_URL}${path}`, {
    ...options,
    headers
  });
  let body = null;
  try {
    body = await resp.json();
  } catch (_) {}
  if (!resp.ok) {
    const message = body?.message || "请求失败";
    throw new Error(message);
  }
  if (body && typeof body.code === "number") {
    if (body.code !== 0) {
      throw new Error(body.message || "请求失败");
    }
    return body.data;
  }
  return body;
}

async function adminRequest(path, options = {}) {
  const headers = {
    "Content-Type": "application/json",
    ...(options.headers || {})
  };
  const adminKey = getAdminKey();
  if (adminKey) {
    headers["X-Admin-Key"] = adminKey;
  }
  const resp = await fetch(`${BASE_URL}${path}`, {
    ...options,
    headers
  });
  let body = null;
  try {
    body = await resp.json();
  } catch (_) {}
  if (!resp.ok) {
    const message = body?.message || "请求失败";
    throw new Error(message);
  }
  if (body && typeof body.code === "number") {
    if (body.code !== 0) {
      throw new Error(body.message || "请求失败");
    }
    return body.data;
  }
  return body;
}

function buildQuery(params = {}) {
  const entries = Object.entries(params).filter(([, value]) => {
    if (value === undefined || value === null) return false;
    if (value === "") return false;
    if (value === "undefined") return false;
    return true;
  });
  if (!entries.length) return "";
  return new URLSearchParams(entries).toString();
}

export const api = {
  login(data) {
    return request("/api/user/login", {
      method: "POST",
      body: JSON.stringify(data)
    });
  },
  smsSend(data) {
    return request("/api/auth/phone/send", {
      method: "POST",
      body: JSON.stringify(data)
    });
  },
  smsLogin(data) {
    return request("/api/auth/phone/login", {
      method: "POST",
      body: JSON.stringify(data)
    });
  },
  captcha() {
    return request("/api/auth/captcha");
  },
  profile(userId) {
    return request(`/api/user/profile?userId=${userId}`);
  },
  dashboard(userId) {
    return request(`/api/statistics/overview?userId=${userId}&period=day`);
  },
  exerciseList(userId, date) {
    return request(`/api/exercise/list?userId=${userId}&date=${date}`);
  },
  dietList(userId, date) {
    return request(`/api/diet/list?userId=${userId}&date=${date}`);
  },
  sleepList(userId, date) {
    return request(`/api/sleep/list?userId=${userId}&date=${date}`);
  },
  weightList(userId, date) {
    return request(`/api/weight/list?userId=${userId}&date=${date}`);
  },
  healthList(userId, date) {
    return request(`/api/health-record/list?userId=${userId}&date=${date}`);
  },
  goalList(userId) {
    return request(`/api/goal/list?userId=${userId}`);
  },
  goalAdd(data) {
    return request("/api/goal/add", {
      method: "POST",
      body: JSON.stringify(data)
    });
  },
  medicationList(userId) {
    return request(`/api/medication/list?userId=${userId}`);
  },
  reportSummary(userId) {
    return request(`/api/reports/summary?userId=${userId}`);
  },
  reportList(userId) {
    return request(`/api/reports/list?userId=${userId}`);
  },
  adminUsers(params = {}) {
    const query = buildQuery(params);
    return adminRequest(`/api/admin/users${query ? `?${query}` : ""}`);
  },
  adminSubscribeTasks(params = {}) {
    const query = buildQuery(params);
    return adminRequest(`/api/admin/subscribe-tasks${query ? `?${query}` : ""}`);
  },
  adminSubscribeTaskAdd(data) {
    return adminRequest("/api/admin/subscribe-tasks/add", {
      method: "POST",
      body: JSON.stringify(data)
    });
  },
  adminSubscribeTaskUpdate(data) {
    return adminRequest("/api/admin/subscribe-tasks/update", {
      method: "POST",
      body: JSON.stringify(data)
    });
  },
  adminSystemLogs(params = {}) {
    const query = buildQuery(params);
    return adminRequest(`/api/admin/logs/system${query ? `?${query}` : ""}`);
  },
  adminAiLogs(params = {}) {
    const query = buildQuery(params);
    return adminRequest(`/api/admin/logs/ai${query ? `?${query}` : ""}`);
  }
};
