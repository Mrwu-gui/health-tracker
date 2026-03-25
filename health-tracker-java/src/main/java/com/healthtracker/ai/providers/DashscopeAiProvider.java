package com.healthtracker.ai.providers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthtracker.ai.AiProvider;
import com.healthtracker.ai.AiProviderRequest;
import com.healthtracker.ai.AiProviderResponse;
import java.util.HashMap;
import java.util.Map;
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
public class DashscopeAiProvider implements AiProvider {
    private static final Logger log = LoggerFactory.getLogger(DashscopeAiProvider.class);
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${dashscope.app-id:}")
    private String appId;

    @Value("${dashscope.api-key:}")
    private String apiKey;

    @Value("${dashscope.app-base-url:https://dashscope.aliyuncs.com/api/v1/apps}")
    private String appBaseUrl;

    @Value("${dashscope.compatible-base-url:https://dashscope.aliyuncs.com/compatible-mode/v1}")
    private String compatibleBaseUrl;

    @Value("${dashscope.chat-model:qwen3-vl-flash}")
    private String chatModel;

    @Value("${dashscope.use-compatible:false}")
    private boolean useCompatible;

    public DashscopeAiProvider(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getName() {
        return "dashscope";
    }

    @Override
    public AiProviderResponse chat(AiProviderRequest request) throws Exception {
        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            throw new IllegalArgumentException("当前模型暂不支持图片识别");
        }
        String key = resolveApiKey();
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("DASHSCOPE_API_KEY 未配置");
        }
        String resolvedAppId = resolveAppId();
        log.info("AI params provider=dashscope useCompatible={} appId={} apiKey={} appBaseUrl={} compatibleBaseUrl={} model={}",
            useCompatible, safe(resolvedAppId), maskKey(key), appBaseUrl, compatibleBaseUrl, chatModel);
        if (useCompatible || resolvedAppId == null || resolvedAppId.isBlank()) {
            return chatCompatible(request, key);
        }
        Map<String, Object> input = new HashMap<>();
        input.put("prompt", request.getPrompt());
        Map<String, Object> payload = new HashMap<>();
        payload.put("input", input);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(key);

        String url = appBaseUrl + "/" + resolvedAppId + "/completion";
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        String text = extractText(response.getBody());
        return new AiProviderResponse(text == null ? "" : text);
    }

    private AiProviderResponse chatCompatible(AiProviderRequest request, String key) throws Exception {
        if (chatModel == null || chatModel.isBlank()) {
            throw new IllegalArgumentException("DASHSCOPE_CHAT_MODEL 未配置");
        }
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", request.getPrompt());
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", chatModel);
        payload.put("messages", new Object[]{message});

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(key);

        String url = compatibleBaseUrl + "/chat/completions";
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        String text = extractCompatibleText(response.getBody());
        return new AiProviderResponse(text == null ? "" : text);
    }

    private String extractText(String body) throws Exception {
        if (body == null || body.isBlank()) return "";
        JsonNode root = objectMapper.readTree(body);
        JsonNode output = root.path("output");
        if (output.isMissingNode()) return "";
        JsonNode textNode = output.path("text");
        if (textNode.isTextual()) return textNode.asText();
        return "";
    }

    private String extractCompatibleText(String body) throws Exception {
        if (body == null || body.isBlank()) return "";
        JsonNode root = objectMapper.readTree(body);
        JsonNode choices = root.path("choices");
        if (!choices.isArray() || choices.isEmpty()) return "";
        JsonNode message = choices.get(0).path("message");
        JsonNode content = message.path("content");
        return content.isTextual() ? content.asText() : "";
    }

    private String maskKey(String key) {
        if (key == null || key.isBlank()) return "";
        int len = key.length();
        if (len <= 8) return "***";
        return key.substring(0, 4) + "****" + key.substring(len - 4) + "(len=" + len + ")";
    }

    private String resolveApiKey() {
        return (apiKey == null || apiKey.isBlank()) ? System.getenv("DASHSCOPE_API_KEY") : apiKey;
    }

    private String resolveAppId() {
        return (appId == null || appId.isBlank()) ? System.getenv("DASHSCOPE_APP_ID") : appId;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
