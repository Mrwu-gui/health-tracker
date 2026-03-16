package com.healthtracker.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthtracker.dto.AiChatRequest;
import com.healthtracker.entity.AiChatMessage;
import com.healthtracker.entity.User;
import com.healthtracker.service.AiChatMessageService;
import com.healthtracker.service.UserService;
import jakarta.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final AiChatMessageService aiChatMessageService;
    private final UserService userService;

    @Value("${yuanqi.base-url:https://yuanqi.tencent.com}")
    private String baseUrl;

    @Value("${yuanqi.appid:}")
    private String appId;

    @Value("${yuanqi.appkey:}")
    private String appKey;

    @Value("${tencent.asr.secret-id:}")
    private String asrSecretId;

    @Value("${tencent.asr.secret-key:}")
    private String asrSecretKey;

    @Value("${tencent.asr.region:ap-shanghai}")
    private String asrRegion;

    @Value("${tencent.asr.engine-model-type:16k_zh}")
    private String asrEngineModelType;

    @Value("${tencent.asr.voice-format:mp3}")
    private String asrVoiceFormat;

    @Value("${tencent.asr.sub-service-type:2}")
    private Integer asrSubServiceType;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    public AiController(ObjectMapper objectMapper,
                        RestTemplateBuilder builder,
                        AiChatMessageService aiChatMessageService,
                        UserService userService) {
        this.objectMapper = objectMapper;
        this.restTemplate = builder
            .setConnectTimeout(Duration.ofSeconds(10))
            .setReadTimeout(Duration.ofSeconds(20))
            .build();
        this.aiChatMessageService = aiChatMessageService;
        this.userService = userService;
    }

    @PostMapping("/chat")
    public Map<String, Object> chat(@Valid @RequestBody AiChatRequest request) throws Exception {
        if (appId == null || appId.isBlank() || appKey == null || appKey.isBlank()) {
            throw new IllegalArgumentException("AI 配置缺失");
        }
        if ((request.getMessage() == null || request.getMessage().isBlank())
            && (request.getImageUrl() == null || request.getImageUrl().isBlank())
            && (request.getAudioUrl() == null || request.getAudioUrl().isBlank())) {
            throw new IllegalArgumentException("消息不能为空");
        }
        String userId = resolveUserId();
        Long userIdLong = parseUserId(userId);
        String wxOpenid = resolveWxOpenid(userIdLong);
        boolean store = request.getStore() == null || request.getStore();

        String text = request.getMessage() == null ? "" : request.getMessage();
        if (text.isBlank() && request.getAudioUrl() != null && !request.getAudioUrl().isBlank()) {
            text = recognizeAudio(request.getAudioUrl());
        }
        Map<String, Object> contentText = Map.of("type", "text", "text", text);
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            Map<String, Object> imageUrl = Map.of("url", request.getImageUrl());
            Map<String, Object> imageContent = Map.of("type", "image_url", "image_url", imageUrl);
            message.put("content", List.of(contentText, imageContent));
        } else {
            message.put("content", List.of(contentText));
        }

        if (store && userIdLong != null) {
            AiChatMessage userMsg = new AiChatMessage();
            userMsg.setUserId(userIdLong);
            userMsg.setRole("user");
            userMsg.setContentText(text);
            Map<String, Object> userContent = new HashMap<>();
            userContent.put("text", text);
            if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
                userContent.put("imageUrl", request.getImageUrl());
            }
            if (request.getAudioUrl() != null && !request.getAudioUrl().isBlank()) {
                userContent.put("audioUrl", request.getAudioUrl());
            }
            userMsg.setContentJson(objectMapper.writeValueAsString(userContent));
            userMsg.setCreatedAt(LocalDateTime.now());
            aiChatMessageService.save(userMsg);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("assistant_id", appId);
        payload.put("user_id", wxOpenid == null || wxOpenid.isBlank() ? userId : wxOpenid);
        payload.put("stream", false);
        payload.put("messages", List.of(message));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(appKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        String url = baseUrl + "/openapi/v1/agent/chat/completions";
        ResponseEntity<String> response;
        try {
            response = restTemplate.postForEntity(url, entity, String.class);
        } catch (ResourceAccessException ex) {
            throw new IllegalArgumentException("AI 服务请求超时，请稍后再试", ex);
        } catch (Exception ex) {
            if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
                throw new IllegalArgumentException("当前模型暂不支持图片识别");
            }
            throw ex;
        }

        String content = "";
        if (response.getBody() != null) {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                JsonNode messageNode = choices.get(0).path("message").path("content");
                if (messageNode.isTextual()) {
                    content = messageNode.asText();
                } else if (messageNode.isArray() && messageNode.size() > 0) {
                    content = messageNode.get(0).path("text").asText("");
                }
            }
        }

        if (store && userIdLong != null) {
            AiChatMessage assistantMsg = new AiChatMessage();
            assistantMsg.setUserId(userIdLong);
            assistantMsg.setRole("assistant");
            assistantMsg.setContentText(content);
            assistantMsg.setContentJson(objectMapper.writeValueAsString(
                List.of(Map.of("type", "text", "text", content))
            ));
            assistantMsg.setCreatedAt(LocalDateTime.now());
            aiChatMessageService.save(assistantMsg);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("content", content == null ? "" : content);
        return result;
    }

    @GetMapping("/history")
    public List<AiChatMessage> history(@RequestParam Long userId) {
        return aiChatMessageService.list(new LambdaQueryWrapper<AiChatMessage>()
            .eq(AiChatMessage::getUserId, userId)
            .orderByAsc(AiChatMessage::getCreatedAt));
    }

    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }
        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String name = UUID.randomUUID().toString().replace("-", "");
        String filename = ext == null || ext.isBlank() ? name : name + "." + ext;
        Path dirPath = Paths.get(uploadDir).toAbsolutePath().normalize().resolve("ai");
        try {
            Files.createDirectories(dirPath);
        } catch (IOException ex) {
            throw new IllegalArgumentException("创建目录失败", ex);
        }
        Path target = dirPath.resolve(filename);
        file.transferTo(target.toFile());

        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        String url = baseUrl + "/uploads/ai/" + filename;
        Map<String, Object> body = new HashMap<>();
        body.put("url", url);
        return body;
    }

    private String resolveUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() != null) {
            return auth.getPrincipal().toString();
        }
        return "guest";
    }

    private Long parseUserId(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String resolveWxOpenid(Long userId) {
        if (userId == null) {
            return null;
        }
        User user = userService.getById(userId);
        if (user == null) {
            return null;
        }
        return user.getWxOpenid();
    }

    private String recognizeAudio(String audioUrl) throws Exception {
        if (asrSecretId == null || asrSecretId.isBlank() || asrSecretKey == null || asrSecretKey.isBlank()) {
            throw new IllegalArgumentException("语音识别配置缺失");
        }
        long timestamp = System.currentTimeMillis() / 1000;
        String date = java.time.LocalDate.now(ZoneOffset.UTC).toString();

        Map<String, Object> payload = new HashMap<>();
        payload.put("SubServiceType", asrSubServiceType);
        payload.put("EngSerViceType", asrEngineModelType);
        payload.put("VoiceFormat", asrVoiceFormat);
        payload.put("ProjectId", 0);

        String localPrefix = "/uploads/ai/";
        if (audioUrl != null && audioUrl.contains(localPrefix)) {
            String filename = audioUrl.substring(audioUrl.indexOf(localPrefix) + localPrefix.length());
            Path filePath = Paths.get(uploadDir, "ai", filename);
            byte[] bytes = Files.readAllBytes(filePath);
            String base64 = Base64.getEncoder().encodeToString(bytes);
            payload.put("SourceType", 1);
            payload.put("Data", base64);
            payload.put("DataLen", bytes.length);
        } else {
            payload.put("SourceType", 0);
            payload.put("Url", audioUrl);
        }

        String body = objectMapper.writeValueAsString(payload);
        String host = "asr.tencentcloudapi.com";
        String service = "asr";
        String version = "2019-06-14";
        String action = "SentenceRecognition";

        String canonicalRequest = "POST\n/\n\ncontent-type:application/json; charset=utf-8\nhost:" + host + "\n\ncontent-type;host\n" + sha256Hex(body);
        String stringToSign = "TC3-HMAC-SHA256\n" + timestamp + "\n" + date + "/" + service + "/tc3_request\n" + sha256Hex(canonicalRequest);
        byte[] secretDate = hmacSha256(("TC3" + asrSecretKey).getBytes(StandardCharsets.UTF_8), date);
        byte[] secretService = hmacSha256(secretDate, service);
        byte[] secretSigning = hmacSha256(secretService, "tc3_request");
        String signature = bytesToHex(hmacSha256(secretSigning, stringToSign));

        String authorization = String.format(
            "TC3-HMAC-SHA256 Credential=%s/%s/%s/tc3_request, SignedHeaders=content-type;host, Signature=%s",
            asrSecretId, date, service, signature
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Host", host);
        headers.set("X-TC-Action", action);
        headers.set("X-TC-Version", version);
        headers.set("X-TC-Timestamp", String.valueOf(timestamp));
        headers.set("X-TC-Region", asrRegion);
        headers.set("Authorization", authorization);

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity("https://" + host, entity, String.class);
        JsonNode root = objectMapper.readTree(response.getBody());
        JsonNode result = root.path("Response").path("Result");
        if (result.isMissingNode() || result.asText().isBlank()) {
            throw new IllegalArgumentException("语音识别失败");
        }
        return result.asText();
    }

    private static byte[] hmacSha256(byte[] key, String msg) throws Exception {
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
        mac.init(new javax.crypto.spec.SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(msg.getBytes(StandardCharsets.UTF_8));
    }

    private static String sha256Hex(String s) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(s.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(digest);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(b & 0xff);
            if (hex.length() == 1) sb.append('0');
            sb.append(hex);
        }
        return sb.toString();
    }
}
