<template>
  <view class="page">
    <scroll-view
      class="chat-list"
      scroll-y
      :show-scrollbar="false"
      :scroll-into-view="scrollIntoView"
      scroll-with-animation
      :style="{ height: listHeight + 'px' }"
    >
      <view class="chat-list-inner">
        <view
          v-for="(item, idx) in messages"
          :key="idx"
          :id="'msg-' + idx"
          class="chat-item"
          :class="{ mine: item.role === 'user' }"
        >
          <view v-if="item.role === 'assistant'" class="avatar assistant">
            <text>智康</text>
          </view>
          <view class="bubble-wrap">
            <view class="bubble">
              <image v-if="item.role === 'user' && item.imageUrl" class="bubble-img" :src="item.imageUrl" mode="widthFix" />
              <text v-if="item.role === 'user' && item.audioUrl" class="bubble-audio-tag">🎤 语音</text>
              <rich-text v-if="item.role === 'assistant' && item.content" class="bubble-rich" :nodes="formatContent(item.content)" />
              <text v-else-if="item.content" class="bubble-text">{{ item.content }}</text>
            </view>
          </view>
          <view v-if="item.role === 'user'" class="avatar user">
            <image v-if="userAvatar" class="avatar-img" :src="userAvatar" mode="aspectFill" />
            <text v-else class="avatar-user-txt">{{ userAvatarLetter }}</text>
          </view>
        </view>

        <view v-if="loading" class="chat-item chat-item-thinking" id="msg-loading">
          <view class="avatar assistant">
            <text>智康</text>
          </view>
          <view class="bubble-wrap">
            <view class="bubble bubble-thinking">
              <view class="thinking-wrap">
                <view class="thinking-dots">
                  <view class="thinking-dot"></view>
                  <view class="thinking-dot"></view>
                  <view class="thinking-dot"></view>
                </view>
                <text class="thinking-text">思考中</text>
              </view>
            </view>
          </view>
        </view>

        <view v-if="showRecommendations" class="recommend-section">
          <text class="recommend-title">试试问我</text>
          <view class="recommend-list">
            <view
              v-for="(q, i) in suggestedQuestions"
              :key="i"
              class="recommend-chip"
              hover-class="recommend-chip-hover"
              @tap="onRecommendTap(q)"
            >
              <text class="recommend-chip-text">{{ q }}</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <view class="composer">
      <view class="composer-inner">
        <input
          class="composer-input"
          v-model="input"
          placeholder="输入你的问题..."
          placeholder-class="input-placeholder"
          confirm-type="send"
          :adjust-position="true"
          :disabled="recording"
          @confirm="sendMessage"
        />
        <view class="composer-plus" @tap="expandComposer = !expandComposer">
          <text class="composer-plus-icon">{{ expandComposer ? '−' : '+' }}</text>
        </view>
        <view class="composer-btn-wrap" @tap="sendMessage">
          <text class="composer-send">发送</text>
        </view>
      </view>
      <view v-if="expandComposer" class="composer-expand">
        <view class="composer-expand-item" @tap="onChooseImage">
          <text class="composer-expand-icon">📷</text>
          <text class="composer-expand-label">相册</text>
        </view>
        <view class="composer-expand-item" @tap="toggleRecord">
          <text class="composer-expand-icon">🎤</text>
          <text class="composer-expand-label">语音</text>
        </view>
      </view>
      <text v-if="recording" class="composer-recording-tip">录音中… 再次点击语音结束</text>
    </view>
  </view>
</template>

<script>
import { request } from "../../utils/api";

const RECOMMEND_QUESTIONS = [
  "经期可以运动吗？",
  "睡眠不好怎么调理？",
  "每天走多少步比较合适？",
  "经期饮食要注意什么？",
  "怎样养成早睡习惯？",
  "久坐如何缓解肩颈不适？",
  "经期周期多少天算正常？",
  "午睡睡多久合适？",
  "减肥期间怎么吃更健康？",
  "血压偏高日常要注意什么？",
  "感冒了可以运动吗？",
  "睡前喝牛奶助眠吗？"
];

function pickRandom(arr, n) {
  const copy = arr.slice();
  for (let i = copy.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [copy[i], copy[j]] = [copy[j], copy[i]];
  }
  return copy.slice(0, n);
}

