<template>
  <view class="page-root">
    <view class="page-body">
      <view v-if="members.length === 0" class="empty-state">
        <view class="empty-content">
          <view class="empty-illustration">
            <image class="empty-illustration-icon" src="/static/tabbar/family_w.png" mode="aspectFit" />
          </view>
          <text class="empty-desc">还没有添加家人档案，添加后可统一记录家人健康数据，守护全家健康。</text>

          <view class="empty-features">
            <view class="feature-item">
              <image class="feature-icon" src="/static/tabbar/family_s.png" mode="aspectFit" />
              <text class="feature-text">成员档案</text>
            </view>
            <view class="feature-item">
              <image class="feature-icon" src="/static/tabbar/tips_s.png" mode="aspectFit" />
              <text class="feature-text">健康备注</text>
            </view>
            <view class="feature-item">
              <image class="feature-icon" src="/static/tabbar/profile.png" mode="aspectFit" />
              <text class="feature-text">关系管理</text>
            </view>
          </view>

          <button class="empty-add-btn pill" @tap="openAdd">
            <image class="empty-add-icon" src="/static/tabbar/add.png" mode="aspectFit" />
            <text class="empty-add-text">添加第一个家人</text>
          </button>
        </view>
      </view>

      <view v-else class="member-list">
        <view v-for="item in members" :key="item.id" class="member-card" @tap="openEdit(item)">
          <view class="member-avatar-wrap-large">
            <view class="member-avatar">
              <text class="member-avatar-text">{{ item.name.slice(0, 1) }}</text>
            </view>
            <view class="member-status-dot" :class="{ active: Number(item.status) === FAMILY_MEMBER_STATUS.ACTIVE }" />
          </view>

          <view class="member-main">
            <view class="member-row">
              <text class="member-name">{{ item.name }}</text>
              <text class="member-arrow">›</text>
            </view>
            <text class="member-meta">上次同步：{{ formatLastSync(item) }}</text>

            <view class="member-tags">
              <text class="member-role">{{ item.role || "成员" }}</text>
              <text v-for="tag in buildMemberTags(item)" :key="tag" class="member-tag-chip">{{ tag }}</text>
            </view>

            <text v-if="item.conditionText" class="member-desc">{{ item.conditionText }}</text>
          </view>
        </view>
      </view>
    </view>

    <view v-if="members.length > 0" class="fab-container">
      <view class="fab pill" @tap="openAdd">
        <text class="fab-icon">+</text>
      </view>
    </view>

    <view v-if="showModal" class="modal-mask" @tap="closeModal">
      <view class="modal-sheet" @tap.stop>
        <view class="modal-sheet-bar" />
        <view class="modal-sheet-head">
          <text class="modal-sheet-title">{{ editingId ? "编辑家人" : "添加家人" }}</text>
          <text class="modal-sheet-close" @tap="closeModal">×</text>
        </view>

        <scroll-view class="modal-sheet-body" scroll-y>
          <view class="modal-hero">
            <view class="modal-hero-icon-wrap">
              <image class="modal-hero-icon" src="/static/tabbar/family.png" mode="aspectFit" />
            </view>
            <text class="modal-hero-title">建立健康档案</text>
            <text class="modal-hero-sub">智康AI 将协助您共同守护家人的健康</text>
          </view>

          <view class="field-card card-sm">
            <text class="field-label">姓名</text>
            <input class="input-main" v-model="form.name" placeholder="请输入姓名" />
          </view>

          <view class="field-card card-sm">
            <text class="field-label">年龄</text>
            <input class="input-main" type="number" v-model="form.age" placeholder="请输入年龄" />
          </view>

          <view class="field-card card-sm">
            <text class="field-label">关系</text>
            <input class="input-main" v-model="form.relation" placeholder="如 父母/孩子" />
          </view>

          <view class="field-card card-sm">
            <text class="field-label">健康状况 / 备注</text>
            <textarea class="input-textarea" v-model="form.conditionText" placeholder="如 高血压、糖尿病或过敏史" maxlength="200" />
          </view>
        </scroll-view>

        <view class="modal-footer">
          <button class="save-btn" @tap="submitAdd" :disabled="saving">
            {{ saving ? "保存中..." : editingId ? "保存修改" : "保存成员信息" }}
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { request } from "../../utils/api";
import { FAMILY_MEMBER_STATUS } from "../../constants/enums";

