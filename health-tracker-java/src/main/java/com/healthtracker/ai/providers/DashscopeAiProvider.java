package com.healthtracker.ai.providers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthtracker.ai.AiProvider;
import com.healthtracker.ai.AiProviderRequest;
import com.healthtracker.ai.AiProviderResponse;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class DashscopeAiProvider implements AiProvider {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${dashscope.app-id:}")
    private String appId;

    @Value("${dashscope.api-key:}")
    private String apiKey;

    @Value("${dashscope.app-base-url:https://dashscope.aliyuncs.com/api/v1/apps}")
    private String appBaseUrl;

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
        if (appId == null || appId.isBlank()) {
            throw new IllegalArgumentException("AI 配置缺失");
        }
        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            throw new IllegalArgumentException("当前模型暂不支持图片识别");
        }
        String key = (apiKey == null || apiKey.isBlank()) ? System.getenv("DASHSCOPE_API_KEY") : apiKey;
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("DASHSCOPE_API_KEY 未配置");
        }
        Map<String, Object> input = new HashMap<>();
        input.put("prompt", request.getPrompt());
        Map<String, Object> payload = new HashMap<>();
        payload.put("input", input);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(key);

        String url = appBaseUrl + "/" + appId + "/completion";
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        String text = extractText(response.getBody());
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
}
