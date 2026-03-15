package com.healthtracker.controller;

import com.healthtracker.entity.DietRecord;
import com.healthtracker.entity.ExerciseRecord;
import com.healthtracker.entity.HealthRecord;
import com.healthtracker.entity.Medication;
import com.healthtracker.entity.SleepRecord;
import com.healthtracker.entity.WeRunRecord;
import com.healthtracker.entity.WeightRecord;
import com.healthtracker.service.DietRecordService;
import com.healthtracker.service.ExerciseRecordService;
import com.healthtracker.service.HealthRecordService;
import com.healthtracker.service.MedicationService;
import com.healthtracker.service.SleepRecordService;
import com.healthtracker.service.WeRunRecordService;
import com.healthtracker.service.WeightRecordService;
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
@RequestMapping("/api/records")
public class RecordSummaryController {
    private final ExerciseRecordService exerciseRecordService;
    private final DietRecordService dietRecordService;
    private final SleepRecordService sleepRecordService;
    private final WeightRecordService weightRecordService;
    private final HealthRecordService healthRecordService;
    private final MedicationService medicationService;
    private final WeRunRecordService weRunRecordService;

    public RecordSummaryController(ExerciseRecordService exerciseRecordService,
                                   DietRecordService dietRecordService,
                                   SleepRecordService sleepRecordService,
                                   WeightRecordService weightRecordService,
                                   HealthRecordService healthRecordService,
                                   MedicationService medicationService,
                                   WeRunRecordService weRunRecordService) {
        this.exerciseRecordService = exerciseRecordService;
        this.dietRecordService = dietRecordService;
        this.sleepRecordService = sleepRecordService;
        this.weightRecordService = weightRecordService;
        this.healthRecordService = healthRecordService;
        this.medicationService = medicationService;
        this.weRunRecordService = weRunRecordService;
    }

    @GetMapping("/summary")
    public Map<String, Object> summary(@RequestParam Long userId) {
        LocalDate today = LocalDate.now();
        Map<String, Object> result = new HashMap<>();

        WeRunRecord steps = weRunRecordService.lambdaQuery()
            .eq(WeRunRecord::getUserId, userId)
            .eq(WeRunRecord::getRecordDate, today)
            .one();
        ExerciseRecord exercise = exerciseRecordService.lambdaQuery()
            .eq(ExerciseRecord::getUserId, userId)
            .eq(ExerciseRecord::getDate, today)
            .orderByDesc(ExerciseRecord::getId)
            .last("LIMIT 1")
            .one();
        Map<String, Object> exerciseMap = new HashMap<>();
        int stepCount = steps == null ? 0 : steps.getSteps();
        if (stepCount == 0 && exercise != null && exercise.getSteps() != null) {
            stepCount = exercise.getSteps();
        }
        exerciseMap.put("steps", stepCount);
        if (exercise != null) {
            exerciseMap.put("type", exercise.getType());
            exerciseMap.put("steps", exercise.getSteps());
            exerciseMap.put("duration", exercise.getDuration());
            exerciseMap.put("calories", exercise.getCalories());
        }
        result.put("exercise", exerciseMap);

        DietRecord diet = dietRecordService.lambdaQuery()
            .eq(DietRecord::getUserId, userId)
            .eq(DietRecord::getDate, today)
            .orderByDesc(DietRecord::getId)
            .last("LIMIT 1")
            .one();
        Map<String, Object> dietMap = new HashMap<>();
        if (diet != null) {
            dietMap.put("mealType", diet.getMealType());
            dietMap.put("foodName", diet.getFoodName());
            dietMap.put("calories", diet.getCalories());
            dietMap.put("protein", diet.getProtein());
            dietMap.put("carbs", diet.getCarbs());
            dietMap.put("fat", diet.getFat());
        }
        result.put("diet", dietMap);

        SleepRecord sleep = sleepRecordService.lambdaQuery()
            .eq(SleepRecord::getUserId, userId)
            .orderByDesc(SleepRecord::getStartTime)
            .last("LIMIT 1")
            .one();
        Map<String, Object> sleepMap = new HashMap<>();
        if (sleep != null) {
            if (sleep.getStartTime() != null && sleep.getEndTime() != null) {
                long minutes = Duration.between(sleep.getStartTime(), sleep.getEndTime()).toMinutes();
                sleepMap.put("duration", (minutes / 60) + "小时" + (minutes % 60) + "分");
                if (sleep.getRoutine() == null || sleep.getRoutine().isBlank()) {
                    String routine = sleep.getStartTime().toLocalTime().toString() + "-" +
                        sleep.getEndTime().toLocalTime().toString();
                    sleepMap.put("routine", routine);
                }
            }
            sleepMap.put("quality", sleep.getQuality());
            if (!sleepMap.containsKey("routine")) {
                sleepMap.put("routine", sleep.getRoutine());
            }
            sleepMap.put("startTime", sleep.getStartTime());
            sleepMap.put("endTime", sleep.getEndTime());
        }
        result.put("sleep", sleepMap);

        List<WeightRecord> weightList = weightRecordService.lambdaQuery()
            .eq(WeightRecord::getUserId, userId)
            .orderByDesc(WeightRecord::getDate)
            .orderByDesc(WeightRecord::getId)
            .last("LIMIT 2")
            .list();
        Map<String, Object> weightMap = new HashMap<>();
        if (!weightList.isEmpty()) {
            WeightRecord latest = weightList.get(0);
            weightMap.put("weight", latest.getWeight());
            weightMap.put("bmi", latest.getBmi());
            if (weightList.size() > 1) {
                double diff = latest.getWeight() - weightList.get(1).getWeight();
                String trend = diff > 0.1 ? "上升" : diff < -0.1 ? "下降" : "平稳";
                weightMap.put("trend", trend);
            } else {
                weightMap.put("trend", "平稳");
            }
        }
        result.put("weight", weightMap);

        HealthRecord health = healthRecordService.lambdaQuery()
            .eq(HealthRecord::getUserId, userId)
            .orderByDesc(HealthRecord::getDate)
            .orderByDesc(HealthRecord::getId)
            .last("LIMIT 1")
            .one();
        Map<String, Object> healthMap = new HashMap<>();
        if (health != null) {
            healthMap.put("systolic", health.getSystolic());
            healthMap.put("diastolic", health.getDiastolic());
            healthMap.put("heartRate", health.getHeartRate());
        }
        result.put("health", healthMap);

        Medication medication = medicationService.lambdaQuery()
            .eq(Medication::getUserId, userId)
            .orderByDesc(Medication::getId)
            .last("LIMIT 1")
            .one();
        Map<String, Object> medicationMap = new HashMap<>();
        if (medication != null) {
            medicationMap.put("drugName", medication.getDrugName());
            medicationMap.put("dosage", medication.getDosage());
            medicationMap.put("frequency", medication.getFrequency());
            medicationMap.put("remindTime", medication.getRemindTime());
            medicationMap.put("stock", medication.getStock());
            medicationMap.put("stockThreshold", medication.getStockThreshold());
        }
        result.put("medication", medicationMap);

        return result;
    }
}
