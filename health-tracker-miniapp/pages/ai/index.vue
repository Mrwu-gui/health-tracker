<template>
  <view class="page">
    <scroll-view
      class="chat-list"
      scroll-y
      :scroll-into-view="scrollIntoView"
      scroll-with-animation
      :style="{ height: listHeight + 'px' }"
    >
      <view v-if="!loading && messages.length === 0" class="empty-state">
        <text class="empty-state-icon">💬</text>
        <text class="empty-state-title">暂无对话记录</text>
        <text class="empty-state-desc">在下方输入问题，开始和 AI 健康助手对话吧</text>
      </view>
      <template v-else>
        <view
          v-for="(item, idx) in messages"
          :key="idx"
          :id="'msg-' + idx"
          class="chat-item"
          :class="{ mine: item.role === 'user' }"
        >
          <view v-if="item.role === 'assistant'" class="avatar assistant">
            <text>AI</text>
          </view>
          <view class="bubble-wrap">
            <view class="bubble">
              <image v-if="item.imageUrl" class="bubble-image" :src="item.imageUrl" mode="aspectFill" />
              <text v-if="item.audioUrl" class="bubble-audio">🎤 语音消息</text>
              <rich-text v-else-if="item.role === 'assistant' && item.content" class="bubble-rich" :nodes="formatContent(item.content)" />
              <text v-else-if="item.content" class="bubble-text">{{ item.content }}</text>
            </view>
          </view>
          <view v-if="item.role === 'user'" class="avatar user">
            <text>我</text>
          </view>
        </view>
        <view v-if="loading" class="chat-item" id="msg-loading">
          <view class="avatar assistant">
            <text>AI</text>
          </view>
          <view class="bubble-wrap">
            <view class="bubble typing">
              <view class="typing-dots">
                <view class="dot"></view>
                <view class="dot"></view>
                <view class="dot"></view>
              </view>
            </view>
          </view>
        </view>
      </template>
    </scroll-view>

    <!-- 加号展开：图片 / 语音 -->
    <view v-if="showPlusMenu" class="plus-menu">
      <view class="plus-menu-inner">
        <view class="plus-option" @tap="onPickImage">
          <view class="plus-option-icon">📷</view>
          <text class="plus-option-label">图片</text>
        </view>
        <view class="plus-option" @tap="onStartVoice">
          <view class="plus-option-icon">🎤</view>
          <text class="plus-option-label">语音</text>
        </view>
      </view>
    </view>

    <view class="composer">
      <view v-if="recording" class="composer-recording" @tap="stopRecord">
        <text class="recording-dot"></text>
        <text class="recording-text">正在录音，点击结束</text>
      </view>
      <template v-else>
        <view class="composer-inner">
          <input
            class="composer-input"
            v-model="input"
            placeholder="输入你的问题..."
            placeholder-class="input-placeholder"
            confirm-type="send"
            :adjust-position="true"
            @confirm="sendMessage"
          />
          <view class="composer-btn-wrap" @tap="onRightBtnTap">
            <text v-if="hasContent" class="composer-send">发送</text>
            <text v-else class="composer-plus">+</text>
          </view>
        </view>
        <view v-if="pendingImage" class="pending-preview">
          <image class="pending-image" :src="pendingImage" mode="aspectFill" />
          <text class="pending-remove" @tap="pendingImage = ''">×</text>
        </view>
        <view v-if="pendingAudio" class="pending-preview">
          <text class="pending-audio">🎤 语音已就绪</text>
          <text class="pending-remove" @tap="pendingAudio = ''">×</text>
        </view>
      </template>
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
      showPlusMenu: false,
      messages: [
        { role: "assistant", content: "你好，我是你的健康助手，可以帮你解读数据或给出建议。" }
      ],
      listHeight: 400,
      scrollIntoView: ""
    };
  },
  computed: {
    hasContent() {
      return (this.input && this.input.trim()) || this.pendingImage || this.pendingAudio;
    }
  },
  onLoad() {
    this.setListHeight();
    this.loadHistory();
  },
  onShow() {
    const pages = getCurrentPages();
    const page = pages[pages.length - 1];
    if (page && typeof page.getTabBar === "function") {
      const tabBar = page.getTabBar();
      if (tabBar && typeof tabBar.setData === "function") tabBar.setData({ selected: 2 });
    }
    this.setListHeight();
    const initial = uni.getStorageSync("aiInitialMessage");
    if (initial && typeof initial === "string" && initial.trim()) {
      uni.removeStorageSync("aiInitialMessage");
      this.sendMessageWithText(initial.trim());
    }
  },
  methods: {
    setListHeight() {
      try {
        const sys = uni.getSystemInfoSync();
        const composerH = 120;
        this.listHeight = (sys.windowHeight || sys.screenHeight || 400) - composerH;
      } catch (e) {
        this.listHeight = 400;
      }
    },
    onRightBtnTap() {
      if (this.hasContent) {
        this.sendMessage();
        return;
      }
      this.showPlusMenu = !this.showPlusMenu;
    },
    onPickImage() {
      this.showPlusMenu = false;
      this.pickImage();
    },
    onStartVoice() {
      this.showPlusMenu = false;
      this.startRecord();
    },
    scrollToBottom() {
      const id = this.loading ? "msg-loading" : "msg-" + (this.messages.length - 1);
      this.scrollIntoView = id;
    },
    async sendMessage() {
      const content = (this.input || "").trim();
      if ((!content && !this.pendingImage && !this.pendingAudio) || this.loading) {
        return;
      }
      this.input = "";
      const imageUrl = this.pendingImage;
      const audioUrl = this.pendingAudio;
      this.pendingImage = "";
      this.pendingAudio = "";
      await this.sendMessageWithText(content, imageUrl, audioUrl);
    },
    async sendMessageWithText(content, imageUrl, audioUrl) {
      if ((!content && !imageUrl && !audioUrl) || this.loading) return;
      this.messages.push({
        role: "user",
        content: content || "",
        imageUrl: imageUrl || "",
        audioUrl: audioUrl || ""
      });
      this.loading = true;
      this.showPlusMenu = false;
      this.$nextTick(() => this.scrollToBottom());
      try {
        const data = await request("/api/ai/chat", "POST", {
          message: content || "请识别图片内容",
          imageUrl: imageUrl || "",
          audioUrl: audioUrl || ""
        });
        const reply = data && data.content ? data.content : "暂时没有回答，请稍后再试。";
        this.messages.push({ role: "assistant", content: reply });
      } catch (err) {
        this.messages.push({ role: "assistant", content: err.message || "请求失败" });
      } finally {
        this.loading = false;
        this.$nextTick(() => this.scrollToBottom());
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
          if (!filePath) return;
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
            let url = data.url;
            if (!url && data.data) {
              url = typeof data.data === "string" ? data.data : data.data.url;
            }
            if (url) {
              if (type === "image") {
                this.pendingImage = url;
              } else {
                this.pendingAudio = url;
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
    },
    formatContent(str) {
      if (!str || typeof str !== "string") return "";
      return str
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/\*\*(.+?)\*\*/g, "<b style='font-weight:600'>$1</b>")
        .replace(/\n/g, "<br/>");
    }
  }
};
</script>

<style>
.page {
  min-height: 100vh;
  background: #f1f5f9;
  display: flex;
  flex-direction: column;
  padding-bottom: env(safe-area-inset-bottom);
}

.chat-list {
  flex: 1;
  padding: 16px 16px 20px;
  box-sizing: border-box;
  background: #f1f5f9;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 24px;
  text-align: center;
}

.empty-state-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.7;
}

