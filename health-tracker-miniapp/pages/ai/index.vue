<template>
  <view class="page-root">
    <!-- Tab 切换 -->
    <view class="chips">
      <view class="chip" :class="{ active: currentTab === 0 }" @tap="onTabChange(0)">AI记录</view>
      <view class="chip" :class="{ active: currentTab === 1 }" @tap="onTabChange(1)">AI对话</view>
    </view>

    <!-- AI记录内容 -->
    <view v-if="currentTab === 0" class="page-content">
      <!-- 识别区域 -->
      <view class="recognize-section">
        <view class="recognize-header">
          <text class="recognize-title">AI 识别</text>
          <text class="recognize-subtitle">支持识别食物和体重秤读数</text>
        </view>
        <view class="recognize-buttons">
          <view class="recognize-btn" @tap="onTakePhoto">
            <image class="recognize-btn-icon" src="/static/tabbar/camera_s.png" mode="aspectFit" />
            <text class="recognize-btn-text">拍照</text>
          </view>
          <view class="recognize-btn" @tap="onChooseAlbum">
            <image class="recognize-btn-icon" src="/static/tabbar/pic_s.png" mode="aspectFit" />
            <text class="recognize-btn-text">相册</text>
          </view>
        </view>
      </view>

      <!-- 记录列表 -->
      <view v-if="recordList.length > 0" class="record-list">
        <view v-for="(item, idx) in recordList" :key="idx" class="record-item card">
          <view class="record-left">
            <view class="record-icon">
              <text>{{ item.icon || '🍽️' }}</text>
            </view>
            <view class="record-info">
              <text class="record-name">{{ item.foodName || '未识别' }}</text>
              <text class="record-cal">{{ item.calories || 0 }} kcal</text>
            </view>
          </view>
          <view v-if="item.image" class="record-img-wrap">
            <image class="record-thumb" :src="item.image" mode="aspectFill" />
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-else class="empty-state">
        <image class="empty-icon" src="/static/tabbar/doc-fail.png" mode="aspectFit"></image>
        <text class="empty-text">暂无记录</text>
        <text class="empty-hint">拍照或上传图片识别食物</text>
      </view>
    </view>

    <!-- AI 对话 -->
    <view v-else class="chat-wrapper">
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
        <view class="composer-inner">
          <input
            class="composer-input"
            v-model="input"
            placeholder="问问智康 AI…"
            placeholder-class="input-placeholder"
            confirm-type="send"
            :adjust-position="true"
            @confirm="sendMessage"
          />
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
      </view>
    </view>

      <!-- 自定义底部导航 -->
      <custom-tabbar :current="2" />

      <!-- 食物识别结果弹层 -->
      <view v-if="showFoodResult" class="food-result-mask" @tap="closeFoodResult">
      <view class="food-result-sheet" @tap.stop>
        <view class="food-result-bar" />
        <view class="food-result-header">
          <text class="food-result-title">识别结果</text>
          <view class="food-result-close" @tap="closeFoodResult">
            <text class="food-result-close-icon">×</text>
          </view>
        </view>

        <scroll-view class="food-result-scroll" scroll-y>
          <!-- 确认识别结果 -->
          <view class="confirm-section">
            <text class="confirm-title">这是"{{ foodResult.foodName || '未识别' }}"吗？</text>
            <view v-if="foodResult.image" class="food-result-image">
              <image class="food-result-img" :src="foodResult.image" mode="aspectFit" />
            </view>
            <view class="food-result-details">
              <view class="food-result-calories">
                <text class="food-cal-num">{{ foodResult.calories || 0 }}</text>
                <text class="food-cal-unit">kcal</text>
              </view>
            </view>
            <!-- 确认按钮 -->
            <view class="confirm-btn" @tap="confirmFoodResult">
              <text class="confirm-btn-text">✓ 确认是这个</text>
            </view>
          </view>

          <!-- 营养成分 -->
          <view class="nutrition-section">
            <text class="nutrition-title">营养成分</text>
            <view class="nutrition-grid">
              <view class="nutrition-item">
                <text class="nutrition-label">蛋白质</text>
                <text class="nutrition-value">{{ foodResult.protein || 0 }}g</text>
              </view>
              <view class="nutrition-item">
                <text class="nutrition-label">碳水</text>
                <text class="nutrition-value">{{ foodResult.carbs || 0 }}g</text>
              </view>
              <view class="nutrition-item">
                <text class="nutrition-label">脂肪</text>
                <text class="nutrition-value">{{ foodResult.fat || 0 }}g</text>
              </view>
            </view>
          </view>

          <!-- AI 目标推荐 - 只有当至少有一个有效目标时才显示 -->
          <view v-if="hasValidGoal" class="goal-recommend-section">
            <view class="goal-recommend-header">
              <view class="goal-recommend-icon">
                <text class="goal-icon-text">🎯</text>
              </view>
              <text class="goal-recommend-title">AI 目标推荐</text>
            </view>
            <view class="goal-recommend-list">
              <view v-if="isValidGoal(foodResult.periodGoal)" class="goal-recommend-item" @tap="setGoal('period')">
                <view class="goal-item-left">
                  <text class="goal-item-icon">📊</text>
                  <text class="goal-item-label">减脂周期目标</text>
                </view>
                <text class="goal-item-value">{{ foodResult.periodGoal || '8周减重3-5kg' }}</text>
              </view>
              <view v-if="isValidGoal(foodResult.calorieGap)" class="goal-recommend-item" @tap="setGoal('calorie')">
                <view class="goal-item-left">
                  <text class="goal-item-icon">🔥</text>
                  <text class="goal-item-label">每日热量缺口</text>
                </view>
                <text class="goal-item-value">{{ foodResult.calorieGap || '300-500kcal' }}</text>
              </view>
              <view v-if="isValidGoal(foodResult.remindFreq)" class="goal-recommend-item" @tap="setGoal('remind')">
                <view class="goal-item-left">
                  <text class="goal-item-icon">⏰</text>
                  <text class="goal-item-label">提醒频率</text>
                </view>
                <text class="goal-item-value">{{ foodResult.remindFreq || '每日3次' }}</text>
              </view>
            </view>
          </view>
        </scroll-view>

        <!-- 操作按钮 -->
        <view class="food-result-actions">
          <view class="food-result-btn secondary" @tap="closeFoodResult">
            <text class="food-result-btn-text">重新识别</text>
          </view>
          <view class="food-result-btn primary" @tap="saveFoodResult">
            <text class="food-result-btn-text">保存记录</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { request, API_BASE_URL } from "../../utils/api";
