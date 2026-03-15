package com.healthtracker.controller;

import com.healthtracker.entity.DietRecord;
import com.healthtracker.entity.ExerciseRecord;
import com.healthtracker.entity.HealthRecord;
import com.healthtracker.entity.SleepRecord;
import com.healthtracker.entity.WeRunRecord;
import com.healthtracker.entity.WeightRecord;
import com.healthtracker.service.DietRecordService;
import com.healthtracker.service.ExerciseRecordService;
import com.healthtracker.service.HealthRecordService;
import com.healthtracker.service.SleepRecordService;
import com.healthtracker.service.WeRunRecordService;
import com.healthtracker.service.WeightRecordService;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public StatisticsController(DietRecordService dietRecordService,
                                ExerciseRecordService exerciseRecordService,
                                SleepRecordService sleepRecordService,
                                WeRunRecordService weRunRecordService,
                                WeightRecordService weightRecordService,
                                HealthRecordService healthRecordService) {
        this.dietRecordService = dietRecordService;
        this.exerciseRecordService = exerciseRecordService;
        this.sleepRecordService = sleepRecordService;
        this.weRunRecordService = weRunRecordService;
        this.weightRecordService = weightRecordService;
        this.healthRecordService = healthRecordService;
    }

    @GetMapping("/overview")
    public Map<String, Object> overview(@RequestParam Long userId, @RequestParam String period) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = switch (period) {
            case "week" -> endDate.minusDays(6);
            case "month" -> endDate.minusDays(29);
            default -> endDate;
        };

        List<WeRunRecord> steps = weRunRecordService.lambdaQuery()
            .eq(WeRunRecord::getUserId, userId)
            .between(WeRunRecord::getRecordDate, startDate, endDate)
            .list();
        int stepTotal = steps.stream().mapToInt(item -> item.getSteps() == null ? 0 : item.getSteps()).sum();

        List<DietRecord> diets = dietRecordService.lambdaQuery()
            .eq(DietRecord::getUserId, userId)
            .between(DietRecord::getDate, startDate, endDate)
            .list();
        int dietCalories = diets.stream().mapToInt(item -> item.getCalories() == null ? 0 : item.getCalories()).sum();

        List<ExerciseRecord> exercises = exerciseRecordService.lambdaQuery()
            .eq(ExerciseRecord::getUserId, userId)
            .between(ExerciseRecord::getDate, startDate, endDate)
            .list();
        int exerciseCalories = exercises.stream().mapToInt(item -> item.getCalories() == null ? 0 : item.getCalories()).sum();
        int exerciseMinutes = exercises.stream().mapToInt(item -> item.getDuration() == null ? 0 : item.getDuration()).sum();

        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.atTime(23, 59, 59);
        List<SleepRecord> sleeps = sleepRecordService.lambdaQuery()
            .eq(SleepRecord::getUserId, userId)
            .between(SleepRecord::getStartTime, startTime, endTime)
            .list();
        long sleepMinutes = 0;
        if (!sleeps.isEmpty()) {
            SleepRecord latest = sleeps.get(sleeps.size() - 1);
            if (latest.getStartTime() != null && latest.getEndTime() != null) {
                sleepMinutes = Duration.between(latest.getStartTime(), latest.getEndTime()).toMinutes();
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

        List<HealthRecord> healthRecords = healthRecordService.lambdaQuery()
            .eq(HealthRecord::getUserId, userId)
            .between(HealthRecord::getDate, startDate, endDate)
            .list();
        String bpStatus = healthRecords.isEmpty() ? "暂无" : "正常";

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("period", period);
        data.put("steps", stepTotal > 0 ? String.valueOf(stepTotal) : "0");
        data.put("sleep", sleepText);
        data.put("calories", String.valueOf(dietCalories + exerciseCalories));
        data.put("dietCalories", dietCalories);
        data.put("exerciseCalories", exerciseCalories);
        data.put("exerciseMinutes", exerciseMinutes);
        data.put("dietCount", diets.size());
        data.put("weight", weight == null ? "" : weight);
        data.put("bmi", bmi == null ? "" : bmi);
        data.put("bpStatus", bpStatus);
        return data;
    }
}
