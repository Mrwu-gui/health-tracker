package com.healthtracker.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.healthtracker.dto.AiCallbackRequest;
import com.healthtracker.entity.DietRecord;
import com.healthtracker.entity.ExerciseRecord;
import com.healthtracker.entity.FamilyMember;
import com.healthtracker.entity.Goal;
import com.healthtracker.entity.HealthRecord;
import com.healthtracker.entity.Medication;
import com.healthtracker.entity.MedicationRecord;
import com.healthtracker.entity.PeriodRecord;
import com.healthtracker.entity.Reminder;
import com.healthtracker.entity.SleepRecord;
import com.healthtracker.entity.WeightRecord;
import com.healthtracker.entity.User;
import com.healthtracker.service.DietRecordService;
import com.healthtracker.service.ExerciseRecordService;
import com.healthtracker.service.FamilyMemberService;
import com.healthtracker.service.GoalService;
import com.healthtracker.service.HealthRecordService;
import com.healthtracker.service.MedicationRecordService;
import com.healthtracker.service.MedicationService;
import com.healthtracker.service.PeriodRecordService;
import com.healthtracker.service.ReminderService;
import com.healthtracker.service.SleepRecordService;
import com.healthtracker.service.WeightRecordService;
import com.healthtracker.service.UserService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiCallbackController {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final ObjectMapper JSON = new ObjectMapper();

    private final ReminderService reminderService;
    private final MedicationService medicationService;
    private final MedicationRecordService medicationRecordService;
    private final ExerciseRecordService exerciseRecordService;
    private final DietRecordService dietRecordService;
    private final SleepRecordService sleepRecordService;
    private final WeightRecordService weightRecordService;
    private final HealthRecordService healthRecordService;
    private final GoalService goalService;
    private final PeriodRecordService periodRecordService;
    private final FamilyMemberService familyMemberService;
    private final UserService userService;

    @Value("${ai.callback.enabled:false}")
    private boolean callbackEnabled;

    public AiCallbackController(ReminderService reminderService,
                                MedicationService medicationService,
                                MedicationRecordService medicationRecordService,
                                ExerciseRecordService exerciseRecordService,
                                DietRecordService dietRecordService,
                                SleepRecordService sleepRecordService,
                                WeightRecordService weightRecordService,
                                HealthRecordService healthRecordService,
                                GoalService goalService,
                                PeriodRecordService periodRecordService,
                                FamilyMemberService familyMemberService,
                                UserService userService) {
        this.reminderService = reminderService;
        this.medicationService = medicationService;
        this.medicationRecordService = medicationRecordService;
        this.exerciseRecordService = exerciseRecordService;
        this.dietRecordService = dietRecordService;
        this.sleepRecordService = sleepRecordService;
        this.weightRecordService = weightRecordService;
        this.healthRecordService = healthRecordService;
        this.goalService = goalService;
        this.periodRecordService = periodRecordService;
        this.familyMemberService = familyMemberService;
        this.userService = userService;
    }

    @PostMapping("/callback")
    public Map<String, Object> callback(@RequestBody AiCallbackRequest request) {
        if (!callbackEnabled) {
            throw new IllegalArgumentException("AI 回调已关闭");
        }
        Map<String, Object> result = new HashMap<>();
        String intent = request.getIntent() == null ? "" : request.getIntent().trim();
        Long userId = request.getUserId();
        String wxOpenid = request.getWxOpenid();
        if (userId == null && wxOpenid != null && !wxOpenid.isBlank()) {
            User user = userService.findByWxOpenid(wxOpenid.trim());
            if (user != null) {
                userId = user.getId();
            }
        }
        LoggerFactory.getLogger(AiCallbackController.class)
            .info("AI callback received (recording disabled). userId={} wxOpenid={} intent={} payload={}",
                userId, mask(wxOpenid), intent, safeJson(request.getPayload()));

        result.put("ok", true);
        result.put("intent", intent);
        result.put("message", "AI 回调记录功能已暂时关闭");
        return result;
    }

    private HandleResult handleReminder(Long userId, JsonNode payload) {
        Integer type = intVal(payload, "type");
        if (type == null) type = 1;
        String title = text(payload, "title");
        if (title == null || title.isBlank()) {
            title = defaultReminderTitle(type);
        }
        Reminder reminder = new Reminder();
        reminder.setUserId(userId);
        reminder.setTitle(title);
        reminder.setType(type);
        reminder.setContent(text(payload, "content"));
        reminder.setRemindTime(dateTime(payload, "remind_time"));
        Integer status = intVal(payload, "status");
        reminder.setStatus(status == null ? 0 : status);
        Integer sourceType = intVal(payload, "source_type");
        reminder.setSourceType(sourceType == null ? 1 : sourceType);
        reminder.setRelatedRecordId(longVal(payload, "related_record_id"));
        reminder.setFinishTime(dateTime(payload, "finish_time"));
        Integer priority = intVal(payload, "priority");
        reminder.setPriority(priority == null ? 1 : priority);
        reminder.setCreatedAt(LocalDateTime.now());
        reminderService.save(reminder);
        return HandleResult.success(reminder.getId());
    }

    private HandleResult handleMedication(Long userId, JsonNode payload) {
        String drugName = text(payload, "drug_name");
        String dosage = text(payload, "dosage");
        String frequency = text(payload, "frequency");
        LocalDate startDate = date(payload, "start_date");
        if (isBlank(drugName) || isBlank(dosage) || isBlank(frequency) || startDate == null) {
            return HandleResult.skip("用药信息不完整");
        }

        Medication medication = new Medication();
        medication.setUserId(userId);
        medication.setDrugName(drugName);
        medication.setDosage(dosage);
        medication.setFrequency(frequency);
        medication.setRemindTime(normalizeRemindTime(text(payload, "remind_time")));
        medication.setStartDate(startDate);
        medication.setEndDate(date(payload, "end_date"));
        medication.setNotes(text(payload, "notes"));
        medicationService.save(medication);
        return HandleResult.success(medication.getId());
    }

    private HandleResult handleMedicationRecord(Long userId, JsonNode payload) {
        Long medicationId = longVal(payload, "medication_id");
        LocalDate date = date(payload, "date");
        LocalTime time = time(payload, "time");
        Integer status = intVal(payload, "status");
        if (medicationId == null || date == null || time == null || status == null) {
            return HandleResult.skip("用药打卡信息不完整");
        }

        MedicationRecord record = new MedicationRecord();
        record.setUserId(userId);
        record.setMedicationId(medicationId);
        record.setDate(date);
        record.setTime(time);
        record.setStatus(status);
        medicationRecordService.save(record);
        return HandleResult.success(record.getId());
    }

    private HandleResult handleExercise(Long userId, JsonNode payload) {
        String type = text(payload, "type");
        Integer duration = intVal(payload, "duration");
        Integer calories = intVal(payload, "calories");
        LocalDate date = date(payload, "date");
        if (isBlank(type)) type = "运动";
        if (duration == null) duration = 0;
        if (calories == null) calories = 0;
        if (date == null) date = LocalDate.now();

        ExerciseRecord record = new ExerciseRecord();
        record.setUserId(userId);
        record.setType(type);
        record.setSteps(intVal(payload, "steps"));
        record.setDuration(duration);
        record.setCalories(calories);
        record.setDate(date);
        exerciseRecordService.save(record);
        return HandleResult.success(record.getId());
    }

    private HandleResult handleDiet(Long userId, JsonNode payload) {
        String mealType = text(payload, "meal_type");
        String foodName = text(payload, "food_name");
        Integer calories = intVal(payload, "calories");
        LocalDate date = date(payload, "date");
        if (isBlank(mealType)) mealType = "其他";
        if (isBlank(foodName)) foodName = "未指定";
        if (calories == null) calories = 0;
        if (date == null) date = LocalDate.now();

        DietRecord record = new DietRecord();
        record.setUserId(userId);
        record.setMealType(mealType);
        record.setFoodName(foodName);
        record.setCalories(calories);
        record.setNote(text(payload, "note"));
        record.setDate(date);
        dietRecordService.save(record);
        return HandleResult.success(record.getId());
    }

    private HandleResult handleSleep(Long userId, JsonNode payload) {
        LocalDateTime startTime = dateTime(payload, "start_time");
        LocalDateTime endTime = dateTime(payload, "end_time");
        if (startTime == null || endTime == null) {
            return HandleResult.skip("睡眠时间不完整");
        }

        SleepRecord record = new SleepRecord();
        record.setUserId(userId);
        record.setStartTime(startTime);
        record.setEndTime(endTime);
        record.setDeepSleepMinutes(intVal(payload, "deep_sleep_minutes"));
        record.setLightSleepMinutes(intVal(payload, "light_sleep_minutes"));
        record.setQuality(text(payload, "quality"));
        sleepRecordService.save(record);
        return HandleResult.success(record.getId());
    }

    private HandleResult handleWeight(Long userId, JsonNode payload) {
        Double weight = doubleVal(payload, "weight");
        LocalDate date = date(payload, "date");
        if (weight == null) {
            return HandleResult.skip("缺少体重");
        }
        if (date == null) date = LocalDate.now();

        WeightRecord record = new WeightRecord();
        record.setUserId(userId);
        record.setWeight(weight);
        record.setBmi(doubleVal(payload, "bmi"));
        record.setDate(date);
        weightRecordService.save(record);
        return HandleResult.success(record.getId());
    }

    private HandleResult handleHealth(Long userId, JsonNode payload) {
        LocalDate date = date(payload, "date");
        if (date == null) date = LocalDate.now();
        HealthRecord record = new HealthRecord();
        record.setUserId(userId);
        record.setSystolic(intVal(payload, "systolic"));
        record.setDiastolic(intVal(payload, "diastolic"));
        record.setHeartRate(intVal(payload, "heart_rate"));
        record.setDate(date);
        healthRecordService.save(record);
        return HandleResult.success(record.getId());
    }

    private HandleResult handleGoal(Long userId, JsonNode payload) {
        Integer goalType = intVal(payload, "goal_type");
        Integer targetValue = intVal(payload, "target_value");
        String period = text(payload, "period");
        if (goalType == null || targetValue == null || isBlank(period)) {
            return HandleResult.skip("目标信息不完整");
        }

        Goal goal = new Goal();
        goal.setUserId(userId);
        goal.setGoalType(goalType);
        goal.setTargetValue(targetValue);
        goal.setCurrentValue(0);
        goal.setPeriod(period);
        goal.setStartDate(date(payload, "start_date"));
        goal.setEndDate(date(payload, "end_date"));
        Integer status = intVal(payload, "status");
        goal.setStatus(status == null ? 1 : status);
        goal.setAiStrategy(text(payload, "ai_strategy"));
        goalService.save(goal);
        return HandleResult.success(goal.getId());
    }

    private HandleResult handlePeriod(Long userId, JsonNode payload) {
        LocalDate startDate = date(payload, "start_date");
        LocalDate endDate = date(payload, "end_date");
        if (startDate == null) {
            return HandleResult.skip("经期开始日期缺失");
        }
        PeriodRecord record = new PeriodRecord();
        record.setUserId(userId);
        record.setStartDate(startDate);
        record.setEndDate(endDate == null ? startDate : endDate);
        record.setFlow(intVal(payload, "flow"));
        record.setNote(text(payload, "note"));
        record.setCreatedAt(LocalDateTime.now());
        periodRecordService.save(record);
        return HandleResult.success(record.getId());
    }

    private HandleResult handleFamily(Long userId, JsonNode payload) {
        String name = text(payload, "name");
        if (isBlank(name)) {
            return HandleResult.skip("家人姓名缺失");
        }
        FamilyMember member = new FamilyMember();
        member.setUserId(userId);
        member.setName(name);
        member.setRelation(text(payload, "relation"));
        member.setAge(intVal(payload, "age"));
        member.setConditionText(text(payload, "condition_text"));
        member.setRole(text(payload, "role"));
        Integer status = intVal(payload, "status");
        member.setStatus(status == null ? 1 : status);
        member.setAvatar(text(payload, "avatar"));
        member.setCreatedAt(LocalDateTime.now());
        familyMemberService.save(member);
        return HandleResult.success(member.getId());
    }

    private String normalizeRemindTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() >= 16) {
            return trimmed.substring(0, 16);
        }
        return trimmed;
    }

    private String defaultReminderTitle(Integer type) {
        if (type == null) {
            return "提醒";
        }
        switch (type) {
            case 1:
                return "运动提醒";
            case 2:
                return "饮食提醒";
            case 3:
                return "睡眠提醒";
            case 4:
                return "用药提醒";
            default:
                return "提醒";
        }
    }

    private String text(JsonNode payload, String field) {
        JsonNode node = payload.get(field);
        return node == null || node.isNull() ? null : node.asText();
    }

    private Integer intVal(JsonNode payload, String field) {
        JsonNode node = payload.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isInt() || node.isLong()) {
            return node.intValue();
        }
        String value = node.asText();
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Long longVal(JsonNode payload, String field) {
        JsonNode node = payload.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isLong() || node.isInt()) {
            return node.longValue();
        }
        String value = node.asText();
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Double doubleVal(JsonNode payload, String field) {
        JsonNode node = payload.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isDouble() || node.isFloat() || node.isInt() || node.isLong()) {
            return node.doubleValue();
        }
        String value = node.asText();
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Double.valueOf(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private LocalDate date(JsonNode payload, String field) {
        String value = text(payload, field);
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value.trim(), DATE);
    }

    private LocalDateTime dateTime(JsonNode payload, String field) {
        String value = text(payload, field);
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(value.trim(), DATE_TIME);
    }

    private LocalTime time(JsonNode payload, String field) {
        String value = text(payload, field);
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalTime.parse(value.trim(), TIME);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String safeJson(JsonNode node) {
        if (node == null || node.isNull()) {
            return "null";
        }
        try {
            String text = JSON.writeValueAsString(node);
            if (text.length() > 2000) {
                return text.substring(0, 2000) + "...";
            }
            return text;
        } catch (JsonProcessingException ex) {
            return node.toString();
        }
    }

    private String mask(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if (value.length() <= 8) {
            return value.substring(0, 2) + "***" + value.substring(value.length() - 2);
        }
        return value.substring(0, 3) + "***" + value.substring(value.length() - 4);
    }

    private static final class HandleResult {
        private final Long id;
        private final String reason;

        private HandleResult(Long id, String reason) {
            this.id = id;
            this.reason = reason;
        }

        private static HandleResult success(Long id) {
            return new HandleResult(id, null);
        }

        private static HandleResult skip(String reason) {
            return new HandleResult(null, reason);
        }
    }
}
