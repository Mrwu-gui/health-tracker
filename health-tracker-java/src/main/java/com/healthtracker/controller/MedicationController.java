package com.healthtracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthtracker.config.WeChatProperties;
import com.healthtracker.dto.MedicationRequest;
import com.healthtracker.dto.MedicationUpdateRequest;
import com.healthtracker.entity.Medication;
import com.healthtracker.entity.PrivacySetting;
import com.healthtracker.entity.SubscribeTask;
import com.healthtracker.entity.User;
import com.healthtracker.service.MedicationService;
import com.healthtracker.service.PrivacySettingService;
import com.healthtracker.service.SubscribeTaskService;
import com.healthtracker.service.UserService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/medication")
public class MedicationController {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final MedicationService medicationService;
    private final UserService userService;
    private final PrivacySettingService privacySettingService;
    private final SubscribeTaskService subscribeTaskService;
    private final WeChatProperties weChatProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MedicationController(MedicationService medicationService,
                                UserService userService,
                                PrivacySettingService privacySettingService,
                                SubscribeTaskService subscribeTaskService,
                                WeChatProperties weChatProperties) {
        this.medicationService = medicationService;
        this.userService = userService;
        this.privacySettingService = privacySettingService;
        this.subscribeTaskService = subscribeTaskService;
        this.weChatProperties = weChatProperties;
    }

    @PostMapping("/add")
    public Medication add(@Valid @RequestBody MedicationRequest request) {
        Medication medication = new Medication();
        medication.setUserId(request.getUserId());
        medication.setDrugName(request.getDrugName());
        medication.setDosage(request.getDosage());
        medication.setFrequency(request.getFrequency());
        medication.setRemindTime(request.getRemindTime());
        medication.setStock(request.getStock());
        medication.setStockThreshold(request.getStockThreshold());
        medication.setStartDate(request.getStartDate());
        medication.setEndDate(request.getEndDate());
        medication.setNotes(request.getNotes());
        medicationService.save(medication);
        scheduleSubscribeMedication(medication);
        return medication;
    }

    @GetMapping("/list")
    public List<Medication> list(@RequestParam Long userId) {
        return medicationService.listByUser(userId);
    }

    @PostMapping("/update")
    public Medication update(@Valid @RequestBody MedicationUpdateRequest request) {
        Medication medication = medicationService.getById(request.getId());
        if (medication == null) {
            throw new IllegalArgumentException("药物不存在");
        }
        medication.setDrugName(request.getDrugName());
        medication.setDosage(request.getDosage());
        medication.setFrequency(request.getFrequency());
        medication.setRemindTime(request.getRemindTime());
        medication.setStock(request.getStock());
        medication.setStockThreshold(request.getStockThreshold());
        medication.setStartDate(request.getStartDate());
        medication.setEndDate(request.getEndDate());
        medication.setNotes(request.getNotes());
        medicationService.updateById(medication);
        scheduleSubscribeMedication(medication);
        return medication;
    }

    private void scheduleSubscribeMedication(Medication medication) {
        if (medication.getRemindTime() == null || medication.getRemindTime().isBlank()) {
            return;
        }
        User user = userService.getById(medication.getUserId());
        if (user == null || user.getWxOpenid() == null || user.getWxOpenid().isBlank()) {
            return;
        }
        PrivacySetting setting = privacySettingService.getOrCreate(user.getId());
        if (setting != null && setting.getAllowSubscribe() != null && setting.getAllowSubscribe() == 0) {
            return;
        }
        LocalDateTime remindTime = parseDateTime(medication.getRemindTime());
        if (remindTime == null) {
            return;
        }
        LocalDateTime sendTime = remindTime.minusMinutes(15);
        if (sendTime.isBefore(LocalDateTime.now())) {
            sendTime = remindTime;
        }
        String templateId = weChatProperties.getMini().getMedicationTemplateId();
        if (templateId == null || templateId.isBlank()) {
            return;
        }
        Map<String, Object> data = Map.of(
            "time2", Map.of("value", formatTime(remindTime)),
            "thing3", Map.of("value", limit(medication.getDrugName(), 20)),
            "short_thing5", Map.of("value", limit(medication.getDosage(), 12)),
            "thing6", Map.of("value", "按时服药更健康")
        );
        SubscribeTask task = new SubscribeTask();
        task.setUserId(user.getId());
        task.setOpenid(user.getWxOpenid());
        task.setTemplateId(templateId);
        task.setPage("pages/medications/index");
        try {
            task.setDataJson(objectMapper.writeValueAsString(data));
        } catch (Exception ex) {
            return;
        }
        task.setSendTime(sendTime);
        task.setStatus(0);
        task.setCreatedAt(LocalDateTime.now());
        subscribeTaskService.save(task);
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() == 16) {
            normalized = normalized + ":00";
        }
        try {
            return LocalDateTime.parse(normalized, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception ex) {
            return null;
        }
    }

    private String formatTime(LocalDateTime time) {
        if (time == null) return "待设置";
        return time.format(TIME_FORMAT);
    }

    private String limit(String value, int max) {
        if (value == null || value.isBlank()) return "未填写";
        return value.length() > max ? value.substring(0, max) : value;
    }
}
