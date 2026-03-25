<template>
  <view class="page">
    <view class="profile-card card" @tap="openProfileModal">
      <view class="avatar-wrap" :class="{ 'avatar-default': !profile.avatar }">
        <image v-if="profile.avatar" class="avatar-img" :src="String(profile.avatar)" mode="aspectFill" />
        <text v-else class="avatar-letter">{{ profile.name ? profile.name.slice(0, 1) : "?" }}</text>
      </view>
      <view class="profile-info">
        <text class="name">{{ profile.name || "未登录" }}</text>
        <text class="desc">{{ profile.summary || "请先登录" }}</text>
      </view>
    </view>

    <!-- 健康管理卡片 -->
    <view class="menu-card card">
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
    </view>

    <!-- 数据与隐私卡片 -->
    <view class="menu-card card">
      <navigator class="menu-item" url="/pages/about/index" hover-class="none">
        <view class="menu-icon icon-a">
          <image class="icon-img" src="/static/tabbar/hands.png" mode="widthFix"></image>
        </view>
        <view class="menu-content">
          <text class="menu-title">关于我们</text>
          <text class="menu-desc">产品介绍 · 联系我们 · 备案信息</text>
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
            <button class="modal-sheet-btn secondary pill" @tap="bodyModalMode = 'edit'">编辑</button>
</template>
          <template v-else>
            <!-- 头像区域 -->
            <view class="avatar-section-edit">
              <button class="avatar-btn" open-type="chooseAvatar" @chooseavatar="onChooseAvatar">
                <image v-if="profile.avatar || bodyForm.avatarUrl" class="avatar-preview" :src="String(profile.avatar || bodyForm.avatarUrl)" mode="aspectFill" />
                <view v-else class="avatar-placeholder">
                  <text class="avatar-placeholder-text">{{ (bodyForm.nickname || '?').slice(0, 1) }}</text>
                </view>
                <view class="avatar-camera">
                  <image class="camera-icon" src="/static/tabbar/camera_s.png" mode="aspectFit"></image>
                </view>
              </button>
              <text class="avatar-tip">点击更换头像</text>
            </view>

            <!-- 基本信息表单 -->
            <view class="form-section-edit">
              <view class="form-field card-white">
                <text class="form-field-label">昵称<text class="required">*</text></text>
                <input class="form-field-input" type="text" v-model="bodyForm.nickname" placeholder="请输入昵称（必填）" />
              </view>
              <view class="form-field card-white" @tap="showSexPicker = true">
                <text class="form-field-label">性别<text class="required">*</text></text>
                <view class="form-field-select">
                  <text :class="{ 'placeholder': !bodyForm.sex }">{{ bodyForm.sex || '请选择（必填）' }}</text>
                  <text class="select-arrow">▼</text>
                </view>
              </view>
              <view class="form-field card-white">
                <text class="form-field-label">年龄</text>
                <input class="form-field-input" v-model="bodyForm.age" placeholder="请输入年龄" type="number" />
              </view>
            </view>

            <!-- 健康数据 -->
            <view class="health-section">
              <view class="health-grid-4">
                <view class="health-card-white">
                  <text class="health-card-label">身高 (CM)</text>
                  <input class="health-card-value-input" v-model="bodyForm.height" placeholder="--" type="number" />
                </view>
                <view class="health-card-white">
                  <text class="health-card-label">体重 (KG)</text>
                  <input class="health-card-value-input" v-model="bodyForm.weight" placeholder="--" type="number" />
                </view>
                <view class="health-card-white">
                  <text class="health-card-label">心率 (BPM)</text>
                  <input class="health-card-value-input" v-model="bodyForm.heartRate" placeholder="--" type="number" />
                </view>
                <view class="health-card-white">
                  <text class="health-card-label">血压 (MMHG)</text>
                  <view class="bp-inputs">
                    <input class="bp-input" v-model="bodyForm.systolic" placeholder="--" type="number" />
                    <text class="bp-divider">/</text>
                    <input class="bp-input" v-model="bodyForm.diastolic" placeholder="--" type="number" />
                  </view>
                </view>
              </view>
            </view>

            <view class="save-btn-wrap">
              <button class="save-btn" @tap="saveBodyProfile" :disabled="bodySaving">
                {{ bodySaving ? "保存中..." : "保存" }}
              </button>
            </view>
          </template>
        </view>
      </view>
    </view>

    <text v-if="message" class="status">{{ message }}</text>
    <text v-if="loading" class="status">加载中...</text>
    <text v-if="error" class="status error">{{ error }}</text>

    <!-- 性别选择器 -->
    <view v-if="showSexPicker" class="picker-mask" @tap="showSexPicker = false">
      <view class="picker-sheet" @tap.stop>
        <view class="picker-options">
          <view class="picker-option" :class="{ active: bodyForm.sex === '男' }" @tap="selectSex('男')">
            <text>男</text>
          </view>
          <view class="picker-option" :class="{ active: bodyForm.sex === '女' }" @tap="selectSex('女')">
            <text>女</text>
          </view>
        </view>
        <view class="picker-cancel" @tap="showSexPicker = false">取消</view>
      </view>
    </view>

    <!-- 自定义底部导航 -->
    <custom-tabbar :current="3" />
  </view>
