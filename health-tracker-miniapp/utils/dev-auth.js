import { request, API_BASE_URL } from "./api";

export function ensureDevLogin() {
  try {
    const isWeixin = typeof wx !== "undefined" && typeof wx.getSystemInfoSync === "function";
    if (isWeixin && !uni.getStorageSync("__enableDevLogin")) return;
    const isDev =
      (typeof process !== "undefined" && process?.env?.NODE_ENV !== "production") ||
      (API_BASE_URL && (API_BASE_URL.includes("127.0.0.1") || API_BASE_URL.includes("localhost")));
    if (!isDev) return;
    if (!API_BASE_URL) return;
    if (uni.getStorageSync("token")) return;
    if (uni.getStorageSync("userId")) return;
    if (uni.getStorageSync("loginSource") && uni.getStorageSync("loginSource") !== "dev") return;
    const lastTriedAt = Number(uni.getStorageSync("__devLoginTriedAt") || 0);
    if (Date.now() - lastTriedAt < 5000) return;
    uni.setStorageSync("__devLoginTriedAt", Date.now());
    setTimeout(() => {
      request("/api/auth/sms/login", "POST", {
        phone: "19243595133",
        code: "1234"
      })
        .then((data) => {
          if (data?.token) {
            uni.setStorageSync("token", data.token);
            if (data.userId) {
              uni.setStorageSync("userId", data.userId);
            }
            uni.setStorageSync("loginSource", "dev");
            if (!uni.getStorageSync("__devLoginNotified")) {
              uni.setStorageSync("__devLoginNotified", 1);
              uni.showToast({ title: "已自动登录", icon: "success" });
            }
          }
        })
        .catch(() => {
          if (!uni.getStorageSync("__devLoginNotified")) {
            uni.setStorageSync("__devLoginNotified", 1);
            uni.showToast({ title: "自动登录失败，请检查后端或合法域名", icon: "none" });
          }
        });
    }, 300);
  } catch (err) {}
}
