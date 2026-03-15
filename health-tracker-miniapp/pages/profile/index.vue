<template>
  <view class="page">
    <view class="header">
      <text class="title">我的</text>
      <button class="edit-btn" @tap="openBodyModal">编辑</button>
    </view>

    <view class="profile-card">
      <view class="avatar-wrap">
        <image v-if="profile.avatar" class="avatar-img" :src="profile.avatar" mode="aspectFill" />
        <text v-else class="avatar-letter">{{ profile.name ? profile.name.slice(0, 1) : "?" }}</text>
      </view>
      <view class="profile-info">
        <text class="name">{{ profile.name || "未登录" }}</text>
        <text class="desc">{{ profile.summary || "请先登录" }}</text>
      </view>
    </view>

    <view class="menu-card">
      <navigator class="menu-item" url="/pages/goal/index" hover-class="none">
        <view class="menu-icon icon-g">
          <image class="icon-img" src="/static/tabbar/goal.png" mode="widthFix"></image>
        </view>
        <view class="menu-content">
          <text class="menu-title">目标管理</text>
          <text class="menu-desc">设置目标 · 追踪进度</text>
        </view>
        <text class="menu-arrow">›</text>
      </navigator>
      <navigator class="menu-item" url="/pages/medications/index" hover-class="none">
        <view class="menu-icon icon-y">
          <image class="icon-img" src="/static/tabbar/pills.png" mode="widthFix"></image>
        </view>
        <view class="menu-content">
          <text class="menu-title">药物管理</text>
          <text class="menu-desc">添加药物 · 查看提醒 · 服药记录</text>
        </view>
        <text class="menu-arrow">›</text>
      </navigator>
      <navigator class="menu-item" url="/pages/reminders/index" hover-class="none">
        <view class="menu-icon icon-t">
          <image class="icon-img" src="/static/tabbar/remind.png" mode="widthFix"></image>
        </view>
        <view class="menu-content">
          <text class="menu-title">提醒设置</text>
          <text class="menu-desc">运动 · 饮食 · 睡眠</text>
        </view>
        <text class="menu-arrow">›</text>
      </navigator>
      <navigator class="menu-item" url="/pages/family/index" hover-class="none">
        <view class="menu-icon icon-f">
          <image class="icon-img" src="/static/tabbar/family.png" mode="widthFix"></image>
        </view>
        <view class="menu-content">
          <text class="menu-title">家庭成员</text>
          <text class="menu-desc">共享健康数据，关心家人</text>
        </view>
        <text class="menu-arrow">›</text>
      </navigator>
      <view class="menu-item" @tap="goReports">
        <view class="menu-icon icon-r">
          <image class="icon-img" src="/static/tabbar/report.png" mode="widthFix"></image>
        </view>
        <view class="menu-content">
          <text class="menu-title">报告中心</text>
          <text class="menu-desc">周报 · 月报 · 导出 PDF</text>
        </view>
        <text class="menu-arrow">›</text>
      </view>
      <navigator class="menu-item" url="/pages/privacy/index" hover-class="none">
        <view class="menu-icon icon-s">
          <image class="icon-img" src="/static/tabbar/shield.png" mode="widthFix"></image>
        </view>
        <view class="menu-content">
          <text class="menu-title">授权与隐私</text>
          <text class="menu-desc">订阅消息 · 数据同步 · 权限控制</text>
        </view>
        <text class="menu-arrow">›</text>
      </navigator>
    </view>

    <view class="ai-card">
      <view class="ai-head">
        <text class="ai-icon">✨</text>
        <text class="ai-title">AI 智能健康分析</text>
      </view>
      <text class="ai-desc">基于运动、睡眠与用药数据，为你生成个性化建议。</text>
      <button class="ai-btn" @tap="goAi">进入对话</button>
    </view>

    <view class="action-row">
      <button class="link" plain>切换账号</button>
      <button class="link danger" plain>退出登录</button>
    </view>

    <view v-if="showBodyModal" class="modal-mask" @tap="closeBodyModal">
      <view class="modal" @tap.stop>
        <text class="modal-title">完善个人信息</text>
        <!-- #ifdef MP-WEIXIN -->
        <button class="ghost" open-type="chooseAvatar" @chooseavatar="onChooseAvatar" :disabled="bodySaving">
          选择头像
        </button>
        <!-- #endif -->
        <input class="input" type="nickname" v-model="bodyForm.nickname" placeholder="请输入昵称" />
        <view class="radio-group">
          <view class="radio-label">性别</view>
          <view class="radio-options">
            <view
              class="radio-item"
              :class="{ active: bodyForm.sex === '男' }"
              @tap="bodyForm.sex = '男'"
            >男</view>
            <view
              class="radio-item"
              :class="{ active: bodyForm.sex === '女' }"
              @tap="bodyForm.sex = '女'"
            >女</view>
          </view>
        </view>
        <input class="input" v-model="bodyForm.age" placeholder="年龄" type="number" />
        <input
          v-if="!profile.height"
          class="input"
          v-model="bodyForm.height"
          placeholder="身高（厘米）"
          type="number"
        />
        <input
          v-if="!profile.weight"
          class="input"
          v-model="bodyForm.weight"
          placeholder="体重（千克）"
          type="number"
        />
        <input
          v-if="!profile.systolic"
          class="input"
          v-model="bodyForm.systolic"
          placeholder="收缩压（高压）"
          type="number"
        />
        <input
          v-if="!profile.diastolic"
          class="input"
          v-model="bodyForm.diastolic"
          placeholder="舒张压（低压）"
          type="number"
        />
        <input
          v-if="!profile.heartRate"
          class="input"
          v-model="bodyForm.heartRate"
          placeholder="心率"
          type="number"
        />
        <button class="primary" @tap="saveBodyProfile" :disabled="bodySaving">
          {{ bodySaving ? "保存中..." : "保存" }}
        </button>
      </view>
    </view>

    <text v-if="message" class="status">{{ message }}</text>
    <text v-if="loading" class="status">加载中...</text>
    <text v-if="error" class="status error">{{ error }}</text>
  </view>