</template>

<script>
import { request, API_BASE_URL } from "../../utils/api";
import CustomTabbar from "@/components/custom-tabbar/custom-tabbar.vue";

export default {
  components: {
    CustomTabbar
  },
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
        heartRate: "",
        avatarUrl: ""
      },
      showSexPicker: false
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
    this.showBodyModal = false;
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
  onHide() {
    this.showBodyModal = false;
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
      const timeoutId = setTimeout(() => {
        if (this.loading) {
          this.loading = false;
          if (!this.error) this.error = "获取资料失败";
        }
      }, 8000);
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
          clearTimeout(timeoutId);
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
      this.bodyModalMode = "edit";
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
        uni.showToast({ title: "请输入昵称", icon: "none" });
        return;
      }
      if (!this.bodyForm.sex) {
        uni.showToast({ title: "请选择性别", icon: "none" });
        return;
      }
      if (age !== null && (age < 1 || age > 120)) {
        uni.showToast({ title: "年龄需在 1-120 之间", icon: "none" });
        return;
      }
      if (height !== null && (height < 50 || height > 250)) {
        uni.showToast({ title: "身高需在 50-250 之间", icon: "none" });
        return;
      }
      if (weight !== null && (weight < 20 || weight > 300)) {
        uni.showToast({ title: "体重需在 20-300 之间", icon: "none" });
        return;
      }
      if (systolic !== null && (systolic < 60 || systolic > 250)) {
        uni.showToast({ title: "收缩压需在 60-250 之间", icon: "none" });
        return;
      }
      if (diastolic !== null && (diastolic < 40 || diastolic > 150)) {
        uni.showToast({ title: "舒张压需在 40-150 之间", icon: "none" });
        return;
      }
      if (heartRate !== null && (heartRate < 40 || heartRate > 200)) {
        uni.showToast({ title: "心率需在 40-200 之间", icon: "none" });
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
      this.bodyForm.avatarUrl = String(avatarUrl);
      this.bodySaving = true;
      this.uploadAvatar(String(avatarUrl))
        .then((serverUrl) => {
          this.profile.avatar = serverUrl;
          this.bodyForm.avatarUrl = serverUrl;
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
    },
    chooseAvatar() {
      // #ifdef MP-WEIXIN
      // 小程序环境下使用 chooseAvatar
      // 这里需要用户手动点击 button 触发
      uni.showToast({ title: "请点击头像按钮选择", icon: "none" });
      // #endif
      // #ifndef MP-WEIXIN
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: (res) => {
          const tempFilePath = res.tempFilePaths[0];
          this.bodySaving = true;
          this.uploadAvatarFile(tempFilePath)
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
      });
      // #endif
    },
    selectSex(sex) {
      this.bodyForm.sex = sex;
      this.showSexPicker = false;
    }
  }
};
</script>

<style>
.page {
  min-height: 100vh;
  background: #FAF8F5;
  padding: 16px 16px 24px;
  padding-bottom: calc(80px + env(safe-area-inset-bottom));
}

.profile-card {
  background: #fff;
  border-radius: var(--radius-card);
  padding: 16px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  border: 1px solid #E9E1D8;
  box-shadow: 0 6rpx 16rpx rgba(0, 0, 0, 0.04);
}

.avatar-wrap {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-card);
  background: #E9E1D8;
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
  background: #E9E1D8;
}

