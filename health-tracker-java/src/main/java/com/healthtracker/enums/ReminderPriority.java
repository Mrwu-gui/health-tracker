package com.healthtracker.enums;

public enum ReminderPriority {
    NORMAL(1, "普通"),
    IMPORTANT(2, "重要"),
    URGENT(3, "紧急");

    private final int code;
    private final String label;

    ReminderPriority(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static ReminderPriority fromCode(Integer code) {
        if (code == null) return null;
        for (ReminderPriority item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }
}
