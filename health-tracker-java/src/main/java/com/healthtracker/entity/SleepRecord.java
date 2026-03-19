package com.healthtracker.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("sleep_record")
public class SleepRecord {
    @TableId(type = IdType.AUTO)
    /** 主键 */
    private Long id;
    /** 用户ID */
    private Long userId;
    /** 入睡时间 */
    private LocalDateTime startTime;
    /** 起床时间 */
    private LocalDateTime endTime;
    /** 记录日期 */
    private LocalDate recordDate;
    /** 深睡分钟 */
    private Integer deepSleepMinutes;
    /** 浅睡分钟 */
    private Integer lightSleepMinutes;
    /** 睡眠质量(文本) */
    private String quality;
    /** 作息标签 */
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

    public LocalDate getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
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
