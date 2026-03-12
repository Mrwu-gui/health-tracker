package com.healthtracker.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class WeRunRecordRequest {
    @NotNull
    private Long userId;

    @NotNull
    private Integer steps;

    @NotNull
    private LocalDate date;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getSteps() {
        return steps;
    }

    public void setSteps(Integer steps) {
        this.steps = steps;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
