<template>
  <view class="page">
    <!-- 时间切换 -->
    <view class="period-tabs">
      <view
        v-for="item in periodTabs"
        :key="item.value"
        class="period-tab pill"
        :class="{ active: period === item.value }"
        @tap="switchPeriod(item.value)"
      >
        <text class="period-tab-text">{{ item.label }}</text>
      </view>
    </view>

    <!-- 维度切换 -->
    <scroll-view scroll-x class="dim-scroll" :show-scrollbar="false">
      <view class="dim-tabs">
        <view
          v-for="item in visibleDims"
          :key="item.value"
          class="dim-tab pill"
          :class="{ active: dimension === item.value }"
          @tap="switchDimension(item.value)"
        >
          <text class="dim-label">{{ item.label }}</text>
        </view>
      </view>
    </scroll-view>

    <!-- 图表卡片 -->
    <view class="chart-card card">
      <!-- 卡片标题 -->
      <view class="chart-header">
        <view class="chart-title-row">
          <text class="chart-title">{{ currentDimLabel }}·趋势</text>
          <view class="chart-badge pill">{{ periodLabel }}</view>
        </view>
        
        <!-- 图例（仅饮食） -->
        <view v-if="dimension === 'diet'" class="chart-legend">
          <view class="legend-item">
            <view class="legend-dot legend-breakfast"></view>
            <text class="legend-label">早餐</text>
          </view>
          <view class="legend-item">
            <view class="legend-dot legend-lunch"></view>
            <text class="legend-label">午餐</text>
          </view>
          <view class="legend-item">
            <view class="legend-dot legend-dinner"></view>
            <text class="legend-label">晚餐</text>
          </view>
          <view class="legend-item">
            <view class="legend-dot legend-snack"></view>
            <text class="legend-label">加餐</text>
          </view>
        </view>
      </view>

      <!-- 步数柱状图 -->
      <view v-if="dimension === 'steps'" class="chart-body">
        <view class="bar-chart">
          <view class="bar-y-axis">
            <text class="y-label">{{ stepsMax }}</text>
            <text class="y-label">{{ Math.round(stepsMax / 2) }}</text>
            <text class="y-label">0</text>
          </view>
          <view class="bar-container">
            <!-- 达标虚线 8000步 -->
            <view v-if="stepsMax >= 8000" class="target-line" :style="'bottom: ' + getTargetLinePos(8000, stepsMax)">
              <text class="target-label">达标 8000步</text>
            </view>
            <view v-for="(item, idx) in stepsSeries" :key="idx" class="bar-item">
              <view class="bar-wrapper">
                <view class="bar-bg"></view>
                <view class="bar-fill" :class="{ 'bar-exceed': item.value >= 8000 }" :style="'height: ' + getBarHeight(item.value, stepsMax)">
                  <text v-if="item.value > 0" class="bar-value">{{ item.value >= 1000 ? Math.round(item.value / 1000 * 10) / 10 + 'k' : item.value }}</text>
                </view>
              </view>
              <text class="bar-label">{{ item.label }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 睡眠柱状图 - 单色 -->
      <view v-else-if="dimension === 'sleep'" class="chart-body">
        <view class="bar-chart">
          <view class="bar-y-axis">
            <text class="y-label">{{ formatSleepStat(sleepMax) }}</text>
            <text class="y-label">{{ formatSleepStat(Math.round(sleepMax / 2)) }}</text>
            <text class="y-label">0</text>
          </view>
          <view class="bar-container">
            <!-- 达标虚线 7小时=420分钟 -->
            <view v-if="sleepMax >= 420" class="target-line" :style="'bottom: ' + getTargetLinePos(420, sleepMax)">
              <text class="target-label">达标 7h</text>
            </view>
            <view v-for="(item, idx) in sleepSeries" :key="idx" class="bar-item">
              <view class="bar-wrapper">
                <view class="bar-bg"></view>
                <view class="bar-fill bar-sleep" :class="{ 'bar-exceed': item.total >= 420 }" :style="'height: ' + getBarHeight(item.total, sleepMax)">
                  <text v-if="item.total > sleepMax * 0.3" class="bar-value">{{ formatSleepShort(item.total) }}</text>
                </view>
              </view>
              <text class="bar-label">{{ item.label }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 饮食堆叠柱状图 -->
      <view v-else-if="dimension === 'diet'" class="chart-body">
        <view class="bar-chart">
          <view class="bar-y-axis">
            <text class="y-label">{{ dietMax }}</text>
            <text class="y-label">{{ Math.round(dietMax / 2) }}</text>
            <text class="y-label">0</text>
          </view>
          <view class="bar-container">
            <view v-for="(item, idx) in dietSeries" :key="idx" class="bar-item">
              <view class="bar-wrapper">
                <view class="bar-bg"></view>
                <view class="bar-stack">
                  <view class="stack-seg stack-breakfast" :style="'height: ' + getBarHeight(item.breakfast, dietMax)"></view>
                  <view class="stack-seg stack-lunch" :style="'height: ' + getBarHeight(item.lunch, dietMax)"></view>
                  <view class="stack-seg stack-dinner" :style="'height: ' + getBarHeight(item.dinner, dietMax)"></view>
                  <view class="stack-seg stack-snack" :style="'height: ' + getBarHeight(item.snack, dietMax)"></view>
                </view>
              </view>
              <text class="bar-label">{{ item.label }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 体重趋势图 - 简化柱状图 -->
      <view v-else-if="dimension === 'weight'" class="chart-body">
        <view class="bar-chart">
          <view class="bar-y-axis">
            <text class="y-label">{{ formatWeightStat(weightSeries, 'max') }}</text>
            <text class="y-label">{{ formatWeightStat(weightSeries, 'avg') }}</text>
            <text class="y-label">{{ formatWeightStat(weightSeries, 'min') }}</text>
          </view>
          <view class="bar-container">
            <view v-for="(item, idx) in weightSeries" :key="idx" class="bar-item">
              <view class="bar-wrapper">
                <view class="bar-bg"></view>
                <view class="bar-fill bar-weight" :style="'height: ' + getWeightBarHeight(item.value)">
                  <text v-if="Number(item.value) > 0" class="bar-value">{{ Number(item.value).toFixed(1) }}</text>
                </view>
              </view>
              <text class="bar-label">{{ item.label }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 经期热力图 -->
      <view v-else-if="dimension === 'period'" class="chart-body">
        <view class="period-heatmap">
          <view class="period-header">
            <text class="period-title">日历热力图</text>
            <view class="period-legend">
              <view class="period-legend-item">
                <view class="period-legend-dot period-level-0"></view>
                <text class="period-legend-label">无</text>
              </view>
              <view class="period-legend-item">
                <view class="period-legend-dot period-level-1"></view>
                <text class="period-legend-label">少</text>
              </view>
              <view class="period-legend-item">
                <view class="period-legend-dot period-level-2"></view>
                <text class="period-legend-label">中</text>
              </view>
              <view class="period-legend-item">
                <view class="period-legend-dot period-level-3"></view>
                <text class="period-legend-label">多</text>
              </view>
            </view>
          </view>
          <view class="period-grid">
            <view
              v-for="(item, idx) in periodSeries"
              :key="idx"
              class="period-cell"
              :class="'period-flow-' + item.flow"
            >
              <text class="period-cell-label">{{ item.label }}</text>
              <!-- 流量标记点 -->
              <view v-if="item.flow > 0" class="period-flow-dot"></view>
            </view>
          </view>
        </view>
      </view>

      <!-- 统计数据 -->
      <view v-if="showStats" class="stats-section">
        <view class="stat-card card-sm">
          <text class="stat-label">最高</text>
          <view class="stat-value-row">
            <text class="stat-value">{{ statMax }}</text>
            <text class="stat-unit">{{ statUnit }}</text>
          </view>
        </view>
        <view class="stat-card stat-highlight card-sm">
          <text class="stat-label">平均</text>
          <view class="stat-value-row">
            <text class="stat-value stat-value-theme">{{ statAvg }}</text>
            <text class="stat-unit">{{ statUnit }}</text>
          </view>
          <text v-if="dimension === 'sleep'" class="stat-desc">{{ sleepStatusText }}</text>
        </view>
        <view class="stat-card card-sm">
          <text class="stat-label">最低</text>
          <view class="stat-value-row">
            <text class="stat-value">{{ statMin }}</text>
            <text class="stat-unit">{{ statUnit }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- AI 分析卡片 -->
    <view class="ai-card card">
      <view class="ai-header">
        <view class="ai-title-wrap">
          <view class="ai-title-row">
            <view class="ai-icon">AI</view>
            <text class="ai-title">AI健康分析</text>
          </view>
          <text class="ai-subtitle">{{ periodLabel }}综合解读</text>
        </view>
        <view class="ai-action">
          <view class="ai-refresh-btn pill" @tap="fetchAiInsight(true)">
            <text class="ai-refresh-text">{{ aiLoading ? '分析中...' : '重新分析' }}</text>
          </view>
        </view>
      </view>

      <view class="ai-content">
        <!-- 加载状态 -->
        <view v-if="aiLoading" class="ai-loading">
          <view class="loading-spinner"></view>
          <text class="loading-text">正在分析您的健康数据...</text>
        </view>

        <!-- 错误状态 -->
        <view v-else-if="aiError" class="ai-error">
          <text class="error-icon">⚠️</text>
          <text class="error-text">{{ aiError }}</text>
          <view class="error-retry pill" @tap="fetchAiInsight(true)">
            <text class="error-retry-text">点击重试</text>
          </view>
        </view>

        <!-- 分析结果 -->
        <view v-else class="ai-result">
          <view class="ai-summary-section">
            <text class="ai-summary-text">{{ aiSummary }}</text>
          </view>

          <view v-if="aiAdvice.length > 0" class="ai-advice-section">
            <text class="ai-advice-title">行动建议</text>
            <view class="ai-advice-list">
              <view v-for="(tip, idx) in aiAdvice" :key="idx" class="advice-item">
                <view class="advice-number">{{ idx + 1 }}</view>
                <text class="advice-text">{{ tip }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>
    <!-- 自定义底部导航 -->
    <custom-tabbar :current="1" />
  </view>
</template>

<script>
import { request } from "../../utils/api";
import CustomTabbar from "@/components/custom-tabbar/custom-tabbar.vue";

export default {
  components: {
    CustomTabbar
  },
  data() {
    return {
      period: "week",
      dimension: "steps",
      userSex: "",
      periodTabs: [
        { label: "7天", value: "week" },
        { label: "30天", value: "month" },
        { label: "半年", value: "half" }
      ],
      dimensions: [
        { label: "步数", value: "steps" },
        { label: "睡眠", value: "sleep" },
        { label: "饮食", value: "diet" },
        { label: "体重", value: "weight" },
        { label: "经期", value: "period" }
      ],
      series: {},
      aiSummary: "",
      aiAdvice: [],
      aiLoading: false,
      aiError: ""
    };
  },
  computed: {
    showPeriod() {
      var sex = this.userSex || uni.getStorageSync("userSex") || "";
      return String(sex).includes("女");
    },
    visibleDims() {
      if (this.showPeriod) {
        return this.dimensions;
      }
      return this.dimensions.filter(function(item) {
        return item.value !== "period";
      });
    },
    periodLabel() {
      if (this.period === "month") return "近30天";
      if (this.period === "half") return "近半年";
      return "近7天";
    },
    currentDimLabel() {
      var item = this.dimensions.find(function(d) {
        return d.value === this.dimension;
      }.bind(this));
      return item ? item.label : "";
    },
    stepsSeries() {
      return this.series.steps || [];
    },
    weightSeries() {
      return this.series.weight || [];
    },
    sleepSeries() {
      var total = this.series.sleep || [];
      var deep = this.series.sleepDeep || [];
      var light = this.series.sleepLight || [];
      return total.map(function(item, idx) {
        var deepVal = Number((deep[idx] && deep[idx].value) || 0);
        var lightVal = Number((light[idx] && light[idx].value) || 0);
        var totalMinutes = Math.max(0, Math.round(Number(item.value || 0) * 60));
        var fallbackLight = totalMinutes > 0 && deepVal + lightVal === 0 ? totalMinutes : 0;
        return {
          label: item.label,
          deep: deepVal,
          light: lightVal + fallbackLight,
          total: deepVal + lightVal + fallbackLight
        };
      });
    },
    dietSeries() {
      var total = this.series.diet || [];
      var breakfast = this.series.dietBreakfast || [];
      var lunch = this.series.dietLunch || [];
      var dinner = this.series.dietDinner || [];
      var snack = this.series.dietSnack || [];
      return total.map(function(item, idx) {
        return {
          label: item.label,
          breakfast: Number((breakfast[idx] && breakfast[idx].value) || 0),
          lunch: Number((lunch[idx] && lunch[idx].value) || 0),
          dinner: Number((dinner[idx] && dinner[idx].value) || 0),
          snack: Number((snack[idx] && snack[idx].value) || 0),
          total: Number(item.value || 0)
        };
      });
    },
    periodSeries() {
      var series = this.series.period || [];
      return series.map(function(item) {
        return {
          label: item.label,
          flow: Number(item.value || 0)
        };
      });
    },
    stepsMax() {
      var max = this.maxValue(this.stepsSeries);
      return Math.max(max, 10000); // 确保至少显示10000步
    },
    sleepMax() {
      var max = 0;
      this.sleepSeries.forEach(function(cur) {
        if (cur.total > max) max = cur.total;
      });
      return Math.max(max, 480); // 确保至少显示8小时
    },
    dietMax() {
      var max = 0;
      this.dietSeries.forEach(function(cur) {
        if (cur.total > max) max = cur.total;
      });
      return max > 0 ? max : 1;
    },
    weightLineDots() {
      var valid = this.weightSeries.filter(function(item) {
        return Number(item.value) > 0;
      });
      if (valid.length === 0) return [];
      var values = valid.map(function(item) {
        return Number(item.value);
      });
      var min = Math.min.apply(null, values);
      var max = Math.max.apply(null, values);
      var span = max - min || 1;
      var dots = valid.map(function(item, idx) {
        var x = valid.length === 1 ? 50 : (idx * (100 / (valid.length - 1)));
        var y = ((Number(item.value) - min) / span) * 80 + 10;
        return { x: x, y: y };
      });
      // 计算连接线角度
      for (var i = 1; i < dots.length; i++) {
        var dx = (dots[i].x - dots[i-1].x) * 2; // 比例
        var dy = dots[i].y - dots[i-1].y;
        var angle = Math.atan2(-dy, dx) * 180 / Math.PI;
        var len = Math.sqrt(dx * dx + dy * dy);
        dots[i].angle = angle;
        dots[i].lineLen = len;
      }
      return dots;
    },
    weightLinePoints() {
      // 不再使用SVG，保留空函数避免报错
      return "";
    },
    showStats() {
      return this.dimension !== "period";
    },
    statMax() {
      if (this.dimension === "steps") return this.stepsMax;
      if (this.dimension === "sleep") return this.formatSleepStat(this.sleepMax);
      if (this.dimension === "diet") return this.dietMax;
      if (this.dimension === "weight") return parseFloat(this.formatWeightStat(this.weightSeries, "max"));
      return "";
    },
    statMin() {
      if (this.dimension === "steps") return this.minValue(this.stepsSeries);
      if (this.dimension === "sleep") return this.formatSleepStat(this.minSleep());
      if (this.dimension === "diet") return this.minValue(this.series.diet || []);
      if (this.dimension === "weight") return parseFloat(this.formatWeightStat(this.weightSeries, "min"));
      return "";
    },
    statAvg() {
      if (this.dimension === "steps") return this.avgValue(this.stepsSeries);
      if (this.dimension === "sleep") return this.formatSleepStat(this.avgSleep());
      if (this.dimension === "diet") return this.avgValue(this.series.diet || []);
      if (this.dimension === "weight") return parseFloat(this.formatWeightStat(this.weightSeries, "avg"));
      return "";
    },
    statUnit() {
      if (this.dimension === "steps") return "步";
      if (this.dimension === "sleep") return "";
      if (this.dimension === "diet") return "kcal";
      if (this.dimension === "weight") return "kg";
      return "";
    },
    sleepStatusText() {
      var avg = this.avgSleep();
      if (avg < 360) return "偏少";
      if (avg >= 420) return "达标";
      return "接近达标";
    }
  },
  onLoad(options) {
    this.userSex = uni.getStorageSync("userSex") || "";
    if (options && options.dimension) {
      this.dimension = options.dimension;
    }
    if (options && options.period) {
      this.period = options.period;
    }
    this.fetchUserSex();
  },
  methods: {
    fetchUserSex: function() {
      var self = this;
      var userId = uni.getStorageSync("userId") || 1;
      request("/api/user/profile", "GET", { userId: userId }).then(function(data) {
        if (data && data.sex) {
          self.userSex = data.sex;
          uni.setStorageSync("userSex", data.sex);
        }
      }).finally(function() {
        if (!self.showPeriod && self.dimension === "period") {
          self.dimension = "steps";
        }
        self.fetchTrend();
      });
    },
    switchPeriod: function(val) {
      if (this.period === val) return;
      this.period = val;
      this.fetchTrend();
    },
    switchDimension: function(val) {
      if (this.dimension === val) return;
      this.dimension = val;
      this.fetchTrend();
    },
    getBarHeight: function(value, maxValue) {
      var max = maxValue > 0 ? maxValue : 1;
      var h = Math.round((Number(value || 0) / max) * 100);
      return Math.max(2, h) + "%";
    },
    getTargetLinePos: function(target, maxValue) {
      var max = maxValue > 0 ? maxValue : 1;
      var pos = Math.round((target / max) * 100);
      return Math.min(pos, 95) + "%";
    },
    formatSleepShort: function(minutes) {
      var h = Math.floor(minutes / 60);
      var m = Math.round(minutes % 60);
      if (h > 0) {
        return h + "." + Math.round(m / 6) + "h";
      }
      return m + "m";
    },
    getDietStatus: function(total) {
      if (total > 2200) return "high";
      if (total < 1500) return "low";
      return "normal";
    },
    getDietStatusText: function(total) {
      if (total > 2200) return "偏高";
      if (total < 1500) return "偏低";
      return "正常";
    },
    getWeightBarHeight: function(value) {
      var valid = this.weightSeries.filter(function(item) {
        return Number(item.value) > 0;
      });
      if (valid.length === 0) return "0%";
      var values = valid.map(function(item) {
        return Number(item.value);
      });
      var min = Math.min.apply(null, values);
      var max = Math.max.apply(null, values);
      var span = max - min || 1;
      var h = ((Number(value || 0) - min) / span) * 80 + 10;
      return Math.max(5, Math.min(100, h)) + "%";
    },
    maxValue: function(list) {
      if (!Array.isArray(list) || list.length === 0) return 0;
      var max = 0;
      list.forEach(function(cur) {
        if (Number(cur.value || 0) > max) max = Number(cur.value || 0);
      });
      return max;
    },
    minValue: function(list) {
      if (!Array.isArray(list) || list.length === 0) return 0;
      var values = list.map(function(item) {
        return Number(item.value || 0);
      });
      return Math.min.apply(null, values);
    },
    avgValue: function(list) {
      if (!Array.isArray(list) || list.length === 0) return 0;
      var sum = 0;
      list.forEach(function(cur) {
        sum += Number(cur.value || 0);
      });
      return Math.round(sum / list.length);
    },
    avgSleep: function() {
      if (this.sleepSeries.length === 0) return 0;
      var sum = 0;
      this.sleepSeries.forEach(function(cur) {
        sum += cur.total || 0;
      });
      return sum / this.sleepSeries.length;
    },
    minSleep: function() {
      if (this.sleepSeries.length === 0) return 0;
      var min = this.sleepSeries[0].total || 0;
      this.sleepSeries.forEach(function(cur) {
        if (cur.total < min) min = cur.total;
      });
      return min;
    },
    formatSleepStat: function(minutes) {
      var h = Math.floor(minutes / 60);
      var m = Math.round(minutes % 60);
      return h + "h" + m + "m";
    },
    formatWeightStat: function(list, type) {
      var valid = list.filter(function(item) {
        return Number(item.value) > 0;
      });
      if (valid.length === 0) return "--";
      var values = valid.map(function(item) {
        return Number(item.value);
      });
      if (type === "max") return Math.max.apply(null, values).toFixed(1);
      if (type === "min") return Math.min.apply(null, values).toFixed(1);
      var sum = 0;
      values.forEach(function(v) { sum += v; });
      return (sum / values.length).toFixed(1);
    },
    fetchTrend: function() {
      var self = this;
      var userId = uni.getStorageSync("userId") || 1;
      request("/api/statistics/trend", "GET", { userId: userId, period: this.period }).then(function(data) {
        self.series = (data && data.series) ? data.series : {};
        self.fetchAiInsight(true);
      }).catch(function() {
        self.series = {};
        self.aiSummary = "";
        self.aiAdvice = [];
      });
    },
    buildAiPrompt: function() {
      var dim = this.dimension;
      var label = this.currentDimLabel;
      var range = this.periodLabel;
      var dataContext = "";
      var specificHint = "";

      if (dim === "steps") {
        var max = this.stepsMax;
        var avg = this.avgValue(this.stepsSeries);
        var min = this.minValue(this.stepsSeries);
        var trend = max > avg ? "上升" : avg > min ? "稳定" : "波动";
        dataContext = "步数数据：最高" + max + "步，平均" + avg + "步，最低" + min + "步，整体趋势" + trend + "。";
        specificHint = "建议需具体到运动方式（如快走、慢跑、爬楼梯）、时间段（如饭后、上下班途中）、持续时间。";
      } else if (dim === "sleep") {
        var avgSleep = this.avgSleep();
        var maxSleep = this.sleepMax;
        var minSleep = this.minSleep();
        var avgHours = Math.floor(avgSleep / 60);
        var avgMins = Math.round(avgSleep % 60);
        dataContext = "睡眠数据：平均睡眠" + avgHours + "小时" + avgMins + "分钟，最长" + this.formatSleepStat(maxSleep) + "，最短" + this.formatSleepStat(minSleep) + "。";
        specificHint = "建议需具体到入睡时间（如23点前）、睡前准备（如提前1小时关灯）、睡眠环境。";
      } else if (dim === "diet") {
        var max = this.dietMax;
        var avg = this.avgValue(this.series.diet || []);
        var min = this.minValue(this.series.diet || []);
        dataContext = "饮食热量数据：最高" + max + "kcal，平均" + avg + "kcal，最低" + min + "kcal。";
        specificHint = "建议需具体到餐次、食物种类、分量。";
      } else if (dim === "weight") {
        var avgWeight = this.formatWeightStat(this.weightSeries, "avg");
        var maxWeight = this.formatWeightStat(this.weightSeries, "max");
        var minWeight = this.formatWeightStat(this.weightSeries, "min");
        dataContext = "体重数据：平均" + avgWeight + "kg，最高" + maxWeight + "kg，最低" + minWeight + "kg。";
        specificHint = "建议需具体到饮食调整、运动计划、称重习惯。";
      } else if (dim === "period") {
        var periodDays = this.periodSeries.filter(function(item) { return item.flow > 0; }).length;
        var flowLevels = { low: 0, medium: 0, high: 0 };
        this.periodSeries.forEach(function(item) {
          if (item.flow === 1) flowLevels.low++;
          else if (item.flow === 2) flowLevels.medium++;
          else if (item.flow === 3) flowLevels.high++;
        });
        dataContext = "经期数据：本周期共记录" + periodDays + "天经期，其中量少" + flowLevels.low + "天、量中" + flowLevels.medium + "天、量多" + flowLevels.high + "天。";
        specificHint = "建议需具体到经期护理、运动调整、饮食补充。";
      }

      return "你是一位专业健康顾问。请根据" + range + "的" + label + "数据生成JSON格式回复：\n"
        + "{ \"summary\": \"3-4句话总结\", \"advice\": [\"建议1\",\"建议2\",\"建议3\",\"建议4\",\"建议5\"] }\n"
        + "要求：summary总结趋势，advice为5条具体可执行建议。不要寒暄。\n"
        + specificHint + "\n数据背景：" + dataContext;
    },
    fetchAiInsight: function(force) {
      var self = this;
      if (this.aiLoading) return;
      if (!force && this.aiSummary) return;
      this.aiLoading = true;
      this.aiError = "";
      var userId = uni.getStorageSync("userId") || 1;
      var prompt = this.buildAiPrompt();
      request("/api/ai/chat", "POST", { userId: userId, message: prompt, store: false }).then(function(data) {
        var raw = data && data.content ? String(data.content) : "";
        var parsed = self.safeParseJson(raw);
        if (parsed && parsed.summary) {
          self.aiSummary = parsed.summary;
          self.aiAdvice = Array.isArray(parsed.advice) ? parsed.advice.slice(0, 5) : [];
        } else {
          self.aiSummary = raw || "暂无解读";
          self.aiAdvice = [];
        }
      }).catch(function() {
        self.aiError = "分析失败，请稍后再试";
      }).finally(function() {
        self.aiLoading = false;
      });
    },
    safeParseJson: function(text) {
      if (!text) return null;
      try {
        return JSON.parse(text);
      } catch (e) {
        var first = text.indexOf("{");
        var last = text.lastIndexOf("}");
        if (first !== -1 && last !== -1) {
          try {
            return JSON.parse(text.slice(first, last + 1));
          } catch (_) {}
        }
        return null;
      }
    }
  }
};
</script>

<style>
/* 页面背景 - 米白底 */
page {
  background: #FAF8F5;
}

.page {
  padding: 16rpx 20rpx 140rpx;
}

/* 时间切换 - 胶囊按钮 */
.period-tabs {
  display: flex;
  gap: 12rpx;
  margin-bottom: 16rpx;
}

.period-tab {
  flex: 1;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 28rpx;
  background: #fff;
  border: 1rpx solid #E9E1D8;
}

.period-tab.active {
  background: #A23F00;
  border-color: #A23F00;
}

.period-tab-text {
  font-size: 24rpx;
  color: #564337;
  font-weight: 500;
}

.period-tab.active .period-tab-text {
  color: #fff;
}

/* 维度切换 - 胶囊按钮 */
.dim-scroll {
  margin-bottom: 16rpx;
}

.dim-tabs {
  display: flex;
  gap: 12rpx;
  padding: 0 4rpx;
}

.dim-tab {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10rpx 24rpx;
  border-radius: 24rpx;
  background: #fff;
  border: 1rpx solid #E9E1D8;
}

.dim-tab.active {
  border-color: #A23F00;
  background: #fff;
}

.dim-label {
  font-size: 24rpx;
  color: #564337;
  font-weight: 500;
  white-space: nowrap;
}

.dim-tab.active .dim-label {
  color: #A23F00;
  font-weight: 600;
}

/* 图表卡片 - 白色卡片，大圆角 */
.chart-card {
  background: #fff;
  border-radius: 52rpx;
  padding: 20rpx;
  border: 1rpx solid #E9E1D8;
  box-shadow: 0 6rpx 16rpx rgba(0,0,0,0.04);
  margin-bottom: 16rpx;
}

.chart-header {
  margin-bottom: 12rpx;
}

.chart-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.chart-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1a1c1a;
}

.chart-badge {
  padding: 4rpx 12rpx;
  border-radius: 12rpx;
  background: #FAF8F5;
  border: 1rpx solid #E9E1D8;
  font-size: 20rpx;
  color: #564337;
  font-weight: 500;
}

.chart-legend {
  display: flex;
  gap: 20rpx;
  margin-top: 10rpx;
  flex-wrap: wrap;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.legend-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 4rpx;
}

.legend-label {
  font-size: 20rpx;
  color: #564337;
}

/* 图例颜色 - 暖色系 */
.legend-deep { background: #c2410c; }
.legend-light { background: #E9E1D8; }
.legend-breakfast { background: #fcd34d; }
.legend-lunch { background: #C25E00; }
.legend-dinner { background: #A23F00; }
.legend-snack { background: #8B3500; }

/* 图表区域 */
.chart-body {
  margin-top: 12rpx;
}

.bar-chart {
  display: flex;
  gap: 8rpx;
  height: 200rpx;
}

.bar-y-axis {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 2rpx 0;
}

.y-label {
  font-size: 18rpx;
  color: #94a3b8;
  text-align: right;
  width: 48rpx;
}

.bar-container {
  flex: 1;
  display: flex;
  justify-content: space-between;
  gap: 8rpx;
  position: relative;
  padding: 0 4rpx;
}

/* 达标虚线 */
.target-line {
  position: absolute;
  left: 0;
  right: 0;
  height: 1rpx;
  background: #A23F00;
  z-index: 10;
  display: flex;
  align-items: center;
}

.target-line::before {
  content: "";
  position: absolute;
  left: 0;
  right: 0;
  border-top: 2rpx dashed #A23F00;
}

.target-label {
  position: absolute;
  right: 0;
  top: -24rpx;
  font-size: 16rpx;
  color: #A23F00;
  background: #fff;
  padding: 0 4rpx;
  white-space: nowrap;
}

.bar-item {
  flex: 1;
  max-width: 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
}

.bar-wrapper {
  width: 100%;
  height: 160rpx;
  position: relative;
}

.bar-bg {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 100%;
  background: #FAF8F5;
  border-radius: 4rpx;
}

.bar-fill {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  border-radius: 4rpx;
  background: linear-gradient(180deg, #C25E00 0%, #A23F00 100%);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 4rpx;
}

/* 达标柱子 */
.bar-exceed {
  background: linear-gradient(180deg, #A23F00 0%, #8B3500 100%);
}

/* 睡眠柱子 */
.bar-sleep {
  background: linear-gradient(180deg, #C25E00 0%, #A23F00 100%);
}

.bar-sleep.bar-exceed {
  background: linear-gradient(180deg, #A23F00 0%, #8B3500 100%);
}

.bar-value {
  font-size: 16rpx;
  color: #fff;
  font-weight: 600;
}

.bar-label {
  font-size: 18rpx;
  color: #94a3b8;
  text-align: center;
}

.bar-stack {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
  border-radius: 4rpx;
  overflow: hidden;
}

.stack-seg {
  width: 100%;
}

/* 饮食状态标签 */
.diet-status {
  position: absolute;
  top: -22rpx;
  left: 50%;
  transform: translateX(-50%);
  padding: 2rpx 8rpx;
  border-radius: 6rpx;
  font-size: 14rpx;
  white-space: nowrap;
}

.diet-high {
  background: #fef2f2;
  border: 1rpx solid #fecaca;
}

.diet-high .diet-status-text {
  color: #ef4444;
}

.diet-normal {
  background: #fff7ed;
  border: 1rpx solid #E9E1D8;
}

.diet-normal .diet-status-text {
  color: #8B3500;
}

.diet-low {
  background: #fefce8;
  border: 1rpx solid #fef08a;
}

.diet-low .diet-status-text {
  color: #ca8a04;
}

.diet-status-text {
  font-size: 14rpx;
  font-weight: 500;
}

/* 饮食总热量标签 */
.diet-total-label {
  position: absolute;
  bottom: 4rpx;
  left: 0;
  right: 0;
  display: flex;
  justify-content: center;
}

.diet-total-text {
  font-size: 14rpx;
  color: #fff;
  font-weight: 600;
  text-shadow: 0 1rpx 2rpx rgba(0, 0, 0, 0.3);
}

/* 堆叠颜色 - 暖色系 */
.stack-light { background: #E9E1D8; }
.stack-deep { background: #c2410c; }
.stack-breakfast { background: #fcd34d; }
.stack-lunch { background: #C25E00; }
.stack-dinner { background: #A23F00; }
.stack-snack { background: #8B3500; }

/* 折线图 */
.line-chart {
  display: flex;
  gap: 8rpx;
  height: 200rpx;
}

.line-y-axis {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 2rpx 0;
}

.line-container {
  flex: 1;
  position: relative;
  height: 160rpx;
}

.line-grid {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.grid-line {
  position: absolute;
  left: 0;
  right: 0;
  height: 1rpx;
  background: #f0f0f0;
}

.grid-line:nth-child(1) { top: 0; }
.grid-line:nth-child(2) { top: 50%; }
.grid-line:nth-child(3) { bottom: 0; }

.line-area {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.line-point {
  position: absolute;
  transform: translate(-50%, 50%);
}

.point-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: #fff;
  border: 3rpx solid #A23F00;
}

.point-line {
  position: absolute;
  right: 8rpx;
  bottom: 8rpx;
  height: 3rpx;
  background: #A23F00;
  transform-origin: right center;
}

.line-labels {
  display: flex;
  justify-content: space-between;
  margin-top: 4rpx;
}

.line-label {
  font-size: 18rpx;
  color: #94a3b8;
  text-align: center;
}

/* 体重柱状图 */
.bar-weight {
  background: linear-gradient(180deg, #C25E00 0%, #A23F00 100%);
}

/* 经期热力图 */
.period-heatmap {
  padding: 4rpx 0;
}

.period-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10rpx;
}

.period-title {
  font-size: 22rpx;
  color: #564337;
  font-weight: 500;
}

.period-legend {
  display: flex;
  gap: 10rpx;
}

.period-legend-item {
  display: flex;
  align-items: center;
  gap: 4rpx;
}

.period-legend-dot {
  width: 14rpx;
  height: 14rpx;
  border-radius: 3rpx;
}

.period-legend-label {
  font-size: 18rpx;
  color: #564337;
}

.period-level-0 { background: #FAF8F5; }
.period-level-1 { background: #FEE2E2; }
.period-level-2 { background: #FCA5A5; }
.period-level-3 { background: #F87171; }

.period-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 6rpx;
}

.period-cell {
  width: 52rpx;
  height: 52rpx;
  border-radius: 8rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.period-flow-0 { background: #FAF8F5; }
.period-flow-1 { background: #FEE2E2; }
.period-flow-2 { background: #FCA5A5; }
.period-flow-3 { background: #F87171; }

.period-flow-dot {
  position: absolute;
  bottom: 4rpx;
  right: 4rpx;
  width: 6rpx;
  height: 6rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.8);
}

.period-cell-label {
  font-size: 18rpx;
  color: #564337;
}

.period-flow-2 .period-cell-label,
.period-flow-3 .period-cell-label {
  color: #fff;
}

/* 统计数据 - 三列均分 */
.stats-section {
  display: flex;
  gap: 12rpx;
  margin-top: 16rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid #E9E1D8;
}

.stat-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
  padding: 12rpx 6rpx;
  border-radius: 30rpx;
  background: #FAF8F5;
  border: 1rpx solid #E9E1D8;
}

.stat-highlight {
  background: #FAF8F5;
  border: 1rpx solid #A23F00;
}

.stat-label {
  font-size: 20rpx;
  color: #564337;
}

.stat-value-row {
  display: flex;
  align-items: baseline;
  gap: 2rpx;
}

.stat-value {
  font-size: 28rpx;
  font-weight: 600;
  color: #1a1c1a;
}

.stat-value-theme {
  color: #A23F00;
}

.stat-unit {
  font-size: 18rpx;
  color: #564337;
}

.stat-desc {
  font-size: 18rpx;
  color: #A23F00;
  margin-top: 2rpx;
}

/* AI 分析卡片 */
.ai-card {
  background: #fff;
  border-radius: 52rpx;
  padding: 24rpx;
  border: 1rpx solid #E9E1D8;
  box-shadow: 0 6rpx 16rpx rgba(0,0,0,0.04);
}

.ai-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.ai-title-wrap {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.ai-title-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.ai-icon {
  width: 44rpx;
  height: 44rpx;
  border-radius: 50%;
  background: #A23F00;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 22rpx;
  font-weight: 700;
}

.ai-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1a1c1a;
}

.ai-subtitle {
  font-size: 24rpx;
  color: #564337;
  padding-left: 56rpx;
}

.ai-action {
  flex-shrink: 0;
}

.ai-refresh-btn {
  padding: 8rpx 20rpx;
  border-radius: 20rpx;
  background: #fff;
  border: 1rpx solid #E9E1D8;
}

.ai-refresh-text {
  font-size: 22rpx;
  color: #A23F00;
  font-weight: 500;
}

/* 加载状态 */
.ai-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  padding: 30rpx 0;
}

.loading-spinner {
  width: 40rpx;
  height: 40rpx;
  border: 3rpx solid #f0f0f0;
  border-top-color: #A23F00;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text {
  font-size: 22rpx;
  color: #564337;
}

/* 错误状态 */
.ai-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  padding: 24rpx 0;
}

.error-icon {
  font-size: 36rpx;
}

.error-text {
  font-size: 22rpx;
  color: #ef4444;
}

.error-retry {
  margin-top: 6rpx;
  padding: 8rpx 20rpx;
  border-radius: 12rpx;
  background: #fef2f2;
  border: 1rpx solid #fecaca;
}

.error-retry-text {
  font-size: 22rpx;
  color: #ef4444;
}

/* 分析结果 */
.ai-result {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

/* AI总结 - 浅米色背景，左侧橙色竖线 */
.ai-summary-section {
  padding: 20rpx 24rpx;
  border-radius: 30rpx;
  background: #FAF8F5;
  border-left: 4rpx solid #A23F00;
}

.ai-summary-text {
  font-size: 26rpx;
  color: #1a1c1a;
  line-height: 1.8;
  letter-spacing: 0.5rpx;
}

/* 行动建议标题 */
.ai-advice-title {
  font-size: 26rpx;
  color: #564337;
  text-align: center;
  margin-bottom: 14rpx;
}

.ai-advice-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

/* 行动建议 - 浅橙底圆角块 */
.advice-item {
  display: flex;
  align-items: flex-start;
  gap: 14rpx;
  padding: 16rpx 20rpx;
  background: #FAF8F5;
  border: 1rpx solid #E9E1D8;
  border-radius: 30rpx;
}

/* 橙色圆形编号 */
.advice-number {
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  background: #A23F00;
  color: #fff;
  font-size: 22rpx;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.advice-text {
  flex: 1;
  font-size: 26rpx;
  color: #334155;
  line-height: 1.7;
  letter-spacing: 0.5rpx;
}
</style>
