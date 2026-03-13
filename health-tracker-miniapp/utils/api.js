const BASE_URL = process.env.UNI_APP_BASE_URL || "/api";

export function request(path, method = "GET", data = {}) {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync("token");
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
          if (body && typeof body.code === "number") {
            if (body.code !== 0) {
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
      fail: () => {
        reject(new Error("网络异常"));
      }
    });
  });
}
