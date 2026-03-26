<template>
  <div class="bg-surface text-on-surface min-h-screen">
    <header class="sticky top-0 z-40 bg-surface/80 backdrop-blur-xl shadow-[0_10px_30px_rgba(86,67,55,0.06)]">
      <div class="max-w-[1200px] mx-auto px-6 md:px-10 h-20 flex items-center justify-between">
        <h2 class="text-xl font-extrabold tracking-tight text-on-surface">健康总览</h2>
        <div class="flex items-center gap-4">
          <button
            class="px-4 py-2 rounded-full text-sm font-semibold bg-surface-container-low text-on-surface hover:bg-surface-container-high transition-colors"
            @click="handleLogout"
          >
            退出登录
          </button>
          <div class="flex items-center gap-3">
            <div class="text-right">
              <p class="text-sm font-bold text-on-surface">{{ displayName }}</p>
            </div>
            <div class="w-10 h-10 rounded-full bg-surface-container-high overflow-hidden flex items-center justify-center text-sm font-bold text-primary">
              <img v-if="avatarUrl" :src="avatarUrl" alt="头像" class="w-full h-full object-cover" />
              <span v-else>{{ displayInitial }}</span>
            </div>
          </div>
        </div>
      </div>
    </header>

    <main class="max-w-[1200px] mx-auto px-6 md:px-10 py-10 space-y-8">
      <section class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        <div class="bg-surface-container-lowest p-8 rounded-xl shadow-[0_20px_40px_rgba(86,67,55,0.06)]">
          <div class="flex items-start justify-between mb-6">
            <div class="w-12 h-12 rounded-full bg-primary/10 flex items-center justify-center">
              <span class="material-symbols-outlined" data-icon="directions_walk">directions_walk</span>
            </div>
            <span class="text-xs font-bold" :class="stepsDelta >= 0 ? 'text-tertiary' : 'text-error'">
              <span class="material-symbols-outlined text-xs mr-1" :data-icon="stepsDelta >= 0 ? 'arrow_upward' : 'arrow_downward'">
                {{ stepsDelta >= 0 ? 'arrow_upward' : 'arrow_downward' }}
              </span>
              {{ Math.abs(stepsDelta) }}
            </span>
          </div>
          <p class="text-on-surface-variant text-sm font-medium mb-1">今日步数</p>
          <h5 class="text-3xl font-extrabold text-on-surface">{{ formatNumber(stepsToday) }}</h5>
        </div>

        <div class="bg-surface-container-lowest p-8 rounded-xl shadow-[0_20px_40px_rgba(86,67,55,0.06)]">
          <div class="flex items-start justify-between mb-6">
            <div class="w-12 h-12 rounded-full bg-secondary/10 flex items-center justify-center">
              <span class="material-symbols-outlined" data-icon="monitor_weight">monitor_weight</span>
            </div>
            <span class="text-xs font-bold" :class="weightDelta >= 0 ? 'text-error' : 'text-tertiary'">
              {{ weightDeltaText }}
            </span>
          </div>
          <p class="text-on-surface-variant text-sm font-medium mb-1">今日体重</p>
          <h5 class="text-3xl font-extrabold text-on-surface">{{ weightTodayText }}</h5>
        </div>

        <div class="bg-surface-container-lowest p-8 rounded-xl shadow-[0_20px_40px_rgba(86,67,55,0.06)]">
          <div class="flex items-start justify-between mb-6">
            <div class="w-12 h-12 rounded-full bg-on-tertiary-fixed-variant/10 flex items-center justify-center">
              <span class="material-symbols-outlined" data-icon="bedtime">bedtime</span>
            </div>
            <span class="text-xs font-bold text-tertiary">{{ sleepStatus }}</span>
          </div>
          <p class="text-on-surface-variant text-sm font-medium mb-1">睡眠时长</p>
          <h5 class="text-3xl font-extrabold text-on-surface">{{ sleepText }}</h5>
        </div>

        <div class="bg-surface-container-lowest p-8 rounded-xl shadow-[0_20px_40px_rgba(86,67,55,0.06)]">
          <div class="flex items-start justify-between mb-6">
            <div class="w-12 h-12 rounded-full bg-primary/10 flex items-center justify-center">
              <span class="material-symbols-outlined" data-icon="restaurant">restaurant</span>
            </div>
            <span class="text-xs font-bold text-on-surface-variant">今日</span>
          </div>
          <p class="text-on-surface-variant text-sm font-medium mb-1">饮食摄入</p>
          <h5 class="text-3xl font-extrabold text-on-surface">{{ formatNumber(dietCaloriesToday) }} kcal</h5>
        </div>
      </section>

      <div class="flex justify-end">
        <div class="bg-surface-container-low p-1 rounded-full flex items-center shadow-sm">
          <button
            class="px-6 py-2 rounded-full text-sm font-bold transition-all"
            :class="period === 'week' ? 'bg-white text-primary shadow-sm' : 'text-on-surface-variant hover:bg-white/50'"
            @click="changePeriod('week')"
          >
            7天
          </button>
          <button
            class="px-6 py-2 rounded-full text-sm font-bold transition-all"
            :class="period === 'month' ? 'bg-white text-primary shadow-sm' : 'text-on-surface-variant hover:bg-white/50'"
            @click="changePeriod('month')"
          >
            30天
          </button>
          <button
            class="px-6 py-2 rounded-full text-sm font-bold transition-all"
            :class="period === 'half' ? 'bg-white text-primary shadow-sm' : 'text-on-surface-variant hover:bg-white/50'"
            @click="changePeriod('half')"
          >
            半年
          </button>
        </div>
      </div>

      <section class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div class="bg-surface-container-lowest p-8 rounded-xl shadow-[0_20px_40px_rgba(86,67,55,0.06)]">
          <div class="flex items-center justify-between mb-6">
            <h3 class="text-lg font-bold text-on-surface">步数统计</h3>
            <span class="text-xs text-on-surface-variant">最近周期</span>
          </div>
          <div ref="stepsChartRef" class="h-52 w-full"></div>
        </div>

        <div class="bg-surface-container-lowest p-8 rounded-xl shadow-[0_20px_40px_rgba(86,67,55,0.06)]">
          <div class="flex items-center justify-between mb-6">
            <h3 class="text-lg font-bold text-on-surface">睡眠统计</h3>
            <span class="text-xs text-on-surface-variant">最近周期</span>
          </div>
          <div ref="sleepChartRef" class="h-52 w-full"></div>
        </div>
      </section>

      <section class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div class="bg-surface-container-lowest p-8 rounded-xl shadow-[0_20px_40px_rgba(86,67,55,0.06)]">
          <div class="flex items-center justify-between mb-6">
            <h3 class="text-lg font-bold text-on-surface">体重趋势</h3>
            <span class="text-xs text-on-surface-variant">最近周期</span>
          </div>
          <div ref="weightChartRef" class="h-52 w-full"></div>
        </div>

        <div class="bg-surface-container-lowest p-8 rounded-xl shadow-[0_20px_40px_rgba(86,67,55,0.06)]">
          <div class="flex items-center justify-between mb-6">
            <h3 class="text-lg font-bold text-on-surface">经期统计</h3>
            <span class="text-xs text-on-surface-variant">最近周期</span>
          </div>
          <div ref="periodChartRef" class="h-52 w-full"></div>
        </div>
      </section>

      <section class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div class="bg-surface-container-lowest p-8 rounded-xl shadow-[0_20px_40px_rgba(86,67,55,0.06)]">
          <div class="flex items-center justify-between mb-6">
            <h3 class="text-lg font-bold text-on-surface">饮食占比</h3>
            <span class="text-xs text-on-surface-variant">{{ dietTotal }} kcal</span>
          </div>
          <div ref="dietChartRef" class="h-64 w-full"></div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, onBeforeUnmount, ref } from "vue";
