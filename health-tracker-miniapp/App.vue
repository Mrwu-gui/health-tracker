<script>
export default {
  onLaunch() {
    if (typeof wx !== "undefined" && wx.cloud) {
      wx.cloud.init({
        env: "health-4gt7erwb330dae07"
      });
    }
  },
  onShow() {},
  onHide() {}
};
</script>

<style>
/* 健康类小程序 - 暖色主题（可整体替换为冷色时搜索 #faf8f5 / #e8e2db 等还原） */
page {
  --page-bg: #faf8f5;
  --page-bg-soft: #f5f1eb;
  --card-bg-light: #fefcf9;
  --border-warm: #e8e2db;
  --border-warm-light: #f2ede8;
}
</style>