export default {
  data() {
    return {
      input: "",
      loading: false,
      messages: [
        { role: "assistant", content: "你好，我是智康，有健康问题可以问我。" }
      ],
      listHeight: 400,
      scrollIntoView: "",
      recording: false,
      recorderManager: null,
      expandComposer: false,
      suggestedQuestions: [],
      userAvatar: "",
      userName: ""
    };
  },
  computed: {
    hasContent() {
      return this.input && this.input.trim();
    },
    userAvatarLetter() {
      return this.userName ? this.userName.slice(0, 1) : "我";
    },
    showRecommendations() {
      return !this.loading && this.messages.length <= 1 && this.suggestedQuestions.length > 0;
    }
  },
  watch: {
    expandComposer() {
      this.$nextTick(() => this.setListHeight());
    }
  },
  onLoad() {
    this.setListHeight();
    this.suggestedQuestions = pickRandom(RECOMMEND_QUESTIONS, 3);
    this.loadHistory();
    this.recorderManager = uni.getRecorderManager?.() || null;
  },
  onShow() {
    const pages = getCurrentPages();
    const page = pages[pages.length - 1];
    if (page && typeof page.getTabBar === "function") {
      const tabBar = page.getTabBar();
      if (tabBar && typeof tabBar.setData === "function") tabBar.setData({ selected: 2 });
    }
    this.userAvatar = uni.getStorageSync("userAvatar") || "";
    this.userName = uni.getStorageSync("userName") || "";
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
        const winH = sys.windowHeight || sys.screenHeight || 400;
        const tabBarH = 50;
        const composerH = this.expandComposer ? 100 : 54;
        this.listHeight = Math.max(300, winH - tabBarH - composerH);
      } catch (e) {
        this.listHeight = 400;
      }
    },
    onRightBtnTap() {
      this.sendMessage();
    },
    scrollToBottom() {
      const id = this.loading ? "msg-loading" : "msg-" + (this.messages.length - 1);
      this.scrollIntoView = id;
    },
    async sendMessage() {
      const content = (this.input || "").trim();
      if (this.loading) return;
      if (!content) return;
      this.input = "";
      await this.sendMessageWithText(content, "", "");
    },
    onChooseImage() {
      if (this.loading) return;
      uni.chooseImage({
        count: 1,
        sizeType: ["compressed"],
        sourceType: ["album", "camera"],
        success: (res) => {
          const path = res.tempFilePaths && res.tempFilePaths[0];
          if (path) this.sendMessageWithText(this.input.trim(), path, "");
        }
      });
    },
    toggleRecord() {
      if (this.loading || !this.recorderManager) {
        if (!this.recorderManager) uni.showToast({ title: "当前环境不支持语音", icon: "none" });
        return;
      }
      if (this.recording) {
        this.recorderManager.stop();
        return;
      }
      this.recording = true;
      this.recorderManager.start({ duration: 60000, sampleRate: 16000, format: "mp3" });
      this.recorderManager.onStop((res) => {
        this.recording = false;
        const path = res.tempFilePath;
        if (path) this.sendMessageWithText(this.input.trim(), "", path);
      });
      this.recorderManager.onError(() => {
        this.recording = false;
        uni.showToast({ title: "录音失败", icon: "none" });
      });
    },
    async getPeriodSummary() {
      const STORAGE_KEY = "periodRecords";
      let list = [];
      try {
        const data = await request("/api/period/list", "GET", { userId: uni.getStorageSync("userId") || 1 });
        if (Array.isArray(data)) list = data;
      } catch (e) {
        try {
          const raw = uni.getStorageSync(STORAGE_KEY);
          if (raw) list = JSON.parse(raw);
        } catch (_) {}
      }
      if (list.length === 0) return "";
      const sorted = list
        .map((item) => ({ start: item.startDate || item.start_date }))
        .filter((item) => item.start)
        .sort((a, b) => (b.start || "").localeCompare(a.start || ""));
      const last = sorted[0];
      if (!last) return "";
      const lastStr = last.start;
      const d = new Date(lastStr.replace(/-/g, "/"));
      d.setDate(d.getDate() + 28);
      const nextStr = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
      return `最近经期 ${lastStr}，预计下次 ${nextStr}`;
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
      this.$nextTick(() => this.scrollToBottom());
      try {
        const periodCtx = await this.getPeriodSummary();
        const messageToSend = periodCtx
          ? `【参考：${periodCtx}】\n\n${content || "请识别图片内容"}`
          : (content || "请识别图片内容");
        const userId = uni.getStorageSync("userId") || 1;
        const data = await request("/api/ai/chat", "POST", {
          userId,
          message: messageToSend,
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
          } else {
            this.suggestedQuestions = pickRandom(RECOMMEND_QUESTIONS, 3);
          }
        })
        .catch(() => {
          this.suggestedQuestions = pickRandom(RECOMMEND_QUESTIONS, 3);
        });
    },
    onRecommendTap(q) {
      if (this.loading || !q) return;
      this.sendMessageWithText(String(q).trim(), "", "");
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
  width: 100%;
  box-sizing: border-box;
  background: #faf8f5;
  display: flex;
  flex-direction: column;
  padding-bottom: 0;
  overflow: hidden;
}

