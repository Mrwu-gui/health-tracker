const BASE_URL = process.env.UNI_APP_BASE_URL || "https://www.datewell.xyz";
export const API_BASE_URL = BASE_URL;
export function request(path, method = "GET", data = {}) {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync("token");
    console.log("[api.request]", method, `${BASE_URL}${path}`, data);
    uni.request({
      url: `${BASE_URL}${path}`,
      method,
      data,
      header: {
        "Content-Type": "application/json",
        Authorization: token ? `Bearer ${token}` : ""
      },
      success: (res) => {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          const body = res.data;
          if (body && (typeof body.code === "number" || typeof body.code === "string")) {
            const code = Number(body.code);
            if (code !== 0) {
              reject(new Error(body.message || "请求失败"));
              return;
            }
            resolve(body.data);
            return;
          }
          resolve(body);
          return;
        }
        reject(new Error(res.data?.message || "请求失败"));
      },
      fail: (err) => {
        const msg = err?.errMsg || "网络异常";
        reject(new Error(msg));
      }
    });
  });
}
