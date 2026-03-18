<template>
  <view class="page">
    <view class="profile-card" @tap="openProfileModal">
      <view class="avatar-wrap" :class="{ 'avatar-default': !profile.avatar }">
        <image v-if="profile.avatar" class="avatar-img" :src="String(profile.avatar)" mode="aspectFill" />
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
      <navigator class="menu-item" url="/pages/reminders/index" open-type="navigateTo" hover-class="none">
        <view class="menu-icon icon-t">
          <image class="icon-img" src="/static/tabbar/remind.png" mode="widthFix"></image>
        </view>
        <view class="menu-content">
          <text class="menu-title">提醒设置</text>
          <text class="menu-desc">运动 · 饮食 · 睡眠</text>
        </view>
        <text class="menu-arrow">›</text>
      </navigator>
      <navigator v-if="showPeriod" class="menu-item" url="/pages/period/index" hover-class="none">
        <view class="menu-icon icon-p">
          <image class="icon-img" src="/static/tabbar/period.png" mode="widthFix"></image>
        </view>
        <view class="menu-content">
          <text class="menu-title">经期记录</text>
          <text class="menu-desc">记录经期 · 预估下次</text>
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

    <view class="logout-wrap">
      <button class="logout-btn" plain @tap="logout">退出登录</button>
    </view>

    <view v-if="showBodyModal" class="modal-mask" @tap="closeBodyModal">
      <view class="modal-sheet" @tap.stop>
        <view class="modal-sheet-bar" />
        <view class="modal-sheet-head">
          <text class="modal-sheet-title">{{ bodyModalMode === 'edit' ? '完善个人信息' : '个人信息' }}</text>
          <text class="modal-sheet-close" @tap="closeBodyModal">×</text>
        </view>
        <view class="modal-sheet-body">
          <template v-if="bodyModalMode === 'view'">
            <view class="info-block">
              <view class="info-avatar-wrap" :class="{ 'avatar-default': !profile.avatar }">
                <image v-if="profile.avatar" class="info-avatar" :src="String(profile.avatar)" mode="aspectFill" />
                <text v-else class="info-avatar-letter">{{ (profile.name || '?').slice(0, 1) }}</text>
              </view>
              <text class="info-name">{{ profile.name || '未设置' }}</text>
            </view>
            <view class="info-rows">
              <view class="info-row"><text class="info-label">性别</text><text class="info-value">{{ profile.sex || '--' }}</text></view>
              <view class="info-row"><text class="info-label">年龄</text><text class="info-value">{{ profile.age ? profile.age + ' 岁' : '--' }}</text></view>
              <view class="info-row"><text class="info-label">身高</text><text class="info-value">{{ profile.height ? profile.height + ' cm' : '--' }}</text></view>
              <view class="info-row"><text class="info-label">体重</text><text class="info-value">{{ profile.weight ? profile.weight + ' kg' : '--' }}</text></view>
              <view class="info-row"><text class="info-label">血压</text><text class="info-value">{{ profile.systolic && profile.diastolic ? profile.systolic + '/' + profile.diastolic : '--' }}</text></view>
              <view class="info-row"><text class="info-label">心率</text><text class="info-value">{{ profile.heartRate ? profile.heartRate + ' 次/分' : '--' }}</text></view>
            </view>
            <button class="modal-sheet-btn secondary" @tap="bodyModalMode = 'edit'">编辑</button>
          </template>
          <template v-else>
            <!-- #ifdef MP-WEIXIN -->
            <view class="field"><text class="field-label">头像</text>
              <button class="ghost" open-type="chooseAvatar" @chooseavatar="onChooseAvatar" :disabled="bodySaving">选择头像</button>
            </view>
            <!-- #endif -->
            <view class="field"><text class="field-label">昵称</text>
              <input class="input" type="nickname" v-model="bodyForm.nickname" placeholder="请输入昵称" />
            </view>
            <view class="field"><text class="field-label">性别</text>
              <view class="radio-options">
                <view class="radio-item" :class="{ active: bodyForm.sex === '男' }" @tap="bodyForm.sex = '男'">男</view>
                <view class="radio-item" :class="{ active: bodyForm.sex === '女' }" @tap="bodyForm.sex = '女'">女</view>
              </view>
            </view>
            <view class="field"><text class="field-label">年龄</text>
              <input class="input" v-model="bodyForm.age" placeholder="年龄" type="number" />
            </view>
            <view class="field"><text class="field-label">身高（cm）</text>
              <input class="input" v-model="bodyForm.height" placeholder="身高" type="number" />
            </view>
            <view class="field"><text class="field-label">体重（kg）</text>
              <input class="input" v-model="bodyForm.weight" placeholder="体重" type="number" />
            </view>
            <view class="field"><text class="field-label">收缩压 / 舒张压</text>
              <view class="input-row">
                <input class="input" v-model="bodyForm.systolic" placeholder="高压" type="number" />
                <input class="input" v-model="bodyForm.diastolic" placeholder="低压" type="number" />
              </view>
            </view>
            <view class="field"><text class="field-label">心率</text>
              <input class="input" v-model="bodyForm.heartRate" placeholder="心率" type="number" />
            </view>
            <button class="modal-sheet-btn primary" @tap="saveBodyProfile" :disabled="bodySaving">
              {{ bodySaving ? "保存中..." : "保存" }}
            </button>
          </template>
        </view>
      </view>
    </view>

    <text v-if="message" class="status">{{ message }}</text>
    <text v-if="loading" class="status">加载中...</text>
    <text v-if="error" class="status error">{{ error }}</text>
  </view>
