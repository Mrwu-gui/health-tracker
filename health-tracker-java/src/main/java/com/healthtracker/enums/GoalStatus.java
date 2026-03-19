package com.healthtracker.enums;

public enum GoalStatus {
    ONGOING(1, "进行中"),
    DONE(2, "已达成"),
    ABANDONED(3, "放弃");

    private final int code;
    private final String label;

    GoalStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static GoalStatus fromCode(Integer code) {
        if (code == null) return null;
        for (GoalStatus item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }
}
