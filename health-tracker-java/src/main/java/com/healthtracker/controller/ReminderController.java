package com.healthtracker.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthtracker.config.WeChatProperties;
import com.healthtracker.dto.ReminderRequest;
import com.healthtracker.dto.ReminderStatusRequest;
import com.healthtracker.dto.ReminderUpdateRequest;
import com.healthtracker.entity.PrivacySetting;
import com.healthtracker.entity.Reminder;
import com.healthtracker.entity.User;
import com.healthtracker.entity.SubscribeTask;
import com.healthtracker.service.PrivacySettingService;
import com.healthtracker.service.ReminderService;
import com.healthtracker.service.SubscribeTaskService;
import com.healthtracker.service.UserService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reminder")
public class ReminderController {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final ReminderService reminderService;
    private final UserService userService;
    private final SubscribeTaskService subscribeTaskService;
    private final PrivacySettingService privacySettingService;
    private final WeChatProperties weChatProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ReminderController(ReminderService reminderService,
                              UserService userService,
                              SubscribeTaskService subscribeTaskService,
                              PrivacySettingService privacySettingService,
                              WeChatProperties weChatProperties) {
        this.reminderService = reminderService;
        this.userService = userService;
        this.subscribeTaskService = subscribeTaskService;
        this.privacySettingService = privacySettingService;
        this.weChatProperties = weChatProperties;
    }

    @PostMapping("/add")
    public Map<String, Object> add(@Valid @RequestBody ReminderRequest request) {
        Long userId = currentUserId();
        Reminder reminder = new Reminder();
        reminder.setUserId(userId);
        reminder.setTitle(resolveTitle(request.getTitle(), request.getType()));
        reminder.setType(request.getType());
        reminder.setContent(request.getContent());
        reminder.setRemindTime(parseTime(request.getRemindTime()));
        reminder.setStatus(0);
        reminder.setCreatedAt(LocalDateTime.now());
        reminderService.save(reminder);

        User user = userService.getById(userId);
        if (user != null && user.getWxOpenid() != null && !user.getWxOpenid().isBlank()) {
            scheduleSubscribeReminder(reminder, user);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("id", reminder.getId());
        return body;
    }

    @PostMapping("/update")
    public Reminder update(@Valid @RequestBody ReminderUpdateRequest request) {
        Reminder reminder = reminderService.getById(request.getId());
        if (reminder == null) {
            throw new IllegalArgumentException("提醒不存在");
        }
        Long userId = currentUserId();
        if (!userId.equals(reminder.getUserId())) {
            throw new IllegalArgumentException("无权限操作该提醒");
        }
        reminder.setTitle(resolveTitle(request.getTitle(), request.getType()));
        reminder.setType(request.getType());
        reminder.setContent(request.getContent());
        reminder.setRemindTime(parseTime(request.getRemindTime()));
        reminderService.updateById(reminder);
        return reminder;
    }

    @PostMapping("/status")
    public Reminder updateStatus(@Valid @RequestBody ReminderStatusRequest request) {
        Reminder reminder = reminderService.getById(request.getId());
        if (reminder == null) {
            throw new IllegalArgumentException("提醒不存在");
        }
        Long userId = currentUserId();
        if (!userId.equals(reminder.getUserId())) {
            throw new IllegalArgumentException("无权限操作该提醒");
        }
        reminder.setStatus(request.getStatus());
        if (request.getRemindTime() != null && !request.getRemindTime().isBlank()) {
            reminder.setRemindTime(parseTime(request.getRemindTime()));
        }
        reminderService.updateById(reminder);

        if (reminder.getStatus() != null && reminder.getStatus() == 0
            && reminder.getRemindTime() != null) {
            User user = userService.getById(reminder.getUserId());
            if (user != null && user.getWxOpenid() != null && !user.getWxOpenid().isBlank()) {
                scheduleSubscribeReminder(reminder, user);
            }
        }

        return reminder;
    }

    @GetMapping("/list")
    public List<Reminder> list(@RequestParam Long userId) {
        return reminderService.list(new LambdaQueryWrapper<Reminder>()
            .eq(Reminder::getUserId, userId)
            .orderByDesc(Reminder::getRemindTime));
    }

    private String resolveTitle(String title, Integer type) {
        if (title != null && !title.isBlank()) {
            return title;
        }
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

    private LocalDateTime parseTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() == 16) {
            normalized = normalized + ":00";
        }
        return LocalDateTime.parse(normalized, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private void scheduleSubscribeReminder(Reminder reminder, User user) {
        PrivacySetting setting = privacySettingService.getOrCreate(user.getId());
        if (setting != null && setting.getAllowSubscribe() != null && setting.getAllowSubscribe() == 0) {
            return;
        }
        if (reminder.getRemindTime() == null) {
            return;
        }
        String templateId;
        Map<String, Object> data;
        if (reminder.getType() != null && reminder.getType() == 3) {
            templateId = weChatProperties.getMini().getSleepTemplateId();
            data = Map.of(
                "thing1", Map.of("value", limit(reminder.getContent(), 20)),
                "time2", Map.of("value", formatTime(reminder.getRemindTime()))
            );
        } else if (reminder.getType() != null && reminder.getType() == 1) {
            templateId = weChatProperties.getMini().getExerciseTemplateId();
            String name = pickName(user);
            data = Map.of(
                "thing1", Map.of("value", limit(name, 20)),
                "thing2", Map.of("value", limit(reminder.getContent(), 20)),
                "time3", Map.of("value", formatTime(reminder.getRemindTime())),
                "thing4", Map.of("value", limit(reminder.getTitle(), 20))
            );
        } else {
            return;
        }
        if (templateId == null || templateId.isBlank()) {
            return;
        }
        LocalDateTime sendTime = reminder.getRemindTime().minusMinutes(30);
        if (sendTime.isBefore(LocalDateTime.now())) {
            sendTime = reminder.getRemindTime();
        }
        SubscribeTask task = new SubscribeTask();
        task.setUserId(user.getId());
        task.setOpenid(user.getWxOpenid());
        task.setTemplateId(templateId);
        task.setPage("pages/reminders/index");
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

    private String pickName(User user) {
        if (user == null) return "用户";
        if (user.getUsername() != null && !user.getUsername().isBlank()) return user.getUsername();
        if (user.getWxNickname() != null && !user.getWxNickname().isBlank()) return user.getWxNickname();
        return "用户";
    }

    private String limit(String value, int max) {
        if (value == null || value.isBlank()) return "未填写";
        return value.length() > max ? value.substring(0, max) : value;
    }

    private String formatTime(LocalDateTime time) {
        if (time == null) return "待设置";
        return time.format(TIME_FORMAT);
    }

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalArgumentException("未登录");
        }
        try {
            return Long.valueOf(auth.getPrincipal().toString());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("登录状态异常");
        }
    }
}
