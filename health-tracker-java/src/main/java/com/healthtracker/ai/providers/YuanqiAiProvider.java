package com.healthtracker.ai.providers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthtracker.ai.AiProvider;
import com.healthtracker.ai.AiProviderRequest;
import com.healthtracker.ai.AiProviderResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
public class YuanqiAiProvider implements AiProvider {
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${yuanqi.base-url:https://yuanqi.tencent.com}")
    private String baseUrl;

    @Value("${yuanqi.appid:}")
    private String appId;

    @Value("${yuanqi.appkey:}")
    private String appKey;

    public YuanqiAiProvider(ObjectMapper objectMapper, RestTemplate restTemplate) {
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }

    @Override
    public String getName() {
        return "yuanqi";
    }

    @Override
    public AiProviderResponse chat(AiProviderRequest request) throws Exception {
        if (appId == null || appId.isBlank() || appKey == null || appKey.isBlank()) {
            throw new IllegalArgumentException("AI 配置缺失");
        }
        Map<String, Object> contentText = Map.of("type", "text", "text", request.getPrompt());
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            Map<String, Object> imageUrl = Map.of("url", request.getImageUrl());
            Map<String, Object> imageContent = Map.of("type", "image_url", "image_url", imageUrl);
            message.put("content", List.of(contentText, imageContent));
        } else {
            message.put("content", List.of(contentText));
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("assistant_id", appId);
        payload.put("user_id", request.getUserIdentity());
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
            throw new IllegalArgumentException("服务响应较慢，请稍后再试", ex);
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
        return new AiProviderResponse(content == null ? "" : content);
    }
}
