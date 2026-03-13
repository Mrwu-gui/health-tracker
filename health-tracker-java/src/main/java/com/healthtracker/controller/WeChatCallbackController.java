package com.healthtracker.controller;

import com.healthtracker.config.WeChatProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wechat")
public class WeChatCallbackController {
    private final WeChatProperties properties;

    public WeChatCallbackController(WeChatProperties properties) {
        this.properties = properties;
    }

    @GetMapping(value = "/callback", produces = MediaType.TEXT_PLAIN_VALUE)
    public String verify(@RequestParam("signature") String signature,
                         @RequestParam("timestamp") String timestamp,
                         @RequestParam("nonce") String nonce,
                         @RequestParam("echostr") String echostr) {
        if (!StringUtils.hasText(signature) || !StringUtils.hasText(timestamp) || !StringUtils.hasText(nonce)) {
            return "invalid";
        }
        String token = properties.getMessage().getToken();
        if (!StringUtils.hasText(token)) {
            return "invalid";
        }
        String sign = sha1(token, timestamp, nonce);
        return signature.equalsIgnoreCase(sign) ? echostr : "invalid";
    }

    @PostMapping(value = "/callback", produces = MediaType.TEXT_PLAIN_VALUE)
    public String receive() {
        // 当前仅确认收到消息，后续可解析并处理事件/消息
        return "success";
    }

    private String sha1(String token, String timestamp, String nonce) {
        String[] arr = new String[] { token, timestamp, nonce };
        Arrays.sort(arr);
        String joined = String.join("", arr);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(joined.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            return "";
        }
    }
}
