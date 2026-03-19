package com.healthtracker.enums;

public enum ReminderType {
    EXERCISE(1, "运动"),
    DIET(2, "饮食"),
    SLEEP(3, "睡眠"),
    MEDICATION(4, "用药");

    private final int code;
    private final String label;

    ReminderType(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static ReminderType fromCode(Integer code) {
        if (code == null) return null;
        for (ReminderType item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }
}
