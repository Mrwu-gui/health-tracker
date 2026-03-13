package com.healthtracker.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.healthtracker.dto.ReminderRequest;
import com.healthtracker.entity.Reminder;
import com.healthtracker.entity.User;
import com.healthtracker.service.ReminderService;
import com.healthtracker.service.UserService;
import com.healthtracker.service.WeChatMessageService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final ReminderService reminderService;
    private final UserService userService;
    private final WeChatMessageService weChatMessageService;

    public ReminderController(ReminderService reminderService,
                              UserService userService,
                              WeChatMessageService weChatMessageService) {
        this.reminderService = reminderService;
        this.userService = userService;
        this.weChatMessageService = weChatMessageService;
    }

    @PostMapping("/add")
    public Map<String, Object> add(@Valid @RequestBody ReminderRequest request) {
        Long userId = currentUserId();
        Reminder reminder = new Reminder();
        reminder.setUserId(userId);
        reminder.setTitle(request.getTitle());
        reminder.setType(request.getType());
        reminder.setContent(request.getContent());
        reminder.setRemindTime(parseTime(request.getRemindTime()));
        reminder.setStatus("待提醒");
        reminder.setCreatedAt(LocalDateTime.now());
        reminderService.save(reminder);

        User user = userService.getById(userId);
        if (user != null && user.getWxOpenid() != null && !user.getWxOpenid().isBlank()) {
            weChatMessageService.sendReminder(user.getWxOpenid(),
                reminder.getTitle(),
                reminder.getContent() == null ? reminder.getTitle() : reminder.getContent(),
                reminder.getRemindTime());
        }

        Map<String, Object> body = new HashMap<>();
        body.put("id", reminder.getId());
        return body;
    }

    @GetMapping("/list")
    public List<Reminder> list(@RequestParam Long userId) {
        return reminderService.list(new LambdaQueryWrapper<Reminder>()
            .eq(Reminder::getUserId, userId)
            .orderByDesc(Reminder::getRemindTime));
    }

    private LocalDateTime parseTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
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
