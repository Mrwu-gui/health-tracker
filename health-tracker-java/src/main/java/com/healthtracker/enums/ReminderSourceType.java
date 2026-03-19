package com.healthtracker.enums;

public enum ReminderSourceType {
    MANUAL(0, "手动"),
    AI(1, "AI生成");

    private final int code;
    private final String label;

    ReminderSourceType(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static ReminderSourceType fromCode(Integer code) {
        if (code == null) return null;
        for (ReminderSourceType item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }
}
