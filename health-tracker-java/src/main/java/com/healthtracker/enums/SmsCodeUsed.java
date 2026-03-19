package com.healthtracker.enums;

public enum SmsCodeUsed {
    UNUSED(0, "未使用"),
    USED(1, "已使用");

    private final int code;
    private final String label;

    SmsCodeUsed(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static SmsCodeUsed fromCode(Integer code) {
        if (code == null) return null;
        for (SmsCodeUsed item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }
}
