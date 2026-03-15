<template>
  <view class="page">
    <view class="header">
      <text class="title">AI 健康助手</text>
      <text class="sub">随时咨询你的健康问题</text>
    </view>

    <view class="chat-list">
      <view v-for="(item, idx) in messages" :key="idx" class="chat-item" :class="{ mine: item.role === 'user' }">
        <view class="bubble">
          <image v-if="item.imageUrl" class="bubble-image" :src="item.imageUrl" mode="aspectFill" />
          <text v-if="item.audioUrl" class="bubble-audio">语音消息</text>
          <text v-if="item.content" class="bubble-text">{{ item.content }}</text>
        </view>
      </view>
      <view v-if="loading" class="chat-item">
        <view class="bubble">
          <text class="bubble-text">正在思考中...</text>
        </view>
      </view>
    </view>

    <view class="composer">
      <button class="icon-btn" @tap="pickImage" :disabled="loading">图片</button>
      <button class="icon-btn" @touchstart="startRecord" @touchend="stopRecord" :disabled="loading">
        {{ recording ? "松开结束" : "按住说话" }}
      </button>
      <input
        class="composer-input"
        v-model="input"
        placeholder="输入你的问题..."
        confirm-type="send"
        @confirm="sendMessage"
      />
      <button class="composer-btn" @tap="sendMessage" :disabled="loading || (!input && !pendingImage && !pendingAudio)">
        发送
      </button>
    </view>
  </view>
</template>

<script>
import { API_BASE_URL, request } from "../../utils/api";

export default {
  data() {
    return {
      input: "",
      loading: false,
      recording: false,
      pendingImage: "",
      pendingAudio: "",
      messages: [
        { role: "assistant", content: "你好，我是你的健康助手，可以帮你解读数据或给出建议。" }
      ]
    };
  },
  onLoad() {
    this.loadHistory();
  },
  methods: {
    async sendMessage() {
      const content = (this.input || "").trim();
      if ((!content && !this.pendingImage && !this.pendingAudio) || this.loading) {
        return;
      }
      this.messages.push({
        role: "user",
        content,
        imageUrl: this.pendingImage,
        audioUrl: this.pendingAudio
      });
      this.input = "";
      const imageUrl = this.pendingImage;
      const audioUrl = this.pendingAudio;
      this.pendingImage = "";
      this.pendingAudio = "";
      this.loading = true;
      try {
        const data = await request("/api/ai/chat", "POST", {
          message: content || "请识别图片内容",
          imageUrl,
          audioUrl
        });
        const reply = data && data.content ? data.content : "暂时没有回答，请稍后再试。";
        this.messages.push({ role: "assistant", content: reply });
      } catch (err) {
        this.messages.push({ role: "assistant", content: err.message || "请求失败" });
      } finally {
        this.loading = false;
      }
    },
    loadHistory() {
      const userId = uni.getStorageSync("userId") || 1;
      request("/api/ai/history", "GET", { userId })
        .then((list) => {
          if (Array.isArray(list) && list.length > 0) {
            this.messages = list.map((item) => ({
              role: item.role,
              content: item.contentText || ""
            }));
          }
        })
        .catch(() => {});
    },
    pickImage() {
      uni.chooseImage({
        count: 1,
        success: (res) => {
          const filePath = res.tempFilePaths[0];
          if (!filePath) {
            return;
          }
          this.uploadFile(filePath, "image");
        }
      });
    },
    startRecord() {
      if (this.recording) return;
      this.recording = true;
      const manager = uni.getRecorderManager();
      manager.start({ format: "mp3", duration: 60000 });
      manager.onStop((res) => {
        this.recording = false;
        if (res && res.tempFilePath) {
          this.uploadFile(res.tempFilePath, "audio");
        }
      });
    },
    stopRecord() {
      if (!this.recording) return;
      const manager = uni.getRecorderManager();
      manager.stop();
    },
    uploadFile(filePath, type) {
      this.loading = true;
      const token = uni.getStorageSync("token");
      uni.uploadFile({
        url: `${API_BASE_URL}/api/ai/upload`,
        filePath,
        name: "file",
        header: {
          Authorization: token ? `Bearer ${token}` : ""
        },
        success: (res) => {
          try {
            const data = JSON.parse(res.data || "{}");
            if (data.url) {
              if (type === "image") {
                this.pendingImage = data.url;
              } else {
                this.pendingAudio = data.url;
              }
            }
          } catch (err) {
            uni.showToast({ title: "上传失败", icon: "none" });
          }
        },
        fail: () => {
          uni.showToast({ title: "上传失败", icon: "none" });
        },
        complete: () => {
          this.loading = false;
        }
      });
    }
  }
};
</script>

<style>
.page {
  min-height: 100vh;
  background: #f4f5f7;
  display: flex;
  flex-direction: column;
}

.header {
  padding: 18px;
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: #0f172a;
  display: block;
}

.sub {
  font-size: 11px;
  color: #94a3b8;
  display: block;
  margin-top: 4px;
}

.chat-list {
  flex: 1;
  padding: 0 18px 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.chat-item {
  display: flex;
  justify-content: flex-start;
}

.chat-item.mine {
  justify-content: flex-end;
}

.bubble {
  max-width: 70%;
  background: #ffffff;
  border-radius: 16px;
  padding: 10px 12px;
  border: 1px solid #e2e8f0;
}

.chat-item.mine .bubble {
  background: #2563eb;
  border-color: #2563eb;
}

.bubble-text {
  font-size: 12px;
  color: #0f172a;
}

.bubble-image {
  width: 180px;
  height: 180px;
  border-radius: 12px;
  margin-bottom: 6px;
}

.bubble-audio {
  font-size: 12px;
  color: #0f172a;
  display: block;
  margin-bottom: 6px;
}

.chat-item.mine .bubble-text {
  color: #ffffff;
}

.composer {
  padding: 12px 18px 20px;
  display: flex;
  gap: 10px;
  align-items: center;
  background: #f4f5f7;
  border-top: 1px solid #e2e8f0;
}

.icon-btn {
  padding: 8px 10px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  font-size: 11px;
  color: #0f172a;
}

.composer-input {
  flex: 1;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 10px 12px;
  font-size: 12px;
}

.composer-btn {
  background: #0f172a;
  color: #ffffff;
  border-radius: 14px;
  padding: 10px 16px;
  font-size: 12px;
}
</style>