</template>

<script>
import { request, API_BASE_URL } from "../../utils/api";

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
      bodyModalMode: "view",
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
  computed: {
    showPeriod() {
      const sex = this.profile.sex || uni.getStorageSync("userSex") || "";
      return String(sex).includes("女");
    }
  },
  onLoad() {
  },
  onShow() {
    const pages = getCurrentPages();
    const page = pages[pages.length - 1];
    if (page && typeof page.getTabBar === "function") {
      const tabBar = page.getTabBar();
      if (tabBar && typeof tabBar.setData === "function") tabBar.setData({ selected: 3 });
    }
    if (uni.getStorageSync("openProfileModal")) {
      uni.removeStorageSync("openProfileModal");
      this.$nextTick(() => this.openProfileModal());
    }
    this.fetchProfile();
    this.loadLocalProfile();
  },
  methods: {
    uploadAvatarFile(filePath) {
      return new Promise((resolve, reject) => {
        const token = uni.getStorageSync("token");
        uni.uploadFile({
          url: `${API_BASE_URL}/api/user/avatar/upload`,
          filePath,
          name: "file",
          header: {
            Authorization: token ? `Bearer ${token}` : ""
          },
          success: (res) => {
            try {
              const body = JSON.parse(res.data || "{}");
              if ((typeof body.code === "number" || typeof body.code === "string") && Number(body.code) !== 0) {
                reject(new Error(body.message || "上传失败"));
                return;
              }
              const data = body.data || body;
              if (data && data.url) {
                resolve(String(data.url));
                return;
              }
              reject(new Error("上传失败"));
            } catch (err) {
              reject(err);
            }
          },
          fail: (err) => reject(err)
        });
      });
    },
    async uploadAvatar(avatarUrl) {
      if (!avatarUrl) return "";
      if (String(avatarUrl).startsWith("http")) {
        const tempPath = await new Promise((resolve, reject) => {
          uni.downloadFile({
            url: avatarUrl,
            success: (res) => {
              if (res.statusCode === 200 && res.tempFilePath) resolve(res.tempFilePath);
              else reject(new Error("下载头像失败"));
            },
            fail: reject
          });
        });
        return this.uploadAvatarFile(tempPath);
      }
      return this.uploadAvatarFile(avatarUrl);
    },
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
            this.profile.avatar = (data.wxAvatar != null && data.wxAvatar !== "") ? String(data.wxAvatar) : "";
            this.profile.sex = data.sex || "";
            this.profile.age = data.age || "";
            this.profile.height = data.height || "";
            this.profile.weight = data.weight || "";
            this.profile.systolic = data.systolic || "";
            this.profile.diastolic = data.diastolic || "";
            this.profile.heartRate = data.heartRate || "";
            uni.setStorageSync("userAvatar", this.profile.avatar || "");
            uni.setStorageSync("userSex", this.profile.sex || "");
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
        this.profile.avatar = String(wxProfile.avatarUrl);
        uni.setStorageSync("userAvatar", this.profile.avatar);
      }
    },
    goReports() {
      uni.showToast({ title: "报告中心开发中", icon: "none" });
    },
    logout() {
      uni.showModal({
        title: "提示",
        content: "确定退出登录？",
        success: (res) => {
          if (res.confirm) {
            uni.removeStorageSync("token");
            uni.removeStorageSync("userId");
            uni.removeStorageSync("userName");
            uni.removeStorageSync("userAvatar");
            uni.reLaunch({ url: "/pages/login/index" });
          }
        }
      });
    },
    openProfileModal() {
      this.showBodyModal = true;
      this.bodyModalMode = "view";
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
      this.bodyModalMode = "view";
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
      this.bodySaving = true;
      this.uploadAvatar(String(avatarUrl))
        .then((serverUrl) => {
          this.profile.avatar = serverUrl;
          uni.setStorageSync("userAvatar", serverUrl);
          return request("/api/user/profile/update", "POST", {
            userId: uni.getStorageSync("userId") || 1,
            wxNickname: this.bodyForm.nickname || this.profile.name || "",
            wxAvatar: serverUrl
          });
        })
        .catch(() => {
          uni.showToast({ title: "头像上传失败", icon: "none" });
        })
        .finally(() => {
          this.bodySaving = false;
        });
    }
  }
};
</script>

