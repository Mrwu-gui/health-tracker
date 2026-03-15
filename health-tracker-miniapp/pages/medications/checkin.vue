<template><view class="page"><text>打卡成功</text></view></template>
<script>
export default {
  onLoad() {
    uni.showToast({ title: "已打卡", icon: "success" });
    setTimeout(() => uni.navigateBack(), 800);
  }
};
</script>
<style scoped>
.page { padding: 40px; text-align: center; font-size: 14px; color: #64748b; }
</style>
