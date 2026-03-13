package com.healthtracker.service;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class CaptchaService {
    private static final String CAPTCHA_PREFIX = "captcha:";
    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final Duration TTL = Duration.ofMinutes(5);
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final SecureRandom random = new SecureRandom();
    private final Map<String, Long> memoryExpire = new ConcurrentHashMap<>();
    private final Map<String, String> memoryStore = new ConcurrentHashMap<>();

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    public Map<String, String> create() {
        String key = UUID.randomUUID().toString().replace("-", "");
        String code = randomCode(4);
        store(key, code);
        String image = renderBase64(code);
        return Map.of("key", key, "image", image);
    }

    public boolean verify(String key, String code) {
        if (key == null || key.isBlank() || code == null || code.isBlank()) {
            return false;
        }
        String normalized = code.trim().toUpperCase();
        if (redisTemplate != null) {
            String cached = redisTemplate.opsForValue().get(redisKey(key));
            if (cached == null) {
                return false;
            }
            redisTemplate.delete(redisKey(key));
            return cached.equalsIgnoreCase(normalized);
        }
        cleanupMemory();
        String cached = memoryStore.remove(key);
        if (cached == null) {
            return false;
        }
        return cached.equalsIgnoreCase(normalized);
    }

    private void store(String key, String code) {
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(redisKey(key), code, TTL);
            return;
        }
        memoryStore.put(key, code);
        memoryExpire.put(key, System.currentTimeMillis() + TTL.toMillis());
    }

    private void cleanupMemory() {
        long now = System.currentTimeMillis();
        memoryExpire.entrySet().removeIf(entry -> {
            if (entry.getValue() <= now) {
                memoryStore.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }

    private String renderBase64(String code) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(new Color(245, 245, 245));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setFont(new Font("SansSerif", Font.BOLD, 24));
        g.setColor(new Color(45, 45, 45));
        g.drawString(code, 18, 28);
        g.setColor(new Color(200, 200, 200));
        for (int i = 0; i < 4; i++) {
            int x1 = random.nextInt(WIDTH);
            int y1 = random.nextInt(HEIGHT);
            int x2 = random.nextInt(WIDTH);
            int y2 = random.nextInt(HEIGHT);
            g.drawLine(x1, y1, x2, y2);
        }
        g.dispose();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
            return "data:image/png;base64," + base64;
        } catch (Exception ex) {
            throw new IllegalArgumentException("验证码生成失败");
        }
    }

    private String randomCode(int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    private String redisKey(String key) {
        return CAPTCHA_PREFIX + key;
    }
}
