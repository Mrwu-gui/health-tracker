package com.healthtracker.enums;

public enum PrivacyFlag {
    DISABLED(0, "关闭"),
    ENABLED(1, "开启");

    private final int code;
    private final String label;

    PrivacyFlag(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static PrivacyFlag fromCode(Integer code) {
        if (code == null) return null;
        for (PrivacyFlag item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }
}
