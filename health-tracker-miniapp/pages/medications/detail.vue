<template>
  <view class="page">
    <view class="card" v-if="item.drugName">
      <view class="row">
        <text class="name">{{ item.drugName }} {{ item.dosage }}</text>
      </view>
      <view class="block">
        <text class="label">服用方案</text>
        <text class="value">{{ item.frequency }} · {{ item.remindTime || "未设置" }}</text>
      </view>
      <view class="block">
        <text class="label">备注</text>
        <text class="value">饭后 30 分钟内服用，避免漏服和重复用药。</text>
      </view>
      <view class="block">
        <text class="label">近 7 天完成情况</text>
        <view class="badge-row">
          <text class="badge ok">日一✓</text>
          <text class="badge ok">日二✓</text>
          <text class="badge warn">日三漏</text>
          <text class="badge ok">日四✓</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return { item: {} };
  },
  onLoad(opts) {
    const id = opts && opts.id;
    this.item = { id, drugName: "药物", dosage: "", frequency: "每日", remindTime: "未设置" };
  }
};
</script>

<style scoped>
.page { padding: 20px; min-height: 100vh; background: #f4f5f7; }
.card { background: #fff; border-radius: 16px; padding: 16px; }
.row { margin-bottom: 12px; }
.name { font-size: 16px; font-weight: 600; }
.block { margin-top: 14px; padding-top: 14px; border-top: 1px solid #f1f5f9; }
.label { font-size: 12px; color: #64748b; display: block; margin-bottom: 6px; }
.value { font-size: 14px; color: #0f172a; }
.badge-row { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 8px; }
.badge { font-size: 12px; padding: 4px 8px; border-radius: 8px; }
.badge.ok { background: #d1fae5; color: #059669; }
.badge.warn { background: #fef3c7; color: #d97706; }
</style>
