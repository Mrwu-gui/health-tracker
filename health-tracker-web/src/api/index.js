// API 服务层
const API_BASE = "/api";

// 通用请求方法
async function request(url, options = {}) {
  const token = localStorage.getItem("token") || localStorage.getItem("adminToken");
  const response = await fetch(url, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  });
  
  if (!response.ok) {
    throw new Error(`请求失败: ${response.status}`);
  }
  
  const data = await response.json();
  if (data.code !== 0 && data.code !== 200) {
    throw new Error(data.message || "请求失败");
  }
  
  return data.data || data.result || data;
}

// ========== 认证相关 API ==========

// 获取微信扫码二维码
export async function getWechatQR() {
  const ts = Date.now();
  return request(`${API_BASE}/auth/web/qr?ts=${ts}`, {
    cache: "no-store",
    headers: {
      "Cache-Control": "no-store",
      Pragma: "no-cache"
    }
  });
}

// 微信扫码回调
export async function wechatCallback(code) {
  return request(`${API_BASE}/auth/web/callback?code=${code}`);
}

// 后台账号密码登录
export async function adminLogin(username, password) {
  return request(`${API_BASE}/auth/admin/login`, {
    method: "POST",
    body: JSON.stringify({ username, password }),
  });
}

// 登出
export async function logout() {
  localStorage.removeItem("token");
  localStorage.removeItem("adminToken");
  localStorage.removeItem("userId");
  localStorage.removeItem("userInfo");
}

// ========== 用户端 API ==========

// 获取今日健康评分
export async function getTodayScore(userId) {
  return request(`${API_BASE}/score/today?userId=${userId}`);
}

// 获取统计数据概览
export async function getStatisticsOverview(userId, period = "day") {
  return request(`${API_BASE}/statistics/overview?userId=${userId}&period=${period}`);
}

// AI 聊天（仅分析，不存储）
export async function getAIAnalysis(messages) {
  return request(`${API_BASE}/ai/chat`, {
    method: "POST",
    body: JSON.stringify({ messages, store: false }),
  });
}

// 获取趋势数据
export async function getStatisticsTrend(userId, period = "week") {
  return request(`${API_BASE}/statistics/trend?userId=${userId}&period=${period}`);
}

// 获取提醒列表
export async function getReminderList(userId) {
  return request(`${API_BASE}/reminder/list?userId=${userId}`);
}

// 获取用药列表
export async function getMedicationList(userId) {
  return request(`${API_BASE}/medication/list?userId=${userId}`);
}

// 获取用药记录列表
export async function getMedicationRecordList(userId) {
  return request(`${API_BASE}/medication/record/list?userId=${userId}`);
}

// 获取家人列表
export async function getFamilyList(userId) {
  return request(`${API_BASE}/family/list?userId=${userId}`);
}

// 获取目标列表
export async function getGoalList(userId, period = "day") {
  return request(`${API_BASE}/goal/list?userId=${userId}&period=${period}`);
}

// ========== 管理端 API ==========

// 获取用户列表
export async function getAdminUsers(keyword = "", limit = 200) {
  return request(`${API_BASE}/admin/users?keyword=${encodeURIComponent(keyword)}&limit=${limit}`);
}

// 获取系统日志
export async function getSystemLogs(level = "", limit = 200) {
  const params = new URLSearchParams({ limit: limit.toString() });
  if (level) params.append("level", level);
  return request(`${API_BASE}/admin/logs/system?${params}`);
}

// 获取AI调用日志
export async function getAILogs(limit = 200) {
  return request(`${API_BASE}/admin/logs/ai?limit=${limit}`);
}

// 获取订阅任务队列
export async function getSubscribeTasks(status = "", userId = "", limit = 200) {
  const params = new URLSearchParams({ limit: limit.toString() });
  if (status) params.append("status", status);
  if (userId) params.append("userId", userId);
  return request(`${API_BASE}/admin/subscribe-tasks?${params}`);
}

// ========== 本地存储工具 ==========

export function getToken() {
  return localStorage.getItem("token");
}

export function getAdminToken() {
  return localStorage.getItem("adminToken");
}

export function setToken(token) {
  localStorage.setItem("token", token);
}

export function setAdminToken(token) {
  localStorage.setItem("adminToken", token);
}

export function setUserInfo(info) {
  localStorage.setItem("userInfo", JSON.stringify(info));
}

export function getUserInfo() {
  const info = localStorage.getItem("userInfo");
  return info ? JSON.parse(info) : null;
}

export function getUserId() {
  return localStorage.getItem("userId") || "1";
}

export function setUserId(userId) {
  localStorage.setItem("userId", userId);
}

export function clearTokens() {
  localStorage.removeItem("token");
  localStorage.removeItem("adminToken");
  localStorage.removeItem("userId");
  localStorage.removeItem("userInfo");
}

export function isLoggedIn() {
  return !!localStorage.getItem("token");
}

export function isAdminLoggedIn() {
  return !!localStorage.getItem("adminToken");
}
