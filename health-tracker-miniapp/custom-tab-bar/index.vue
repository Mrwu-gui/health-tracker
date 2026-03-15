<template>
  <view class="tab-bar">
    <view
      v-for="(item, index) in list"
      :key="index"
      class="tab-item"
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
        { pagePath: "/pages/index/index", text: "首页", icon: "🏠" },
        { pagePath: "/pages/data/index", text: "数据", icon: "📊" },
        { pagePath: "/pages/goal/index", text: "目标", icon: "🎯" },
        { pagePath: "/pages/profile/index", text: "我的", icon: "👤" }
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
  height: 50px;
  padding-bottom: env(safe-area-inset-bottom);
  background: #fff;
  border-top: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: space-around;
  z-index: 999;
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4px 0;
}

.tab-icon-wrap {
  width: 26px;
  height: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 2px;
}

.tab-icon {
  font-size: 20px;
  color: #94a3b8;
  line-height: 1;
}

.tab-icon-wrap.active .tab-icon {
  color: #2563eb;
}

.tab-text {
  font-size: 10px;
  color: #94a3b8;
}

.tab-text.active {
  color: #2563eb;
  font-weight: 500;
}
</style>
