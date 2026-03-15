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
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
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
                                FamilyMemberService familyMemberService) {
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
    }

    @PostMapping("/callback")
    public Map<String, Object> callback(@Valid @RequestBody AiCallbackRequest request) {
        Long userId = request.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("缺少 userId");
        }
        String intent = request.getIntent() == null ? "" : request.getIntent().trim();
        JsonNode payload = request.getPayload();
        if (payload == null || payload.isNull()) {
            throw new IllegalArgumentException("缺少 payload");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("intent", intent);

        switch (intent) {
            case "reminder":
                result.put("id", handleReminder(userId, payload));
                break;
            case "medication":
                result.put("id", handleMedication(userId, payload));
                break;
            case "medication_record":
                result.put("id", handleMedicationRecord(userId, payload));
                break;
            case "exercise_record":
                result.put("id", handleExercise(userId, payload));
                break;
            case "diet_record":
                result.put("id", handleDiet(userId, payload));
                break;
            case "sleep_record":
                result.put("id", handleSleep(userId, payload));
                break;
            case "weight_record":
                result.put("id", handleWeight(userId, payload));
                break;
            case "health_record":
                result.put("id", handleHealth(userId, payload));
                break;
            case "goal":
                result.put("id", handleGoal(userId, payload));
                break;
            case "period_record":
                result.put("id", handlePeriod(userId, payload));
                break;
            case "family_member":
                result.put("id", handleFamily(userId, payload));
                break;
            default:
                throw new IllegalArgumentException("未知意图: " + intent);
        }

        return result;
    }

    private Long handleReminder(Long userId, JsonNode payload) {
        Integer type = intVal(payload, "type");
        if (type == null) {
            throw new IllegalArgumentException("缺少 type");
        }
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
        reminder.setStatus(text(payload, "status") == null ? "待提醒" : text(payload, "status"));
        reminder.setCreatedAt(LocalDateTime.now());
        reminderService.save(reminder);
        return reminder.getId();
    }

    private Long handleMedication(Long userId, JsonNode payload) {
        String drugName = requireText(payload, "drug_name");
        String dosage = requireText(payload, "dosage");
        String frequency = requireText(payload, "frequency");
        LocalDate startDate = requireDate(payload, "start_date");

        Medication medication = new Medication();
        medication.setUserId(userId);
        medication.setDrugName(drugName);
        medication.setDosage(dosage);
        medication.setFrequency(frequency);
        medication.setRemindTime(text(payload, "remind_time"));
        medication.setStartDate(startDate);
        medication.setEndDate(date(payload, "end_date"));
        medication.setNotes(text(payload, "notes"));
        medicationService.save(medication);
        return medication.getId();
    }

    private Long handleMedicationRecord(Long userId, JsonNode payload) {
        LocalDate date = requireDate(payload, "date");
        LocalTime time = requireTime(payload, "time");
        String status = requireText(payload, "status");

        MedicationRecord record = new MedicationRecord();
        record.setUserId(userId);
        record.setMedicationId(longVal(payload, "medication_id"));
        record.setDate(date);
        record.setTime(time);
        record.setStatus(status);
        medicationRecordService.save(record);
        return record.getId();
    }

    private Long handleExercise(Long userId, JsonNode payload) {
        String type = requireText(payload, "type");
        Integer duration = requireInt(payload, "duration");
        Integer calories = requireInt(payload, "calories");
        LocalDate date = requireDate(payload, "date");

        ExerciseRecord record = new ExerciseRecord();
        record.setUserId(userId);
        record.setType(type);
        record.setSteps(intVal(payload, "steps"));
        record.setDuration(duration);
        record.setCalories(calories);
        record.setDate(date);
        exerciseRecordService.save(record);
        return record.getId();
    }

    private Long handleDiet(Long userId, JsonNode payload) {
        String mealType = requireText(payload, "meal_type");
        String foodName = requireText(payload, "food_name");
        Integer calories = requireInt(payload, "calories");
        LocalDate date = requireDate(payload, "date");

        DietRecord record = new DietRecord();
        record.setUserId(userId);
        record.setMealType(mealType);
        record.setFoodName(foodName);
        record.setCalories(calories);
        record.setNote(text(payload, "note"));
        record.setDate(date);
        dietRecordService.save(record);
        return record.getId();
    }

    private Long handleSleep(Long userId, JsonNode payload) {
        LocalDateTime startTime = requireDateTime(payload, "start_time");
        LocalDateTime endTime = requireDateTime(payload, "end_time");

        SleepRecord record = new SleepRecord();
        record.setUserId(userId);
        record.setStartTime(startTime);
        record.setEndTime(endTime);
        record.setDeepSleepMinutes(intVal(payload, "deep_sleep_minutes"));
        record.setLightSleepMinutes(intVal(payload, "light_sleep_minutes"));
        record.setQuality(text(payload, "quality"));
        sleepRecordService.save(record);
        return record.getId();
    }

    private Long handleWeight(Long userId, JsonNode payload) {
        Double weight = requireDouble(payload, "weight");
        LocalDate date = requireDate(payload, "date");

        WeightRecord record = new WeightRecord();
        record.setUserId(userId);
        record.setWeight(weight);
        record.setBmi(doubleVal(payload, "bmi"));
        record.setDate(date);
        weightRecordService.save(record);
        return record.getId();
    }

    private Long handleHealth(Long userId, JsonNode payload) {
        LocalDate date = requireDate(payload, "date");
        HealthRecord record = new HealthRecord();
        record.setUserId(userId);
        record.setSystolic(intVal(payload, "systolic"));
        record.setDiastolic(intVal(payload, "diastolic"));
        record.setHeartRate(intVal(payload, "heart_rate"));
        record.setDate(date);
        healthRecordService.save(record);
        return record.getId();
    }

    private Long handleGoal(Long userId, JsonNode payload) {
        Integer goalType = requireInt(payload, "goal_type");
        Integer targetValue = requireInt(payload, "target_value");
        String period = requireText(payload, "period");

        Goal goal = new Goal();
        goal.setUserId(userId);
        goal.setGoalType(goalType);
        goal.setTargetValue(targetValue);
        goal.setCurrentValue(0);
        goal.setPeriod(period);
        goalService.save(goal);
        return goal.getId();
    }

    private Long handlePeriod(Long userId, JsonNode payload) {
        LocalDate startDate = requireDate(payload, "start_date");
        LocalDate endDate = date(payload, "end_date");
        PeriodRecord record = new PeriodRecord();
        record.setUserId(userId);
        record.setStartDate(startDate);
        record.setEndDate(endDate == null ? startDate : endDate);
        record.setFlow(intVal(payload, "flow"));
        record.setNote(text(payload, "note"));
        record.setCreatedAt(LocalDateTime.now());
        periodRecordService.save(record);
        return record.getId();
    }

    private Long handleFamily(Long userId, JsonNode payload) {
        String name = requireText(payload, "name");
        FamilyMember member = new FamilyMember();
        member.setUserId(userId);
        member.setName(name);
        member.setRelation(text(payload, "relation"));
        member.setAge(intVal(payload, "age"));
        member.setConditionText(text(payload, "condition_text"));
        member.setRole(text(payload, "role"));
        member.setStatus(text(payload, "status"));
        member.setAvatar(text(payload, "avatar"));
        member.setCreatedAt(LocalDateTime.now());
        familyMemberService.save(member);
        return member.getId();
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

    private String requireText(JsonNode payload, String field) {
        String value = text(payload, field);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("缺少 " + field);
        }
        return value.trim();
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

    private Integer requireInt(JsonNode payload, String field) {
        Integer value = intVal(payload, field);
        if (value == null) {
            throw new IllegalArgumentException("缺少 " + field);
        }
        return value;
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

    private Double requireDouble(JsonNode payload, String field) {
        Double value = doubleVal(payload, field);
        if (value == null) {
            throw new IllegalArgumentException("缺少 " + field);
        }
        return value;
    }

    private LocalDate date(JsonNode payload, String field) {
        String value = text(payload, field);
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value.trim(), DATE);
    }

    private LocalDate requireDate(JsonNode payload, String field) {
        LocalDate value = date(payload, field);
        if (value == null) {
            throw new IllegalArgumentException("缺少 " + field);
        }
        return value;
    }

    private LocalDateTime dateTime(JsonNode payload, String field) {
        String value = text(payload, field);
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(value.trim(), DATE_TIME);
    }

    private LocalDateTime requireDateTime(JsonNode payload, String field) {
        LocalDateTime value = dateTime(payload, field);
        if (value == null) {
            throw new IllegalArgumentException("缺少 " + field);
        }
        return value;
    }

    private LocalTime requireTime(JsonNode payload, String field) {
        String value = text(payload, field);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("缺少 " + field);
        }
        return LocalTime.parse(value.trim(), TIME);
    }
}
