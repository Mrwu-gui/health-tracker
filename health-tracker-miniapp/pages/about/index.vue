<template>
  <view class="page">
    <view class="container">
      <!-- Logo区域 -->
      <view class="logo-section">
        <view class="logo-wrap">
          <view class="logo">
            <text class="logo-text">智康</text>
          </view>
          <view class="logo-glow"></view>
        </view>
        <text class="app-name">智康 AI</text>
        <text class="app-version">版本 1.0.0</text>
      </view>

      <!-- 关于我们 -->
      <view class="section">
        <view class="section-header">
          <view class="section-icon">
            <image class="icon-img" src="/static/tabbar/hands_s.png" mode="aspectFit" />
          </view>
          <text class="section-title">关于我们</text>
        </view>
        <view class="section-content">
          <text class="text">我们致力于用简单好用的工具，帮助每个人更轻松地管理健康。</text>
          <text class="text">智康 AI 聚焦步数、睡眠、饮食与体重等日常记录，通过可视化数据与智能建议，让健康习惯更容易坚持。</text>
          <text class="text">我们相信，健康不是一场短跑，而是每天一点点的改变。</text>
        </view>
      </view>

      <!-- 数据使用说明 -->
      <view class="section">
        <view class="section-header">
          <view class="section-icon">
            <image class="icon-img" src="/static/tabbar/protect.png" mode="aspectFit" />
          </view>
          <text class="section-title">数据使用说明</text>
        </view>
        <view class="section-content">
          <text class="text">我们承诺：用户数据仅用于健康分析与功能改进，不用于广告推送或对外共享。</text>
        </view>
      </view>

      <!-- 联系我们 -->
      <view class="section">
        <view class="section-header">
          <view class="section-icon">
            <image class="icon-img" src="/static/tabbar/mail.png" mode="aspectFit" />
          </view>
          <text class="section-title">联系我们</text>
        </view>
        <view class="section-content">
          <text class="text">如有改进建议，欢迎发送邮件至：</text>
          <text class="email" @tap="copyEmail">928578564@qq.com</text>
        </view>
      </view>

      <!-- 备案信息 -->
      <view class="footer">
        <text class="icp">晋ICP备2026003386号-1</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  methods: {
    copyEmail() {
      uni.setClipboardData({
        data: '928578564@qq.com',
        success: () => {
          uni.showToast({
            title: '邮箱已复制',
            icon: 'success'
          });
        }
      });
    }
  }
};
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #FAF9F6;
  box-sizing: border-box;
}

.container {
  padding: 40px 20px 30px;
}

.logo-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 0 50px;
}

.logo-wrap {
  position: relative;
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.logo {
  width: 80px;
  height: 80px;
  border-radius: 20px;
  background: linear-gradient(135deg, #C45200 0%, #8B3400 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 12px 32px rgba(162, 63, 0, 0.35);
}

.logo-text {
  font-size: 28px;
  font-weight: 700;
  color: #fff;
}

.logo-glow {
  position: absolute;
  width: 120%;
  height: 120%;
  border-radius: 30px;
  background: radial-gradient(circle, rgba(196, 82, 0, 0.2) 0%, transparent 70%);
  z-index: -1;
}

.app-name {
  font-size: 24px;
  font-weight: 700;
  color: #2D3748;
  margin-bottom: 8px;
}

.app-version {
  font-size: 14px;
  color: #A0AEC0;
}

.section {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(162, 63, 0, 0.04);
}

.section-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #E8E2DB;
}

.section-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
}




.icon-img {
  width: 20px;
  height: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #2D3748;
}

.section-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.text {
  font-size: 14px;
  line-height: 24px;
  color: #4A5568;
}

.email {
  font-size: 14px;
  color: #A23F00;
  font-weight: 500;
  text-decoration: underline;
}

.footer {
  text-align: center;
  padding: 30px 0 20px;
}

.icp {
  font-size: 12px;
  color: #A0AEC0;
}
</style>
