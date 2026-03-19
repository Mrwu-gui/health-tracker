package com.healthtracker.enums;

public enum AiLogStatus {
    SUCCESS(0, "成功"),
    FAIL(1, "失败");

    private final int code;
    private final String label;

    AiLogStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static AiLogStatus fromCode(Integer code) {
        if (code == null) return null;
        for (AiLogStatus item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }
}
