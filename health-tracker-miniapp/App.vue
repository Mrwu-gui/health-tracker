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

  /* 统一圆角变量 */
  --radius-card: 52rpx;
  --radius-card-sm: 30rpx;
  --radius-pill: 999rpx;

  /* 统一阴影 */
  --shadow-card: 0 8rpx 24rpx rgba(0, 0, 0, 0.06);
}

/* 统一卡片类 */
.card {
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-card);
  overflow: hidden;
  background: #fff;
}

.card-sm {
  border-radius: var(--radius-card-sm);
}

.pill {
  border-radius: var(--radius-pill);
}

.progress-track,
.progress-fill {
  border-radius: var(--radius-pill);
}
</style>
