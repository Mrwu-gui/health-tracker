package com.healthtracker.service;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthtracker.config.SmsProperties;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class SmsService {
    private final SmsCodeService smsCodeService;
    private final SecureRandom random = new SecureRandom();
    private final SmsProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SmsService(SmsCodeService smsCodeService, SmsProperties properties) {
        this.smsCodeService = smsCodeService;
        this.properties = properties;
    }

    public String sendCode(String phone) {
        String code = String.format("%06d", random.nextInt(1_000_000));
        if (!isEnabled()) {
            smsCodeService.saveCode(phone, code, 5);
            return code;
        }
        sendAliyunSms(phone, code);
        smsCodeService.saveCode(phone, code, 5);
        return "";
    }

    public boolean isEnabled() {
        return properties.isEnabled()
            && notBlank(properties.getAliyun().getAccessKeyId())
            && notBlank(properties.getAliyun().getAccessKeySecret())
            && notBlank(properties.getAliyun().getTemplateCode())
            && notBlank(properties.getAliyun().getSignName());
    }

    private void sendAliyunSms(String phone, String code) {
        try {
            Config config = new Config()
                .setAccessKeyId(properties.getAliyun().getAccessKeyId())
                .setAccessKeySecret(properties.getAliyun().getAccessKeySecret());
            config.endpoint = "dysmsapi.aliyuncs.com";
            Client client = new Client(config);
            Map<String, String> params = new HashMap<>();
            params.put("code", code);
            SendSmsRequest request = new SendSmsRequest()
                .setPhoneNumbers(phone)
                .setSignName(properties.getAliyun().getSignName())
                .setTemplateCode(properties.getAliyun().getTemplateCode())
                .setTemplateParam(objectMapper.writeValueAsString(params));
            SendSmsResponse response = client.sendSms(request);
            String result = response.getBody() == null ? null : response.getBody().getCode();
            if (!"OK".equalsIgnoreCase(result)) {
                String msg = response.getBody() == null ? "短信发送失败" : response.getBody().getMessage();
                throw new IllegalArgumentException("短信发送失败: " + msg);
            }
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("短信发送失败");
        }
    }

    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}
