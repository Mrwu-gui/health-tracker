package com.healthtracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.util.Base64;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

@Service
public class WeChatCryptoService {
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
            throw new IllegalArgumentException("微信数据解密失败");
        }
    }
}
