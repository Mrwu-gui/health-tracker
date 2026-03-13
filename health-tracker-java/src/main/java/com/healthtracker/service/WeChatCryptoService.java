package com.healthtracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.util.Base64;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WeChatCryptoService {
    private static final Logger log = LoggerFactory.getLogger(WeChatCryptoService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @SuppressWarnings("unchecked")
    public Map<String, Object> decrypt(String sessionKey, String encryptedData, String iv) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(sessionKey);
            byte[] dataBytes = Base64.getDecoder().decode(encryptedData);
            byte[] ivBytes = Base64.getDecoder().decode(iv);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            AlgorithmParameters params = AlgorithmParameters.getInstance("AES");
            params.init(new IvParameterSpec(ivBytes));
            cipher.init(Cipher.DECRYPT_MODE, keySpec, params);
            byte[] result = cipher.doFinal(dataBytes);
            String json = new String(result, StandardCharsets.UTF_8);
            return objectMapper.readValue(json, Map.class);
        } catch (Exception ex) {
            log.error("WeChat decrypt failed: sessionKeyLen={} encryptedLen={} ivLen={}",
                safeLen(sessionKey), safeLen(encryptedData), safeLen(iv), ex);
            throw new IllegalArgumentException("微信数据解密失败");
        }
    }

    private int safeLen(String value) {
        return value == null ? 0 : value.length();
    }
}
