<template>
  <view class="tab-bar">
    <view
      v-for="(item, index) in list"
      :key="index"
      class="tab-item"
      :class="{ 'tab-item-ai': item.isAi }"
      @tap="onTap(index)"
    >
      <view class="tab-icon-wrap" :class="{ active: selected === index }">
        <text class="tab-icon">{{ item.icon }}</text>
      </view>
      <text class="tab-text" :class="{ active: selected === index }">{{ item.text }}</text>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      selected: 0,
      list: [
        { pagePath: "/pages/index/index", text: "首页", icon: "🏠", isAi: false },
        { pagePath: "/pages/data/index", text: "数据", icon: "📈", isAi: false },
        { pagePath: "/pages/ai/index", text: "AI", icon: "✨", isAi: true },
        { pagePath: "/pages/profile/index", text: "我的", icon: "👤", isAi: false }
      ]
    };
  },
  mounted() {
    this.updateSelectedByRoute();
  },
  methods: {
    setData(data) {
      if (data && typeof data.selected === "number") {
        this.selected = data.selected;
      }
    },
    updateSelectedByRoute() {
      const pages = getCurrentPages();
      const current = pages[pages.length - 1];
      const route = current ? `/${current.route}` : "";
      const idx = this.list.findIndex((item) => item.pagePath === route);
      if (idx >= 0) {
        this.selected = idx;
      }
    },
    onTap(index) {
      if (index === this.selected) return;
      this.selected = index;
      uni.switchTab({
        url: this.list[index].pagePath
      });
    }
  }
};
</script>

<style scoped>
.tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 56px;
  padding-bottom: env(safe-area-inset-bottom);
  background: #ffffff;
  border-top: 1px solid #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: space-around;
  z-index: 999;
  box-shadow: 0 -2px 12px rgba(15, 23, 42, 0.04);
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 6px 0;
  gap: 4px;
}

.tab-icon-wrap {
  width: 28px;
  height: 28px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.tab-icon {
  font-size: 22px;
  line-height: 1;
}

.tab-icon-wrap.active .tab-icon {
  color: #2563eb;
}

.tab-item .tab-icon-wrap.active {
  background: #eff6ff;
}

.tab-item.tab-item-ai .tab-icon-wrap.active {
  background: linear-gradient(135deg, #e0e7ff 0%, #f5f3ff 100%);
}

.tab-item.tab-item-ai .tab-icon-wrap.active .tab-icon {
  color: #4f46e5;
}

.tab-text {
  font-size: 11px;
  color: #94a3b8;
}

.tab-text.active {
  color: #1e293b;
  font-weight: 600;
}

.tab-item.tab-item-ai .tab-text.active {
  color: #4f46e5;
}
</style>
