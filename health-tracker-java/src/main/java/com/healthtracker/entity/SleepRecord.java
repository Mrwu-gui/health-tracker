package com.healthtracker.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("sleep_record")
public class SleepRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer deepSleepMinutes;
    private Integer lightSleepMinutes;
    private String quality;
    private String routine;

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

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Integer getDeepSleepMinutes() {
        return deepSleepMinutes;
    }

    public void setDeepSleepMinutes(Integer deepSleepMinutes) {
        this.deepSleepMinutes = deepSleepMinutes;
    }

    public Integer getLightSleepMinutes() {
        return lightSleepMinutes;
    }

    public void setLightSleepMinutes(Integer lightSleepMinutes) {
        this.lightSleepMinutes = lightSleepMinutes;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public String getRoutine() {
        return routine;
    }

    public void setRoutine(String routine) {
        this.routine = routine;
    }
}
