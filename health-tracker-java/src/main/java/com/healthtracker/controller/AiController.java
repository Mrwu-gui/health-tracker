package com.healthtracker.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthtracker.ai.AiProviderRequest;
import com.healthtracker.ai.AiProviderResponse;
import com.healthtracker.ai.AiProviderRouter;
import com.healthtracker.ai.vision.DashscopeVisionService;
import com.healthtracker.dto.AiChatRequest;
import com.healthtracker.dto.AiVisionRequest;
import com.healthtracker.entity.AiChatMessage;
import com.healthtracker.entity.AiLog;
import com.healthtracker.entity.AiVisionRecord;
import com.healthtracker.entity.FileRecord;
import com.healthtracker.entity.User;
import com.healthtracker.service.AiChatMessageService;
import com.healthtracker.service.AiLogService;
import com.healthtracker.service.AiVisionRecordService;
import com.healthtracker.service.FileRecordService;
import com.healthtracker.service.UserService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    private static final String MEDICAL_GUARD_RESPONSE = String.join("\n",
        "这类问题涉及疾病判断或用药建议，为了您的安全，我无法提供具体用药指导。🙏",
        "建议您尽快咨询专业医生或就医确认，并遵医嘱处理。"
    );

    private static final String MEDICAL_KEYWORDS_REGEX =
        "(?i).*(用药|吃药|药物|药品|药名|用量|剂量|处方|开药|抗生素|消炎药|止痛药|布洛芬|对乙酰氨基酚|阿司匹林|头孢|治疗|诊断|病因|症状|疾病|发烧|疼痛|头痛|感染|就医|医生|挂号|药效|副作用|过敏|mg|毫克|剂型|疗程).*";
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final AiProviderRouter aiProviderRouter;
    private final DashscopeVisionService dashscopeVisionService;
    private final AiVisionRecordService aiVisionRecordService;
    private final AiChatMessageService aiChatMessageService;
    private final UserService userService;
    private final AiLogService aiLogService;
    private final FileRecordService fileRecordService;

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
                        RestTemplate restTemplate,
                        AiProviderRouter aiProviderRouter,
                        DashscopeVisionService dashscopeVisionService,
                        AiVisionRecordService aiVisionRecordService,
                        AiChatMessageService aiChatMessageService,
                        UserService userService,
                        AiLogService aiLogService,
                        FileRecordService fileRecordService) {
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
        this.aiProviderRouter = aiProviderRouter;
        this.dashscopeVisionService = dashscopeVisionService;
        this.aiVisionRecordService = aiVisionRecordService;
        this.aiChatMessageService = aiChatMessageService;
        this.userService = userService;
        this.aiLogService = aiLogService;
        this.fileRecordService = fileRecordService;
    }

    @PostMapping("/chat")
    public Map<String, Object> chat(@Valid @RequestBody AiChatRequest request) throws Exception {
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

        String messageToSend = text;
        if ((messageToSend == null || messageToSend.isBlank())
            && request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            messageToSend = "请识别图片内容";
        }

        AiProviderRequest providerRequest = new AiProviderRequest();
        providerRequest.setUserId(userIdLong);
        providerRequest.setUserIdentity(wxOpenid == null || wxOpenid.isBlank() ? userId : wxOpenid);
        providerRequest.setPrompt(messageToSend == null ? "" : messageToSend);
        providerRequest.setImageUrl(request.getImageUrl());
        AiProviderResponse providerResponse;
        try {
            providerResponse = aiProviderRouter.current().chat(providerRequest);
        } catch (Exception ex) {
            saveAiLog(userIdLong, wxOpenid, messageToSend, null, 1, ex.getMessage());
            throw ex;
        }

        String content = providerResponse == null ? "" : providerResponse.getContent();
        content = stripGoalBlock(content);
        if ((messageToSend != null && messageToSend.matches(MEDICAL_KEYWORDS_REGEX))
            || (content != null && content.matches(MEDICAL_KEYWORDS_REGEX))) {
            content = MEDICAL_GUARD_RESPONSE;
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
        saveAiLog(userIdLong, wxOpenid, messageToSend, content, 0, null);
        return result;
    }

    @GetMapping("/history")
    public List<AiChatMessage> history(@RequestParam Long userId) {
        return aiChatMessageService.list(new LambdaQueryWrapper<AiChatMessage>()
            .eq(AiChatMessage::getUserId, userId)
            .orderByAsc(AiChatMessage::getCreatedAt));
    }

    @PostMapping("/clear")
    public Map<String, Object> clearHistory(@RequestBody(required = false) Map<String, Object> body,
                                            @RequestParam(required = false) Long userId) {
        Long targetUserId = userId;
        if (targetUserId == null && body != null && body.get("userId") != null) {
            try {
                targetUserId = Long.valueOf(String.valueOf(body.get("userId")));
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("用户信息不合法");
            }
        }
        if (targetUserId == null) {
            targetUserId = parseUserId(resolveUserId());
        }
        if (targetUserId == null) {
            throw new IllegalArgumentException("用户信息缺失");
        }
        aiChatMessageService.remove(new LambdaQueryWrapper<AiChatMessage>()
            .eq(AiChatMessage::getUserId, targetUserId));
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return result;
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
        FileRecord record = new FileRecord();
        record.setUserId(parseUserId(resolveUserId()));
        record.setType("ai");
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

    @PostMapping("/vision/recognize")
    public Map<String, Object> recognize(@Valid @RequestBody AiVisionRequest request) throws Exception {
        Map<String, Object> result = dashscopeVisionService.recognize(request.getImageUrl());
        Long userId = request.getUserId();
        if (userId != null) {
            AiVisionRecord record = new AiVisionRecord();
            record.setUserId(userId);
            record.setType(String.valueOf(result.getOrDefault("type", "invalid")));
            record.setImageUrl(request.getImageUrl());
            record.setPayloadJson(objectMapper.writeValueAsString(result));
            record.setCreatedAt(LocalDateTime.now());
            aiVisionRecordService.save(record);
            result.put("recordId", record.getId());
        }
        return result;
    }

    @GetMapping("/vision/records")
    public List<AiVisionRecord> visionRecords(@RequestParam Long userId) {
        return aiVisionRecordService.lambdaQuery()
            .eq(AiVisionRecord::getUserId, userId)
            .orderByDesc(AiVisionRecord::getCreatedAt)
            .list();
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

    private void saveAiLog(Long userId, String wxOpenid, String requestText,
                           String responseText, Integer status, String error) {
        try {
            AiLog log = new AiLog();
            log.setUserId(userId);
            log.setWxOpenid(wxOpenid);
            log.setRequestText(requestText);
            log.setResponseText(responseText);
            log.setStatus(status);
            log.setError(error);
            log.setCreatedAt(LocalDateTime.now());
            aiLogService.save(log);
        } catch (Exception ignore) {
            // avoid breaking ai flow
        }
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

    private String stripGoalBlock(String content) {
        if (content == null) return "";
        return content.replaceAll("(?s)<goal>.*?</goal>", "").trim();
    }

    private boolean isGoalValueSafe(int goalType, int targetValue, String period) {
        switch (goalType) {
            case 1: return targetValue >= 1000 && targetValue <= 50000; // steps
            case 2:
                if (!"week".equalsIgnoreCase(period)) return false;
                return targetValue >= 1 && targetValue <= 2; // kg per week
            case 3: return targetValue >= 500 && targetValue <= 6000; // ml
            case 4: return targetValue >= 4 && targetValue <= 12; // hours
            default: return false;
        }
    }
}
