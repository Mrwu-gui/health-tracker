<template>
  <view class="page">

    <!-- 消息列表 -->
    <scroll-view
      class="chat-list"
      scroll-y
      :show-scrollbar="false"
      :scroll-into-view="scrollIntoView"
      scroll-with-animation
    >
      <view class="chat-list-inner">

        <!-- 推荐问题（初始状态） -->
        <view v-if="showRecommendations" class="recommend-section">
          <view class="recommend-top">
            <view class="recommend-logo">
              <text class="recommend-logo-text">✨</text>
            </view>
            <text class="recommend-hello">你好，我是智康 AI</text>
            <text class="recommend-sub">{{ recommendSubtitle }}</text>
          </view>
          <text class="recommend-title">大家都在问</text>
          <view class="recommend-list">
            <view
              v-for="(q, i) in suggestedQuestions"
              :key="i"
              class="recommend-chip"
              @tap="onRecommendTap(q)"
            >
              <text class="recommend-chip-num">{{ i + 1 }}</text>
              <text class="recommend-chip-text">{{ q }}</text>
              <text class="recommend-chip-arrow">›</text>
            </view>
          </view>
        </view>

        <!-- 消息气泡 -->
        <view
          v-for="(item, idx) in messages"
          :key="idx"
          :id="'msg-' + idx"
          class="chat-item"
          :class="{ mine: isUserRole(item.role) }"
        >
          <view v-if="isAssistantRole(item.role)" class="avatar assistant">
            <text class="avatar-ai-text">智</text>
          </view>
          <view class="bubble-wrap">
            <view class="bubble" :class="isUserRole(item.role) ? 'bubble-user' : 'bubble-ai'">
              <image v-if="isUserRole(item.role) && item.imageUrl" class="bubble-img" :src="item.imageUrl" mode="widthFix" />
              <text v-if="isUserRole(item.role) && item.audioUrl" class="bubble-audio-tag">🎤 语音消息</text>
              <rich-text v-if="isAssistantRole(item.role) && item.content" class="bubble-rich" :nodes="formatContent(item.content)" />
              <text v-else-if="item.content" class="bubble-text">{{ item.content }}</text>
            </view>
          </view>
          <view v-if="isUserRole(item.role)" class="avatar user">
            <image v-if="userAvatar" class="avatar-img" :src="userAvatar" mode="aspectFill" />
            <text v-else class="avatar-user-txt">{{ userAvatarLetter }}</text>
          </view>
        </view>

        <!-- 思考中 -->
        <view v-if="loading" class="chat-item" id="msg-loading">
          <view class="avatar assistant">
            <text class="avatar-ai-text">智</text>
          </view>
          <view class="bubble-wrap">
            <view class="bubble bubble-ai bubble-thinking">
              <view class="thinking-dots">
                <view class="thinking-dot"></view>
                <view class="thinking-dot"></view>
                <view class="thinking-dot"></view>
              </view>
            </view>
          </view>
        </view>

      </view>
    </scroll-view>

    <!-- 输入区 -->
    <view class="composer">
      <!-- 展开的菜单 -->
      <view v-if="extraMenuOpen" class="extra-menu">
        <view class="extra-menu-item" @tap="onChooseAlbum">
          <view class="extra-menu-icon extra-menu-icon-album">
            <image class="extra-menu-icon-img" src="/static/tabbar/xiangce.png" mode="aspectFit" />
          </view>
          <text class="extra-menu-label">相册</text>
        </view>
        <view class="extra-menu-item" @tap="onTakePhoto">
          <view class="extra-menu-icon extra-menu-icon-camera">
            <text class="extra-menu-icon-text">📷</text>
          </view>
          <text class="extra-menu-label">拍照</text>
        </view>
        <view class="extra-menu-item" @tap="onVoiceRecord">
          <view class="extra-menu-icon extra-menu-icon-voice">
            <image class="extra-menu-icon-img" :class="{ recording: recording }" src="/static/tabbar/yuyin.png" mode="aspectFit" />
          </view>
          <text class="extra-menu-label">语音</text>
        </view>
        <view class="extra-menu-item" @tap="onChooseFile">
          <view class="extra-menu-icon extra-menu-icon-file">
            <text class="extra-menu-icon-text">📁</text>
          </view>
          <text class="extra-menu-label">文件</text>
        </view>
      </view>

      <view class="composer-inner">
        <input
          class="composer-input"
          v-model="input"
          :placeholder="recording ? '录音中…' : '问问智康 AI…'"
          placeholder-class="input-placeholder"
          confirm-type="send"
          :adjust-position="true"
          :disabled="recording"
          @confirm="sendMessage"
        />
        <view class="composer-plus-btn" :class="{ 'composer-plus-open': extraMenuOpen }" @tap="toggleExtraMenu">
          <text class="composer-plus-icon">{{ extraMenuOpen ? '✕' : '+' }}</text>
        </view>
        <view
          class="composer-send-btn"
          :class="{ 'composer-send-active': hasContent }"
          @tap="sendMessage"
        >
          <text class="composer-send-icon">↑</text>
        </view>
        <view v-if="messages.length > 0" class="composer-clear-btn" @tap="clearHistory">
          <text class="composer-clear-icon">🗑</text>
        </view>
      </view>
      <text v-if="recording" class="recording-tip">🔴 录音中，点击麦克风结束</text>
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