<style>
.page {
  min-height: 100vh;
  background: #f5f1eb;
  padding: 16px 16px 24px;
  padding-bottom: calc(56px + env(safe-area-inset-bottom));
}

.profile-card {
  background: #fff;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  border: 1px solid #e8e2db;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}

.avatar-wrap {
  width: 48px;
  height: 48px;
  border-radius: 24px;
  background: #e8e2db;
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

.avatar-wrap.avatar-default {
  background: #e2e8f0;
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
  border: 1px solid #e8e2db;
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
  border-bottom: 1px solid #f2ede8;
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

/* 用药管理（icon-y）：医疗/健康属性 → 橙色系 */
.menu-icon.icon-y {
	border-radius: 18px;
	background: #fff7ed;
	color: #ea580c;
}

/* 目标管理（icon-g）：目标/进度属性 → 橙色系 */
.menu-icon.icon-g {
	border-radius: 18px;
	background: #fff7ed;
	color: #f97316;
}

/* 提醒设置（icon-t）：通知/提醒属性 → 橙色系 */
.menu-icon.icon-t {
	border-radius: 18px;
	background: #fff7ed;
	color: #f59e0b;
}

/* 家庭（icon-f）：亲情/温暖属性 → 治愈的浅绿+深绿背景 */
.menu-icon.icon-f {
	border-radius: 18px;
	background: #ecfdf5; /* 浅绿背景，体现温暖/健康 */
	color: #059669;      /* 深绿文字/图标，符合家庭健康的视觉认知 */
}

/* 经期记录（icon-p）：经期/女性健康 → 柔和粉紫 */
.menu-icon.icon-p {
	border-radius: 18px;
	background: linear-gradient(135deg, #fce7f3 0%, #fbcfe8 100%);
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
	background: #fefcf9; /* 浅灰背景，体现安全/中立 */
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

.logout-wrap {
  margin-top: 24px;
  padding: 0 4px;
}

.logout-btn {
  width: 100%;
  padding: 14px;
  font-size: 15px;
  color: #64748b;
  background: #fefcf9;
  border: 1px solid #e8e2db;
  border-radius: 14px;
}

.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.5);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 100;
}

.modal-sheet {
  width: 100%;
  max-width: 400px;
  max-height: 85vh;
  background: #fff;
  border-radius: 20px 20px 0 0;
  padding-bottom: env(safe-area-inset-bottom);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.modal-sheet-bar {
  width: 36px;
  height: 4px;
  border-radius: 2px;
  background: #e8e2db;
  margin: 10px auto 0;
}

.modal-sheet-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px 16px;
  border-bottom: 1px solid #f2ede8;
}

.modal-sheet-title {
  font-size: 17px;
  font-weight: 600;
  color: #0f172a;
}

.modal-sheet-close {
  font-size: 24px;
  color: #94a3b8;
  padding: 4px;
  line-height: 1;
}

.modal-sheet-body {
  padding: 20px;
  overflow-y: auto;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-block {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f2ede8;
}

.info-avatar-wrap {
  width: 64px;
  height: 64px;
  border-radius: 32px;
  background: #e8e2db;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.info-avatar {
  width: 100%;
  height: 100%;
}

.info-avatar-wrap.avatar-default {
  background: #e2e8f0;
}

.info-avatar-letter {
  font-size: 24px;
  font-weight: 600;
  color: #64748b;
}

.info-name {
  font-size: 18px;
  font-weight: 600;
  color: #0f172a;
}

.info-rows {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #fefcf9;
}

.info-label {
  font-size: 14px;
  color: #64748b;
}

.info-value {
  font-size: 14px;
  font-weight: 500;
  color: #0f172a;
}

.modal-sheet-btn {
  margin-top: 8px;
  padding: 14px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 14px;
  border: none;
  width: 100%;
}

.modal-sheet-btn.primary {
  background: #f97316;
  color: #fff;
}

.modal-sheet-btn.secondary {
  background: #f5f1eb;
  color: #475569;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field-label {
  font-size: 13px;
  color: #64748b;
}

.input {
  border: 1px solid #e8e2db;
  border-radius: 12px;
  padding: 12px 14px;
  font-size: 14px;
  color: #0f172a;
  background: #fff;
}

.input-row {
  display: flex;
  gap: 10px;
}

.input-row .input {
  flex: 1;
}

.radio-options {
  display: flex;
  gap: 10px;
}

.radio-item {
  flex: 1;
  text-align: center;
  padding: 12px 0;
  border-radius: 12px;
  border: 1px solid #e8e2db;
  color: #64748b;
  font-size: 14px;
  background: #fff;
}

.radio-item.active {
  border-color: #f97316;
  color: #f97316;
  background: #fff7ed;
}

.ghost {
  padding: 12px 14px;
  font-size: 13px;
  color: #475569;
  background: #fefcf9;
  border: 1px solid #e8e2db;
  border-radius: 12px;
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
