package com.healthtracker.controller;

import com.healthtracker.entity.DietRecord;
import com.healthtracker.entity.ExerciseRecord;
import com.healthtracker.entity.SleepRecord;
import com.healthtracker.entity.WeRunRecord;
import com.healthtracker.service.DietRecordService;
import com.healthtracker.service.ExerciseRecordService;
import com.healthtracker.service.SleepRecordService;
import com.healthtracker.service.WeRunRecordService;
import java.time.Duration;
import java.time.LocalDate;
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

    public StatisticsController(DietRecordService dietRecordService,
                                ExerciseRecordService exerciseRecordService,
                                SleepRecordService sleepRecordService,
                                WeRunRecordService weRunRecordService) {
        this.dietRecordService = dietRecordService;
        this.exerciseRecordService = exerciseRecordService;
        this.sleepRecordService = sleepRecordService;
        this.weRunRecordService = weRunRecordService;
    }

    @GetMapping("/overview")
    public Map<String, Object> overview(@RequestParam Long userId, @RequestParam String period) {
        LocalDate date = LocalDate.now();
        List<WeRunRecord> steps = weRunRecordService.lambdaQuery()
            .eq(WeRunRecord::getUserId, userId)
            .eq(WeRunRecord::getRecordDate, date)
            .list();
        int stepTotal = steps.stream().mapToInt(item -> item.getSteps() == null ? 0 : item.getSteps()).sum();

        List<DietRecord> diets = dietRecordService.listByUserAndDate(userId, date);
        int dietCalories = diets.stream().mapToInt(item -> item.getCalories() == null ? 0 : item.getCalories()).sum();

        List<ExerciseRecord> exercises = exerciseRecordService.listByUserAndDate(userId, date);
        int exerciseCalories = exercises.stream().mapToInt(item -> item.getCalories() == null ? 0 : item.getCalories()).sum();

        List<SleepRecord> sleeps = sleepRecordService.listByUserAndDate(userId, date);
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
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("period", period);
        data.put("steps", stepTotal > 0 ? String.valueOf(stepTotal) : "0");
        data.put("sleep", sleepText);
        data.put("calories", String.valueOf(dietCalories + exerciseCalories));
        data.put("dietCalories", dietCalories);
        data.put("exerciseCalories", exerciseCalories);
        return data;
    }
}
