const PRIMARY_BASE_URL = process.env.UNI_APP_BASE_URL || "http://127.0.0.1:8080";
const FALLBACK_BASE_URL = "";
export const API_BASE_URL = PRIMARY_BASE_URL;
export function request(path, method = "GET", data = {}) {
  const token = uni.getStorageSync("token");
  const doRequest = (baseUrl) =>
    new Promise((resolve, reject) => {
      console.log("[api.request]", method, `${baseUrl}${path}`, data);
      uni.request({
        url: `${baseUrl}${path}`,
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

  return doRequest(PRIMARY_BASE_URL).catch((err) => {
    if (FALLBACK_BASE_URL && FALLBACK_BASE_URL !== PRIMARY_BASE_URL) {
      return doRequest(FALLBACK_BASE_URL).catch(() => {
        throw err;
      });
    }
    throw err;
  });
}