</template>

<script>
import { request } from "../../utils/api";

export default {
  data() {
    return {
      profile: {
        name: "未登录",
        avatar: "",
        summary: "请先登录",
        sex: "",
        age: "",
        height: "",
        weight: "",
        systolic: "",
        diastolic: "",
        heartRate: ""
      },
      loading: false,
      error: "",
      message: "",
      showBodyModal: false,
      bodySaving: false,
      bodyForm: {
        nickname: "",
        sex: "",
        age: "",
        height: "",
        weight: "",
        systolic: "",
        diastolic: "",
        heartRate: ""
      }
    };
  },
  onLoad() {
  },
  onShow() {
    this.fetchProfile();
    this.loadLocalProfile();
    const needBodyProfile = uni.getStorageSync("needBodyProfile");
    const hasProfile =
      this.profile.name &&
      this.profile.name !== "未登录" &&
      (this.profile.age || this.profile.sex || this.profile.height || this.profile.weight);
    if (needBodyProfile && !hasProfile) {
      this.openBodyModal();
    }
  },
  methods: {
    fetchProfile() {
      this.loading = true;
      this.error = "";
      this.message = "";
      const userId = uni.getStorageSync("userId") || 1;
      request("/api/user/profile", "GET", { userId })
        .then((data) => {
          if (data) {
            const name = data.wxNickname || data.username || "用户";
            this.profile.name = name;
            const height = data.height ? `${data.height}cm` : "--";
            const weight = data.weight ? `${data.weight}kg` : "--";
            const bmi = data.bmi ? `BMI ${data.bmi}` : "";
            const bp = data.systolic && data.diastolic ? `${data.systolic}/${data.diastolic}` : "--";
            const hr = data.heartRate ? `${data.heartRate}` : "--";
            this.profile.summary = `${data.sex || "未知"} · ${data.age || "--"}岁 · ${height} · ${weight} ${bmi} · 血压 ${bp} · 心率 ${hr}`.trim();
            if (data.wxAvatar) {
              this.profile.avatar = data.wxAvatar;
            }
            this.profile.sex = data.sex || "";
            this.profile.age = data.age || "";
            this.profile.height = data.height || "";
            this.profile.weight = data.weight || "";
            this.profile.systolic = data.systolic || "";
            this.profile.diastolic = data.diastolic || "";
            this.profile.heartRate = data.heartRate || "";
            if (this.profile.name && this.profile.name !== "未登录") {
              uni.setStorageSync("userName", this.profile.name);
              if (this.profile.sex || this.profile.age || this.profile.height || this.profile.weight) {
                uni.setStorageSync("needBodyProfile", false);
              }
            }
          }
        })
        .catch(() => {
          this.error = "获取资料失败";
        })
        .finally(() => {
          this.loading = false;
        });
    },
    loadLocalProfile() {
      const wxProfile = uni.getStorageSync("wxProfile") || {};
      if (wxProfile.nickName) {
        this.profile.name = wxProfile.nickName;
      }
      if (wxProfile.avatarUrl) {
        this.profile.avatar = wxProfile.avatarUrl;
      }
    },
    goReports() {
      uni.showToast({ title: "报告中心开发中", icon: "none" });
    },
    goAi() {
      uni.switchTab({ url: "/pages/ai/index" });
    },
    openBodyModal() {
      this.showBodyModal = true;
      const body = uni.getStorageSync("bodyProfile") || {};
      this.bodyForm.nickname = body.nickname || this.profile.name || "";
      this.bodyForm.sex = body.sex || this.profile.sex || "";
      this.bodyForm.age = body.age || this.profile.age || "";
      this.bodyForm.height = body.height || this.profile.height || "";
      this.bodyForm.weight = body.weight || this.profile.weight || "";
      this.bodyForm.systolic = body.systolic || this.profile.systolic || "";
      this.bodyForm.diastolic = body.diastolic || this.profile.diastolic || "";
      this.bodyForm.heartRate = body.heartRate || this.profile.heartRate || "";
    },
    closeBodyModal() {
      this.showBodyModal = false;
    },
    saveBodyProfile() {
      const toInt = (val) => {
        const num = parseInt(val, 10);
        return Number.isNaN(num) ? null : num;
      };
      const toFloat = (val) => {
        const num = parseFloat(val);
        return Number.isNaN(num) ? null : num;
      };
      const height = toInt(this.bodyForm.height);
      const weight = toFloat(this.bodyForm.weight);
      const age = toInt(this.bodyForm.age);
      const systolic = toInt(this.bodyForm.systolic);
      const diastolic = toInt(this.bodyForm.diastolic);
      const heartRate = toInt(this.bodyForm.heartRate);
      if (!this.bodyForm.nickname) {
        this.message = "请输入用户名";
        return;
      }
      if (!this.bodyForm.sex) {
        this.message = "请选择性别";
        return;
      }
      if (age !== null && (age < 1 || age > 120)) {
        this.message = "年龄需在 1-120 之间";
        return;
      }
      if (height !== null && (height < 50 || height > 250)) {
        this.message = "身高需在 50-250 之间";
        return;
      }
      if (weight !== null && (weight < 20 || weight > 300)) {
        this.message = "体重需在 20-300 之间";
        return;
      }
      if (systolic !== null && (systolic < 60 || systolic > 250)) {
        this.message = "收缩压需在 60-250 之间";
        return;
      }
      if (diastolic !== null && (diastolic < 40 || diastolic > 150)) {
        this.message = "舒张压需在 40-150 之间";
        return;
      }
      if (heartRate !== null && (heartRate < 40 || heartRate > 200)) {
        this.message = "心率需在 40-200 之间";
        return;
      }
      this.bodySaving = true;
      uni.setStorageSync("bodyProfile", {
        nickname: this.bodyForm.nickname,
        sex: this.bodyForm.sex,
        age,
        height,
        weight,
        systolic,
        diastolic,
        heartRate
      });
      uni.setStorageSync("needBodyProfile", false);
      uni.setStorageSync("userName", this.bodyForm.nickname);
      const userId = uni.getStorageSync("userId") || 1;
      const bmi = height && weight ? (weight / Math.pow(height / 100, 2)).toFixed(1) : null;
      const updates = [
        request("/api/user/profile/update", "POST", {
          userId,
          wxNickname: this.bodyForm.nickname,
          wxAvatar: this.profile.avatar || "",
          sex: this.bodyForm.sex,
          age,
          height,
          weight,
          systolic,
          diastolic,
          heartRate
        })
      ];
      if (height && weight && bmi) {
        updates.push(
          request("/api/weight/add", "POST", {
            userId,
            weight,
            bmi,
            date: new Date().toISOString().slice(0, 10)
          })
        );
      }
      if (systolic && diastolic) {
        updates.push(
          request("/api/health-record/add", "POST", {
            userId,
            systolic,
            diastolic,
            heartRate: heartRate || null,
            date: new Date().toISOString().slice(0, 10)
          })
        );
      }
      Promise.allSettled(updates).finally(() => {
        this.bodySaving = false;
        this.showBodyModal = false;
        uni.showToast({ title: "已保存", icon: "success" });
        this.fetchProfile();
      });
    },
    onChooseAvatar(e) {
      const avatarUrl = e?.detail?.avatarUrl;
      if (!avatarUrl) {
        uni.showToast({ title: "未获取头像", icon: "none" });
        return;
      }
      this.profile.avatar = avatarUrl;
      request("/api/user/profile/update", "POST", {
        userId: uni.getStorageSync("userId") || 1,
        wxNickname: this.bodyForm.nickname || this.profile.name || "",
        wxAvatar: avatarUrl
      }).catch(() => {});
    }
  }
};
</script>

