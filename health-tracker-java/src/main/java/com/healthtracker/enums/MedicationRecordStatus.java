package com.healthtracker.enums;

public enum MedicationRecordStatus {
    NOT_TAKEN(0, "未服"),
    TAKEN(1, "已服"),
    MISSED(2, "漏服");

    private final int code;
    private final String label;

    MedicationRecordStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static MedicationRecordStatus fromCode(Integer code) {
        if (code == null) return null;
        for (MedicationRecordStatus item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }
}