.avatar-letter {
  font-size: 18px;
  font-weight: 600;
  color: #564337;
}

.profile-info {
  flex: 1;
  min-width: 0;
}

.name {
  font-size: 15px;
  font-weight: 700;
  color: #1a1c1a;
  display: block;
}

.desc {
  font-size: 12px;
  color: #564337;
  margin-top: 4px;
  display: block;
}

.menu-card {
  background: #fff;
  border-radius: var(--radius-card);
  margin-bottom: 16px;
  border: 1px solid #E9E1D8;
  overflow: hidden;
  box-shadow: 0 6rpx 16rpx rgba(0, 0, 0, 0.04);
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  text-align: left;
  background: none;
  border: none;
  border-bottom: 1px solid #E9E1D8;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-icon {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-card);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

/* 目标管理（icon-g）：淡蓝色 */
.menu-icon.icon-g {
	border-radius: var(--radius-card);
	background: #dbeafe;
	color: #1e40af;
}

/* 用药管理（icon-y）：淡黄色 */
.menu-icon.icon-y {
	border-radius: var(--radius-card);
	background: #fef9c3;
	color: #854d0e;
}

/* 提醒设置（icon-t）：淡紫色 */
.menu-icon.icon-t {
	border-radius: var(--radius-card);
	background: #f3e8ff;
	color: #6b21a8;
}

/* 家庭（icon-f）：淡绿色 - 保持不变 */
.menu-icon.icon-f {
	border-radius: var(--radius-card);
	background: #ecfdf5;
	color: #059669;
}


/* 关于我们（icon-a）：淡橙色 */
.menu-icon.icon-a {
	border-radius: var(--radius-card);
	background: #FFE8D6;
	color: #C45200;
}
/* 经期记录（icon-p）：淡粉色 - 保持不变 */
.menu-icon.icon-p {
	border-radius: var(--radius-card);
	background: #fce7f3;
	color: #be185d;
}

/* 报表（icon-r）：淡青色 */
.menu-icon.icon-r {
	border-radius: var(--radius-card);
	background: #ccfbf1;
	color: #0f766e;
}

/* 授权（icon-s）：淡灰色 */
.menu-icon.icon-s {
	border-radius: var(--radius-card);
	background: #f3f4f6;
	color: #4b5563;
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
  color: #1a1c1a;
  display: block;
}

.menu-desc {
  font-size: 11px;
  color: #564337;
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
  padding: 0 0;
}

.logout-btn {
  width: 100%;
  padding: 16px;
  font-size: 15px;
  font-weight: 700;
  color: #ef4444 !important;
  background: #fff;
  border: 1px solid #ef4444 !important;
  border-radius: 52rpx;
  box-shadow: var(--shadow-card);
}

.logout-btn::after {
  border: none;
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
  border-radius: 24rpx 20px 0 0;
  padding-bottom: calc(100px + env(safe-area-inset-bottom));
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.modal-sheet-bar {
  width: 36px;
  height: 4px;
  border-radius: var(--radius-card);
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
  color: #1a1c1a;
}

.modal-sheet-close {
  font-size: 24px;
  color: #564337;
  padding: 4px;
  line-height: 1;
}

.modal-sheet-body {
  padding: 20px;
  overflow-y: auto;
  flex: 1;
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
  border-radius: var(--radius-card);
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
  background: #E9E1D8;
}

.info-avatar-letter {
  font-size: 24px;
  font-weight: 600;
  color: #564337;
}

.info-name {
  font-size: 18px;
  font-weight: 600;
  color: #1a1c1a;
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
  color: #564337;
}

.info-value {
  font-size: 14px;
  font-weight: 500;
  color: #1a1c1a;
}

.modal-sheet-btn {
  margin-top: 8px;
  padding: 14px;
  font-size: 15px;
  font-weight: 600;
  border-radius: var(--radius-card);
  border: none;
  width: 100%;
}

.modal-sheet-btn.primary {
  background: #A23F00;
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
  color: #564337;
}

.input {
  border: 1px solid #E9E1D8;
  border-radius: var(--radius-card);
  padding: 12px 14px;
  font-size: 14px;
  color: #1a1c1a;
  background: #fff;
  height: 44px;
  line-height: 20px;
  box-sizing: border-box;
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
  border-radius: var(--radius-card);
  border: 1px solid #E9E1D8;
  color: #564337;
  font-size: 14px;
  background: #fff;
}

.radio-item.active {
  border-color: #A23F00;
  color: #A23F00;
  background: #fff7ed;
}

.ghost {
  padding: 12px 14px;
  font-size: 13px;
  color: #475569;
  background: #FAF8F5;
  border: 1px solid #E9E1D8;
  border-radius: var(--radius-card);
  width: 100%;
}

.status {
  font-size: 12px;
  color: #564337;
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

/* 头像区域 */
.avatar-section-edit {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 32rpx;
}

.avatar-btn {
  position: relative;
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  overflow: visible;
  background: transparent;
  border: none;
  padding: 0;
}

.avatar-btn::after {
  border: none;
}

.avatar-preview {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: linear-gradient(135deg, #A23F00 0%, #FA7025 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-placeholder-text {
  font-size: 56rpx;
  color: #fff;
  font-weight: 600;
}

.avatar-camera {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 48rpx;
  height: 48rpx;
  background: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.camera-icon {
  width: 24rpx;
  height: 24rpx;
}

.avatar-tip {
  font-size: 24rpx;
  color: #8B7355;
}

/* 基本信息表单 */
.form-section-edit {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin-bottom: 32rpx;
}

.form-field {
  display: flex;
  align-items: center;
  padding: 0 24rpx;
  height: 88rpx;
}

.form-field.card-white {
  background: #fff;
  border: 1px solid #E9E1D8;
  border-radius: var(--radius-card);
}

.form-field-label {
  font-size: 28rpx;
  color: #1a1c1a;
  font-weight: 500;
  width: 120rpx;
  flex-shrink: 0;
}

.required {
  color: #ef4444;
  margin-left: 4rpx;
}

.form-field-input {
  flex: 1;
  font-size: 28rpx;
  color: #1a1c1a;
  text-align: right;
  background: transparent;
}

.form-field-select {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8rpx;
}

.form-field-select text {
  font-size: 28rpx;
  color: #1a1c1a;
}

.form-field-select .placeholder {
  color: #8B7355;
}

.select-arrow {
  font-size: 20rpx;
  color: #8B7355;
}

/* 健康数据区域 */
.health-section {
  margin-bottom: 32rpx;
}

.health-grid-4 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16rpx;
}

.health-card-white {
  background: #fff;
  border: 1px solid #E9E1D8;
  border-radius: var(--radius-card);
  padding: 24rpx 20rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
}

.health-card-label {
  font-size: 24rpx;
  color: #8B7355;
}

.health-card-value-input {
  font-size: 36rpx;
  font-weight: 600;
  color: #1a1c1a;
  background: transparent;
  padding: 0;
  height: auto;
  line-height: 1;
  text-align: center;
  width: 100%;
}

.bp-inputs {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.bp-input {
  width: 80rpx;
  font-size: 32rpx;
  font-weight: 600;
  color: #1a1c1a;
  background: transparent;
  text-align: center;
  padding: 0;
  height: auto;
  line-height: 1;
}

.bp-divider {
  font-size: 32rpx;
  color: #8B7355;
  font-weight: 600;
}

/* 保存按钮 */
.save-btn-wrap {
  margin-top: 32rpx;
}

.save-btn {
  width: 100%;
  height: 88rpx;
  background: linear-gradient(135deg, #A23F00 0%, #FA7025 100%);
  border-radius: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  font-weight: 600;
  color: #fff;
  border: none;
  box-shadow: 0 8rpx 24rpx rgba(162, 63, 0, 0.3);
}

.save-btn::after {
  border: none;
}

.save-btn[disabled] {
  opacity: 0.6;
}

/* 性别选择器 */
.picker-mask {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.5);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 200;
}

.picker-sheet {
  width: 100%;
  background: #fff;
  border-radius: 32rpx 32rpx 0 0;
  padding: 24rpx 32rpx calc(24rpx + env(safe-area-inset-bottom));
}

.picker-options {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.picker-option {
  height: 88rpx;
  background: #FAF8F5;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  color: #1a1c1a;
  border: 2rpx solid transparent;
}

.picker-option.active {
  background: #FFF4ED;
  border-color: #A23F00;
  color: #A23F00;
}

.picker-cancel {
  height: 88rpx;
  background: #fff;
  border: 2rpx solid #E9E1D8;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  color: #8B7355;
} 
</style>