export default {
  data() {
    return {
      members: [],
      showModal: false,
      saving: false,
      editingId: null,
      form: {
        name: "",
        age: "",
        relation: "",
        conditionText: ""
      }
    };
  },
  onShow() {
    this.fetchMembers();
  },
  methods: {
    formatLastSync(item) {
      const val = item.updatedAt || item.updated_at || item.createdAt || item.created_at;
      if (!val) return "刚刚";
      const s = String(val).replace("T", " ");
      return s.slice(0, 16);
    },
    buildMemberTags(item) {
      const tags = [];
      if (item.relation) tags.push(item.relation);
      if (item.age !== "" && item.age !== null && item.age !== undefined) tags.push(`${item.age}岁`);
      if (item.conditionText) tags.push("有备注");
      return tags.slice(0, 3);
    },
    fetchMembers() {
      const userId = uni.getStorageSync("userId") || 1;
      request("/api/family/list", "GET", { userId })
        .then((list) => {
          this.members = Array.isArray(list) ? list : [];
        })
        .catch(() => {
          this.members = [];
        });
    },
    openAdd() {
      this.showModal = true;
      this.editingId = null;
      this.form = { name: "", age: "", relation: "", conditionText: "" };
    },
    openEdit(item) {
      if (!item) return;
      this.editingId = item.id;
      this.showModal = true;
      this.form = {
        name: item.name || "",
        age: item.age || "",
        relation: item.relation || "",
        conditionText: item.conditionText || ""
      };
    },
    closeModal() {
      this.showModal = false;
    },
    submitAdd() {
      if (!this.form.name || !this.form.age) {
        uni.showToast({ title: "请填写姓名和年龄", icon: "none" });
        return;
      }
      const age = Number(this.form.age);
      if (Number.isNaN(age) || age < 0 || age > 120) {
        uni.showToast({ title: "年龄需在 0-120 之间", icon: "none" });
        return;
      }
      this.saving = true;
      const userId = uni.getStorageSync("userId") || 1;
      const payload = {
        userId,
        name: this.form.name,
        relation: this.form.relation,
        age: Number(this.form.age),
        conditionText: this.form.conditionText,
        role: "成员",
        status: FAMILY_MEMBER_STATUS.ACTIVE
      };
      const url = this.editingId ? "/api/family/update" : "/api/family/add";
      if (this.editingId) payload.id = this.editingId;

      request(url, "POST", payload)
        .then(() => {
          uni.showToast({ title: "已保存", icon: "success" });
          this.closeModal();
          this.fetchMembers();
        })
        .catch((err) => {
          uni.showToast({ title: err.message || "保存失败", icon: "none" });
        })
        .finally(() => {
          this.saving = false;
        });
    }
  }
};
</script>

<style>
.page-root {
  min-height: 100vh;
  background: #faf8f5;
  padding-bottom: env(safe-area-inset-bottom);
}

.page-header {
  padding: 16px 16px 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-avatar-wrap {
  width: 36px;
  height: 36px;
  border-radius: 18px;
  background: #efeeeb;
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-avatar {
  width: 18px;
  height: 18px;
}

.header-title {
  font-size: 24px;
  font-weight: 800;
  color: #1a1c1a;
}

.page-body {
  padding: 8px 16px 24px;
}

.empty-state {
  min-height: 100vh;
  background: #FAF8F5;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 0 32rpx;
}

.empty-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  margin-top: -120rpx;
  background: transparent;
  border: none;
  box-shadow: none;
  padding: 0;
}

.empty-illustration {
  width: 200rpx;
  height: 200rpx;
  background: linear-gradient(135deg, #A23F00 0%, #FA7025 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 40rpx;
  box-shadow: 0 8rpx 24rpx rgba(162, 63, 0, 0.3);
}

.empty-illustration-icon {
  width: 100rpx;
  height: 100rpx;
}

.empty-content .empty-desc {
  font-size: 28rpx;
  color: #564337;
  margin-bottom: 60rpx;
}

.empty-features {
  display: flex;
  gap: 32rpx;
  margin-bottom: 60rpx;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  background: transparent;
  border-radius: 0;
  padding: 0;
  box-shadow: none;
}

.feature-icon {
  width: 48rpx;
  height: 48rpx;
}

.feature-text {
  font-size: 22rpx;
  color: #564337;
  white-space: nowrap;
}

.empty-add-btn {
  width: 480rpx;
  height: 88rpx;
  background: linear-gradient(135deg, #A23F00 0%, #FA7025 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  box-shadow: 0 8rpx 24rpx rgba(162, 63, 0, 0.3);
  border: none;
  border-radius: 999rpx;
  position: relative;
  z-index: 10;
}

.empty-add-btn::after {
  border: none;
}

.empty-add-icon {
  width: 36rpx;
  height: 36rpx;
}

.empty-add-text {
  font-size: 28rpx;
  font-weight: 700;
  color: #ffffff;
}

.empty-title-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 16rpx;
}

.empty-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #1a1c1a;
  line-height: 1.4;
}

.member-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-top: 6px;
}

.member-list-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 2px 2px 6px;
}

.member-list-title {
  font-size: 22px;
  font-weight: 800;
  color: #1a1c1a;
}

.member-list-count {
  font-size: 12px;
  color: #865300;
  background: #ffddb9;
  border-radius: 999rpx;
  padding: 4px 10px;
  font-weight: 700;
}

