package com.healthtracker.ai.vision;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class DashscopeVisionService {
    private static final Logger log = LoggerFactory.getLogger(DashscopeVisionService.class);
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${dashscope.compatible-base-url:https://dashscope.aliyuncs.com/compatible-mode/v1}")
    private String baseUrl;

    @Value("${dashscope.vision-model:qwen3-vl-flash}")
    private String visionModel;

    @Value("${dashscope.api-key:}")
    private String apiKey;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    public DashscopeVisionService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> recognize(String imageUrl) throws Exception {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("图片地址为空");
        }
        String key = (apiKey == null || apiKey.isBlank()) ? System.getenv("DASHSCOPE_API_KEY") : apiKey;
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("DASHSCOPE_API_KEY 未配置");
        }

        String prompt = """
你是健康记录识别器。请根据图片判断类型，只输出 JSON，不要输出其他文字。
可选类型：food（食物）、weight（体重秤）、invalid（无法识别）。
如果是 food，请给出：
food_name（食物名称）, calories（kcal 整数）, protein（克，数字）, carbs（克，数字）, fat（克，数字）
并给出 period_goal（目标）、calorie_gap（热量缺口）、remind_freq（提醒频率）。
如果是 weight，请给出：weight（kg，数字）。
如果无法识别或不确定，请 type=invalid。
JSON 示例：
{"type":"food","food_name":"米饭","calories":200,"protein":4.5,"carbs":45,"fat":0.5,"period_goal":"8周减重3-5kg","calorie_gap":"300-500kcal","remind_freq":"每日3次"}
""";

        String modelImageUrl = buildModelImageUrl(imageUrl);
        Map<String, Object> imagePart = Map.of(
            "type", "image_url",
            "image_url", Map.of("url", modelImageUrl)
        );
        Map<String, Object> textPart = Map.of("type", "text", "text", prompt);
        Map<String, Object> message = Map.of(
            "role", "user",
            "content", new Object[] { textPart, imagePart }
        );

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", visionModel);
        payload.put("messages", new Object[] { message });
        payload.put("temperature", 0.2);
        payload.put("stream", false);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(key);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        String url = baseUrl + "/chat/completions";
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        String body = response.getBody();
        if (body != null) {
            log.debug("Dashscope vision raw response: {}", body);
        }
        String content = extractContent(body);
        if (content != null && !content.isBlank()) {
            log.debug("Dashscope vision content: {}", content);
        }
        return normalizeResult(parseJson(content));
    }

    private String buildModelImageUrl(String imageUrl) throws Exception {
        if (imageUrl == null) return "";
        String lower = imageUrl.toLowerCase();
        if (lower.startsWith("data:image/")) {
            return imageUrl;
        }
        boolean isLocal = lower.contains("127.0.0.1")
            || lower.contains("localhost")
            || lower.contains("/uploads/ai/");
        if (!isLocal) {
            return imageUrl;
        }
        String localPrefix = "/uploads/ai/";
        int idx = imageUrl.indexOf(localPrefix);
        if (idx < 0) {
            return imageUrl;
        }
        String filename = imageUrl.substring(idx + localPrefix.length());
        Path filePath = Paths.get(uploadDir, "ai", filename);
        if (!Files.exists(filePath)) {
            return imageUrl;
        }
        byte[] bytes = Files.readAllBytes(filePath);
        String base64 = Base64.getEncoder().encodeToString(bytes);
        String mime = guessMime(filename);
        return "data:" + mime + ";base64," + base64;
    }

    private String guessMime(String filename) {
        String lower = filename == null ? "" : filename.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".gif")) return "image/gif";
        return "image/jpeg";
    }

    private String extractContent(String body) throws Exception {
        if (body == null || body.isBlank()) return "";
        JsonNode root = objectMapper.readTree(body);
        JsonNode choices = root.path("choices");
        if (!choices.isArray() || choices.isEmpty()) return "";
        JsonNode message = choices.get(0).path("message").path("content");
        if (message.isTextual()) return message.asText();
        if (message.isArray() && message.size() > 0) {
            JsonNode first = message.get(0);
            if (first.isTextual()) return first.asText();
            if (first.has("text")) return first.path("text").asText("");
        }
        return "";
    }

    private JsonNode parseJson(String text) throws Exception {
        if (text == null) return objectMapper.createObjectNode();
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            int idx = trimmed.indexOf('{');
            int end = trimmed.lastIndexOf('}');
            if (idx >= 0 && end > idx) trimmed = trimmed.substring(idx, end + 1);
        }
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            trimmed = trimmed.substring(start, end + 1);
        }
        return objectMapper.readTree(trimmed);
    }

    private Map<String, Object> normalizeResult(JsonNode json) {
        Map<String, Object> result = new HashMap<>();
        String type = text(json, "type");
        if (type.isBlank()) type = "invalid";
        result.put("type", type);
        switch (type) {
            case "food" -> {
                result.put("foodName", text(json, "food_name", "foodName"));
                result.put("calories", intVal(json, "calories"));
                result.put("protein", doubleVal(json, "protein"));
                result.put("carbs", doubleVal(json, "carbs"));
                result.put("fat", doubleVal(json, "fat"));
                result.put("periodGoal", text(json, "period_goal", "periodGoal"));
                result.put("calorieGap", text(json, "calorie_gap", "calorieGap"));
                result.put("remindFreq", text(json, "remind_freq", "remindFreq"));
                String foodName = String.valueOf(result.getOrDefault("foodName", "")).trim();
                if (foodName.isBlank()) {
                    result.clear();
                    result.put("type", "invalid");
                    result.put("message", "未识别");
                }
            }
            case "weight" -> result.put("weight", doubleVal(json, "weight"));
            default -> result.put("message", "未识别");
        }
        return result;
    }

    private String text(JsonNode node, String... keys) {
        for (String key : keys) {
            JsonNode val = node.path(key);
            if (!val.isMissingNode() && !val.isNull()) {
                String text = val.asText("");
                if (!text.isBlank()) return text.trim();
            }
        }
        return "";
    }

    private Integer intVal(JsonNode node, String key) {
        JsonNode val = node.path(key);
        if (val.isInt()) return val.asInt();
        if (val.isNumber()) return (int) Math.round(val.asDouble());
        try {
            String txt = val.asText("").trim();
            if (!txt.isBlank()) return (int) Math.round(Double.parseDouble(txt));
        } catch (NumberFormatException ignore) {}
        return 0;
    }

    private Double doubleVal(JsonNode node, String key) {
        JsonNode val = node.path(key);
        if (val.isNumber()) return val.asDouble();
        try {
            String txt = val.asText("").trim();
            if (!txt.isBlank()) return Double.parseDouble(txt);
        } catch (NumberFormatException ignore) {}
        return 0.0;
    }
}
