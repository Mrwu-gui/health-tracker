package com.healthtracker.controller;

import com.healthtracker.dto.MiniBindPhoneRequest;
import com.healthtracker.dto.MiniLoginRequest;
import com.healthtracker.dto.MiniWeRunRequest;
import com.healthtracker.dto.SmsLoginRequest;
import com.healthtracker.dto.SmsSendRequest;
import com.healthtracker.entity.User;
import com.healthtracker.security.JwtService;
import com.healthtracker.service.CaptchaService;
import com.healthtracker.service.SmsCodeService;
import com.healthtracker.service.SmsService;
import com.healthtracker.service.UserService;
import com.healthtracker.service.WeChatCryptoService;
import com.healthtracker.service.WeChatService;
import com.healthtracker.service.WeRunRecordService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final WeChatService weChatService;
    private final WeChatCryptoService weChatCryptoService;
    private final WeRunRecordService weRunRecordService;
    private final SmsService smsService;
    private final SmsCodeService smsCodeService;
    private final CaptchaService captchaService;
    private final com.healthtracker.config.SmsProperties smsProperties;
    private final JwtService jwtService;

    public AuthController(UserService userService,
                          WeChatService weChatService,
                          WeChatCryptoService weChatCryptoService,
                          WeRunRecordService weRunRecordService,
                          SmsService smsService,
                          SmsCodeService smsCodeService,
                          CaptchaService captchaService,
                          com.healthtracker.config.SmsProperties smsProperties,
                          JwtService jwtService) {
        this.userService = userService;
        this.weChatService = weChatService;
        this.weChatCryptoService = weChatCryptoService;
        this.weRunRecordService = weRunRecordService;
        this.smsService = smsService;
        this.smsCodeService = smsCodeService;
        this.captchaService = captchaService;
        this.smsProperties = smsProperties;
        this.jwtService = jwtService;
    }

    @PostMapping("/mini/login")
    public Map<String, Object> miniLogin(@Valid @RequestBody MiniLoginRequest request) {
        Map<String, Object> session = weChatService.miniCode2Session(request.getCode());
        String openid = sessionValue(session, "openid");
        String unionid = sessionValue(session, "unionid");
        if (openid == null || openid.isBlank()) {
            throw new IllegalArgumentException("微信登录失败");
        }
        User user = userService.findByWxOpenid(openid);
        if (user == null) {
            user = userService.registerByWeChat(openid, unionid, null, null);
        }
        return buildTokenResponse(user);
    }

    @PostMapping("/mini/bind-phone")
    public Map<String, Object> miniBindPhone(@Valid @RequestBody MiniBindPhoneRequest request) {
        Long userId = currentUserId();
        User user = userService.getById(userId);
        Map<String, Object> session = weChatService.miniCode2Session(request.getCode());
        String sessionKey = sessionValue(session, "session_key");
        if (sessionKey == null || sessionKey.isBlank()) {
            throw new IllegalArgumentException("微信会话已过期");
        }
        Map<String, Object> payload = weChatCryptoService.decrypt(sessionKey, request.getEncryptedData(), request.getIv());
        String phone = (String) payload.get("phoneNumber");
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("手机号解析失败");
        }
        User existing = userService.findByPhone(phone);
        if (existing != null && !existing.getId().equals(userId)) {
            throw new IllegalArgumentException("该手机号已被绑定");
        }
        userService.bindWeChatPhone(user, phone);
        Map<String, Object> body = new HashMap<>();
        body.put("phone", phone);
        return body;
    }

    @PostMapping("/mini/werun")
    @SuppressWarnings("unchecked")
    public Map<String, Object> syncWeRun(@Valid @RequestBody MiniWeRunRequest request) {
        Long userId = currentUserId();
        Map<String, Object> session = weChatService.miniCode2Session(request.getCode());
        String sessionKey = sessionValue(session, "session_key");
        if (sessionKey == null || sessionKey.isBlank()) {
            throw new IllegalArgumentException("微信会话已过期");
        }
        Map<String, Object> payload = weChatCryptoService.decrypt(sessionKey, request.getEncryptedData(), request.getIv());
        Object listObj = payload.get("stepInfoList");
        if (!(listObj instanceof List)) {
            throw new IllegalArgumentException("微信步数数据为空");
        }
        List<Map<String, Object>> list = (List<Map<String, Object>>) listObj;
        int lastSteps = 0;
        for (Map<String, Object> item : list) {
            Number timestamp = (Number) item.get("timestamp");
            Number step = (Number) item.get("step");
            if (timestamp == null || step == null) {
                continue;
            }
            LocalDate date = Instant.ofEpochSecond(timestamp.longValue())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
            weRunRecordService.upsert(userId, date, step.intValue());
            lastSteps = step.intValue();
        }
        Map<String, Object> body = new HashMap<>();
        body.put("steps", lastSteps);
        body.put("count", list.size());
        return body;
    }

    @PostMapping("/phone/send")
    public Map<String, Object> sendSms(@Valid @RequestBody SmsSendRequest request) {
        boolean captchaOk = captchaService.verify(request.getCaptchaKey(), request.getCaptchaCode());
        if (!captchaOk) {
            throw new IllegalArgumentException("图形验证码错误或已过期");
        }
        if (smsCodeService.isCooldown(request.getPhone())) {
            throw new IllegalArgumentException("操作过于频繁，请稍后再试");
        }
        String code = smsService.sendCode(request.getPhone());
        smsCodeService.markCooldown(request.getPhone(), smsProperties.getCooldownSeconds());
        Map<String, Object> body = new HashMap<>();
        body.put("message", "验证码已发送");
        body.put("cooldown", smsProperties.getCooldownSeconds());
        if (!smsService.isEnabled()) {
            body.put("devCode", code);
        }
        return body;
    }

    @PostMapping("/phone/login")
    public Map<String, Object> loginBySms(@Valid @RequestBody SmsLoginRequest request) {
        boolean ok = smsCodeService.verifyAndConsume(request.getPhone(), request.getCode());
        if (!ok) {
            throw new IllegalArgumentException("验证码无效或已过期");
        }
        User user = userService.findByPhone(request.getPhone());
        if (user == null) {
            user = userService.registerByPhone(request.getPhone());
        }
        return buildTokenResponse(user);
    }

    @GetMapping("/captcha")
    public Map<String, String> captcha() {
        return captchaService.create();
    }

    @GetMapping("/web/qr")
    public Map<String, Object> webQr() {
        String state = UUID.randomUUID().toString().replace("-", "");
        String qrUrl = weChatService.buildWebQrUrl(state);
        Map<String, Object> body = new HashMap<>();
        body.put("qrUrl", qrUrl);
        body.put("state", state);
        return body;
    }

    @GetMapping("/web/callback")
    public Map<String, Object> webCallback(@RequestParam String code) {
        Map<String, Object> token = weChatService.webAccessToken(code);
        String openid = (String) token.get("openid");
        String unionid = (String) token.get("unionid");
        if (openid == null || openid.isBlank()) {
            throw new IllegalArgumentException("微信网页授权失败");
        }
        User user = userService.findByWxOpenid(openid);
        if (user == null) {
            user = userService.registerByWeChat(openid, unionid, null, null);
        }
        return buildTokenResponse(user);
    }

    private Map<String, Object> buildTokenResponse(User user) {
        String token = jwtService.generateToken(String.valueOf(user.getId()));
        Map<String, Object> body = new HashMap<>();
        body.put("token", token);
        body.put("userId", user.getId());
        return body;
    }

    private String sessionValue(Map<String, Object> session, String key) {
        if (session == null) {
            throw new IllegalArgumentException("微信服务响应异常");
        }
        if (session.get("errcode") != null) {
            throw new IllegalArgumentException("微信服务调用失败");
        }
        Object value = session.get(key);
        return value == null ? null : value.toString();
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
