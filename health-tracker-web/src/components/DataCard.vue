<template>
  <div class="data-card" :class="{ 'with-padding': padding }">
    <div class="card-header" v-if="title || $slots.header">
      <slot name="header">
        <div class="card-title">{{ title }}</div>
        <div class="card-extra" v-if="$slots.extra">
          <slot name="extra"></slot>
        </div>
      </slot>
    </div>
    <div class="card-body">
      <slot></slot>
    </div>
    <div class="card-footer" v-if="$slots.footer">
      <slot name="footer"></slot>
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    default: ''
  },
  padding: {
    type: Boolean,
    default: true
  }
})
</script>

<style scoped>
.data-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.data-card.with-padding .card-body {
  padding: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.card-extra {
  font-size: 14px;
  color: #666;
}

.card-footer {
  padding: 16px 20px;
  border-top: 1px solid #f0f0f0;
  background: #fafafa;
}
</style>