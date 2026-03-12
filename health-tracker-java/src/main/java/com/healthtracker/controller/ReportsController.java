package com.healthtracker.controller;

import com.healthtracker.entity.SleepRecord;
import com.healthtracker.entity.WeRunRecord;
import com.healthtracker.entity.WeightRecord;
import com.healthtracker.service.SleepRecordService;
import com.healthtracker.service.WeRunRecordService;
import com.healthtracker.service.WeightRecordService;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportsController {
    private final WeRunRecordService weRunRecordService;
    private final SleepRecordService sleepRecordService;
    private final WeightRecordService weightRecordService;

    public ReportsController(WeRunRecordService weRunRecordService,
                             SleepRecordService sleepRecordService,
                             WeightRecordService weightRecordService) {
        this.weRunRecordService = weRunRecordService;
        this.sleepRecordService = sleepRecordService;
        this.weightRecordService = weightRecordService;
    }

    @GetMapping("/summary")
    public Map<String, Object> summary(@RequestParam Long userId) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(29);
        Map<String, Object> data = new HashMap<>();

        List<WeRunRecord> steps = weRunRecordService.lambdaQuery()
            .eq(WeRunRecord::getUserId, userId)
            .ge(WeRunRecord::getRecordDate, start)
            .le(WeRunRecord::getRecordDate, end)
            .list();
        int stepSum = steps.stream().mapToInt(item -> item.getSteps() == null ? 0 : item.getSteps()).sum();
        int avgSteps = steps.isEmpty() ? 0 : Math.round((float) stepSum / steps.size());
        data.put("avgSteps", avgSteps > 0 ? String.valueOf(avgSteps) : "--");

        List<SleepRecord> sleeps = sleepRecordService.lambdaQuery()
            .eq(SleepRecord::getUserId, userId)
            .ge(SleepRecord::getStartTime, start.atStartOfDay())
            .lt(SleepRecord::getStartTime, end.plusDays(1).atStartOfDay())
            .list();
        long sleepMinutes = 0;
        for (SleepRecord record : sleeps) {
            if (record.getStartTime() != null && record.getEndTime() != null) {
                sleepMinutes += Duration.between(record.getStartTime(), record.getEndTime()).toMinutes();
            }
        }
        String avgSleep = sleeps.isEmpty()
            ? "--"
            : (sleepMinutes / sleeps.size() / 60) + "小时" + (sleepMinutes / sleeps.size() % 60) + "分";
        data.put("avgSleep", avgSleep);

        List<WeightRecord> weights = weightRecordService.lambdaQuery()
            .eq(WeightRecord::getUserId, userId)
            .ge(WeightRecord::getDate, start)
            .le(WeightRecord::getDate, end)
            .orderByAsc(WeightRecord::getDate)
            .list();
        if (weights.size() >= 2) {
            double diff = weights.get(weights.size() - 1).getWeight() - weights.get(0).getWeight();
            data.put("weightChange", String.format("%.1f 千克", diff));
        } else {
            data.put("weightChange", "--");
        }
        data.put("medicationRate", "--");
        return data;
    }

    @GetMapping("/list")
    public List<Map<String, Object>> list(@RequestParam Long userId) {
        List<Map<String, Object>> list = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(WeekFields.of(Locale.CHINA).dayOfWeek(), 1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M月d日");
        for (int i = 0; i < 4; i++) {
            LocalDate start = weekStart.minusWeeks(i);
            LocalDate end = start.plusDays(6);
            int weekNumber = start.get(WeekFields.of(Locale.CHINA).weekOfWeekBasedYear());
            Map<String, Object> item = new HashMap<>();
            item.put("title", "第 " + weekNumber + " 周");
            item.put("subtitle", formatter.format(start) + " - " + formatter.format(end));
            item.put("downloadable", false);
            list.add(item);
        }
        return list;
    }

    @GetMapping("/weekly")
    public Map<String, Object> weekly(@RequestParam Long userId) {
        Map<String, Object> data = new HashMap<>();
        LocalDate today = LocalDate.now();
        List<Integer> steps = new ArrayList<>();
        int totalSteps = 0;
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            WeRunRecord record = weRunRecordService.lambdaQuery()
                .eq(WeRunRecord::getUserId, userId)
                .eq(WeRunRecord::getRecordDate, date)
                .one();
            int value = record == null ? 0 : record.getSteps();
            steps.add(value);
            totalSteps += value;
        }
        data.put("steps", steps);
        data.put("avgSteps", steps.isEmpty() ? 0 : Math.round((float) totalSteps / steps.size()));

        List<SleepRecord> sleeps = sleepRecordService.lambdaQuery()
            .eq(SleepRecord::getUserId, userId)
            .ge(SleepRecord::getStartTime, today.minusDays(6).atStartOfDay())
            .lt(SleepRecord::getStartTime, today.plusDays(1).atStartOfDay())
            .list();
        Map<String, Object> quality = new HashMap<>();
        int excellent = 0;
        int good = 0;
        int normal = 0;
        for (SleepRecord record : sleeps) {
            if ("优".equals(record.getQuality())) {
                excellent++;
            } else if ("良".equals(record.getQuality())) {
                good++;
            } else if ("一般".equals(record.getQuality())) {
                normal++;
            }
        }
        quality.put("excellent", excellent);
        quality.put("good", good);
        quality.put("normal", normal);
        data.put("sleepQuality", quality);

        List<WeightRecord> weights = weightRecordService.lambdaQuery()
            .eq(WeightRecord::getUserId, userId)
            .orderByDesc(WeightRecord::getDate)
            .last("LIMIT 2")
            .list();
        Map<String, Object> weightTrend = new HashMap<>();
        if (weights.size() >= 1) {
            weightTrend.put("from", weights.size() > 1 ? weights.get(1).getWeight() : weights.get(0).getWeight());
            weightTrend.put("to", weights.get(0).getWeight());
            if (weights.size() > 1) {
                double diff = weights.get(0).getWeight() - weights.get(1).getWeight();
                String trend = diff > 0.1 ? "上升" : diff < -0.1 ? "下降" : "平稳";
                weightTrend.put("trend", trend);
            } else {
                weightTrend.put("trend", "平稳");
            }
        } else {
            weightTrend.put("from", "--");
            weightTrend.put("to", "--");
            weightTrend.put("trend", "暂无");
        }
        data.put("weightTrend", weightTrend);

        return data;
    }
}