import { useRouter } from "vue-router";
import { getStatisticsOverview, getStatisticsTrend, logout } from "../api";
import * as echarts from "echarts";

const router = useRouter();
const period = ref("week");
const userId = ref(Number(localStorage.getItem("userId")) || null);
const userInfo = ref(localStorage.getItem("userInfo") ? JSON.parse(localStorage.getItem("userInfo")) : null);

const overview = ref({});
const trend = ref({
  series: { steps: [], sleep: [], weight: [], period: [], dietProtein: [], dietCarbs: [], dietFat: [] }
});

const displayName = computed(() => userInfo.value?.username || userInfo.value?.wxNickname || "用户");
const displayInitial = computed(() => displayName.value.slice(0, 1));
const avatarUrl = computed(() => userInfo.value?.avatar || userInfo.value?.wxAvatar || "");

const stepsToday = computed(() => Number(overview.value?.stepsToday || 0));
const stepsDelta = computed(() => Number(overview.value?.stepsDelta || 0));

const weightTodayText = computed(() => {
  const value = overview.value?.weightToday;
  return value ? `${value} kg` : "--";
});
const weightDeltaText = computed(() => {
  const delta = overview.value?.weightDelta;
  if (delta === null || delta === undefined) return "--";
  const sign = delta > 0 ? "+" : "";
  return `${sign}${delta.toFixed(1)}`;
});
const weightDelta = computed(() => overview.value?.weightDelta ?? 0);

