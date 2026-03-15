package com.healthtracker.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("privacy_settings")
public class PrivacySetting {
    private Long id;
    private Long userId;
    private Integer allowSubscribe;
    private Integer allowCloudSync;
    private Integer allowFamilyShare;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