<style>
.page {
  min-height: 100vh;
  background: #f1f5f9;
  padding: 16px 16px 24px;
  padding-bottom: calc(24px + env(safe-area-inset-bottom));
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: #0f172a;
}

.edit-btn {
  padding: 6px 14px;
  font-size: 12px;
  color: #475569;
  background: #f1f5f9;
  border-radius: 999px;
  border: none;
  line-height: 1.4;
}

.profile-card {
  background: #fff;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}

.avatar-wrap {
  width: 48px;
  height: 48px;
  border-radius: 24px;
  background: #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  flex-shrink: 0;
}

.avatar-img {
  width: 100%;
  height: 100%;
}

.avatar-letter {
  font-size: 18px;
  font-weight: 600;
  color: #64748b;
}

.profile-info {
  flex: 1;
  min-width: 0;
}

.name {
  font-size: 15px;
  font-weight: 600;
  color: #0f172a;
  display: block;
}

.desc {
  font-size: 12px;
  color: #64748b;
  margin-top: 4px;
  display: block;
}

.menu-card {
  background: #fff;
  border-radius: 16px;
  margin-bottom: 16px;
  border: 1px solid #e2e8f0;
  overflow: hidden;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  text-align: left;
  background: none;
  border: none;
  border-bottom: 1px solid #f1f5f9;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

/* 用药管理（icon-y）：医疗/健康属性 → 沉稳的深青蓝+浅青蓝背景 */
.menu-icon.icon-y {
	border-radius: 18px;
	background: #eff6ff; /* 浅青蓝背景，柔和不刺眼 */
	color: #1d4ed8;      /* 深青蓝文字/图标，体现医疗专业性 */
}

/* 目标管理（icon-g）：目标/进度属性 → 清爽的浅蓝+深蓝背景 */
.menu-icon.icon-g {
	border-radius: 18px;
	background: #dbeafe; /* 浅蓝背景 */
	color: #2563eb;      /* 主色蓝 */
}

/* 提醒设置（icon-t）：通知/提醒属性 → 醒目的蓝紫+浅紫背景 */
.menu-icon.icon-t {
	border-radius: 18px;
	background: #f5f3ff; /* 浅紫背景，区分提醒功能 */
	color: #7c3aed;      /* 蓝紫文字/图标，醒目且不突兀 */
}

/* 家庭（icon-f）：亲情/温暖属性 → 治愈的浅绿+深绿背景 */
.menu-icon.icon-f {
	border-radius: 18px;
	background: #ecfdf5; /* 浅绿背景，体现温暖/健康 */
	color: #059669;      /* 深绿文字/图标，符合家庭健康的视觉认知 */
}

/* 报表（icon-r）：数据/统计属性 → 专业的浅橙+深橙背景 */
.menu-icon.icon-r {
	border-radius: 18px;
	background: #fff7ed; /* 浅橙背景，适配数据图表的视觉风格 */
	color: #ea580c;      /* 深橙文字/图标，突出数据可视化 */
}

/* 授权（icon-s）：安全/隐私属性 → 稳重的浅灰+深灰背景 */
.menu-icon.icon-s {
	border-radius: 18px;
	background: #f8fafc; /* 浅灰背景，体现安全/中立 */
	color: #334155;      /* 深灰文字/图标，符合隐私授权的视觉调性 */
}

.icon-txt {
  font-size: 14px;
  font-weight: 600;
  color: inherit;
}

.menu-content {
  flex: 1;
  min-width: 0;
}

.menu-title {
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
  display: block;
}

.menu-desc {
  font-size: 11px;
  color: #64748b;
  margin-top: 2px;
  display: block;
}

.menu-arrow {
  font-size: 16px;
  color: #cbd5e1;
  font-weight: 300;
}

.ai-card {
  background: #0f172a;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 16px;
  border: 1px solid #1e293b;
}

.ai-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.ai-icon {
  font-size: 14px;
}

.ai-title {
  font-size: 13px;
  font-weight: 600;
  color: #f8fafc;
}

.ai-desc {
  font-size: 12px;
  color: #cbd5e1;
  line-height: 1.45;
  margin-bottom: 12px;
}

.ai-btn {
  align-self: flex-start;
  padding: 8px 16px;
  font-size: 12px;
  color: #0f172a;
  background: #f8fafc;
  border-radius: 999px;
  border: none;
}


.action-row {
  display: flex;
  justify-content: space-between;
  padding: 0 4px;
}

.link {
  font-size: 12px;
  color: #64748b;
  background: none;
  border: none;
  padding: 8px 0;
}

.link.danger {
  color: #dc2626;
}

.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.modal {
  width: 86%;
  max-width: 320px;
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.modal-title {
  font-size: 15px;
  font-weight: 600;
  color: #0f172a;
  margin-bottom: 4px;
}

.input {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 10px 14px;
  font-size: 14px;
  color: #0f172a;
  background: #fff;
}

.radio-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.radio-label {
  font-size: 12px;
  color: #64748b;
}

.radio-options {
  display: flex;
  gap: 10px;
}

.radio-item {
  flex: 1;
  text-align: center;
  padding: 10px 0;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  color: #64748b;
  font-size: 13px;
  background: #fff;
}

.radio-item.active {
  border-color: #2563eb;
  color: #2563eb;
  background: #eff6ff;
}


.ghost {
  padding: 10px 14px;
  font-size: 12px;
  color: #475569;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  width: 100%;
}

.primary {
  padding: 12px 0;
  font-size: 14px;
  font-weight: 500;
  color: #fff;
  background: #0f172a;
  border-radius: 12px;
  border: none;
  width: 100%;
}

.status {
  font-size: 12px;
  color: #64748b;
  margin-top: 8px;
}

.status.error {
  color: #dc2626;
}

.icon-img {
  /* 图标大小：适配 36px 容器，留边距更美观 */
    width: 18px;
    height: 18px;
    /* 核心：保证图标透明底，不覆盖背景色 */
    background: transparent !important;
    background-color: transparent !important;
    border: none !important;
    /* 去掉图片默认间隙 */
    vertical-align: middle;
} 
</style>
