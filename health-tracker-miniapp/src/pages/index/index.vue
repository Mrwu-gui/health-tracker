<template>
  <view class="page">
    <view class="section">
      <text class="section-title">今日关怀 💛</text>
      <view class="care">
        <text class="care-text">你已经很努力啦，慢慢来，不必着急。</text>
        <text class="care-sub">喝一杯温水，做一次深呼吸，给自己一点小小的奖励 🙂</text>
      </view>
    </view>

    <view class="hero">
      <view>
        <text class="title">今日概览</text>
        <text class="subtitle">保持稳定节奏与习惯。</text>
        <text class="subtitle hint">步数来自微信运动授权</text>
      </view>
      <view class="hero-actions">
        <view class="chip">
          {{ needsSteps ? "未同步" : `${overview.steps} 步` }}
        </view>
        <!-- #ifdef MP-WEIXIN -->
        <button
          v-if="needsSteps"
          class="ghost"
          @tap="syncSteps"
          :disabled="syncLoading"
        >
          {{ syncLoading ? "同步中..." : "获取步数" }}
        </button>
        <!-- #endif -->
      </view>
    </view>

    <view class="cards">
      <view class="card">
        <text class="card-title">睡眠</text>
        <text class="card-value">{{ overview.sleep }}</text>
        <text class="card-hint">{{ overview.sleepHint }}</text>
      </view>
      <view class="card">
        <text class="card-title">热量</text>
        <text class="card-value">{{ overview.calories }}</text>
        <text class="card-hint">{{ overview.calorieHint }}</text>
      </view>
      <view class="card">
        <text class="card-title">用药</text>
        <text class="card-value">{{ overview.medication }}</text>
        <text class="card-hint">{{ overview.medicationHint }}</text>
      </view>
    </view>
    <text v-if="loading" class="status">加载中...</text>
    <text v-if="error" class="status error">{{ error }}</text>
  </view>
</template>

<script>
import { request } from "../../utils/api";

export default {
  data() {
    return {
      overview: {
        steps: "0",
        sleep: "0小时0分",
        calories: "0",
        medication: "暂无用药",
        medicationHint: "暂无提醒",
        sleepHint: "暂无睡眠记录",
        calorieHint: "暂无热量记录"
      },
      loading: false,
      error: "",
      syncLoading: false
    };
  },
  onLoad() {
    this.fetchOverview();
  },
  computed: {
    needsSteps() {
      return String(this.overview.steps || "0") === "0";
    }
  },
  methods: {
    fetchOverview() {
      this.loading = true;
      this.error = "";
      const userId = uni.getStorageSync("userId") || 1;
      request("/api/statistics/overview", "GET", { userId, period: "day" })
        .then((data) => {
          this.overview.steps = data.steps || this.overview.steps;
          this.overview.sleep = data.sleep || this.overview.sleep;
          this.overview.calories = data.calories || this.overview.calories;
          this.overview.sleepHint = data.sleep && data.sleep !== "0小时0分" ? "已记录" : "暂无睡眠记录";
          this.overview.calorieHint = data.calories && data.calories !== "0" ? "饮食 + 运动累计" : "暂无热量记录";
        })
        .then(() => {
          return request("/api/medication/list", "GET", { userId });
        })
        .then((list) => {
          if (Array.isArray(list) && list.length > 0) {
            const latest = list[list.length - 1];
            this.overview.medication = `${latest.drugName || "用药"} ${latest.dosage || ""}`.trim();
            this.overview.medicationHint = latest.remindTime
              ? `提醒 ${latest.remindTime}`
              : latest.frequency
                ? `频率 ${latest.frequency}`
                : "暂无提醒";
          } else {
            this.overview.medication = "暂无用药";
            this.overview.medicationHint = "暂无提醒";
          }
        })
        .catch(() => {
          this.error = "获取概览失败";
        })
        .finally(() => {
          this.loading = false;
        });
    },
    syncSteps() {
      this.syncLoading = true;
      // #ifdef MP-WEIXIN
      uni.login({
        provider: "weixin",
        success: (loginRes) => {
          request("/api/auth/mini/login", "POST", { code: loginRes.code })
            .then((data) => {
              if (data?.token) {
                uni.setStorageSync("token", data.token);
                if (data.userId) {
                  uni.setStorageSync("userId", data.userId);
                }
              }
            })
            .finally(() => {
              uni.login({
                provider: "weixin",
                success: (res) => {
                  uni.getWeRunData({
                    success: (werun) => {
                      request("/api/auth/mini/werun", "POST", {
                        code: res.code,
                        encryptedData: werun.encryptedData,
                        iv: werun.iv
                      })
                        .then((data) => {
                          if (data?.steps) {
                            this.overview.steps = String(data.steps);
                            uni.showToast({ title: "已同步步数", icon: "success" });
                          }
                        })
                        .catch((err) => {
                          uni.showToast({ title: err.message || "同步失败", icon: "none" });
                        })
                        .finally(() => {
                          this.syncLoading = false;
                        });
                    },
                    fail: () => {
                      uni.showToast({ title: "未授权微信运动", icon: "none" });
                      this.syncLoading = false;
                    }
                  });
                },
                fail: () => {
                  uni.showToast({ title: "微信登录失败", icon: "none" });
                  this.syncLoading = false;
                }
              });
            });
        },
        fail: () => {
          uni.showToast({ title: "微信登录失败", icon: "none" });
          this.syncLoading = false;
        }
      });
      // #endif
      // #ifndef MP-WEIXIN
      this.syncLoading = false;
      uni.showToast({ title: "请在微信小程序内操作", icon: "none" });
      // #endif
    }
  }
};
</script>

