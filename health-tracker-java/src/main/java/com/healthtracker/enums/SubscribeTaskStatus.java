package com.healthtracker.enums;

public enum SubscribeTaskStatus {
    PENDING(0, "待发送"),
    SENT(1, "已发送"),
    FAILED(2, "失败");

    private final int code;
    private final String label;

    SubscribeTaskStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static SubscribeTaskStatus fromCode(Integer code) {
        if (code == null) return null;
        for (SubscribeTaskStatus item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }
}
