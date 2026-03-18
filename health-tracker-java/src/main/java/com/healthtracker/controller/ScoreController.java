package com.healthtracker.controller;

import com.healthtracker.entity.DietRecord;
import com.healthtracker.entity.Goal;
import com.healthtracker.entity.SleepRecord;
import com.healthtracker.entity.WeRunRecord;
import com.healthtracker.entity.WeightRecord;
import com.healthtracker.service.DietRecordService;
import com.healthtracker.service.GoalService;
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
@RequestMapping("/api/score")
public class ScoreController {
    private final WeRunRecordService weRunRecordService;
    private final SleepRecordService sleepRecordService;
    private final DietRecordService dietRecordService;
    private final WeightRecordService weightRecordService;
    private final GoalService goalService;

    public ScoreController(WeRunRecordService weRunRecordService,
                           SleepRecordService sleepRecordService,
                           DietRecordService dietRecordService,
                           WeightRecordService weightRecordService,
                           GoalService goalService) {
        this.weRunRecordService = weRunRecordService;
        this.sleepRecordService = sleepRecordService;
        this.dietRecordService = dietRecordService;
        this.weightRecordService = weightRecordService;
        this.goalService = goalService;
    }

    @GetMapping("/rule")
    public Map<String, Object> rule() {
        Map<String, Object> data = new HashMap<>();
        data.put("summary", "评分由步数、睡眠、饮食、体重四项组成，突出日常习惯质量。");
        data.put("items", List.of(
            Map.of("label", "步数/运动（30%）", "desc", "按目标完成度计算，达标得分最高"),
            Map.of("label", "睡眠（30%）", "desc", "7~8小时最佳，过短或过长会扣分"),
            Map.of("label", "饮食（20%）", "desc", "清淡均衡更高分，高油高糖会扣分"),
            Map.of("label", "体重（20%）", "desc", "稳定或接近目标得高分")
        ));
        data.put("tips", List.of());
        return data;
    }

    @GetMapping("/today")
    public Map<String, Object> today(@RequestParam Long userId) {
        LocalDate today = LocalDate.now();
        ScoreResult todayScore = calcScore(userId, today);
        ScoreResult yesterdayScore = calcScore(userId, today.minusDays(1));

        int diff = yesterdayScore.total > 0 ? todayScore.total - yesterdayScore.total : 0;
        String diffText = diff == 0
            ? "状态" + statusText(todayScore.total) + " · 今日综合评估"
            : (diff > 0 ? "比昨天 +" + diff + " 分" : "比昨天 " + diff + " 分");

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("date", today.toString());
        data.put("score", todayScore.total);
        data.put("status", statusText(todayScore.total));
        data.put("diff", diff);
        data.put("diffText", diffText);
        data.put("breakdown", todayScore.breakdown);
        return data;
    }

    private ScoreResult calcScore(Long userId, LocalDate date) {
        ScoreResult result = new ScoreResult();
        boolean hasSteps = hasStepsRecord(userId, date);
        boolean hasSleep = hasSleepRecord(userId, date);
        boolean hasDiet = hasDietRecord(userId, date);
        boolean hasWeight = hasWeightRecord(userId, date);

        int steps = hasSteps ? getSteps(userId, date) : 0;
        int stepsTarget = getStepsTarget(userId);
        double completion = stepsTarget > 0 ? (double) steps / stepsTarget : 0;
        int stepsScore = hasSteps ? scoreSteps(completion) : 0;

        double sleepHours = hasSleep ? getSleepHours(userId, date) : 0;
        int sleepScore = hasSleep ? scoreSleep(sleepHours) : 0;

        DietResult dietResult = hasDiet ? getDietScore(userId, date) : new DietResult("未记录", 0);
        int dietScore = hasDiet ? dietResult.score : 0;

        WeightResult weightResult = hasWeight ? getWeightScore(userId, date) : new WeightResult(0, 0);
        int weightScore = hasWeight ? weightResult.score : 0;

        int totalWeight = (hasSteps ? 30 : 0) + (hasSleep ? 30 : 0) + (hasDiet ? 20 : 0) + (hasWeight ? 20 : 0);
        int base = stepsScore + sleepScore + dietScore + weightScore;
        int total = totalWeight > 0 ? (int) Math.round(base * 100.0 / totalWeight) : 0;
        if (totalWeight > 0 && total < 30) total = 30;
        if (total > 100) total = 100;

        Map<String, Object> breakdown = new HashMap<>();
        breakdown.put("steps", steps);
        breakdown.put("stepsTarget", stepsTarget);
        breakdown.put("stepsScore", stepsScore);
        breakdown.put("sleepHours", sleepHours);
        breakdown.put("sleepScore", sleepScore);
        breakdown.put("dietType", dietResult.type);
        breakdown.put("dietScore", dietScore);
        breakdown.put("weightDelta", weightResult.delta);
        breakdown.put("weightScore", weightScore);
        breakdown.put("base", base);
        breakdown.put("weightTotal", totalWeight);
        breakdown.put("hasSteps", hasSteps);
        breakdown.put("hasSleep", hasSleep);
        breakdown.put("hasDiet", hasDiet);
        breakdown.put("hasWeight", hasWeight);
        breakdown.put("missingDimensions", buildMissing(hasSteps, hasSleep, hasDiet, hasWeight));
        breakdown.put("total", total);

        result.total = total;
        result.breakdown = breakdown;
        return result;
    }

