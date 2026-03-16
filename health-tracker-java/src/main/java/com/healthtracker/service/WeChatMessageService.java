package com.healthtracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthtracker.config.WeChatProperties;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WeChatMessageService {
    private static final Logger log = LoggerFactory.getLogger(WeChatMessageService.class);
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
            log.info("WeChat reminder skipped: templateId not configured");
            return;
        }
        sendTemplate(openid, templateId, "pages/reminders/index", buildData(title, content, remindTime));
    }

    public String sendTemplate(String openid, String templateId, String page, Map<String, Object> data) {
        if (templateId == null || templateId.isBlank()) {
            log.info("WeChat subscribe skipped: templateId not configured");
            return null;
        }
        String token = getAccessToken();
        if (token == null || token.isBlank()) {
            log.warn("WeChat subscribe failed: access token is empty");
            throw new IllegalArgumentException("微信服务不可用");
        }
        String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + token;
        Map<String, Object> payload = new HashMap<>();
        payload.put("touser", openid);
        payload.put("template_id", templateId);
        payload.put("page", page);
        payload.put("data", data);
        String body;
        try {
            body = objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            log.error("WeChat subscribe payload build failed openid={}", mask(openid), ex);
            throw new IllegalArgumentException("微信推送失败");
        }
        String resp = restTemplate.postForObject(url, body, String.class);
        log.info("WeChat subscribe response openid={} {}", mask(openid), truncate(resp, 300));
        try {
            Map<String, Object> map = objectMapper.readValue(resp, Map.class);
            Object errcode = map.get("errcode");
            if (errcode != null && !"0".equals(String.valueOf(errcode))) {
                log.warn("WeChat subscribe errcode={} errmsg={}", errcode, map.get("errmsg"));
                throw new IllegalArgumentException("微信推送失败");
            }
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("WeChat subscribe parse failed openid={}", mask(openid), ex);
            throw new IllegalArgumentException("微信推送失败");
        }
        return resp;
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
                log.info("WeChat access token cache hit");
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
            Object errcode = resp.get("errcode");
            if (errcode != null && !"0".equals(String.valueOf(errcode))) {
                log.warn("WeChat access token errcode={} errmsg={}", errcode, resp.get("errmsg"));
            }
            if (token == null) {
                log.warn("WeChat access token empty response={}", truncate(body, 300));
                return null;
            }
            String value = token.toString();
            if (redisTemplate != null) {
                redisTemplate.opsForValue().set(TOKEN_KEY, value, TOKEN_TTL);
            }
            log.info("WeChat access token refreshed");
            return value;
        } catch (Exception ex) {
            log.error("WeChat access token parse failed", ex);
            return null;
        }
    }

    private String mask(String value) {
        if (value == null || value.length() <= 6) {
            return "******";
        }
        return value.substring(0, 3) + "****" + value.substring(value.length() - 3);
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return "";
        }
        return value.length() > max ? value.substring(0, max) + "..." : value;
    }
}
