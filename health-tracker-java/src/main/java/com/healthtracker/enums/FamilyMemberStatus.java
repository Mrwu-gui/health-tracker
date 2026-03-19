package com.healthtracker.enums;

public enum FamilyMemberStatus {
    UNAUTHORIZED(0, "未授权"),
    AUTHORIZED(1, "已授权");

    private final int code;
    private final String label;

    FamilyMemberStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static FamilyMemberStatus fromCode(Integer code) {
        if (code == null) return null;
        for (FamilyMemberStatus item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }
}
