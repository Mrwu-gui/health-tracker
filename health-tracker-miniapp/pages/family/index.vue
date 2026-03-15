<template>
  <view class="page">
    <view class="header">
      <view />
      <button class="add-btn" @tap="openAdd">+</button>
    </view>

    <view class="intro">
      <text class="intro-title">家庭健康组</text>
      <text class="intro-sub">共享关键指标，关心更有依据。</text>
    </view>

    <view class="list">
      <view v-if="members.length === 0" class="empty-state">
        <text class="empty-state-icon">👨‍👩‍👧</text>
        <text class="empty-state-title">暂无家庭成员</text>
        <text class="empty-state-desc">点击下方卡片或右上角 + 添加家人</text>
      </view>
      <view v-for="item in members" :key="item.id" class="card" @tap="openEdit(item)">
        <view class="avatar">{{ item.name.slice(0, 1) }}</view>
        <view class="info">
          <text class="name">{{ item.name }}</text>
          <text class="desc">{{ item.age }} 岁 · {{ item.conditionText || "健康" }}</text>
        </view>
        <text class="tag">{{ item.role || "成员" }}</text>
      </view>
      <view class="card dashed" @tap="openAdd">
        <text class="desc">添加新的家庭成员</text>
      </view>
    </view>

    <view v-if="showModal" class="modal-mask" @tap="closeModal">
      <view class="modal-sheet" @tap.stop>
        <view class="modal-sheet-bar" />
        <view class="modal-sheet-head">
          <text class="modal-sheet-title">{{ editingId ? "编辑家人" : "添加家人" }}</text>
          <text class="modal-sheet-close" @tap="closeModal">×</text>
        </view>
        <view class="modal-sheet-body">
        <view class="field">
          <text class="field-label">姓名</text>
          <input class="input" v-model="form.name" placeholder="请输入姓名" />
        </view>
        <view class="field">
          <text class="field-label">年龄</text>
          <input class="input" type="number" v-model="form.age" placeholder="请输入年龄" />
        </view>
        <view class="field">
          <text class="field-label">关系</text>
          <input class="input" v-model="form.relation" placeholder="如 父母/孩子" />
        </view>
        <view class="field">
          <text class="field-label">健康状况/备注</text>
          <input class="input" v-model="form.conditionText" placeholder="如 高血压" />
        </view>
        <button class="modal-sheet-btn primary" @tap="submitAdd" :disabled="saving">
          {{ saving ? "保存中..." : editingId ? "保存修改" : "保存" }}
        </button>
        </view>
      </view>
    </view>

  </view>
</template>

<script>
import { request } from "../../utils/api";

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
    fetchMembers() {
      const userId = uni.getStorageSync("userId") || 1;
      request("/api/family/list", "GET", { userId })
        .then((list) => {
          if (Array.isArray(list) && list.length > 0) {
            this.setMembers(list);
          } else {
            this.setMembers([]);
          }
        })
        .catch(() => {
          this.setMembers([]);
        });
    },
    setMembers(list) {
      if (typeof this.$set === "function") {
        this.$set(this, "members", list);
      } else {
        this.members = list;
      }
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
        status: "已授权"
      };
      const url = this.editingId ? "/api/family/update" : "/api/family/add";
      if (this.editingId) {
        payload.id = this.editingId;
      }
      request(url, "POST", payload)
        .then(() => {
          uni.showToast({ title: "已添加", icon: "success" });
          this.closeModal();
          this.fetchMembers();
        })
        .catch((err) => {
          uni.showToast({ title: err.message || "添加失败", icon: "none" });
        })
        .finally(() => {
          this.saving = false;
        });
    }
  }
};
</script>

<style>
.page {
  padding: 18px;
  min-height: 100vh;
  background: #f4f5f7;
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

.add-btn {
  width: 36px;
  height: 36px;
  border-radius: 18px;
  border: 1px solid #e2e8f0;
  background: #ffffff;
  color: #2563eb;
  font-size: 18px;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.intro {
  background: #0f172a;
  color: #e2e8f0;
  border-radius: 16px;
  padding: 12px;
}

.intro-title {
  font-size: 12px;
  font-weight: 600;
  display: block;
}

.intro-sub {
  font-size: 10px;
  color: #cbd5f5;
  margin-top: 4px;
  display: block;
}

.list {
  display: grid;
  gap: 10px;
}

.empty-state {
  background: #f8fafc;
  border-radius: 16px;
  padding: 32px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  border: 1px dashed #e2e8f0;
}

.empty-state-icon {
  font-size: 40px;
  margin-bottom: 12px;
  opacity: 0.8;
}

.empty-state-title {
  font-size: 15px;
  font-weight: 600;
  color: #475569;
  margin-bottom: 6px;
}

.empty-state-desc {
  font-size: 12px;
  color: #94a3b8;
}

.card {
  background: #ffffff;
  border-radius: 16px;
  padding: 12px;
  border: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.card.dashed {
  border-style: dashed;
  color: #94a3b8;
  justify-content: center;
}

.avatar {
  width: 34px;
  height: 34px;
  border-radius: 17px;
  background: #e2e8f0;
  display: grid;
  place-items: center;
  font-size: 12px;
  font-weight: 600;
}

.info {
  flex: 1;
}

.name {
  font-size: 12px;
  font-weight: 600;
  display: block;
}

.desc {
  font-size: 10px;
  color: #94a3b8;
}

.tag {
  font-size: 10px;
  color: #2563eb;
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
  background: #e2e8f0;
  margin: 10px auto 0;
}

.modal-sheet-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px 16px;
  border-bottom: 1px solid #f1f5f9;
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
  display: flex;
  flex-direction: column;
  gap: 16px;
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
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 12px 14px;
  font-size: 14px;
  color: #0f172a;
  background: #fff;
}

.modal-sheet-btn.primary {
  width: 100%;
  padding: 14px;
  border-radius: 14px;
  background: #2563eb;
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  border: none;
}
</style>
