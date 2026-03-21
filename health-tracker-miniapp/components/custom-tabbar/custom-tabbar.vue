<template>
  <view class="custom-tabbar">
    <view 
      v-for="(item, index) in tabList" 
      :key="index" 
      class="tabbar-item"
      @tap="switchTab(index)"
    >
      <view class="tabbar-icon-wrapper" :class="{ active: currentIndex === index }">
        <image 
          class="tabbar-icon" 
          :src="currentIndex === index ? item.selectedIconPath : item.iconPath" 
          mode="aspectFit"
        />
      </view>
      <text class="tabbar-text" :class="{ active: currentIndex === index }">{{ item.text }}</text>
    </view>
  </view>
</template>

<script>
export default {
  name: 'CustomTabbar',
  props: {
    current: {
      type: Number,
      default: 0
    }
  },
  data() {
    return {
      tabList: [
        {
          pagePath: '/pages/index/index',
          text: '首页',
          iconPath: '/static/tabbar/home.png',
          selectedIconPath: '/static/tabbar/home-active.png'
        },
        {
          pagePath: '/pages/data/index',
          text: '分析',
          iconPath: '/static/tabbar/data.png',
          selectedIconPath: '/static/tabbar/data-active.png'
        },
        {
          pagePath: '/pages/ai/index',
          text: 'AI',
          iconPath: '/static/tabbar/robot.png',
          selectedIconPath: '/static/tabbar/robot-active.png'
        },
        {
          pagePath: '/pages/profile/index',
          text: '我的',
          iconPath: '/static/tabbar/profile.png',
          selectedIconPath: '/static/tabbar/profile-active.png'
        }
      ]
    };
  },
  computed: {
    currentIndex() {
      return this.current;
    }
  },
  methods: {
    switchTab(index) {
      uni.switchTab({
        url: this.tabList[index].pagePath
      });
    }
  }
};
</script>

<style scoped>
.custom-tabbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 100rpx;
  background: #ffffff;
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding-bottom: env(safe-area-inset-bottom);
  box-shadow: 0 -2rpx 20rpx rgba(0, 0, 0, 0.04);
  z-index: 9999;
}

.tabbar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6rpx;
  position: relative;
}

.tabbar-icon-wrapper {
  width: 88rpx;
  height: 88rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  background: transparent;
}

.tabbar-icon-wrapper.active {
  background: linear-gradient(135deg, #A23F00 0%, #FA7025 100%);
  box-shadow: 0 8rpx 24rpx rgba(162, 63, 0, 0.3);
  transform: translateY(-16rpx);
}

.tabbar-icon {
  width: 48rpx;
  height: 48rpx;
  transition: all 0.3s ease;
}

.tabbar-text {
  font-size: 20rpx;
  color: #94a3b8;
  font-weight: 500;
  transition: all 0.3s ease;
  position: absolute;
  bottom: -8rpx;
}

.tabbar-text.active {
  color: #A23F00;
  font-weight: 700;
}
</style>