.empty-state-title {
  font-size: 16px;
  font-weight: 600;
  color: #475569;
  margin-bottom: 8px;
}

.empty-state-desc {
  font-size: 13px;
  color: #94a3b8;
  line-height: 1.5;
}

.chat-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 16px;
}

.chat-item.mine {
  flex-direction: row-reverse;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 18px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 600;
  color: #fff;
}

.avatar.assistant {
  background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
}

.avatar.user {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
}

.bubble-wrap {
  max-width: 76%;
  min-height: 36px;
}

.bubble {
  padding: 12px 14px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.5;
}

.chat-item:not(.mine) .bubble {
  background: #ffffff;
  color: #334155;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

.chat-item.mine .bubble {
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  color: #ffffff;
  border-bottom-right-radius: 4px;
  box-shadow: 0 2px 8px rgba(37, 99, 235, 0.25);
}

.bubble-text {
  display: block;
  word-break: break-word;
  white-space: pre-wrap;
}

.bubble-rich {
  display: block;
  word-break: break-word;
  font-size: 14px;
  line-height: 1.6;
  color: inherit;
}

.bubble-rich b {
  font-weight: 600;
}

.bubble-image {
  width: 180px;
  height: 180px;
  border-radius: 12px;
  display: block;
  margin-bottom: 6px;
}

.bubble-audio {
  display: block;
  font-size: 13px;
}

.bubble.typing {
  padding: 14px 18px;
}

.typing-dots {
  display: flex;
  align-items: center;
  gap: 5px;
}

.typing-dots .dot {
  width: 6px;
  height: 6px;
  border-radius: 3px;
  background: #94a3b8;
  animation: typing 0.6s ease-in-out infinite;
}

.typing-dots .dot:nth-child(2) { animation-delay: 0.1s; }
.typing-dots .dot:nth-child(3) { animation-delay: 0.2s; }

@keyframes typing {
  0%, 60% { transform: translateY(0); opacity: 0.5; }
  30% { transform: translateY(-4px); opacity: 1; }
}

/* 加号展开菜单 */
.plus-menu {
  background: #ffffff;
  border-top: 1px solid #e2e8f0;
  padding: 12px 16px;
  flex-shrink: 0;
}

.plus-menu-inner {
  display: flex;
  gap: 24px;
  justify-content: flex-start;
}

.plus-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.plus-option-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.plus-option-label {
  font-size: 12px;
  color: #64748b;
}

/* 底部输入栏 */
.composer {
  background: #ffffff;
  border-top: 1px solid #e2e8f0;
  padding: 10px 16px 16px;
  padding-bottom: calc(16px + env(safe-area-inset-bottom));
  flex-shrink: 0;
}

.composer-inner {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #f1f5f9;
  border-radius: 22px;
  padding: 6px 6px 6px 16px;
  min-height: 44px;
}

.composer-input {
  flex: 1;
  font-size: 15px;
  color: #0f172a;
  min-height: 32px;
}

.input-placeholder {
  color: #94a3b8;
}

.composer-btn-wrap {
  width: 40px;
  height: 40px;
  border-radius: 20px;
  background: #2563eb;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.composer-plus {
  font-size: 24px;
  font-weight: 300;
  line-height: 1;
}

.composer-send {
  font-size: 14px;
  font-weight: 600;
}

.pending-preview {
  margin-top: 10px;
  padding: 8px 12px;
  background: #f1f5f9;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.pending-image {
  width: 48px;
  height: 48px;
  border-radius: 8px;
}

.pending-audio {
  font-size: 13px;
  color: #64748b;
}

.pending-remove {
  margin-left: auto;
  font-size: 18px;
  color: #94a3b8;
  padding: 0 4px;
}

.composer-recording {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  height: 48px;
  background: #fef2f2;
  border-radius: 22px;
}

.recording-dot {
  width: 8px;
  height: 8px;
  border-radius: 4px;
  background: #ef4444;
  animation: pulse 0.8s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.6; transform: scale(1.1); }
}

.recording-text {
  font-size: 14px;
  color: #b91c1c;
}
</style>
