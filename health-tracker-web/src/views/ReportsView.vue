<template>
  <div class="reports">
    <a-card title="周报" class="panel">
      <div class="report-grid">
        <a-card v-for="report in reports" :key="report.title" class="report" hoverable>
          <div class="report__title">{{ report.title }}</div>
          <div class="report__subtitle">{{ report.subtitle }}</div>
          <a-button type="primary" block :disabled="!report.downloadable">下载报告</a-button>
        </a-card>
      </div>
      <div v-if="!loading && reports.length === 0" class="empty">暂无周报</div>
    </a-card>

    <a-card title="月度汇总" class="panel">
      <a-descriptions :column="2">
        <a-descriptions-item label="平均步数">{{ summary.avgSteps }}</a-descriptions-item>
        <a-descriptions-item label="平均睡眠">{{ summary.avgSleep }}</a-descriptions-item>
        <a-descriptions-item label="体重变化">{{ summary.weightChange }}</a-descriptions-item>
        <a-descriptions-item label="用药达成率">{{ summary.medicationRate }}</a-descriptions-item>
      </a-descriptions>
    </a-card>
  </div>
</template>

<script setup>
  import { onMounted, ref } from "vue";
  import { api } from "../api/http";

  const loading = ref(false);
  const reports = ref([]);
  const summary = ref({
    avgSteps: "--",
    avgSleep: "--",
    weightChange: "--",
    medicationRate: "--"
  });

  const fetchReports = async () => {
    loading.value = true;
    try {
      const userId = Number(localStorage.getItem("userId") || 1);
      const list = await api.reportList(userId);
      reports.value = Array.isArray(list) ? list : [];
      const sum = await api.reportSummary(userId);
      if (sum) {
        summary.value.avgSteps = sum.avgSteps || "--";
        summary.value.avgSleep = sum.avgSleep || "--";
        summary.value.weightChange = sum.weightChange || "--";
        summary.value.medicationRate = sum.medicationRate || "--";
      }
    } finally {
      loading.value = false;
    }
  };

  onMounted(fetchReports);
</script>

<style scoped>
.reports {
  display: grid;
  gap: 18px;
}

.panel {
  border-radius: 16px;
}

.report-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.report {
  border-radius: 16px;
  text-align: center;
}

.report__title {
  font-weight: 600;
  margin-bottom: 6px;
}

.report__subtitle {
  color: #64748b;
  margin-bottom: 12px;
}

.empty {
  color: #94a3b8;
  font-size: 0.9rem;
  margin-top: 12px;
  text-align: center;
}

@media (max-width: 900px) {
  .report-grid {
    grid-template-columns: 1fr;
  }
}
</style>