import CustomTabbar from "@/components/custom-tabbar/custom-tabbar.vue";

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
  components: {
    CustomTabbar
  },
  data() {
    return {
      input: "",
      loading: false,
      messages: [],
      listHeight: 400,
      scrollIntoView: "",
      suggestedQuestions: [],
      userAvatar: "",
      userName: "",
      userSex: "",
      currentTab: 0,
      recordList: [],
      recordLoading: false,
      showFoodResult: false,
      foodResult: {
        image: "",
        foodName: "",
        calories: 0,
        protein: 0,
        carbs: 0,
        fat: 0,
        periodGoal: "",
        calorieGap: "",
        remindFreq: ""
      }
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
    },
    hasValidGoal() {
      return this.isValidGoal(this.foodResult.periodGoal) ||
             this.isValidGoal(this.foodResult.calorieGap) ||
             this.isValidGoal(this.foodResult.remindFreq);
    }
  },
  onLoad() {
    this.setListHeight();
    this.userSex = uni.getStorageSync("userSex") || "";
    this.suggestedQuestions = pickRandom(filterQuestions(RECOMMEND_QUESTIONS, !this.showPeriod), 4);
    this.loadHistory();
    this.loadRecords();
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
    this.loadRecords();
    const initial = uni.getStorageSync("aiInitialMessage");
    if (initial && typeof initial === "string" && initial.trim()) {
      uni.removeStorageSync("aiInitialMessage");
      this.currentTab = 1; // 自动切换到AI对话Tab
      this.sendMessageWithText(initial.trim());
    }
  },
  methods: {
    onTabChange(idx) {
      this.currentTab = idx;
    },
    closeFoodResult() {
      this.showFoodResult = false;
      this.foodResult = {
        image: "",
        foodName: "",
        calories: 0,
        protein: 0,
        carbs: 0,
        fat: 0,
        periodGoal: "",
        calorieGap: "",
        remindFreq: ""
      };
    },
    showFoodResultModal(result) {
      this.foodResult = {
        image: result.image || "",
        foodName: result.foodName || "未识别",
        calories: result.calories || 0,
        protein: result.protein || 0,
        carbs: result.carbs || 0,
        fat: result.fat || 0,
        periodGoal: this.normalizeGoalValue(result.periodGoal),
        calorieGap: this.normalizeGoalValue(result.calorieGap),
        remindFreq: this.normalizeGoalValue(result.remindFreq)
      };
      this.showFoodResult = true;
    },
    confirmFoodResult() {
      if (this.recordLoading) return;
      this.saveFoodResult();
    },
    normalizeGoalValue(value) {
      if (value === null || value === undefined) return "";
      const text = String(value).trim();
      if (!text || text === "无" || text === "未知") return "";
      return text;
    },
    isValidGoal(value) {
      return !!this.normalizeGoalValue(value);
    },
    setGoal(type) {
      if (type === 'period') {
        uni.showToast({
          title: '已设置减脂目标',
          icon: 'success'
        });
      } else if (type === 'calorie') {
        uni.showToast({
          title: '已设置热量目标',
          icon: 'success'
        });
      } else if (type === 'remind') {
        uni.showToast({
          title: '已设置提醒频率',
          icon: 'success'
        });
      }
    },

    saveFoodResult() {
      const userId = uni.getStorageSync("userId") || 1;
      const payload = {
        userId,
        mealType: "其他",
        foodName: this.foodResult.foodName,
        calories: this.foodResult.calories,
        protein: this.foodResult.protein,
        carbs: this.foodResult.carbs,
        fat: this.foodResult.fat,
        note: "AI识别"
      };
      this.recordLoading = true;
      request("/api/diet/add", "POST", payload)
        .then(() => {
          this.addFoodRecord(
            this.foodResult.image,
            this.foodResult.foodName,
            this.foodResult.calories,
            `${this.foodResult.protein}g蛋白质 ${this.foodResult.carbs}g碳水 ${this.foodResult.fat}g脂肪`
          );
          this.loadRecords();
          this.closeFoodResult();
          uni.showToast({ title: "已保存", icon: "success" });
        })
        .catch((err) => {
          uni.showToast({ title: err.message || "保存失败", icon: "none" });
        })
        .finally(() => {
          this.recordLoading = false;
        });
    },
    loadRecords() {
      const userId = uni.getStorageSync("userId") || 1;
      request("/api/ai/vision/records", "GET", { userId })
        .then((list) => {
          if (!Array.isArray(list)) {
            this.recordList = [];
            return;
          }
          this.recordList = list.map((item) => {
            let payload = {};
            try {
              payload = item.payloadJson ? JSON.parse(item.payloadJson) : {};
            } catch (e) {
              payload = {};
            }
            const type = item.type || payload.type || "invalid";
            if (type === "food") {
              return {
                image: item.imageUrl,
                foodName: payload.foodName || payload.food_name || "未识别",
                calories: payload.calories || 0,
                description: `${payload.protein || 0}g蛋白质 ${payload.carbs || 0}g碳水 ${payload.fat || 0}g脂肪`,
                date: item.createdAt,
                icon: "🍽️"
              };
            }
            if (type === "weight") {
              return {
                image: item.imageUrl,
                foodName: `体重 ${payload.weight || 0} kg`,
                calories: 0,
                description: "体重识别",
                date: item.createdAt,
                icon: "⚖️"
              };
            }
            if (type === "medication") {
              return {
                image: item.imageUrl,
                foodName: `药品 ${payload.drugName || payload.drug_name || "未识别"}`,
                calories: 0,
                description: "药品识别",
                date: item.createdAt,
                icon: "💊"
              };
            }
            return {
              image: item.imageUrl,
              foodName: "未识别",
              calories: 0,
              description: "识别失败",
              date: item.createdAt,
              icon: "❓"
            };
          });
        })
        .catch(() => {
          this.recordList = [];
        });
    },
    async addFoodRecord(image, foodName, calories, description) {
      const now = new Date();
      const dateStr = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}-${String(now.getDate()).padStart(2, "0")}`;
      const timeStr = `${String(now.getHours()).padStart(2, "0")}:${String(now.getMinutes()).padStart(2, "0")}`;
      const record = {
        image,
        foodName: foodName || "未识别",
        calories: calories || 0,
        description,
        date: `${dateStr} ${timeStr}`,
        icon: "🍽️"
      };
      this.recordList.unshift(record);
    },
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
    onTakePhoto() {
      if (this.recordLoading) return;
      uni.chooseImage({
        count: 1,
        sizeType: ["compressed"],
        sourceType: ["camera"],
        success: async (res) => {
          const path = res.tempFilePaths && res.tempFilePaths[0];
          if (path) {
            await this.recognizeImageForRecord(path);
          }
        }
      });
    },
    onChooseAlbum() {
      if (this.recordLoading) return;
      uni.chooseImage({
        count: 1,
        sizeType: ["compressed"],
        sourceType: ["album"],
        success: async (res) => {
          const path = res.tempFilePaths && res.tempFilePaths[0];
          if (path) {
            await this.recognizeImageForRecord(path);
          }
        }
      });
    },
    async recognizeImageForRecord(path) {
      if (this.recordLoading) return;
      this.recordLoading = true;
      uni.showLoading({ title: "识别中…" });
      try {
        const upload = await this.uploadAiImage(path);
        const imageUrl = upload && upload.url ? upload.url : "";
        if (!imageUrl) throw new Error("图片上传失败");
        const userId = uni.getStorageSync("userId") || 1;
        const result = await request("/api/ai/vision/recognize", "POST", { userId, imageUrl });
        if (!result || !result.type) {
          uni.showToast({ title: "识别失败", icon: "none" });
          return;
        }
        if (result.type === "food") {
          this.showFoodResultModal({
            image: imageUrl,
            foodName: result.foodName,
            calories: result.calories,
            protein: result.protein,
            carbs: result.carbs,
            fat: result.fat,
            periodGoal: result.periodGoal,
            calorieGap: result.calorieGap,
            remindFreq: result.remindFreq
          });
          return;
        }
        if (result.type === "weight") {
          const weight = Number(result.weight || 0);
          if (!weight) {
            uni.showToast({ title: "未识别到体重", icon: "none" });
            return;
          }
          uni.showModal({
            title: "识别体重",
            content: `识别到体重 ${weight} kg，是否添加记录？`,
            confirmColor: "#A23F00",
            success: async (res) => {
              if (res.confirm) {
                await request("/api/weight/add", "POST", { userId, weight });
                this.loadRecords();
                uni.showToast({ title: "已记录体重", icon: "success" });
              }
            }
          });
          return;
        }
        uni.showToast({ title: "未识别到有效内容", icon: "none" });
      } catch (e) {
        uni.showToast({ title: e.message || "识别失败", icon: "none" });
      } finally {
        uni.hideLoading();
        this.recordLoading = false;
      }
    },
    uploadAiImage(filePath) {
      return new Promise((resolve, reject) => {
        const token = uni.getStorageSync("token");
        uni.uploadFile({
          url: `${API_BASE_URL}/api/ai/upload`,
          filePath,
          name: "file",
          header: {
            Authorization: token ? `Bearer ${token}` : ""
          },
          success: (res) => {
            if (res.statusCode >= 200 && res.statusCode < 300) {
              let data = res.data;
              if (typeof data === "string") {
                try {
                  data = JSON.parse(data);
                } catch (e) {
                  reject(new Error("解析上传结果失败"));
                  return;
                }
              }
              if (data && (typeof data.code === "number" || typeof data.code === "string")) {
                const code = Number(data.code);
                if (code !== 0) {
                  reject(new Error(data.message || "上传失败"));
                  return;
                }
                resolve(data.data || {});
                return;
              }
              resolve(data || {});
            } else {
              reject(new Error("上传失败"));
            }
          },
          fail: (err) => {
            reject(new Error(err.errMsg || "上传失败"));
          }
        });
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
      if (!content || this.loading) return;
      this.messages.push({ role: "user", content: content || "" });
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
        confirmColor: "#A23F00",
        success: async (res) => {
          if (res.confirm) {
            if (this.loading) return;
            this.loading = true;
            try {
              const userId = uni.getStorageSync("userId") || 1;
              await request("/api/ai/clear", "POST", { userId });
              this.messages = [];
              this.suggestedQuestions = pickRandom(filterQuestions(RECOMMEND_QUESTIONS, !this.showPeriod), 4);
              this.scrollIntoView = "";
              uni.showToast({ title: "已清空", icon: "success" });
            } catch (err) {
              uni.showToast({ title: err.message || "清空失败", icon: "none" });
            } finally {
              this.loading = false;
            }
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
.page-root {
  height: 100vh;
  width: 100%;
  box-sizing: border-box;
  background: #FAF8F5;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding-bottom: calc(56px + env(safe-area-inset-bottom));
}

/* 页面根容器 */
.page-root {
  min-height: 100vh;
  background: #FAF8F5;
}

.page-content {
  padding: 24rpx 32rpx 180rpx;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

/* 筛选标签 */
.chips {
  display: flex;
  gap: 16rpx;
  padding: 16rpx 32rpx;
  width: 100%;
  box-sizing: border-box;
}

.chip {
  flex: 1;
  padding: 16rpx 0;
  border-radius: 999rpx;
  background: #fff;
  color: #564337;
  font-size: 26rpx;
  font-weight: 500;
  border: 1px solid #E9E1D8;
  text-align: center;
}

.chip.active {
  background: #A23F00;
  color: #fff;
  border-color: #564337;
}

/* 识别区域 */
.recognize-section {
  background: #fff;
  border-radius: var(--radius-card);
  padding: 32rpx;
  border: 1px solid #E9E1D8;
}

.recognize-header {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  align-items: center;
  margin-bottom: 32rpx;
}

.recognize-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1a1c1a;
}

.recognize-subtitle {
  font-size: 24rpx;
  color: #8B7355;
}

.recognize-buttons {
  display: flex;
  gap: 16rpx;
}

.recognize-btn {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  justify-content: center;
  padding: 28rpx 20rpx;
  background: #fff;
  border-radius: 20rpx;
  border: 1px solid #E9E1D8;
  transition: all 0.2s;
}

.recognize-btn:active {
  background: #F5F5F5;
  transform: scale(0.98);
}

.recognize-btn-icon {
  width: 64rpx;
  height: 64rpx;
}

.recognize-btn-text {
  font-size: 28rpx;
  color: #564337;
  font-weight: 600;
}

/* 记录列表 */
.record-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.record-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-radius: var(--radius-card);
  padding: 20rpx 24rpx;
  border: 1px solid #E9E1D8;
}

.record-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex: 1;
}

.record-item .record-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 40rpx;
  background: #F5F5F5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
  flex-shrink: 0;
}

.record-info {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.record-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #1a1c1a;
}

.record-cal {
  font-size: 24rpx;
  color: #564337;
  font-weight: 500;
}

.record-img-wrap {
  width: 100rpx;
  height: 100rpx;
  border-radius: 16rpx;
  overflow: hidden;
  flex-shrink: 0;
}

.record-thumb {
  width: 100%;
  height: 100%;
  display: block;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 40rpx;
  gap: 16rpx;
}

.empty-icon {
  width: 160rpx;  /* 调整图片宽度（建议值） */
  height: 160rpx;  /* 调整图片高度（建议值） */
  opacity: 0.8;    /* 调整透明度 */
}


.empty-text {
  font-size: 28rpx;
  font-weight: 500;
  color: #564337;
}

.empty-hint {
  font-size: 24rpx;
  color: #94a3b8;
  text-align: center;
}

.card {
  border-radius: var(--radius-card);
}

/* AI 对话页面 */
.chat-wrapper {
  height: calc(100vh - 56px - env(safe-area-inset-bottom));
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #FAF8F5;
}

/* 消息区 */
.chat-list {
  flex: 1;
  width: 100%;
  box-sizing: border-box;
  background: #FAF8F5;
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
  border-radius: 30rpx;
  background: linear-gradient(135deg, #A23F00 0%, #8B3500 100%);
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
  color: #1a1c1a;
}

.recommend-sub {
  font-size: 13px;
  color: #564337;
  text-align: center;
}

.recommend-title {
  font-size: 12px;
  color: #564337;
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
  border-radius: 52rpx;
  border: 1px solid #E9E1D8;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}

.recommend-chip-num {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #FAF8F5;
  color: #564337;
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
  border-radius: 30rpx;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.avatar.assistant {
  background: linear-gradient(135deg, #A23F00 0%, #8B3500 100%);
  box-shadow: 0 3px 10px rgba(249, 115, 22, 0.3);
}

.avatar-ai-text {
  font-size: 14px;
  font-weight: 700;
  color: #fff;
}

.avatar.user {
  background: #E9E1D8;
}

.avatar-img {
  width: 100%;
  height: 100%;
  display: block;
}

.avatar-user-txt {
  font-size: 13px;
  font-weight: 600;
  color: #564337;
}

.bubble-wrap {
  max-width: 73%;
  min-height: 34px;
}

.bubble {
  padding: 11px 14px;
  border-radius: 30rpx;
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
  background: linear-gradient(135deg, #A23F00 0%, #8B3500 100%);
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
  background: #A23F00;
  animation: thinking-bounce 0.6s ease-in-out infinite;
}

.thinking-dot:nth-child(2) { animation-delay: 0.12s; }
.thinking-dot:nth-child(3) { animation-delay: 0.24s; }

@keyframes thinking-bounce {
  0%, 60% { transform: translateY(0) scale(1); opacity: 0.5; }
  30% { transform: translateY(-5px) scale(1.2); opacity: 1; }
}

/* 输入区 */
.composer {
  background: #fff;
  border-top: 1px solid #f0ece6;
  padding: 8px 14px;
  padding-bottom: calc(8px + env(safe-area-inset-bottom));
  flex-shrink: 0;
}

.composer-inner {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #f5f1eb;
  border-radius: 30rpx;
  padding: 6px 6px 6px 10px;
  min-height: 46px;
}

.composer-input {
  flex: 1;
  font-size: 15px;
  color: #1a1c1a;
  min-height: 30px;
  background: transparent;
}

.input-placeholder {
  color: #564337;
}

.composer-send-btn {
  width: 38px;
  height: 38px;
  border-radius: var(--radius-pill);
  background: #E9E1D8;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: background 0.2s;
}

.composer-send-active {
  background: #A23F00;
  box-shadow: 0 6rpx 16rpx rgba(162, 63, 0, 0.35);
}

.composer-send-icon {
  font-size: 18px;
  font-weight: 700;
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

/* 食物识别结果弹层 */
.food-result-mask {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.6);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  transform: translateZ(0);
  z-index: 99999;
}

.food-result-sheet {
  width: 100%;
  max-height: 90vh;
  background: #fff;
  border-radius: 32rpx 32rpx 0 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

.food-result-bar {
  width: 72rpx;
  height: 8rpx;
  background: #E9E1D8;
  border-radius: 8rpx;
  margin: 20rpx auto;
}

.food-result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 40rpx 24rpx;
}

.food-result-title {
  font-size: 34rpx;
  font-weight: 700;
  color: #1a1c1a;
}

.food-result-close {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.food-result-close-icon {
  font-size: 48rpx;
  color: #8B7355;
}

.food-result-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 0 32rpx 24rpx;
  width: 100%;
  box-sizing: border-box;
}

/* 确认识别结果部分 */
.confirm-section {
  background: #FFF4ED;
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  border: 1px solid rgba(162, 63, 0, 0.1);
}

.confirm-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1a1c1a;
  text-align: center;
  display: block;
  margin-bottom: 20rpx;
}

.food-result-image {
  width: 100%;
  max-height: 300rpx;
  border-radius: 20rpx;
  overflow: hidden;
  margin-bottom: 20rpx;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.food-result-img {
  width: 100%;
  max-height: 300rpx;
  display: block;
}

.food-result-details {
  display: flex;
  justify-content: center;
  align-items: baseline;
  gap: 8rpx;
  margin-bottom: 24rpx;
}

.food-result-name {
  font-size: 32rpx;
  font-weight: 700;
  color: #1a1c1a;
}

.food-result-calories {
  display: flex;
  align-items: baseline;
  gap: 4rpx;
  background: #fff;
  padding: 12rpx 24rpx;
  border-radius: 20rpx;
}

.food-cal-num {
  font-size: 40rpx;
  font-weight: 800;
  color: #564337;
}

.food-cal-unit {
  font-size: 24rpx;
  color: #564337;
  font-weight: 500;
}

/* 确认按钮 */
.confirm-btn {
  width: 100%;
  height: 88rpx;
  border-radius: 44rpx;
  background: linear-gradient(135deg, #A23F00 0%, #FA7025 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(162, 63, 0, 0.3);
}

.confirm-btn-text {
  font-size: 28rpx;
  font-weight: 600;
}

/* 营养成分 */
.nutrition-section {
  margin-bottom: 24rpx;
}

.nutrition-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1a1c1a;
  display: block;
  margin-bottom: 16rpx;
}

.nutrition-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12rpx;
  justify-content: center;
}

.nutrition-item {
  background: #F5F5F5;
  border-radius: 20rpx;
  padding: 20rpx 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.nutrition-label {
  font-size: 22rpx;
  color: #8B7355;
}

.nutrition-value {
  font-size: 28rpx;
  font-weight: 700;
  color: #564337;
}

/* AI 目标推荐 */
.goal-recommend-section {
  background: linear-gradient(135deg, #FFF4ED 0%, #FFEBE0 100%);
  border-radius: 24rpx;
  padding: 24rpx;
  border: 1px solid rgba(162, 63, 0, 0.1);
}

.goal-recommend-header {
  display: flex;
  align-items: center;
  gap: 12rpx;
  justify-content: center;
  margin-bottom: 20rpx;
}

.goal-recommend-icon {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #A23F00 0%, #FA7025 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.goal-icon-text {
  font-size: 24rpx;
}

.goal-recommend-title {
  font-size: 28rpx;
  font-weight: 700;
  color: #1a1c1a;
}

.goal-recommend-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.goal-recommend-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-radius: 20rpx;
  padding: 16rpx 20rpx;
  position: relative;
  overflow: hidden;
}

.goal-recommend-item:active {
  background: #FFF4ED;
}

.goal-recommend-item::after {
  content: '›';
  position: absolute;
  right: 20rpx;
  color: #564337;
  font-size: 32rpx;
  font-weight: 300;
}

.goal-item-left {
  display: flex;
  align-items: center;
  gap: 12rpx;
  justify-content: center;
}

.goal-item-icon {
  font-size: 28rpx;
}

.goal-item-label {
  font-size: 24rpx;
  color: #564337;
  font-weight: 500;
}

.goal-item-value {
  font-size: 24rpx;
  color: #564337;
  font-weight: 600;
  padding-right: 40rpx;
}

/* 弹层操作按钮 */
.food-result-actions {
  display: flex;
  gap: 16rpx;
  padding: 24rpx 32rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
  border-top: 1px solid #E9E1D8;
}

.food-result-btn {
  flex: 1;
  height: 88rpx;
  border-radius: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 600;
}

.food-result-btn.primary {
  background: linear-gradient(135deg, #A23F00 0%, #FA7025 100%);
  color: #fff;
  box-shadow: 0 8rpx 24rpx rgba(162, 63, 0, 0.3);
}

.food-result-btn.secondary {
  background: #F5F5F5;
  color: #564337;
  border: 1px solid #E9E1D8;
}
</style>
