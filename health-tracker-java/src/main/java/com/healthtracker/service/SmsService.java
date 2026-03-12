package com.healthtracker.service;

import java.security.SecureRandom;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsService {
    private final SmsCodeService smsCodeService;
    private final SecureRandom random = new SecureRandom();
    private final boolean enabled;

    public SmsService(SmsCodeService smsCodeService,
                      @Value("${sms.enabled:false}") boolean enabled) {
        this.smsCodeService = smsCodeService;
        this.enabled = enabled;
    }

    public String sendCode(String phone) {
        String code = String.format("%06d", random.nextInt(1_000_000));
        smsCodeService.saveCode(phone, code, 5);
        if (!enabled) {
            return code;
        }
        throw new IllegalArgumentException("短信服务未配置，请先完善阿里云短信参数");
    }

    public boolean isEnabled() {
        return enabled;
    }
}
