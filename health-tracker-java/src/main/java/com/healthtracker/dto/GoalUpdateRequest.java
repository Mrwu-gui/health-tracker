package com.healthtracker.dto;

import jakarta.validation.constraints.NotNull;

public class GoalUpdateRequest {
    @NotNull
    private Long id;

    @NotNull
    private Integer goalType;

    @NotNull
    private Integer targetValue;

    @NotNull
    private Integer currentValue;

    @NotNull
    private String period;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getGoalType() {
        return goalType;
    }

    public void setGoalType(Integer goalType) {
        this.goalType = goalType;
    }

    public Integer getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(Integer targetValue) {
        this.targetValue = targetValue;
    }

    public Integer getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(Integer currentValue) {
        this.currentValue = currentValue;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }
}
