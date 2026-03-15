<template><view class="page"><text>周报已生成</text></view></template>
<script>
export default {
  onLoad() {
    uni.showToast({ title: "已生成周报", icon: "success" });
    setTimeout(() => uni.navigateBack(), 800);
  }
};
</script>
<style scoped>
.page { padding: 40px; text-align: center; font-size: 14px; color: #64748b; }
</style>