const sleepText = computed(() => overview.value?.sleep || "0小时0分");
const sleepMinutesToday = computed(() => Number(overview.value?.sleepMinutesToday || 0));
const sleepStatus = computed(() => (sleepMinutesToday.value >= 420 ? "良好" : "一般"));

const dietCaloriesToday = computed(() => Number(overview.value?.dietCaloriesToday || 0));

const stepSeries = computed(() => trend.value?.series?.steps || []);
const sleepSeries = computed(() => trend.value?.series?.sleep || []);
const weightSeries = computed(() => trend.value?.series?.weight || []);
const periodSeries = computed(() => trend.value?.series?.period || []);

const dietTotals = computed(() => {
  const sumSeries = (items = []) => items.reduce((sum, item) => sum + Number(item.value || 0), 0);
  return {
    protein: sumSeries(trend.value?.series?.dietProtein || []),
    carbs: sumSeries(trend.value?.series?.dietCarbs || []),
    fat: sumSeries(trend.value?.series?.dietFat || [])
  };
});

const dietTotal = computed(() => {
  const totals = dietTotals.value;
  return Math.round(totals.protein + totals.carbs + totals.fat);
});

const dietPercentages = computed(() => {
  const total = dietTotal.value || 1;
  const totals = dietTotals.value;
  return {
    protein: Math.round((totals.protein / total) * 100),
    carbs: Math.round((totals.carbs / total) * 100),
    fat: Math.round((totals.fat / total) * 100)
  };
});

const formatNumber = (value) => new Intl.NumberFormat().format(Number(value || 0));

const fetchOverview = async () => {
  if (!userId.value) return;
  overview.value = await getStatisticsOverview(userId.value, "day");
};

const fetchTrend = async () => {
  if (!userId.value) return;
  trend.value = await getStatisticsTrend(userId.value, period.value);
  updateCharts();
};

const changePeriod = async (next) => {
  period.value = next;
  await fetchTrend();
};

const handleLogout = () => {
  logout();
  router.push("/login");
};

const stepsChartRef = ref(null);
const sleepChartRef = ref(null);
const weightChartRef = ref(null);
const periodChartRef = ref(null);
const dietChartRef = ref(null);

let stepsChart;
let sleepChart;
let weightChart;
let periodChart;
let dietChart;

const buildCategorySeries = (series = []) => ({
  labels: series.map((item) => item.label),
  values: series.map((item) => Number(item.value || 0))
});

const renderStepsChart = () => {
  if (!stepsChart && stepsChartRef.value) stepsChart = echarts.init(stepsChartRef.value);
  if (!stepsChart) return;
  const { labels, values } = buildCategorySeries(stepSeries.value);
  stepsChart.setOption({
    grid: { left: 20, right: 10, top: 10, bottom: 20, containLabel: true },
    xAxis: { type: "category", data: labels, axisLine: { show: false }, axisTick: { show: false } },
    yAxis: { type: "value", axisLine: { show: false }, splitLine: { show: false } },
    series: [{ type: "bar", data: values, barWidth: 18, itemStyle: { color: "#a23f00", borderRadius: [12, 12, 12, 12] } }]
  });
};

