import { request } from "./api";

export const TEMPLATE_IDS = {
  sleep: "99UN5VNoIf_2eEH1r1AuxY2fTeXQurSDucD-dZgy-ZE",
  exercise: "BW1-MP6TisjBwbMOEvA1j99pJ8XLPrMKKFyYPh_cchw",
  period: "tgrB0RlYGtbsyFnRv2yskI-daCieU9ATfmunmYM6JFw",
  medication: "0bxQEB2rY1VMzlLk7C1L-gIXexSufnXICswP3Oi0S8w"
};

export function requestSubscribeByKey(key) {
  const id = TEMPLATE_IDS[key];
  if (!id) return Promise.resolve(false);
  if (!uni.requestSubscribeMessage) return Promise.resolve(false);
  return new Promise((resolve) => {
    uni.requestSubscribeMessage({
      tmplIds: [id],
      success: (res) => {
        const status = res[id];
        if (status === "accept") {
          const userId = uni.getStorageSync("userId") || 1;
          request("/api/privacy/update", "POST", { userId, allowSubscribe: 1 }).catch(() => {});
        }
        resolve(status === "accept");
      },
      fail: () => resolve(false)
    });
  });
}