const PERIOD_KEYWORDS = ["经期", "月经"];

function filterQuestions(arr, isMale) {
  if (!isMale) return arr;
  return arr.filter(q => !PERIOD_KEYWORDS.some(k => q.includes(k)));
}

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
      messages: [],
      listHeight: 400,
      scrollIntoView: "",
      recording: false,
      recorderManager: null,
      suggestedQuestions: [],
      userAvatar: "",
      userName: "",
      userSex: "",
      extraMenuOpen: false
    };
  },
  computed: {
    hasContent() {
      return this.input && this.input.trim();
    },
    showPeriod() {
      const sex = this.userSex || uni.getStorageSync("userSex") || "";
      return String(sex).includes("女");
    },
    userAvatarLetter() {
      return this.userName ? this.userName.slice(0, 1) : "我";
    },
    showRecommendations() {
      return !this.loading && this.messages.length === 0 && this.suggestedQuestions.length > 0;
    },
    recommendSubtitle() {
      return this.showPeriod
        ? "健康问题、饮食运动、睡眠经期，都可以问我"
        : "健康问题、饮食运动、睡眠，都可以问我";
    }
  },
  onLoad() {
    this.setListHeight();
    this.userSex = uni.getStorageSync("userSex") || "";
    this.suggestedQuestions = pickRandom(filterQuestions(RECOMMEND_QUESTIONS, !this.showPeriod), 4);
    this.loadHistory();
    this.recorderManager = (uni.getRecorderManager && typeof uni.getRecorderManager === "function")
      ? uni.getRecorderManager()
      : null;
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
    this.userSex = uni.getStorageSync("userSex") || "";
    this.setListHeight();
    const initial = uni.getStorageSync("aiInitialMessage");
    if (initial && typeof initial === "string" && initial.trim()) {
      uni.removeStorageSync("aiInitialMessage");
      this.sendMessageWithText(initial.trim());
    }
  },
  methods: {
    normalizeRole(role) {
      const r = String(role || "").toLowerCase().trim();
      if (r === "assistant" || r === "ai" || r === "bot") return "assistant";
      if (r === "user" || r === "me") return "user";
      return r === "system" ? "assistant" : "user";
    },
    isUserRole(role) {
      return this.normalizeRole(role) === "user";
    },
    isAssistantRole(role) {
      return this.normalizeRole(role) === "assistant";
    },
    cleanUserContent(text) {
      if (!text) return "";
      if (text.startsWith("【参考：")) {
        const idx = text.indexOf("】");
        if (idx !== -1) {
          const rest = text.slice(idx + 1).replace(/^\s*\n+/, "");
          return rest.trim();
        }
      }
      return text;
    },
    setListHeight() {
      try {
        const sys = uni.getSystemInfoSync();
        const winH = sys.windowHeight || sys.screenHeight || 400;
        const tabBarH = 56 + (sys.safeAreaInsets ? sys.safeAreaInsets.bottom : 0);
        const composerH = 64 + (sys.safeAreaInsets ? sys.safeAreaInsets.bottom : 0);
        this.listHeight = Math.max(300, winH - tabBarH - composerH);
      } catch (e) {
        this.listHeight = 400;
      }
    },
    scrollToBottom() {
      const id = this.loading ? "msg-loading" : "msg-" + (this.messages.length - 1);
      this.scrollIntoView = id;
    },
    async sendMessage() {
      const content = (this.input || "").trim();
      if (this.loading || !content) return;
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
    toggleExtraMenu() {
      this.extraMenuOpen = !this.extraMenuOpen;
    },
    onChooseAlbum() {
      this.extraMenuOpen = false;
      if (this.loading) return;
      uni.chooseImage({
        count: 1,
        sizeType: ["compressed"],
        sourceType: ["album"],
        success: (res) => {
          const path = res.tempFilePaths && res.tempFilePaths[0];
          if (path) this.sendMessageWithText(this.input.trim(), path, "");
        }
      });
    },
    onTakePhoto() {
      this.extraMenuOpen = false;
      if (this.loading) return;
      uni.chooseImage({
        count: 1,
        sizeType: ["compressed"],
        sourceType: ["camera"],
        success: (res) => {
          const path = res.tempFilePaths && res.tempFilePaths[0];
          if (path) this.sendMessageWithText(this.input.trim(), path, "");
        }
      });
    },
    onVoiceRecord() {
      this.extraMenuOpen = false;
      this.toggleRecord();
    },
    onChooseFile() {
      this.extraMenuOpen = false;
      if (this.loading) return;
      // #ifdef MP-WEIXIN
      uni.chooseMessageFile({
        count: 1,
        type: "file",
        success: (res) => {
          const file = res.tempFiles && res.tempFiles[0];
          if (file && file.path) {
            uni.showToast({ title: "文件已选择: " + file.name, icon: "none" });
            // 可根据后端支持情况上传文件
          }
        },
        fail: () => {
          uni.showToast({ title: "选择文件失败", icon: "none" });
        }
      });
      // #endif
      // #ifndef MP-WEIXIN
      uni.showToast({ title: "请在微信小程序内使用", icon: "none" });
      // #endif
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
        if (res.tempFilePath) this.sendMessageWithText(this.input.trim(), "", res.tempFilePath);
      });
      this.recorderManager.onError(() => {
        this.recording = false;
        uni.showToast({ title: "录音失败", icon: "none" });
      });
    },
    async getPeriodSummary() {
      if (!this.showPeriod) return "";
      let list = [];
      try {
        const data = await request("/api/period/list", "GET", { userId: uni.getStorageSync("userId") || 1 });
        if (Array.isArray(data)) list = data;
      } catch (e) {
        try {
          const raw = uni.getStorageSync("periodRecords");
          if (raw) list = JSON.parse(raw);
        } catch (_) {}
      }
      if (list.length === 0) return "";
      const sorted = list
        .map(item => ({ start: item.startDate || item.start_date }))
        .filter(item => item.start)
        .sort((a, b) => (b.start || "").localeCompare(a.start || ""));
      const last = sorted[0];
      if (!last) return "";
      const d = new Date(last.start.replace(/-/g, "/"));
      d.setDate(d.getDate() + 28);
      const nextStr = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
      return `最近经期 ${last.start}，预计下次 ${nextStr}`;
    },
    async sendMessageWithText(content, imageUrl, audioUrl) {
      if ((!content && !imageUrl && !audioUrl) || this.loading) return;
      this.messages.push({ role: "user", content: content || "", imageUrl: imageUrl || "", audioUrl: audioUrl || "" });
      this.loading = true;
      this.$nextTick(() => this.scrollToBottom());
      try {
        const messageToSend = content || "请识别图片内容";
        const userId = uni.getStorageSync("userId") || 1;
        const data = await request("/api/ai/chat", "POST", {
          userId, message: messageToSend, imageUrl: imageUrl || "", audioUrl: audioUrl || ""
        });
        const reply = data && data.content ? data.content : "暂时没有回答，请稍后再试。";
        this.messages.push({ role: "assistant", content: reply });
      } catch (err) {
        this.messages.push({ role: "assistant", content: err.message || "请求失败，请稍后重试" });
      } finally {
        this.loading = false;
        this.$nextTick(() => this.scrollToBottom());
      }
    },
    loadHistory() {
      const userId = uni.getStorageSync("userId") || 1;
      request("/api/ai/history", "GET", { userId })
        .then(list => {
          if (Array.isArray(list) && list.length > 0) {
            this.messages = list.map(item => {
              const role = this.normalizeRole(item.role);
              const rawText = item.contentText || "";
              const content = role === "user" ? this.cleanUserContent(rawText) : rawText;
              return { role, content };
            });
            this.$nextTick(() => {
              setTimeout(() => {
                this.scrollToBottom();
              }, 100);
            });
          } else {
            this.suggestedQuestions = pickRandom(filterQuestions(RECOMMEND_QUESTIONS, !this.showPeriod), 4);
          }
        })
        .catch(() => {
          this.suggestedQuestions = pickRandom(RECOMMEND_QUESTIONS, 4);
        });
    },
    clearHistory() {
      uni.showModal({
        title: "清空对话",
        content: "确认清空所有对话记录？",
        confirmColor: "#f97316",
        success: (res) => {
          if (res.confirm) {
            this.messages = [];
            this.suggestedQuestions = pickRandom(RECOMMEND_QUESTIONS, 4);
          }
        }
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
  height: 100vh;
  width: 100%;
  box-sizing: border-box;
  background: #faf8f5;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 消息区 */
.chat-list {
  flex: 1;
  width: 100%;
  box-sizing: border-box;
  background: #faf8f5;
  overflow: hidden;
}

.chat-list-inner {
  padding: 16px 14px 20px;
  display: block;
  width: 100%;
  box-sizing: border-box;
}

/* 推荐问题 */
.recommend-section {
  padding-bottom: 24px;
}

.recommend-top {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 0 20px;
  gap: 8px;
}

.recommend-logo {
  width: 56px;
  height: 56px;
  border-radius: 20px;
  background: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(249, 115, 22, 0.35);
  margin-bottom: 4px;
}

.recommend-logo-text {
  font-size: 24px;
}

.recommend-hello {
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
}

.recommend-sub {
  font-size: 13px;
  color: #64748b;
  text-align: center;
}

.recommend-title {
  font-size: 12px;
  color: #94a3b8;
  font-weight: 500;
  display: block;
  margin-bottom: 10px;
}

.recommend-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.recommend-chip {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 13px 14px;
  background: #fff;
  border-radius: 14px;
  border: 1px solid #e8e2db;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}

.recommend-chip-num {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #fff7ed;
  color: #f97316;
  font-size: 11px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.recommend-chip-text {
  flex: 1;
  font-size: 14px;
  color: #334155;
}

.recommend-chip-arrow {
  font-size: 18px;
  color: #cbd5e1;
  font-weight: 300;
}

/* 消息气泡 */
.chat-item {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  margin-bottom: 14px;
  width: 100%;
}

.chat-item.mine {
  justify-content: flex-end;
}

.avatar {
  width: 34px;
  height: 34px;
  border-radius: 12px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.avatar.assistant {
  background: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
  box-shadow: 0 3px 10px rgba(249, 115, 22, 0.3);
}

.avatar-ai-text {
  font-size: 14px;
  font-weight: 700;
  color: #fff;
}

.avatar.user {
  background: #e2e8f0;
}

.avatar-img {
  width: 100%;
  height: 100%;
  display: block;
}

.avatar-user-txt {
  font-size: 13px;
  font-weight: 600;
  color: #64748b;
}

.bubble-wrap {
  max-width: 73%;
  min-height: 34px;
}

.bubble {
  padding: 11px 14px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.6;
}

.bubble-ai {
  background: #fff;
  color: #334155;
  border-bottom-left-radius: 4px;
  border: 1px solid #f0ece6;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
}

.bubble-user {
  background: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
  color: #fff;
  border-bottom-right-radius: 4px;
  box-shadow: 0 3px 10px rgba(249, 115, 22, 0.3);
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
  line-height: 1.65;
  color: inherit;
}

/* 思考中 */
.bubble-thinking {
  padding: 14px 16px;
}

.thinking-dots {
  display: flex;
  align-items: center;
  gap: 5px;
}

.thinking-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #f97316;
  animation: thinking-bounce 0.6s ease-in-out infinite;
}

.thinking-dot:nth-child(2) { animation-delay: 0.12s; }
.thinking-dot:nth-child(3) { animation-delay: 0.24s; }

@keyframes thinking-bounce {
  0%, 60% { transform: translateY(0) scale(1); opacity: 0.5; }
  30% { transform: translateY(-5px) scale(1.2); opacity: 1; }
}

/* 图片/语音 */
.bubble-img {
  max-width: 200px;
  border-radius: 8px;
  display: block;
  margin-bottom: 4px;
}

.bubble-audio-tag {
  font-size: 13px;
  opacity: 0.9;
  display: block;
}

/* 输入区 */
.composer {
  background: #fff;
  border-top: 1px solid #f0ece6;
  padding: 8px 14px;
  padding-bottom: calc(8px + env(safe-area-inset-bottom));
  flex-shrink: 0;
}

/* 展开菜单 */
.extra-menu {
  display: flex;
  justify-content: space-around;
  padding: 12px 8px 16px;
  margin-bottom: 8px;
  border-bottom: 1px solid #f0ece6;
}

.extra-menu-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
}

.extra-menu-icon {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.extra-menu-icon-album { background: #fff7ed; border: 1px solid #fed7aa; }
.extra-menu-icon-camera { background: #eff6ff; border: 1px solid #bfdbfe; }
.extra-menu-icon-voice { background: #f0fdf4; border: 1px solid #bbf7d0; }
.extra-menu-icon-file { background: #fefce8; border: 1px solid #fde68a; }

.extra-menu-icon-img {
  width: 22px;
  height: 22px;
}

.extra-menu-icon-text {
  font-size: 20px;
  line-height: 1;
}

.extra-menu-label {
  font-size: 12px;
  color: #64748b;
  font-weight: 500;
}

.composer-inner {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #f5f1eb;
  border-radius: 24px;
  padding: 6px 6px 6px 10px;
  min-height: 46px;
}

.composer-extra-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.composer-extra-icon {
  width: 18px;
  height: 18px;
}

.composer-extra-icon.recording {
  animation: recording-pulse 0.8s ease-in-out infinite;
}

@keyframes recording-pulse {
  50% { opacity: 0.4; }
}

.composer-input {
  flex: 1;
  font-size: 15px;
  color: #0f172a;
  min-height: 30px;
  background: transparent;
}

.input-placeholder {
  color: #94a3b8;
}

.composer-send-btn {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: #cbd5e1;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: background 0.2s;
}

.composer-send-active {
  background: #f97316;
  box-shadow: 0 3px 10px rgba(249, 115, 22, 0.35);
}

.composer-send-icon {
  font-size: 18px;
  font-weight: 700;
  color: #fff;
  line-height: 1;
}

.composer-plus-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #f97316;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.2s;
}

.composer-plus-open {
  background: #64748b;
}

.composer-plus-icon {
  font-size: 20px;
  font-weight: 600;
  color: #fff;
  line-height: 1;
}

.composer-clear-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.composer-clear-icon {
  font-size: 16px;
}

.recording-tip {
  display: block;
  font-size: 12px;
  color: #ef4444;
  text-align: center;
  margin-top: 6px;
  font-weight: 500;
}
</style>