const renderSleepChart = () => {
  if (!sleepChart && sleepChartRef.value) sleepChart = echarts.init(sleepChartRef.value);
  if (!sleepChart) return;
  const { labels, values } = buildCategorySeries(sleepSeries.value);
  sleepChart.setOption({
    grid: { left: 20, right: 10, top: 10, bottom: 20, containLabel: true },
    xAxis: { type: "category", data: labels, axisLine: { show: false }, axisTick: { show: false } },
    yAxis: { type: "value", axisLine: { show: false }, splitLine: { show: false } },
    series: [{ type: "bar", data: values, barWidth: 18, itemStyle: { color: "#865300", borderRadius: [12, 12, 12, 12] } }]
  });
};

const renderWeightChart = () => {
  if (!weightChart && weightChartRef.value) weightChart = echarts.init(weightChartRef.value);
  if (!weightChart) return;
  const { labels, values } = buildCategorySeries(weightSeries.value);
  weightChart.setOption({
    grid: { left: 20, right: 10, top: 10, bottom: 20, containLabel: true },
    xAxis: { type: "category", data: labels, axisLine: { show: false }, axisTick: { show: false } },
    yAxis: { type: "value", axisLine: { show: false }, splitLine: { show: false } },
    series: [{
      type: "line",
      data: values,
      smooth: true,
      symbol: "circle",
      symbolSize: 6,
      lineStyle: { color: "#006d37", width: 3 },
      itemStyle: { color: "#006d37" }
    }]
  });
};

const renderPeriodChart = () => {
  if (!periodChart && periodChartRef.value) periodChart = echarts.init(periodChartRef.value);
  if (!periodChart) return;
  const { labels, values } = buildCategorySeries(periodSeries.value);
  const data = labels.map((label, idx) => [idx, 0, values[idx]]);
  periodChart.setOption({
    grid: { left: 20, right: 10, top: 10, bottom: 20, containLabel: true },
    xAxis: { type: "category", data: labels, axisLine: { show: false }, axisTick: { show: false } },
    yAxis: { type: "category", data: [""], axisLine: { show: false }, axisTick: { show: false }, axisLabel: { show: false } },
    visualMap: { show: false, min: 0, max: Math.max(...values, 1), inRange: { color: ["#fde1d1", "#a23f00"] } },
    series: [{ type: "heatmap", data, label: { show: false }, itemStyle: { borderRadius: 8 } }]
  });
};

const renderDietChart = () => {
  if (!dietChart && dietChartRef.value) dietChart = echarts.init(dietChartRef.value);
  if (!dietChart) return;
  const totals = dietTotals.value;
  dietChart.setOption({
    tooltip: { trigger: "item" },
    series: [{
      type: "pie",
      radius: ["55%", "80%"],
      avoidLabelOverlap: false,
      label: { show: false },
      data: [
        { value: totals.protein, name: "蛋白质", itemStyle: { color: "#FF8C42" } },
        { value: totals.carbs, name: "碳水", itemStyle: { color: "#FFA64D" } },
        { value: totals.fat, name: "脂肪", itemStyle: { color: "#C45B00" } }
      ]
    }]
  });
};

const updateCharts = () => {
  renderStepsChart();
  renderSleepChart();
  renderWeightChart();
  renderPeriodChart();
  renderDietChart();
};

onMounted(async () => {
  if (!userId.value) {
    router.push("/login");
    return;
  }
  try {
    await fetchOverview();
    await fetchTrend();
  } catch (error) {
    console.error(error);
  }
  window.addEventListener("resize", updateCharts);
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", updateCharts);
  stepsChart?.dispose();
  sleepChart?.dispose();
  weightChart?.dispose();
  periodChart?.dispose();
  dietChart?.dispose();
});
</script>

<style scoped>
</style>
