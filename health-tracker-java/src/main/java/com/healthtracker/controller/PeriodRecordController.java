package com.healthtracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthtracker.config.WeChatProperties;
import com.healthtracker.dto.PeriodRecordRequest;
import com.healthtracker.entity.PrivacySetting;
import com.healthtracker.entity.PeriodRecord;
import com.healthtracker.entity.SubscribeTask;
import com.healthtracker.entity.User;
import com.healthtracker.service.PeriodRecordService;
import com.healthtracker.service.PrivacySettingService;
import com.healthtracker.service.SubscribeTaskService;
import com.healthtracker.service.UserService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/period")
public class PeriodRecordController {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final PeriodRecordService periodRecordService;
    private final UserService userService;
    private final PrivacySettingService privacySettingService;
    private final SubscribeTaskService subscribeTaskService;
    private final WeChatProperties weChatProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PeriodRecordController(PeriodRecordService periodRecordService,
                                  UserService userService,
                                  PrivacySettingService privacySettingService,
                                  SubscribeTaskService subscribeTaskService,
                                  WeChatProperties weChatProperties) {
        this.periodRecordService = periodRecordService;
        this.userService = userService;
        this.privacySettingService = privacySettingService;
        this.subscribeTaskService = subscribeTaskService;
        this.weChatProperties = weChatProperties;
    }

    @GetMapping("/list")
    public List<PeriodRecord> list(@RequestParam Long userId) {
        return periodRecordService.listByUser(userId);
    }

    @PostMapping("/add")
    public PeriodRecord add(@Valid @RequestBody PeriodRecordRequest request) {
        PeriodRecord record = new PeriodRecord();
        record.setUserId(request.getUserId());
        record.setStartDate(parseDate(request.getStartDate()));
        record.setEndDate(parseDate(request.getEndDate() == null || request.getEndDate().isBlank()
            ? request.getStartDate()
            : request.getEndDate()));
        record.setFlow(parseFlow(request.getFlow()));
        record.setNote(trim(request.getNote()));
        record.setCreatedAt(LocalDateTime.now());
        periodRecordService.save(record);
        scheduleSubscribePeriod(record);
        return record;
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value.trim(), DATE_FORMAT);
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Integer parseFlow(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toLowerCase();
        switch (normalized) {
            case "light":
                return 1;
            case "medium":
                return 2;
            case "heavy":
                return 3;
            default:
                try {
                    return Integer.valueOf(normalized);
                } catch (NumberFormatException ex) {
                    return null;
                }
        }
    }

    private void scheduleSubscribePeriod(PeriodRecord record) {
        User user = userService.getById(record.getUserId());
        if (user == null || user.getWxOpenid() == null || user.getWxOpenid().isBlank()) {
            return;
        }
        PrivacySetting setting = privacySettingService.getOrCreate(user.getId());
        if (setting != null && setting.getAllowSubscribe() != null && setting.getAllowSubscribe() == 0) {
            return;
        }
        if (record.getStartDate() == null) {
            return;
        }
        String templateId = weChatProperties.getMini().getPeriodTemplateId();
        if (templateId == null || templateId.isBlank()) {
            return;
        }
        LocalDate nextStart = record.getStartDate().plusDays(28);
        LocalDate nextEnd = nextStart.plusDays(5);
        LocalDateTime sendTime = LocalDateTime.of(nextStart.minusDays(1), LocalTime.of(9, 0));
        Map<String, Object> data = Map.of(
            "date1", Map.of("value", nextStart.format(DATE_FORMAT)),
            "thing2", Map.of("value", "经期提醒"),
            "time3", Map.of("value", nextEnd.format(DATE_FORMAT))
        );
        SubscribeTask task = new SubscribeTask();
        task.setUserId(user.getId());
        task.setOpenid(user.getWxOpenid());
        task.setTemplateId(templateId);
        task.setPage("pages/period/index");
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
}