    private List<String> buildMissing(boolean hasSteps, boolean hasSleep, boolean hasDiet, boolean hasWeight) {
        List<String> list = new java.util.ArrayList<>();
        if (!hasSteps) list.add("steps");
        if (!hasSleep) list.add("sleep");
        if (!hasDiet) list.add("diet");
        if (!hasWeight) list.add("weight");
        return list;
    }

    private int getSteps(Long userId, LocalDate date) {
        List<WeRunRecord> records = weRunRecordService.lambdaQuery()
            .eq(WeRunRecord::getUserId, userId)
            .eq(WeRunRecord::getRecordDate, date)
            .list();
        return records.stream().mapToInt(item -> item.getSteps() == null ? 0 : item.getSteps()).sum();
    }

    private int getStepsTarget(Long userId) {
        Goal goal = goalService.lambdaQuery()
            .eq(Goal::getUserId, userId)
            .eq(Goal::getGoalType, 1)
            .eq(Goal::getPeriod, "day")
            .orderByDesc(Goal::getId)
            .last("limit 1")
            .one();
        return goal != null && goal.getTargetValue() != null ? goal.getTargetValue() : 10000;
    }

    private double getSleepHours(Long userId, LocalDate date) {
        List<SleepRecord> records = sleepRecordService.listByUserAndDate(userId, date);
        if (records.isEmpty()) return 0;
        SleepRecord latest = records.get(0);
        if (latest.getStartTime() == null || latest.getEndTime() == null) return 0;
        long minutes = Math.max(0, Duration.between(latest.getStartTime(), latest.getEndTime()).toMinutes());
        return Math.round((minutes / 60.0) * 10.0) / 10.0;
    }

    private DietResult getDietScore(Long userId, LocalDate date) {
        List<DietRecord> diets = dietRecordService.lambdaQuery()
            .eq(DietRecord::getUserId, userId)
            .eq(DietRecord::getDate, date)
            .list();
        if (diets.isEmpty()) {
            return new DietResult("未记录", 0);
        }
        double avgCalories = diets.stream()
            .mapToInt(item -> item.getCalories() == null ? 0 : item.getCalories())
            .average()
            .orElse(0);
        if (avgCalories <= 600) return new DietResult("健康", 20);
        if (avgCalories <= 900) return new DietResult("一般", 10);
        return new DietResult("高油高糖", 0);
    }

    private WeightResult getWeightScore(Long userId, LocalDate date) {
        List<WeightRecord> weights = weightRecordService.lambdaQuery()
            .eq(WeightRecord::getUserId, userId)
            .between(WeightRecord::getDate, date.minusDays(7), date)
            .orderByDesc(WeightRecord::getDate)
            .list();
        if (weights.isEmpty()) return new WeightResult(0, 0);
        double latest = weights.get(0).getWeight() == null ? 0 : weights.get(0).getWeight();
        if (weights.size() < 2) return new WeightResult(0, 10);
        double prev = weights.get(1).getWeight() == null ? latest : weights.get(1).getWeight();
        double delta = Math.abs(latest - prev);
        if (delta <= 0.5) return new WeightResult(delta, 20);
        if (delta <= 1.5) return new WeightResult(delta, 10);
        return new WeightResult(delta, 0);
    }

    private int scoreSteps(double completion) {
        if (completion >= 1) return 30;
        if (completion >= 0.8) return 20 + (int) Math.round((completion - 0.8) / 0.2 * 10);
        if (completion >= 0.5) return 10 + (int) Math.round((completion - 0.5) / 0.3 * 10);
        return (int) Math.round(Math.max(0, completion / 0.5 * 10));
    }

    private int scoreSleep(double hours) {
        if (hours >= 7 && hours <= 8) return 30;
        if ((hours >= 6 && hours < 7) || (hours > 8 && hours <= 9)) return 20;
        if (hours > 0 && (hours < 6 || hours > 9)) return 10;
        if (hours >= 10 || hours <= 4) return 0;
        return 0;
    }

    private boolean hasStepsRecord(Long userId, LocalDate date) {
        return !weRunRecordService.lambdaQuery()
            .eq(WeRunRecord::getUserId, userId)
            .eq(WeRunRecord::getRecordDate, date)
            .list()
            .isEmpty();
    }

    private boolean hasSleepRecord(Long userId, LocalDate date) {
        return !sleepRecordService.listByUserAndDate(userId, date).isEmpty();
    }

    private boolean hasDietRecord(Long userId, LocalDate date) {
        return !dietRecordService.lambdaQuery()
            .eq(DietRecord::getUserId, userId)
            .eq(DietRecord::getDate, date)
            .list()
            .isEmpty();
    }

    private boolean hasWeightRecord(Long userId, LocalDate date) {
        return !weightRecordService.listByUserAndDate(userId, date).isEmpty();
    }

    private String statusText(int score) {
        if (score >= 90) return "优秀";
        if (score >= 75) return "良好";
        if (score >= 60) return "一般";
        return "较差";
    }

    private static class ScoreResult {
        int total = 0;
        Map<String, Object> breakdown = new HashMap<>();
    }

    private static class DietResult {
        String type;
        int score;

        DietResult(String type, int score) {
            this.type = type;
            this.score = score;
        }
    }

    private static class WeightResult {
        double delta;
        int score;

        WeightResult(double delta, int score) {
            this.delta = delta;
            this.score = score;
        }
    }
}
