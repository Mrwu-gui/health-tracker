package com.healthtracker.controller;

import com.healthtracker.dto.UserLoginRequest;
import com.healthtracker.dto.UserProfileUpdateRequest;
import com.healthtracker.dto.UserRegisterRequest;
import com.healthtracker.entity.User;
import com.healthtracker.entity.FileRecord;
import com.healthtracker.security.JwtService;
import com.healthtracker.service.FileRecordService;
import com.healthtracker.service.PasswordService;
import com.healthtracker.service.UserService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    private final PasswordService passwordService;
    private final JwtService jwtService;
    private final FileRecordService fileRecordService;
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    public UserController(UserService userService, PasswordService passwordService, JwtService jwtService,
                          FileRecordService fileRecordService) {
        this.userService = userService;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
        this.fileRecordService = fileRecordService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public User register(@Valid @RequestBody UserRegisterRequest request) {
        User existing = userService.findByUsername(request.getUsername());
        if (existing != null) {
            throw new IllegalArgumentException("用户名已存在");
        }
        User user = userService.register(request.getUsername(), passwordService.hash(request.getPassword()));
        return sanitize(user);
    }

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody UserLoginRequest request) {
        User user = userService.findByUsername(request.getUsername());
        if (user == null || !passwordService.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        String token = jwtService.generateToken(String.valueOf(user.getId()));
        Map<String, Object> body = new HashMap<>();
        body.put("token", token);
        body.put("userId", user.getId());
        return body;
    }

    @GetMapping("/profile")
    public User profile(@RequestParam Long userId) {
        return sanitize(userService.getById(userId));
    }

    @PostMapping("/profile/update")
    public User updateProfile(@RequestBody UserProfileUpdateRequest request) {
        Long userId = resolveUserId(request.getUserId());
        User user = userService.getById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (request.getWxNickname() != null) {
            user.setWxNickname(request.getWxNickname());
        }
        if (request.getWxAvatar() != null) {
            user.setWxAvatar(request.getWxAvatar());
        }
        if (request.getSex() != null) {
            user.setSex(request.getSex());
        }
        if (request.getAge() != null) {
            user.setAge(request.getAge());
        }
        if (request.getHeight() != null) {
            user.setHeight(request.getHeight());
        }
        if (request.getWeight() != null) {
            user.setWeight(request.getWeight());
        }
        if (request.getSystolic() != null) {
            user.setSystolic(request.getSystolic());
        }
        if (request.getDiastolic() != null) {
            user.setDiastolic(request.getDiastolic());
        }
        if (request.getHeartRate() != null) {
            user.setHeartRate(request.getHeartRate());
        }
        userService.updateById(user);
        return sanitize(user);
    }

    @PostMapping("/avatar/upload")
    public Map<String, Object> uploadAvatar(@RequestParam("file") MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }
        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String name = UUID.randomUUID().toString().replace("-", "");
        String filename = ext == null || ext.isBlank() ? name : name + "." + ext;
        Path dirPath = Paths.get(uploadDir).toAbsolutePath().normalize().resolve("avatar");
        try {
            Files.createDirectories(dirPath);
        } catch (IOException ex) {
            throw new IllegalArgumentException("创建目录失败", ex);
        }
        Path target = dirPath.resolve(filename);
        file.transferTo(target.toFile());
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        String url = baseUrl + "/uploads/avatar/" + filename;
        FileRecord record = new FileRecord();
        record.setUserId(resolveUserId(null));
        record.setType("avatar");
        record.setOriginalName(file.getOriginalFilename());
        record.setFileName(filename);
        record.setFilePath(target.toString());
        record.setFileUrl(url);
        record.setFileSize(file.getSize());
        record.setContentType(file.getContentType());
        record.setCreatedAt(LocalDateTime.now());
        fileRecordService.save(record);
        Map<String, Object> body = new HashMap<>();
        body.put("url", url);
        body.put("id", record.getId());
        return body;
    }

    private User sanitize(User user) {
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }

    private Long resolveUserId(Long fallbackUserId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() != null) {
            try {
                return Long.valueOf(auth.getPrincipal().toString());
            } catch (NumberFormatException ignored) {
            }
        }
        if (fallbackUserId != null) {
            return fallbackUserId;
        }
        throw new IllegalArgumentException("未登录");
    }
}
