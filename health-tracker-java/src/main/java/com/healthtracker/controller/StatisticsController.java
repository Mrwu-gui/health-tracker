package com.healthtracker.controller;

import com.healthtracker.entity.DietRecord;
import com.healthtracker.entity.ExerciseRecord;
import com.healthtracker.entity.HealthRecord;
import com.healthtracker.entity.PeriodRecord;
import com.healthtracker.entity.SleepRecord;
import com.healthtracker.entity.WeRunRecord;
import com.healthtracker.entity.WeightRecord;
import com.healthtracker.service.DietRecordService;
import com.healthtracker.service.ExerciseRecordService;
import com.healthtracker.service.HealthRecordService;
import com.healthtracker.service.PeriodRecordService;
import com.healthtracker.service.SleepRecordService;
import com.healthtracker.service.WeRunRecordService;
import com.healthtracker.service.WeightRecordService;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {
    private final DietRecordService dietRecordService;
    private final ExerciseRecordService exerciseRecordService;
    private final SleepRecordService sleepRecordService;
    private final WeRunRecordService weRunRecordService;
    private final WeightRecordService weightRecordService;
    private final HealthRecordService healthRecordService;
    private final PeriodRecordService periodRecordService;

    public StatisticsController(DietRecordService dietRecordService,
                                ExerciseRecordService exerciseRecordService,
                                SleepRecordService sleepRecordService,
                                WeRunRecordService weRunRecordService,
                                WeightRecordService weightRecordService,
                                HealthRecordService healthRecordService,
                                PeriodRecordService periodRecordService) {
        this.dietRecordService = dietRecordService;
        this.exerciseRecordService = exerciseRecordService;
        this.sleepRecordService = sleepRecordService;
        this.weRunRecordService = weRunRecordService;
        this.weightRecordService = weightRecordService;
        this.healthRecordService = healthRecordService;
        this.periodRecordService = periodRecordService;
    }

    @GetMapping("/overview")
    public Map<String, Object> overview(@RequestParam Long userId, @RequestParam String period) {
        LocalDate endDate = LocalDate.now();
        LocalDate today = endDate;
        LocalDate yesterday = endDate.minusDays(1);
        LocalDate startDate = switch (period) {
            case "week" -> endDate.minusDays(6);
            case "month" -> endDate.minusDays(29);
            case "half" -> endDate.minusDays(179);
            default -> endDate;
        };

        List<WeRunRecord> steps = weRunRecordService.lambdaQuery()
            .eq(WeRunRecord::getUserId, userId)
            .between(WeRunRecord::getRecordDate, startDate, endDate)
            .list();
        int stepTotal = steps.stream().mapToInt(item -> item.getSteps() == null ? 0 : item.getSteps()).sum();
        int stepsToday = steps.stream()
            .filter(item -> today.equals(item.getRecordDate()))
            .mapToInt(item -> item.getSteps() == null ? 0 : item.getSteps())
            .sum();
        int stepsYesterday;
        if (startDate.isAfter(yesterday)) {
            stepsYesterday = weRunRecordService.lambdaQuery()
                .eq(WeRunRecord::getUserId, userId)
                .eq(WeRunRecord::getRecordDate, yesterday)
                .list()
                .stream()
                .mapToInt(item -> item.getSteps() == null ? 0 : item.getSteps())
                .sum();
        } else {
            stepsYesterday = steps.stream()
                .filter(item -> yesterday.equals(item.getRecordDate()))
                .mapToInt(item -> item.getSteps() == null ? 0 : item.getSteps())
                .sum();
        }

        List<DietRecord> diets = dietRecordService.lambdaQuery()
            .eq(DietRecord::getUserId, userId)
            .between(DietRecord::getDate, startDate, endDate)
            .list();
        int dietCalories = diets.stream().mapToInt(item -> item.getCalories() == null ? 0 : item.getCalories()).sum();
        int dietCaloriesToday = diets.stream()
            .filter(item -> today.equals(item.getDate()))
            .mapToInt(item -> item.getCalories() == null ? 0 : item.getCalories())
            .sum();
        int dietCaloriesYesterday;
        if (startDate.isAfter(yesterday)) {
            dietCaloriesYesterday = dietRecordService.listByUserAndDate(userId, yesterday).stream()
                .mapToInt(item -> item.getCalories() == null ? 0 : item.getCalories())
                .sum();
        } else {
            dietCaloriesYesterday = diets.stream()
                .filter(item -> yesterday.equals(item.getDate()))
                .mapToInt(item -> item.getCalories() == null ? 0 : item.getCalories())
                .sum();
        }

        List<ExerciseRecord> exercises = exerciseRecordService.lambdaQuery()
            .eq(ExerciseRecord::getUserId, userId)
            .between(ExerciseRecord::getDate, startDate, endDate)
            .list();
        int exerciseCalories = exercises.stream().mapToInt(item -> item.getCalories() == null ? 0 : item.getCalories()).sum();
        int exerciseMinutes = exercises.stream().mapToInt(item -> item.getDuration() == null ? 0 : item.getDuration()).sum();

        List<SleepRecord> sleeps = sleepRecordService.lambdaQuery()
            .eq(SleepRecord::getUserId, userId)
            .between(SleepRecord::getRecordDate, startDate, endDate)
            .orderByAsc(SleepRecord::getStartTime)
            .list();
        long sleepMinutes = 0;
        long sleepMinutesToday = 0;
        long sleepMinutesYesterday = 0;
        if (!sleeps.isEmpty()) {
            SleepRecord latest = sleeps.get(sleeps.size() - 1);
            if (latest.getStartTime() != null && latest.getEndTime() != null) {
                sleepMinutes = Duration.between(latest.getStartTime(), latest.getEndTime()).toMinutes();
            }
        }
        if (!sleeps.isEmpty()) {
            for (SleepRecord item : sleeps) {
                if (item.getStartTime() == null || item.getEndTime() == null) continue;
                LocalDate recordDate = item.getRecordDate();
                if (recordDate == null) {
                    recordDate = item.getStartTime().toLocalDate();
                }
                long minutes = Math.max(0, Duration.between(item.getStartTime(), item.getEndTime()).toMinutes());
                if (today.equals(recordDate)) {
                    sleepMinutesToday = Math.max(sleepMinutesToday, minutes);
                }
                if (yesterday.equals(recordDate)) {
                    sleepMinutesYesterday = Math.max(sleepMinutesYesterday, minutes);
                }
            }
        }
        if (sleepMinutesYesterday == 0) {
            List<SleepRecord> sleepYesterdayList = sleepRecordService.listByUserAndDate(userId, yesterday);
            for (SleepRecord item : sleepYesterdayList) {
                if (item.getStartTime() == null || item.getEndTime() == null) continue;
                long minutes = Math.max(0, Duration.between(item.getStartTime(), item.getEndTime()).toMinutes());
                sleepMinutesYesterday = Math.max(sleepMinutesYesterday, minutes);
            }
        }
        String sleepText = sleepMinutes > 0
            ? (sleepMinutes / 60) + "小时" + (sleepMinutes % 60) + "分"
            : "0小时0分";

        List<WeightRecord> weights = weightRecordService.lambdaQuery()
            .eq(WeightRecord::getUserId, userId)
            .between(WeightRecord::getDate, startDate, endDate)
            .list();
        Double weight = weights.isEmpty() ? null : weights.get(weights.size() - 1).getWeight();
        Double bmi = weights.isEmpty() ? null : weights.get(weights.size() - 1).getBmi();
        Double weightToday = null;
        Double weightYesterday = null;
        if (!weights.isEmpty()) {
            for (WeightRecord item : weights) {
                if (item.getDate() == null || item.getWeight() == null) continue;
                if (today.equals(item.getDate())) weightToday = item.getWeight();
                if (yesterday.equals(item.getDate())) weightYesterday = item.getWeight();
            }
        }
        if (startDate.isAfter(yesterday) && weightYesterday == null) {
            List<WeightRecord> weightYesterdayList = weightRecordService.listByUserAndDate(userId, yesterday);
            if (!weightYesterdayList.isEmpty() && weightYesterdayList.get(0).getWeight() != null) {
                weightYesterday = weightYesterdayList.get(0).getWeight();
            }
        }

        List<HealthRecord> healthRecords = healthRecordService.lambdaQuery()
            .eq(HealthRecord::getUserId, userId)
            .between(HealthRecord::getDate, startDate, endDate)
            .list();
        String bpStatus = healthRecords.isEmpty() ? "暂无" : "正常";

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("period", period);
        data.put("steps", stepTotal > 0 ? String.valueOf(stepTotal) : "0");
        data.put("stepsToday", stepsToday);
        data.put("stepsYesterday", stepsYesterday);
        data.put("stepsDelta", stepsToday - stepsYesterday);
        data.put("sleep", sleepText);
        data.put("sleepMinutesToday", sleepMinutesToday);
        data.put("sleepMinutesYesterday", sleepMinutesYesterday);
        data.put("sleepDeltaMinutes", sleepMinutesToday - sleepMinutesYesterday);
        data.put("calories", String.valueOf(dietCalories + exerciseCalories));
        data.put("dietCaloriesToday", dietCaloriesToday);
        data.put("dietCaloriesYesterday", dietCaloriesYesterday);
        data.put("dietDeltaCalories", dietCaloriesToday - dietCaloriesYesterday);
        data.put("dietCalories", dietCalories);
        data.put("exerciseCalories", exerciseCalories);
        data.put("exerciseMinutes", exerciseMinutes);
        data.put("dietCount", diets.size());
        data.put("weight", weight == null ? "" : weight);
        data.put("bmi", bmi == null ? "" : bmi);
        data.put("weightToday", weightToday);
        data.put("weightYesterday", weightYesterday);
        data.put("weightDelta", (weightToday != null && weightYesterday != null) ? weightToday - weightYesterday : null);
        data.put("bpStatus", bpStatus);
        return data;
    }

    @GetMapping("/trend")
    public Map<String, Object> trend(@RequestParam Long userId, @RequestParam String period) {
        int days = switch (period) {
            case "week" -> 7;
            case "month" -> 30;
            case "half" -> 180;
            default -> 7;
        };
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1L);

        List<WeRunRecord> steps = weRunRecordService.lambdaQuery()
            .eq(WeRunRecord::getUserId, userId)
            .between(WeRunRecord::getRecordDate, startDate, endDate)
            .list();
        Map<LocalDate, Integer> stepsMap = steps.stream()
            .collect(Collectors.groupingBy(WeRunRecord::getRecordDate,
                Collectors.summingInt(item -> item.getSteps() == null ? 0 : item.getSteps())));

        List<SleepRecord> sleeps = sleepRecordService.lambdaQuery()
            .eq(SleepRecord::getUserId, userId)
            .between(SleepRecord::getStartTime, startDate.atStartOfDay(), endDate.atTime(23, 59, 59))
            .list();
        Map<LocalDate, Double> sleepMap = new TreeMap<>();
        Map<LocalDate, Integer> sleepDeepMap = new TreeMap<>();
        Map<LocalDate, Integer> sleepLightMap = new TreeMap<>();
        for (SleepRecord item : sleeps) {
            LocalDate key = item.getRecordDate();
            if (key == null && item.getStartTime() != null) {
                key = item.getStartTime().toLocalDate();
            }
            if (key == null || item.getStartTime() == null || item.getEndTime() == null) continue;
            double hours = Math.max(0, Duration.between(item.getStartTime(), item.getEndTime()).toMinutes() / 60.0);
            sleepMap.put(key, Math.max(sleepMap.getOrDefault(key, 0.0), hours));
            if (item.getDeepSleepMinutes() != null) {
                sleepDeepMap.put(key, Math.max(sleepDeepMap.getOrDefault(key, 0), item.getDeepSleepMinutes()));
            }
            if (item.getLightSleepMinutes() != null) {
                sleepLightMap.put(key, Math.max(sleepLightMap.getOrDefault(key, 0), item.getLightSleepMinutes()));
            }
        }

        List<WeightRecord> weights = weightRecordService.lambdaQuery()
            .eq(WeightRecord::getUserId, userId)
            .between(WeightRecord::getDate, startDate, endDate)
            .list();
        Map<LocalDate, Double> weightMap = new TreeMap<>();
        for (WeightRecord item : weights) {
            if (item.getDate() == null || item.getWeight() == null) continue;
            weightMap.put(item.getDate(), item.getWeight());
        }

        List<DietRecord> diets = dietRecordService.lambdaQuery()
            .eq(DietRecord::getUserId, userId)
            .between(DietRecord::getDate, startDate, endDate)
            .list();
        Map<LocalDate, Integer> dietTotalMap = new TreeMap<>();
        Map<LocalDate, Integer> dietBreakfastMap = new TreeMap<>();
        Map<LocalDate, Integer> dietLunchMap = new TreeMap<>();
        Map<LocalDate, Integer> dietDinnerMap = new TreeMap<>();
        Map<LocalDate, Integer> dietSnackMap = new TreeMap<>();
        for (DietRecord item : diets) {
            if (item.getDate() == null) continue;
            int calories = item.getCalories() == null ? 0 : item.getCalories();
            LocalDate key = item.getDate();
            dietTotalMap.put(key, dietTotalMap.getOrDefault(key, 0) + calories);
            String mealType = item.getMealType() == null ? "" : item.getMealType();
            if (mealType.contains("早")) {
                dietBreakfastMap.put(key, dietBreakfastMap.getOrDefault(key, 0) + calories);
            } else if (mealType.contains("午")) {
                dietLunchMap.put(key, dietLunchMap.getOrDefault(key, 0) + calories);
            } else if (mealType.contains("晚")) {
                dietDinnerMap.put(key, dietDinnerMap.getOrDefault(key, 0) + calories);
            } else if (mealType.contains("加")) {
                dietSnackMap.put(key, dietSnackMap.getOrDefault(key, 0) + calories);
            } else {
                dietSnackMap.put(key, dietSnackMap.getOrDefault(key, 0) + calories);
            }
        }

        List<PeriodRecord> periodRecords = periodRecordService.lambdaQuery()
            .eq(PeriodRecord::getUserId, userId)
            .list();

        Map<String, List<Map<String, Object>>> series = new HashMap<>();

        List<Map<String, Object>> stepsSeries = buildSeries(startDate, days, period,
            date -> stepsMap.getOrDefault(date, 0));
        List<Map<String, Object>> sleepSeries = buildSeries(startDate, days, period,
            date -> sleepMap.getOrDefault(date, 0.0));
        List<Map<String, Object>> weightSeries = buildSeries(startDate, days, period,
            date -> weightMap.getOrDefault(date, 0.0));
        List<Map<String, Object>> dietSeries = buildSeries(startDate, days, period,
            date -> dietTotalMap.getOrDefault(date, 0));
        List<Map<String, Object>> dietBreakfastSeries = buildSeries(startDate, days, period,
            date -> dietBreakfastMap.getOrDefault(date, 0));
        List<Map<String, Object>> dietLunchSeries = buildSeries(startDate, days, period,
            date -> dietLunchMap.getOrDefault(date, 0));
        List<Map<String, Object>> dietDinnerSeries = buildSeries(startDate, days, period,
            date -> dietDinnerMap.getOrDefault(date, 0));
        List<Map<String, Object>> dietSnackSeries = buildSeries(startDate, days, period,
            date -> dietSnackMap.getOrDefault(date, 0));
        List<Map<String, Object>> sleepDeepSeries = buildSeries(startDate, days, period,
            date -> sleepDeepMap.getOrDefault(date, 0));
        List<Map<String, Object>> sleepLightSeries = buildSeries(startDate, days, period,
            date -> sleepLightMap.getOrDefault(date, 0));
        List<Map<String, Object>> periodSeries = buildSeries(startDate, days, period,
            date -> periodFlowForDate(date, periodRecords));

        series.put("steps", stepsSeries);
        series.put("sleep", sleepSeries);
        series.put("sleepDeep", sleepDeepSeries);
        series.put("sleepLight", sleepLightSeries);
        series.put("weight", weightSeries);
        series.put("diet", dietSeries);
        series.put("dietBreakfast", dietBreakfastSeries);
        series.put("dietLunch", dietLunchSeries);
        series.put("dietDinner", dietDinnerSeries);
        series.put("dietSnack", dietSnackSeries);
        series.put("period", periodSeries);

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("period", period);
        data.put("series", series);
        return data;
    }

    private List<Map<String, Object>> buildSeries(LocalDate startDate,
                                                 int days,
                                                 String period,
                                                 java.util.function.Function<LocalDate, Number> supplier) {
        return java.util.stream.IntStream.range(0, days)
            .mapToObj(i -> {
                LocalDate date = startDate.plusDays(i);
                String label = formatLabel(date, period, i);
                Map<String, Object> item = new HashMap<>();
                item.put("label", label);
                item.put("value", supplier.apply(date));
                return item;
            })
            .collect(Collectors.toList());
    }

    private String formatLabel(LocalDate date, String period, int index) {
        if ("week".equals(period)) {
            return date.getMonthValue() + "/" + date.getDayOfMonth();
        }
        if ("month".equals(period)) {
            return index % 5 == 0 ? date.getDayOfMonth() + "日" : "";
        }
        if ("half".equals(period)) {
            return index % 14 == 0 ? date.getMonthValue() + "月" : "";
        }
        return date.getMonthValue() + "/" + date.getDayOfMonth();
    }

    private int periodFlowForDate(LocalDate date, List<PeriodRecord> records) {
        if (records == null || records.isEmpty()) return 0;
        for (PeriodRecord record : records) {
            if (record.getStartDate() == null) continue;
            LocalDate end = record.getEndDate() == null ? record.getStartDate() : record.getEndDate();
            if ((date.isEqual(record.getStartDate()) || date.isAfter(record.getStartDate()))
                && (date.isEqual(end) || date.isBefore(end))) {
                return record.getFlow() == null ? 1 : record.getFlow();
            }
        }
        return 0;
    }
}
