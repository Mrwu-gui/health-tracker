package com.healthtracker.dto;

import jakarta.validation.constraints.NotNull;

public class PrivacyUpdateRequest {
    @NotNull
    private Long userId;
    private Integer allowSubscribe;
    private Integer allowCloudSync;
    private Integer allowFamilyShare;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getAllowSubscribe() {
        return allowSubscribe;
    }

    public void setAllowSubscribe(Integer allowSubscribe) {
        this.allowSubscribe = allowSubscribe;
    }

    public Integer getAllowCloudSync() {
        return allowCloudSync;
    }

    public void setAllowCloudSync(Integer allowCloudSync) {
        this.allowCloudSync = allowCloudSync;
    }

    public Integer getAllowFamilyShare() {
        return allowFamilyShare;
    }

    public void setAllowFamilyShare(Integer allowFamilyShare) {
        this.allowFamilyShare = allowFamilyShare;
    }
}
