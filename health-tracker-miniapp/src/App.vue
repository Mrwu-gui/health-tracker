<template></template>

<script>
export default {
  onShow() {
    const cached = uni.getStorageSync("token");
    if (!cached) {
      const pages = getCurrentPages();
      const current = pages[pages.length - 1];
      const route = current?.route || "";
      if (route !== "pages/login/index") {
        uni.navigateTo({ url: "/pages/login/index" });
      }
    }
  }
};
</script>

<style>
page {
  background: linear-gradient(180deg, #e0f2fe 0%, #f8fafc 35%, #ecfeff 100%);
  color: #0f172a;
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
}
</style>
