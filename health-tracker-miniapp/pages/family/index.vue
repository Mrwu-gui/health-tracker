<template>
  <view class="page">
    <view class="header">
      <text class="title">家庭成员</text>
      <button class="btn-dark" @tap="openAdd">邀请家人</button>
    </view>

    <view class="intro">
      <text class="intro-title">家庭健康组</text>
      <text class="intro-sub">共享关键指标，关心更有依据。</text>
    </view>

    <view class="list">
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
      <view class="modal" @tap.stop>
        <text class="modal-title">{{ editingId ? "编辑家人" : "添加家人" }}</text>
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
        <button class="primary" @tap="submitAdd" :disabled="saving">
          {{ saving ? "保存中..." : editingId ? "保存修改" : "保存" }}
        </button>
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
  onLoad() {
    this.useDefaultMembers();
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
            this.useDefaultMembers();
          }
        })
        .catch(() => {
          this.useDefaultMembers();
        });
    },
    setMembers(list) {
      if (typeof this.$set === "function") {
        this.$set(this, "members", list);
      } else {
        this.members = list;
      }
    },
    useDefaultMembers() {
      this.setMembers([
        { id: 1, name: "李阿姨", age: 68, conditionText: "高血压", role: "已授权" },
        { id: 2, name: "张先生", age: 35, conditionText: "可查看周报", role: "管理员" }
      ]);
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

.btn-dark {
  background: #0f172a;
  color: #ffffff;
  border-radius: 12px;
  font-size: 11px;
  padding: 6px 12px;
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
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  padding: 20px;
}

.modal {
  width: 100%;
  max-width: 320px;
  background: #fff;
  border-radius: 16px;
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.modal-title {
  font-size: 15px;
  font-weight: 600;
  color: #0f172a;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field-label {
  font-size: 12px;
  color: #64748b;
}

.input {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 10px 12px;
  font-size: 14px;
  color: #0f172a;
  background: #fff;
}

.primary {
  width: 100%;
  padding: 12px 0;
  border-radius: 12px;
  background: #0f172a;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
}
</style>
