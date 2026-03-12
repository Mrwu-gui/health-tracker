package com.healthtracker.dto;

import jakarta.validation.constraints.NotBlank;

public class MiniWeRunRequest {
    @NotBlank
    private String code;
    @NotBlank
    private String encryptedData;
    @NotBlank
    private String iv;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getEncryptedData() {
        return encryptedData;
    }

    public void setEncryptedData(String encryptedData) {
        this.encryptedData = encryptedData;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }
}
