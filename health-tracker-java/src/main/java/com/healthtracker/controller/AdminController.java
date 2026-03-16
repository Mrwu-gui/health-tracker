package com.healthtracker.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.healthtracker.dto.AdminSubscribeTaskRequest;
import com.healthtracker.dto.AdminSubscribeTaskUpdateRequest;
import com.healthtracker.entity.AiLog;
import com.healthtracker.entity.SubscribeTask;
import com.healthtracker.entity.SystemLog;
import com.healthtracker.entity.User;
import com.healthtracker.service.AiLogService;
import com.healthtracker.service.SubscribeTaskService;
import com.healthtracker.service.SystemLogService;
import com.healthtracker.service.UserService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final UserService userService;
    private final SubscribeTaskService subscribeTaskService;
    private final SystemLogService systemLogService;
    private final AiLogService aiLogService;

    @Value("${admin.api-key:}")
    private String adminApiKey;

    public AdminController(UserService userService,
                           SubscribeTaskService subscribeTaskService,
                           SystemLogService systemLogService,
                           AiLogService aiLogService) {
        this.userService = userService;
        this.subscribeTaskService = subscribeTaskService;
        this.systemLogService = systemLogService;
        this.aiLogService = aiLogService;
    }

    @GetMapping("/users")
    public List<Map<String, Object>> users(@RequestHeader(value = "X-Admin-Key", required = false) String key,
                                           @RequestParam(required = false) String keyword,
                                           @RequestParam(defaultValue = "200") Integer limit) {
        assertAdminKey(key);
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            query.like(User::getUsername, keyword)
                .or()
                .like(User::getPhone, keyword)
                .or()
                .like(User::getWxOpenid, keyword);
        }
        query.orderByDesc(User::getCreatedAt).last("limit " + limit);
        return userService.list(query).stream()
            .map(this::toUserSummary)
            .collect(Collectors.toList());
    }

    @GetMapping("/subscribe-tasks")
    public List<SubscribeTask> subscribeTasks(
        @RequestHeader(value = "X-Admin-Key", required = false) String key,
        @RequestParam(required = false) Integer status,
        @RequestParam(required = false) Long userId,
        @RequestParam(defaultValue = "200") Integer limit) {
        assertAdminKey(key);
        LambdaQueryWrapper<SubscribeTask> query = new LambdaQueryWrapper<>();
        if (status != null) {
            query.eq(SubscribeTask::getStatus, status);
        }
        if (userId != null) {
            query.eq(SubscribeTask::getUserId, userId);
        }
        query.orderByDesc(SubscribeTask::getSendTime).last("limit " + limit);
        return subscribeTaskService.list(query);
    }

    @PostMapping("/subscribe-tasks/add")
    public SubscribeTask addSubscribeTask(
        @RequestHeader(value = "X-Admin-Key", required = false) String key,
        @Valid @RequestBody AdminSubscribeTaskRequest request) {
        assertAdminKey(key);
        SubscribeTask task = new SubscribeTask();
        task.setUserId(request.getUserId());
        task.setOpenid(request.getOpenid());
        task.setTemplateId(request.getTemplateId());
        task.setPage(request.getPage());
        task.setDataJson(request.getDataJson());
        LocalDateTime sendTime = parseTime(request.getSendTime());
        task.setSendTime(sendTime == null ? LocalDateTime.now() : sendTime);
        task.setStatus(0);
        task.setCreatedAt(LocalDateTime.now());
        subscribeTaskService.save(task);
        return task;
    }

    @PostMapping("/subscribe-tasks/update")
    public SubscribeTask updateSubscribeTask(
        @RequestHeader(value = "X-Admin-Key", required = false) String key,
        @Valid @RequestBody AdminSubscribeTaskUpdateRequest request) {
        assertAdminKey(key);
        SubscribeTask task = subscribeTaskService.getById(request.getId());
        if (task == null) {
            throw new IllegalArgumentException("任务不存在");
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        if (request.getSendTime() != null && !request.getSendTime().isBlank()) {
            task.setSendTime(parseTime(request.getSendTime()));
        }
        if (request.getResponse() != null) {
            task.setResponse(request.getResponse());
        }
        subscribeTaskService.updateById(task);
        return task;
    }

    @GetMapping("/logs/system")
    public List<SystemLog> systemLogs(
        @RequestHeader(value = "X-Admin-Key", required = false) String key,
        @RequestParam(required = false) String level,
        @RequestParam(defaultValue = "200") Integer limit) {
        assertAdminKey(key);
        LambdaQueryWrapper<SystemLog> query = new LambdaQueryWrapper<>();
        if (level != null && !level.isBlank()) {
            query.eq(SystemLog::getLevel, level);
        }
        query.orderByDesc(SystemLog::getCreatedAt).last("limit " + limit);
        return systemLogService.list(query);
    }

    @GetMapping("/logs/ai")
    public List<AiLog> aiLogs(
        @RequestHeader(value = "X-Admin-Key", required = false) String key,
        @RequestParam(required = false) Integer status,
        @RequestParam(defaultValue = "200") Integer limit) {
        assertAdminKey(key);
        LambdaQueryWrapper<AiLog> query = new LambdaQueryWrapper<>();
        if (status != null) {
            query.eq(AiLog::getStatus, status);
        }
        query.orderByDesc(AiLog::getCreatedAt).last("limit " + limit);
        return aiLogService.list(query);
    }

    private void assertAdminKey(String key) {
        if (adminApiKey == null || adminApiKey.isBlank()) {
            return;
        }
        if (key == null || !adminApiKey.equals(key)) {
            throw new IllegalArgumentException("无权限");
        }
    }

    private Map<String, Object> toUserSummary(User user) {
        return Map.of(
            "id", user.getId(),
            "username", user.getUsername(),
            "phone", user.getPhone(),
            "wxOpenid", user.getWxOpenid(),
            "wxNickname", user.getWxNickname(),
            "createdAt", user.getCreatedAt()
        );
    }

    private LocalDateTime parseTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() == 16) {
            normalized = normalized + ":00";
        }
        return LocalDateTime.parse(normalized, DATE_TIME);
    }
}
