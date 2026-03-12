const BASE_URL = "http://localhost:8080";

export function getToken() {
  return localStorage.getItem("token") || "";
}

export function setToken(token) {
  localStorage.setItem("token", token);
}

export function clearToken() {
  localStorage.removeItem("token");
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
  register(data) {
    return request("/api/user/register", {
      method: "POST",
      body: JSON.stringify(data)
    });
  },
  wechatQr() {
    return request("/api/auth/web/qr");
  },
  wechatCallback(code) {
    return request(`/api/auth/web/callback?code=${encodeURIComponent(code)}`);
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
  }
};
