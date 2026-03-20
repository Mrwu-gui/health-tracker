<template>
  <view class="page">
    <view class="header">
      <text class="title">授权与隐私</text>
    </view>

    <view class="card">
      <view class="row">
        <view>
          <text class="name">微信订阅消息</text>
          <text class="desc">运动、睡眠、用药提醒推送</text>
        </view>
        <view class="row-actions">
          <button class="help" @tap="showWxInfo = true">?</button>
          <button class="btn-dark" @tap="openWxAuth">去微信授权</button>
        </view>
      </view>

      <view class="row">
        <view>
          <text class="name">健康数据云端同步</text>
          <text class="desc">支持 PC 端查看与管理</text>
        </view>
        <switch :checked="settings.allowCloudSync" @change="toggle('allowCloudSync', $event)" />
      </view>

      <view class="row">
        <view>
          <text class="name">家庭成员查看权限</text>
          <text class="desc">允许家人查看关键指标与周报</text>
        </view>
        <switch :checked="settings.allowFamilyShare" @change="toggle('allowFamilyShare', $event)" />
      </view>
    </view>

    <view class="card dark">
      <text class="name light">隐私说明</text>
      <text class="desc light">
        你可以随时撤回授权。撤回后将停止对应数据处理和通知推送，历史数据可选择导出或删除。
      </text>
    </view>

    <button class="primary" @tap="save">保存设置</button>

    <view v-if="showWxInfo" class="modal-mask" @tap="showWxInfo = false">
      <view class="modal" @tap.stop>
        <text class="modal-title">订阅消息说明</text>
        <text class="modal-desc">
          我们会在你允许的范围内发送运动、睡眠与用药提醒。你可随时在微信设置中取消授权。
        </text>
        <button class="primary" @tap="showWxInfo = false">我知道了</button>
      </view>
    </view>
  </view>
</template>

<script>
import { request } from "../../utils/api";

export default {
  data() {
    return {
      settings: {
        allowCloudSync: true,
        allowFamilyShare: true
      },
      showWxInfo: false
    };
  },
  onLoad() {
    this.fetchSettings();
  },
  methods: {
    fetchSettings() {
      const userId = uni.getStorageSync("userId") || 1;
      request("/api/privacy/get", "GET", { userId })
        .then((data) => {
          this.settings.allowCloudSync = data.allowCloudSync === 1;
          this.settings.allowFamilyShare = data.allowFamilyShare === 1;
        })
        .catch(() => {});
    },
    toggle(key, event) {
      this.settings[key] = event.detail.value;
    },
    save() {
      const userId = uni.getStorageSync("userId") || 1;
      request("/api/privacy/update", "POST", {
        userId,
        allowCloudSync: this.settings.allowCloudSync ? 1 : 0,
        allowFamilyShare: this.settings.allowFamilyShare ? 1 : 0
      })
        .then(() => {
          uni.showToast({ title: "已保存", icon: "success" });
        })
        .catch((err) => {
          uni.showToast({ title: err.message || "保存失败", icon: "none" });
        });
    },
    openWxAuth() {
      uni.showToast({ title: "请在微信设置中授权", icon: "none" });
    }
  }
};
</script>

<style>
.page {
  padding: 18px;
  min-height: 100vh;
  background: #FAF8F5;
  color: #0f172a;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 18px;
  font-weight: 600;
}

.card {
  background: #ffffff;
  border-radius: 16px;
  padding: 14px;
  border: 1px solid #e8e2db;
  display: grid;
  gap: 12px;
}

.card.dark {
  background: #0f172a;
  border-color: #0f172a;
}

.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.row-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.name {
  font-size: 12px;
  font-weight: 600;
  display: block;
}

.desc {
  font-size: 10px;
  color: #94a3b8;
  display: block;
  margin-top: 4px;
}

.light {
  color: #e2e8f0;
}

.btn-dark {
  background: #A23F00;
  color: #ffffff;
  border-radius: 12px;
  font-size: 11px;
  padding: 6px 12px;
}

.primary {
  background: #A23F00;
  color: #ffffff;
  border-radius: 16px;
  font-size: 12px;
  padding: 12px 0;
}

.help {
  width: 20px;
  height: 20px;
  border-radius: 10px;
  background: #f5f1eb;
  color: #94a3b8;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

.modal {
  width: 80%;
  max-width: 320px;
  background: #ffffff;
  border-radius: 16px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.modal-title {
  font-size: 14px;
  font-weight: 600;
}

.modal-desc {
  font-size: 11px;
  color: #64748b;
  line-height: 1.5;
}
</style>
