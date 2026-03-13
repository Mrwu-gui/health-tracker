package com.healthtracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthtracker.config.WeChatProperties;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WeChatMessageService {
    private static final String TOKEN_KEY = "wechat:mini:access_token";
    private static final Duration TOKEN_TTL = Duration.ofMinutes(110);

    private final WeChatProperties properties;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    public WeChatMessageService(WeChatProperties properties) {
        this.properties = properties;
    }

    public void sendReminder(String openid, String title, String content, LocalDateTime remindTime) {
        String templateId = properties.getMini().getTemplateId();
        if (templateId == null || templateId.isBlank()) {
            return;
        }
        String token = getAccessToken();
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("微信服务不可用");
        }
        String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + token;
        Map<String, Object> payload = new HashMap<>();
        payload.put("touser", openid);
        payload.put("template_id", templateId);
        payload.put("page", "pages/reminders/index");
        payload.put("data", buildData(title, content, remindTime));
        String body;
        try {
            body = objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            throw new IllegalArgumentException("微信推送失败");
        }
        String resp = restTemplate.postForObject(url, body, String.class);
        try {
            Map<String, Object> map = objectMapper.readValue(resp, Map.class);
            Object errcode = map.get("errcode");
            if (errcode != null && !"0".equals(String.valueOf(errcode))) {
                throw new IllegalArgumentException("微信推送失败");
            }
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("微信推送失败");
        }
    }

    private Map<String, Object> buildData(String title, String content, LocalDateTime remindTime) {
        Map<String, Object> data = new HashMap<>();
        data.put("thing1", Map.of("value", limit(title, 20)));
        data.put("thing2", Map.of("value", limit(content, 20)));
        data.put("time3", Map.of("value", formatTime(remindTime)));
        return data;
    }

    private String formatTime(LocalDateTime time) {
        if (time == null) {
            return "待设置";
        }
        return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    private String limit(String value, int len) {
        if (value == null) {
            return "未填写";
        }
        return value.length() > len ? value.substring(0, len) : value;
    }

    private String getAccessToken() {
        if (redisTemplate != null) {
            String cached = redisTemplate.opsForValue().get(TOKEN_KEY);
            if (cached != null && !cached.isBlank()) {
                return cached;
            }
        }
        String appid = properties.getMini().getAppid();
        String secret = properties.getMini().getSecret();
        if (appid == null || appid.isBlank() || secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("未配置小程序 AppID 或 AppSecret");
        }
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + appid
            + "&secret=" + secret;
        String body = restTemplate.getForObject(url, String.class);
        try {
            Map<String, Object> resp = objectMapper.readValue(body, Map.class);
            Object token = resp.get("access_token");
            if (token == null) {
                return null;
            }
            String value = token.toString();
            if (redisTemplate != null) {
                redisTemplate.opsForValue().set(TOKEN_KEY, value, TOKEN_TTL);
            }
            return value;
        } catch (Exception ex) {
            return null;
        }
    }
}
