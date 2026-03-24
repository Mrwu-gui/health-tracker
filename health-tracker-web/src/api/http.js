// 向后兼容的 HTTP API 模块
// 重新导出现有 API 并添加缺失的函数

import * as apiModule from './index.js';

// 重新导出所有现有函数
export * from './index.js';

// 向后兼容的别名
export const setAdminKey = apiModule.setAdminToken;
export const clearAdminKey = apiModule.clearTokens;

// API 对象（供 AdminTasksView 使用）
export const api = {
  // 订阅任务相关
  adminSubscribeTasks: apiModule.getSubscribeTasks,
  
  // 模拟更新和添加函数（暂时返回成功）
  adminSubscribeTaskUpdate: async (data) => {
    console.log('模拟更新任务:', data);
    return { success: true };
  },
  
  adminSubscribeTaskAdd: async (data) => {
    console.log('模拟新增任务:', data);
    return { success: true, id: Date.now() };
  },
  
  // 用户资料相关（供 SettingsView 使用）
  profile: async (userId) => {
    console.log('模拟获取用户资料:', userId);
    return { name: '模拟用户', age: 30, height: 175, weight: 70.5 };
  }
};