<style>
.page {
  padding: 22px;
  min-height: 100vh;
  background: #f7f4ef;
  color: #2d2a26;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.hero {
  background: linear-gradient(135deg, #f6f0e9, #ffffff);
  color: #2d2a26;
  border-radius: 20px;
  padding: 18px 18px 16px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  border: 1px solid #efe7dd;
  box-shadow: 0 10px 24px rgba(30, 25, 18, 0.06);
}

.title {
  font-size: 18px;
  font-weight: 600;
}

.subtitle {
  display: block;
  margin-top: 6px;
  color: #7a736d;
}

.hint {
  font-size: 12px;
  opacity: 0.8;
}

.chip {
  background: #efe7dd;
  color: #5c5046;
  padding: 6px 12px;
  border-radius: 999px;
  font-weight: 600;
}

.hero-actions {
  display: grid;
  gap: 8px;
  align-items: end;
}

.ghost {
  border-radius: 999px;
  background: #ffffff;
  border: 1px solid #e6dccf;
  color: #6a5f55;
  font-size: 12px;
  padding: 6px 12px;
}

.cards {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
  margin-top: 18px;
}

.card {
  background: #ffffff;
  border-radius: 16px;
  padding: 14px 16px;
  border: 1px solid #efe7dd;
  box-shadow: 0 8px 18px rgba(30, 25, 18, 0.05);
}

.card-title {
  color: #7c736b;
  font-size: 12px;
}

.card-value {
  font-size: 16px;
  font-weight: 600;
  margin-top: 6px;
  display: block;
}

.card-hint {
  color: #8d847c;
  font-size: 11px;
  margin-top: 4px;
  display: block;
}

.section {
  margin-bottom: 0;
}

.section-title {
  font-weight: 600;
  margin-bottom: 12px;
  display: block;
}

.care {
  background: #ffffff;
  border-radius: 18px;
  padding: 16px;
  border: 1px solid #efe7dd;
  box-shadow: 0 12px 26px rgba(30, 25, 18, 0.06);
}

.care-text {
  font-weight: 600;
  display: block;
}

.care-sub {
  color: #7c736b;
  font-size: 12px;
  margin-top: 6px;
  display: block;
}

.status {
  display: block;
  color: #7c736b;
  margin-top: 10px;
}

.status.error {
  color: #ef4444;
}
</style>
