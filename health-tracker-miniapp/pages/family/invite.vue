<template><view class="page"><text>邀请链接已复制</text></view></template>
<script>
export default {
  onLoad() {
    uni.showToast({ title: "邀请链接已复制", icon: "success" });
    setTimeout(() => uni.navigateBack(), 800);
  }
};
</script>
<style scoped>
.page { padding: 40px; text-align: center; font-size: 14px; color: #64748b; }
</style>