.member-card {
  background: #fff;
  border-radius: 32rpx;
  border: 1px solid #e9e1d8;
  padding: 16px;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  box-shadow: 0 8rpx 20rpx rgba(0, 0, 0, 0.03);
}

.member-avatar-wrap-large {
  position: relative;
  width: 46px;
  height: 46px;
}

.member-avatar {
  width: 42px;
  height: 42px;
  border-radius: 21px;
  background: #efeeeb;
  display: flex;
  align-items: center;
  justify-content: center;
}

.member-status-dot {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid #fff;
  background: #fa7025;
}

.member-status-dot.active {
  background: #00b05c;
}

.member-avatar-text {
  font-size: 15px;
  color: #564337;
  font-weight: 700;
}

.member-main {
  flex: 1;
  min-width: 0;
}

.member-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.member-name {
  font-size: 16px;
  color: #1a1c1a;
  font-weight: 700;
}

.member-role {
  font-size: 20rpx;
  color: #A23F00;
  background: rgba(162, 63, 0, 0.08);
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  font-weight: 500;
}

.member-meta {
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #8B7355;
}

.member-tags {
  margin-top: 16rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.member-desc {
  margin-top: 16rpx;
  font-size: 24rpx;
  color: #564337;
  line-height: 1.5;
  background: #f7f4ef;
  border-radius: 16rpx;
  padding: 12rpx 20rpx;
  display: block;
}

.member-arrow {
  color: #b8aa9f;
  font-size: 22px;
  line-height: 1;
}

.member-tag-chip {
  font-size: 20rpx;
  color: #8B7355;
  background: #f5f1eb;
  border-radius: 20rpx;
  padding: 6rpx 16rpx;
  font-weight: 500;
}

.fab-container {
  position: fixed;
  bottom: 160rpx;
  right: 48rpx;
  z-index: 100;
}

.fab {
  width: 128rpx;
  height: 128rpx;
  background: #a23f00;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 16rpx 40rpx rgba(162, 63, 0, 0.4);
  transition: all 0.2s ease;
}

.fab:active {
  transform: scale(0.9);
}

.fab-icon {
  font-size: 64rpx;
  color: #ffffff;
  font-weight: 300;
}

.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  z-index: 100;
  display: flex;
  align-items: flex-end;
}

.modal-sheet {
  width: 100%;
  max-height: 85vh;
  background: #fff;
  border-radius: 32rpx 32rpx 0 0;
  padding-bottom: env(safe-area-inset-bottom);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-sizing: border-box;
}

.modal-sheet-bar {
  width: 72rpx;
  height: 8rpx;
  background: #E9E1D8;
  border-radius: 8rpx;
  margin: 20rpx auto;
}

.modal-sheet-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 40rpx 32rpx;
}

.modal-sheet-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #1a1c1a;
}

.modal-sheet-close {
  font-size: 48rpx;
  color: #8B7355;
  padding: 8rpx;
}

.modal-sheet-body {
  flex: 1;
  overflow-y: auto;
  padding: 0 40rpx 48rpx;
  box-sizing: border-box;
}

.modal-hero {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  margin-bottom: 12px;
}

.modal-hero-icon-wrap {
  width: 64px;
  height: 64px;
  border-radius: 32px;
  background: #ffddb9;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
}

.modal-hero-icon {
  width: 30px;
  height: 30px;
}

.modal-hero-title {
  font-size: 16px;
  color: #1a1c1a;
  font-weight: 800;
}

.modal-hero-sub {
  margin-top: 2px;
  font-size: 12px;
  color: #7b6a58;
}

.field-card {
  background: #fff;
  border-radius: 28rpx;
  padding: 12px;
  margin-bottom: 10px;
}

.field-label {
  display: block;
  font-size: 26rpx;
  color: #8B7355;
  font-weight: 500;
  margin-bottom: 16rpx;
}

.input-main {
  background: #fff;
  border: 1px solid #E9E1D8;
  height: 88rpx;
  border-radius: 24rpx;
  padding: 0 28rpx;
  font-size: 28rpx;
  color: #1a1c1a;
  box-sizing: border-box;
  line-height: 86rpx;
}

.input-textarea {
  width: 100%;
  min-height: 160rpx;
  box-sizing: border-box;
  background: #fff;
  border: 1px solid #E9E1D8;
  border-radius: 24rpx;
  padding: 24rpx 28rpx;
  font-size: 28rpx;
  line-height: 1.5;
  color: #1a1c1a;
}

.modal-footer {
  padding: 10px 18px calc(10px + env(safe-area-inset-bottom));
  background: #faf8f5;
}

.save-btn {
  width: 100%;
  height: 96rpx;
  border-radius: 48rpx;
  border: none;
  color: #fff;
  font-size: 32rpx;
  font-weight: 600;
  background: linear-gradient(135deg, #A23F00 0%, #FA7025 100%);
}

.save-btn::after {
  border: none;
}

.pill {
  border-radius: 999rpx;
}

.card-sm {
  border-radius: 22rpx;
}
</style>