.chat-list {
  flex: 1;
  width: 100%;
  box-sizing: border-box;
  padding: 16px 16px 20px;
  background: #faf8f5;
  overflow: hidden;
}

.chat-list-inner {
  display: block;
  width: 100%;
  box-sizing: border-box;
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
  background: linear-gradient(145deg, #f59e0b 0%, #ea580c 100%);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.35);
}

.avatar.user {
  background: #e2e8f0;
  overflow: hidden;
}

.avatar.user .avatar-img {
  width: 100%;
  height: 100%;
  display: block;
}

.avatar-user-txt {
  color: #64748b;
  font-size: 14px;
  font-weight: 600;
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


.chat-item-thinking .bubble-wrap {
  min-height: 48px;
}

.bubble-thinking {
  padding: 14px 18px;
}

.thinking-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
}

.thinking-dots {
  display: flex;
  align-items: center;
  gap: 4px;
}

.thinking-dot {
  width: 6px;
  height: 6px;
  border-radius: 3px;
  background: #f59e0b;
  animation: thinking-bounce 0.6s ease-in-out infinite;
}

.thinking-dot:nth-child(2) { animation-delay: 0.12s; }
.thinking-dot:nth-child(3) { animation-delay: 0.24s; }

@keyframes thinking-bounce {
  0%, 60% { transform: translateY(0) scale(1); opacity: 0.6; }
  30% { transform: translateY(-5px) scale(1.1); opacity: 1; }
}

.thinking-text {
  font-size: 13px;
  color: #64748b;
}

.recommend-section {
  margin-top: 24px;
  padding: 16px 0;
}

.recommend-title {
  font-size: 12px;
  color: #94a3b8;
  display: block;
  margin-bottom: 12px;
  padding-left: 2px;
}

.recommend-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.recommend-chip {
  padding: 10px 14px;
  background: #fff;
  border-radius: 20px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}

.recommend-chip-hover {
  background: #f8fafc;
  border-color: #cbd5e1;
}

.recommend-chip-text {
  font-size: 13px;
  color: #475569;
}


.composer {
  background: #ffffff;
  border-top: 1px solid #e8e2db;
  padding: 6px 16px;
  padding-bottom: calc(6px + env(safe-area-inset-bottom));
  flex-shrink: 0;
}

.composer-inner {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #f5f1eb;
  border-radius: 22px;
  padding: 6px 6px 6px 14px;
  min-height: 44px;
}

.composer-plus {
  width: 32px;
  height: 32px;
  border-radius: 16px;
  background: rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.composer-plus-icon {
  font-size: 20px;
  font-weight: 300;
  color: #64748b;
  line-height: 1;
}

.composer-expand {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 10px 0 4px;
  border-top: 1px solid #f2ede8;
  margin-top: 6px;
}

.composer-expand-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: #f5f1eb;
  border-radius: 12px;
}

.composer-expand-icon {
  font-size: 18px;
}

.composer-expand-label {
  font-size: 13px;
  color: #475569;
}

.composer-recording-tip {
  display: block;
  font-size: 11px;
  color: #64748b;
  text-align: center;
  margin-top: 4px;
}

.bubble-img {
  max-width: 200px;
  border-radius: 8px;
  display: block;
  margin-bottom: 6px;
}

.bubble-audio-tag {
  font-size: 13px;
  opacity: 0.9;
  display: block;
  margin-bottom: 4px;
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

.composer-send {
  font-size: 14px;
  font-weight: 600;
}
</style>
