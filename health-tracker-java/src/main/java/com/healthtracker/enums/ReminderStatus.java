package com.healthtracker.enums;

public enum ReminderStatus {
    PENDING(0, "未提醒"),
    DONE(1, "已提醒"),
    IGNORED(2, "已忽略");

    private final int code;
    private final String label;

    ReminderStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static ReminderStatus fromCode(Integer code) {
        if (code == null) return null;
        for (ReminderStatus item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }
}
