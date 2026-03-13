package com.healthtracker.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.healthtracker.entity.SmsCode;
import com.healthtracker.mapper.SmsCodeMapper;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

@Service
public class SmsCodeService extends ServiceImpl<SmsCodeMapper, SmsCode> {
    private static final String COOLDOWN_PREFIX = "sms:cooldown:";
    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;
    private final Map<String, Long> cooldownMemory = new java.util.concurrent.ConcurrentHashMap<>();

    public void saveCode(String phone, String code, int minutes) {
        SmsCode entity = new SmsCode();
        entity.setPhone(phone);
        entity.setCode(code);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setExpiresAt(LocalDateTime.now().plusMinutes(minutes));
        entity.setUsed(0);
        save(entity);
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(redisKey(phone), code, Duration.ofMinutes(minutes));
        }
    }

    public boolean verifyAndConsume(String phone, String code) {
        if (redisTemplate != null) {
            String cached = redisTemplate.opsForValue().get(redisKey(phone));
            if (cached != null) {
                if (!cached.equals(code)) {
                    return false;
                }
                redisTemplate.delete(redisKey(phone));
                markDbUsed(phone, code);
                return true;
            }
        }
        return verifyFromDb(phone, code);
    }

    public boolean isCooldown(String phone) {
        if (redisTemplate != null) {
            return Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey(phone)));
        }
        Long expireAt = cooldownMemory.get(phone);
        if (expireAt == null) {
            return false;
        }
        if (expireAt <= System.currentTimeMillis()) {
            cooldownMemory.remove(phone);
            return false;
        }
        return true;
    }

    public void markCooldown(String phone, int seconds) {
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(cooldownKey(phone), "1", Duration.ofSeconds(seconds));
            return;
        }
        cooldownMemory.put(phone, System.currentTimeMillis() + seconds * 1000L);
    }

    private boolean verifyFromDb(String phone, String code) {
        SmsCode latest = getOne(new LambdaQueryWrapper<SmsCode>()
            .eq(SmsCode::getPhone, phone)
            .eq(SmsCode::getCode, code)
            .orderByDesc(SmsCode::getCreatedAt)
            .last("LIMIT 1"));
        if (latest == null) {
            return false;
        }
        if (latest.getUsed() != null && latest.getUsed() == 1) {
            return false;
        }
        if (latest.getExpiresAt() != null && latest.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }
        latest.setUsed(1);
        updateById(latest);
        return true;
    }

    private void markDbUsed(String phone, String code) {
        SmsCode latest = getOne(new LambdaQueryWrapper<SmsCode>()
            .eq(SmsCode::getPhone, phone)
            .eq(SmsCode::getCode, code)
            .orderByDesc(SmsCode::getCreatedAt)
            .last("LIMIT 1"));
        if (latest != null) {
            latest.setUsed(1);
            updateById(latest);
        }
    }

    private String redisKey(String phone) {
        return "sms:code:" + phone;
    }

    private String cooldownKey(String phone) {
        return COOLDOWN_PREFIX + phone;
    }
}
