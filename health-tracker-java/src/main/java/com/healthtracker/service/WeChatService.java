package com.healthtracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthtracker.config.WeChatProperties;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class WeChatService {
    private static final Logger log = LoggerFactory.getLogger(WeChatService.class);
    private final WeChatProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WeChatService(WeChatProperties properties) {
        this.properties = properties;
        this.restTemplate = new RestTemplate();
    }

    public Map<String, Object> miniCode2Session(String code) {
        String appid = properties.getMini().getAppid();
        String secret = properties.getMini().getSecret();
        if (appid == null || appid.isBlank() || secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("未配置小程序 AppID 或 AppSecret");
        }
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + appid
            + "&secret=" + secret
            + "&js_code=" + code
            + "&grant_type=authorization_code";
        return requestJson(url);
    }

    public Map<String, Object> webAccessToken(String code) {
        String appid = properties.getWeb().getAppid();
        String secret = properties.getWeb().getSecret();
        if (appid == null || appid.isBlank() || secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("未配置网站应用 AppID 或 AppSecret");
        }
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + appid
            + "&secret=" + secret
            + "&code=" + code
            + "&grant_type=authorization_code";
        return requestJson(url);
    }

    public String buildWebQrUrl(String state) {
        String appid = properties.getWeb().getAppid();
        String redirectUri = properties.getWeb().getRedirectUri();
        if (appid == null || appid.isBlank() || redirectUri == null || redirectUri.isBlank()) {
            throw new IllegalArgumentException("未配置网站应用 AppID 或回调地址");
        }
        String encoded = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        return "https://open.weixin.qq.com/connect/qrconnect?appid=" + appid
            + "&redirect_uri=" + encoded
            + "&response_type=code&scope=snsapi_login&state=" + state
            + "#wechat_redirect";
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> requestJson(String url) {
        String safeUrl = sanitizeUrl(url);
        log.info("WeChat request: {}", safeUrl);
        String body;
        try {
            body = restTemplate.getForObject(url, String.class);
        } catch (RestClientException ex) {
            log.error("WeChat request failed: {}", safeUrl, ex);
            throw new IllegalArgumentException("微信接口调用失败");
        }
        log.info("WeChat response: {}", truncate(body, 300));
        try {
            Map<String, Object> map = objectMapper.readValue(body, Map.class);
            Object errcode = map.get("errcode");
            if (errcode != null && !"0".equals(String.valueOf(errcode))) {
                log.warn("WeChat errcode={} errmsg={}", errcode, map.get("errmsg"));
            }
            return map;
        } catch (Exception ex) {
            log.error("WeChat response parse failed: {}", safeUrl, ex);
            throw new IllegalArgumentException("微信接口响应解析失败");
        }
    }

    private String sanitizeUrl(String url) {
        return maskParam(maskParam(maskParam(url, "secret"), "js_code"), "code");
    }

    private String maskParam(String url, String key) {
        String token = key + "=";
        int idx = url.indexOf(token);
        if (idx < 0) {
            return url;
        }
        int start = idx + token.length();
        int end = url.indexOf("&", start);
        if (end < 0) {
            end = url.length();
        }
        return url.substring(0, start) + "***" + url.substring(end);
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return "";
        }
        return value.length() > max ? value.substring(0, max) + "..." : value;
    }
}
